package org.rublin.nodemonitorbot.repository;

import org.rublin.nodemonitorbot.model.Node;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface NodeRepository extends MongoRepository<Node, String> {
}
