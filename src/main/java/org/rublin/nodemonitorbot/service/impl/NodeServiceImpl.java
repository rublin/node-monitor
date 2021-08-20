package org.rublin.nodemonitorbot.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.rublin.nodemonitorbot.dto.NodeInfoResponseDto;
import org.rublin.nodemonitorbot.events.OnNodeNotifyEvent;
import org.rublin.nodemonitorbot.exception.TelegramProcessException;
import org.rublin.nodemonitorbot.model.Node;
import org.rublin.nodemonitorbot.model.TelegramUser;
import org.rublin.nodemonitorbot.repository.NodeRepository;
import org.rublin.nodemonitorbot.service.NodeService;
import org.rublin.nodemonitorbot.utils.NodeConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.validation.constraints.NotBlank;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Slf4j
@Service
@RequiredArgsConstructor
public class NodeServiceImpl implements NodeService {

    private static final String URL = "http://%s:%d/getinfo";

    private final NodeRepository nodeRepository;
    private final RestTemplate infoRestTemplate;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${node.port}")
    private int port;

    @Value("${node.height.limit}")
    private Long heightLimit;

    @Override
    public Node registerNode(String address) {
        Optional<NodeInfoResponseDto> optionalResponse = verifyNode(address);
        if (optionalResponse.isPresent()) {
            Node node = NodeConverter.convert(new Node(), optionalResponse);
            node.setAddress(address);
            try {
                Node save = nodeRepository.save(node);
                log.info("Register new node with {} address", address);
                return save;
            } catch (DuplicateKeyException exception) {
                String message = format("Node with IP %s already present", address);
                log.warn(message);
                throw new TelegramProcessException(message);
            }
        }

        log.warn("There is no Karbo node at {}", address);
        throw new RuntimeException("There is no Karbo node at " + address);
    }

    @Override
    public Node update(Node node) {
        try {
            Optional<NodeInfoResponseDto> optionalResponse = verifyNode(node.getAddress());
            return nodeRepository.save(NodeConverter.convert(node, optionalResponse));
        } catch (Throwable e) {
            log.error("Failed to receive update for node {}: {}", node.getAddress(), e.getMessage());
        }
        if (node.isAvailable()) {
            node.setAvailable(false);
            nodeRepository.save(node);
        }
        return null;
    }

    @Override
    public Node update(Node node, long height, String version) {
        String message = "";
        if (isNodeHeightNeedsToCorrect(node, height)) {
            message = node.isHeightOk() ?
                    format("Your node [%s] height is good now \ud83d\udc4d\n", node.getAddress()) :
                    format("\u26A0\u26A0\u26A0 Your node [%s] has wrong height: %d. The correct height: %d\n", node.getAddress(), node.getHeight(), height);
        }
        if (isNodeVersionNeedsToUpdate(node, version)) {
            message = node.isVersionOk() ?
                    message.concat(format("Your node [%s] version is the latest now \ud83d\udc4d", node.getAddress())) :
                    message.concat(format("\u26A0\u26A0\u26A0 Your node [%s] version %s is not the latest. The latest version is %s",
                            node.getAddress(),
                            node.getVersion(),
                            version));
        }
        if (message.isEmpty()) {
            return node;
        } else {
            eventPublisher.publishEvent(new OnNodeNotifyEvent(message, node));
            return nodeRepository.save(node);
        }
    }

    boolean isNodeVersionNeedsToUpdate(Node node, String version) {
        String nodeVersion = node.getVersion().contains(" ") ? node.getVersion().split(" ")[0] : node.getVersion();
        version = version.contains(" ") ? version.split(" ")[0] : version;
        if (!nodeVersion.startsWith(version) && node.isVersionOk()) {
            node.setVersionOk(false);
            log.info("Node {} has wrong version {}", node.getAddress(), node.getVersion());
            return true;
        } else if (nodeVersion.startsWith(version) && !node.isVersionOk()) {
            log.info("Node {} has good version now", node.getAddress());
            node.setVersionOk(true);
            return true;
        }
        return false;
    }

    boolean isNodeHeightNeedsToCorrect(Node node, long height) {
        if (height - node.getHeight() > heightLimit && node.isHeightOk()) {
            // node is not up to date
            node.setAvailable(false);
            node.setHeightOk(false);
            log.info("Node {} has wrong height {}", node.getAddress(), node.getHeight());
            return true;
        } else if (height - node.getHeight() == 0 && !node.isHeightOk()) {
            node.setAvailable(true);
            node.setHeightOk(true);
            return true;
        }
        return false;
    }

    @Override
    public Node subscribe(Node node, TelegramUser user) {
        node.getSubscribers().add(user);
        log.info("User {} will subscribe to node {}", user.getTelegramId(), node.getAddress());
        return nodeRepository.save(node);
    }

    @Override
    public Node unsubscribe(String address, TelegramUser user) {
        Node node = get(address);
        node.getSubscribers().remove(user);
        log.info("User {} unsubscribed from node {}", user.getTelegramId(), node.getAddress());
        return nodeRepository.save(node);
    }

    @Override
    public List<Node> mySubscriptions(TelegramUser user) {
        List<Node> mySubscriptions = nodeRepository.findBySubscribers(user);
        log.info("Found {} nodes by user {}", mySubscriptions.size(), user.getTelegramId());
        return mySubscriptions;
    }

    @Override
    public List<Node> getAll() {
        List<Node> nodes = nodeRepository.findAll(new Sort(Sort.Direction.DESC, "height"));
        log.info("Found all {} nodes", nodes.size());
        return nodes;
    }

    @Override
    public List<Node> getAllActive() {
        List<Node> activeNodes = getAll().stream()
                .filter(Node::isAvailable)
                .sorted(Comparator.comparing(Node::uptime).reversed())
                .collect(Collectors.toList());
        log.info("There are {} active nodes", activeNodes.size());
        return activeNodes;
    }

    @Override
    public Node get(@NotBlank String address) {
        Optional<Node> optionalNode = nodeRepository.findByAddress(address);
        log.info("{} node by address {}", optionalNode.isPresent() ? "Found" : "Not found", address);
        return optionalNode.orElseGet(() -> registerNode(address));
    }

    private Optional<NodeInfoResponseDto> verifyNode(String ip) {
        try {
            ResponseEntity<NodeInfoResponseDto> response = infoRestTemplate.getForEntity(format(URL, ip, port), NodeInfoResponseDto.class);
            if (HttpStatus.OK == response.getStatusCode()) {
                log.debug("Received response from {} node:\n{}", ip, response.getBody());
                return Optional.ofNullable(response.getBody());
            }
        } catch (Throwable throwable) {
            log.warn("Failed to verify node {}: {}", ip, throwable.getMessage());
        }

        return Optional.empty();
    }
}
