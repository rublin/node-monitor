package org.rublin.nodemonitorbot.service;

import org.rublin.nodemonitorbot.model.Node;
import org.rublin.nodemonitorbot.model.Peer;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;

public interface PeerService {
    List<String> getPeers(@NotNull Node node);

    List<Peer> getKnownPeers(Set<String> addresses);

    List<Peer> create(List<String> addresses);

    List<Peer> update(Set<Peer> peers);
}
