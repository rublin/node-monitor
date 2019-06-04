package org.rublin.nodemonitorbot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.rublin.nodemonitorbot.dto.NodeInfoResponseDto;
import org.rublin.nodemonitorbot.model.Node;
import org.rublin.nodemonitorbot.model.TelegramUser;
import org.rublin.nodemonitorbot.repository.NodeRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;

@Slf4j
@Service
@RequiredArgsConstructor
public class NodeServiceImpl implements NodeService {

    public static final String URL = "http://%s:%d/getinfo";

    private final NodeRepository nodeRepository;
    private final RestTemplate restTemplate;

    @Value("${node.port}")
    private int port;

    @Override
    public Node registerNode(String ip) {
        Optional<NodeInfoResponseDto> optionalResponse = verifyNode(ip);
        if (optionalResponse.isPresent()) {
            NodeInfoResponseDto info = optionalResponse.get();
            Node node = new Node();
            node.setIpAddress(ip);
            node.setHeight(info.getHeight());
            node.setVersion(info.getVersion());
            node.setUpdated(LocalDateTime.now());
            node.setUptime(100);
            return nodeRepository.save(node);
        }

        throw new RuntimeException("There is no Karbo node at " + ip);
    }

    @Override
    public TelegramUser subscribe(Node node) {
        return null;
    }

    @Override
    public List<Node> getAll() {
        return nodeRepository.findAll(new Sort(Sort.Direction.DESC, "uptime"));
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
