package org.rublin.nodemonitorbot.repository;

import org.rublin.nodemonitorbot.model.Node;
import org.rublin.nodemonitorbot.model.TelegramUser;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface NodeRepository extends MongoRepository<Node, String> {
    List<Node> findBySubscribers(TelegramUser user);

    Optional<Node> findByAddress(String address);
}
