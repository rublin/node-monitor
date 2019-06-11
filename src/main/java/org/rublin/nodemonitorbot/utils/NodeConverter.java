package org.rublin.nodemonitorbot.utils;

import org.rublin.nodemonitorbot.dto.NodeInfoResponseDto;
import org.rublin.nodemonitorbot.model.Node;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Optional;

import static java.time.ZoneOffset.UTC;

public class NodeConverter {
    public static Node convert(Node node, Optional<NodeInfoResponseDto> infoOptional) {
        if (infoOptional.isPresent()) {
            NodeInfoResponseDto info = infoOptional.get();
            node.setHeight(info.getHeight());
            node.setVersion(info.getVersion());
            node.setAvailable(true);
            node.setOnline(node.getOnline().plus(Duration.between(node.getUpdated(), ZonedDateTime.now(UTC))));
        } else {
            node.setAvailable(false);
        }
        node.setUpdated(ZonedDateTime.now(UTC));

        return node;
    }
}
