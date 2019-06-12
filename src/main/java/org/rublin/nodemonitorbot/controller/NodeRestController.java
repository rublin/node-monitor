package org.rublin.nodemonitorbot.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.rublin.nodemonitorbot.dto.NodeDto;
import org.rublin.nodemonitorbot.model.Node;
import org.rublin.nodemonitorbot.service.NodeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
public class NodeRestController {

    private final NodeService nodeService;

    @GetMapping("/api/nodes")
    public List<NodeDto> allAvailableNodes() {
        List<Node> activeNodes = nodeService.getAllActive();
        log.info("Return {} active nodes", activeNodes.size());
        return activeNodes.stream()
                .filter(Node::isHeightOk)
                .map(node -> NodeDto.builder()
                        .address(node.getAddress())
                        .version(node.getVersion())
                        .updated(node.getUpdated())
                        .uptime(node.uptime())
                        .height(node.getHeight())
                        .build())
//                .sorted()
                .collect(Collectors.toList());
    }

}
