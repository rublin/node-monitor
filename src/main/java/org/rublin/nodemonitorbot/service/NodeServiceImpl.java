package org.rublin.nodemonitorbot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.rublin.nodemonitorbot.dto.NodeInfoResponseDto;
import org.rublin.nodemonitorbot.events.OnHeightEvent;
import org.rublin.nodemonitorbot.events.OnNodeVersionEvent;
import org.rublin.nodemonitorbot.exception.TelegramProcessException;
import org.rublin.nodemonitorbot.model.Node;
import org.rublin.nodemonitorbot.model.TelegramUser;
import org.rublin.nodemonitorbot.repository.NodeRepository;
import org.rublin.nodemonitorbot.utils.NodeConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Slf4j
@Service
@RequiredArgsConstructor
public class NodeServiceImpl implements NodeService {

    public static final String URL = "http://%s:%d/getinfo";

    private final NodeRepository nodeRepository;
    private final RestTemplate restTemplate;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${node.port}")
    private int port;

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
        Optional<NodeInfoResponseDto> optionalResponse = verifyNode(node.getAddress());
        return nodeRepository.save(NodeConverter.convert(node, optionalResponse));

    }

    @Override
    public Node update(Node node, long height, String version) {
        if (height - node.getHeight() > 3 && node.isHeightOk()) {
            // node is not up to date
            node.setAvailable(false);
            node.setHeightOk(false);
            nodeRepository.save(node);
            eventPublisher.publishEvent(new OnHeightEvent(
                    format("Your node has wrong height: %d. The correct height: %d",
                            node.getHeight(),
                            height),
                    node));
        } else if (!version.equals(node.getVersion()) && node.isVersionOk()) {
            node.setVersionOk(false);
            nodeRepository.save(node);
            eventPublisher.publishEvent(new OnNodeVersionEvent(
                    format("Your node version (%s) is not the latest. The latest version is %s",
                            node.getVersion(),
                            version),
                    node));
        }
        return node;
    }

    @Override
    public Node subscribe(Node node, TelegramUser user) {
        node.getSubscribers().add(user);
        log.info("User {} will subscribe to node {}", user.getTelegramId(), node.getAddress());
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

    private Optional<NodeInfoResponseDto> verifyNode(String ip) {
        ResponseEntity<NodeInfoResponseDto> response = restTemplate.getForEntity(format(URL, ip, port), NodeInfoResponseDto.class);
        if (HttpStatus.OK == response.getStatusCode()) {
            log.debug("Received response from {} node", ip);
            return Optional.ofNullable(response.getBody());
        }
        return Optional.empty();
    }
}
