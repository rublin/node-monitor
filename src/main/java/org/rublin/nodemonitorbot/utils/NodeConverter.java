package org.rublin.nodemonitorbot.utils;

import org.rublin.nodemonitorbot.dto.NodeInfoResponseDto;
import org.rublin.nodemonitorbot.model.Node;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

public class NodeConverter {
    public static Node convert(Node node, Optional<NodeInfoResponseDto> infoOptional) {
        if (infoOptional.isPresent()) {
            NodeInfoResponseDto info = infoOptional.get();
            node.setHeight(info.getHeight());
            node.setVersion(info.getVersion());
            node.setAvailable(true);
            node.setOnline(node.getOnline().plus(Duration.between(node.getUpdated(), LocalDateTime.now())));
        } else {
            node.setAvailable(false);
        }
        node.setUpdated(LocalDateTime.now());

        return node;
    }
}
