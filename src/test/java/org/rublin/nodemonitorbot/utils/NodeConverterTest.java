package org.rublin.nodemonitorbot.utils;

import org.junit.Test;
import org.rublin.nodemonitorbot.dto.NodeInfoResponseDto;
import org.rublin.nodemonitorbot.model.Node;

import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NodeConverterTest {

    @Test
    public void convert() {
        Node node = new Node();
        node = NodeConverter.convert(node, optional());
        assertFalse(node.isHeightOk());
        assertFalse(node.isVersionOk());

        node = NodeConverter.convert(node, optional());
        assertFalse(node.isHeightOk());
        node.setHeightOk(true);
        assertTrue(NodeConverter.convert(node, optional()).isHeightOk());
    }

    private Optional<NodeInfoResponseDto> optional() {
        NodeInfoResponseDto response = new NodeInfoResponseDto();
        response.setHeight(5);
        return Optional.of(response);
    }
}