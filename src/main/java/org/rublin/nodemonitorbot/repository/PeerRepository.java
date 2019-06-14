package org.rublin.nodemonitorbot.repository;

import org.rublin.nodemonitorbot.model.Peer;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Set;

public interface PeerRepository extends MongoRepository<Peer, String> {
    List<Peer> findByAddressIn(Set<String> addresses);
}
