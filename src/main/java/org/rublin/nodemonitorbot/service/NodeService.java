package org.rublin.nodemonitorbot.service;

import org.rublin.nodemonitorbot.model.Node;
import org.rublin.nodemonitorbot.model.TelegramUser;

import java.util.List;

public interface NodeService {
    Node registerNode(String ip);

    Node update(Node node);

    Node update(Node node, long height, String version);

    TelegramUser subscribe(Node node);

    List<Node> getAll();
}
