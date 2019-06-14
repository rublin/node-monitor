package org.rublin.nodemonitorbot.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.rublin.nodemonitorbot.dto.PeersFromNodeDto;
import org.rublin.nodemonitorbot.model.Node;
import org.rublin.nodemonitorbot.model.Peer;
import org.rublin.nodemonitorbot.repository.PeerRepository;
import org.rublin.nodemonitorbot.service.NodeService;
import org.rublin.nodemonitorbot.service.PeerService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.validation.constraints.NotNull;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.lang.String.format;
import static java.time.ZoneOffset.UTC;
import static java.util.stream.Collectors.toList;

@Slf4j
@Service
@RequiredArgsConstructor
public class PeerServiceImpl implements PeerService {

    private static final String URL = "http://%s:%d/peers";

    private final PeerRepository peerRepository;
    private final NodeService nodeService;
    private final RestTemplate peerRestTemplate;

    @Value("${node.port}")
    private int port;

    @Override
    public List<String> getPeers(@NotNull Node node) {
        Optional<PeersFromNodeDto> optionalPeers = getPeers(node.getAddress());
        if (optionalPeers.isPresent()) {
            List<String> peers = optionalPeers.get().getPeers().stream()
                    .map(this::preparePeerAddress)
                    .collect(toList());
            log.info("Found {} peers from node {}", peers.size(), node.getAddress());
            return peers;
        }
        log.warn("Peers from node {} not found", node.getAddress());
        return Collections.emptyList();
    }

     String preparePeerAddress(String address) {
        return address.substring(0, address.indexOf(":"));
    }

    @Override
    public List<Peer> getKnownPeers(Set<String> addresses) {
        List<Peer> knownPeers = peerRepository.findByAddressIn(addresses);
        log.info("Found {} known addresses from {}. {} are new",
                knownPeers.size(),
                addresses.size(),
                addresses.size() - knownPeers.size());
        return knownPeers;
    }

    @Override
    public List<Peer> create(List<String> addresses) {
        List<Peer> peers = addresses.stream()
                .map(this::createPeer)
                .collect(toList());
        peers = peerRepository.saveAll(peers);
        log.info("Saved {} new peers", peers.size());
        peers.forEach(this::findNode);
        return peers;
    }

    @Override
    public List<Peer> update(Set<Peer> peers) {
        peers.forEach(peer -> {
            findNode(peer);
            peer.setUpdated(ZonedDateTime.now(UTC));
        });
        List<Peer> savedPeers = peerRepository.saveAll(peers);
        log.info("Updated {} peers", savedPeers.size());
        return savedPeers;
    }

    private void findNode(Peer peer) {
        try {
            Node node = nodeService.registerNode(peer.getAddress());
            log.info("Node {} was found", node.getAddress());
        } catch (Throwable throwable) {
            log.trace("Looks like {} not a node", peer.getAddress());
        }
    }

    private Peer createPeer(String address) {
        Peer peer = new Peer();
        peer.setAddress(address);
        return peer;
    }

    private Optional<PeersFromNodeDto> getPeers(String ip) {
        try {
            ResponseEntity<PeersFromNodeDto> response = peerRestTemplate.getForEntity(format(URL, ip, port), PeersFromNodeDto.class);
            if (HttpStatus.OK == response.getStatusCode()) {
                log.debug("Received peer response from {} node", ip);
                return Optional.ofNullable(response.getBody());
            }
        } catch (Throwable throwable) {
            log.warn("Failed to getPeers from {} node: {}", ip, throwable.getMessage());
        }

        return Optional.empty();
    }
}
