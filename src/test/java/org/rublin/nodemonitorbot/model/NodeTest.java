package org.rublin.nodemonitorbot.model;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.ZonedDateTime;

import static java.time.ZoneOffset.UTC;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class NodeTest {

    @Test
    public void uptime() {
        Node node = new Node();
        node.setAdded(ZonedDateTime.now().minusDays(30));
        node.setOnline(Duration.ofDays(30).minusMinutes(10));
        assertEquals(100, node.uptime(), "Expect node with 100% uptime");
        node.setOnline(Duration.ofDays(15));
        assertEquals(50, node.uptime(), "Expect node with 50% uptime");
        node.setAdded(ZonedDateTime.now(UTC).minusDays(100));
        node.setOnline(Duration.ofDays(90));
        assertEquals(90, node.uptime(), "Expect node with 90% uptime");
    }
}