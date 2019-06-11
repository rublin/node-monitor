package org.rublin.nodemonitorbot.service;

import org.rublin.nodemonitorbot.model.Node;
import org.rublin.nodemonitorbot.model.TelegramUser;

import javax.validation.constraints.NotBlank;
import java.util.List;

public interface NodeService {
    Node registerNode(String ip);

    Node update(Node node);

    Node update(Node node, long height, String version);

    Node subscribe(Node node, TelegramUser user);

    Node unsubscribe(String address, TelegramUser user);

    List<Node> mySubscriptions(TelegramUser user);

    List<Node> getAll();

    List<Node> getAllActive();

    Node get(@NotBlank String address);
}
