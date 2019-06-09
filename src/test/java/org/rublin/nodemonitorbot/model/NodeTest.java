package org.rublin.nodemonitorbot.model;

import org.junit.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.Assert.*;

public class NodeTest {

    @Test
    public void uptime() {
        Node node = new Node();
        node.setAdded(LocalDateTime.now().minusDays(30));
        node.setOnline(Duration.ofDays(30));
        assertEquals("Expect node with 100% uptime", 100, node.uptime());
        node.setOnline(Duration.ofDays(15));
        assertEquals("Expect node with 50% uptime", 50, node.uptime());
        node.setAdded(LocalDateTime.now().minusDays(100));
        node.setOnline(Duration.ofDays(90));
        assertEquals("Expect node with 90% uptime", 90, node.uptime());
    }
}