package org.rublin.nodemonitorbot.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.rublin.nodemonitorbot.model.Node;
import org.rublin.nodemonitorbot.model.Peer;
import org.rublin.nodemonitorbot.service.NodeService;
import org.rublin.nodemonitorbot.service.PeerService;
import org.rublin.nodemonitorbot.service.ScheduledService;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static java.time.ZoneOffset.UTC;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Slf4j
@Service
@EnableScheduling
@RequiredArgsConstructor
public class ScheduledServiceImpl implements ScheduledService {

    private final NodeService nodeService;
    private final PeerService peerService;

    @Override
    @Scheduled(cron = "${node.cron}")
    public void checkNode() {
        log.info("Cron job started");
        List<Node> allNodes = nodeService.getAll().stream()
                .map(nodeService::update)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(Node::getHeight).reversed())
                .collect(toList());

        List<String> versions = allNodes.stream()
                .map(Node::getVersion)
                .sorted(Comparator.reverseOrder())
                .collect(toList());
        String latestVersion = versions.get(0);

//        should be the correct height
        long height = allNodes.get(0).getHeight();

        allNodes.forEach(node -> nodeService.update(node, height, latestVersion));
    }

    @Override
    @Scheduled(cron = "${node.find.cron}")
    public void findNodes() {
        long start = System.currentTimeMillis();
        Set<String> peers = nodeService.getAllActive().stream()
                .map(peerService::getPeers)
                .flatMap(List::stream)
                .collect(toSet());
        log.info("Found {} unique peers", peers.size());
        List<Peer> knownPeers = peerService.getKnownPeers(peers);
        Set<String> knownAddresses = knownPeers.stream()
                .map(Peer::getAddress)
                .collect(toSet());
        ZonedDateTime updateTimeLimit = ZonedDateTime.now(UTC).minusDays(7);
        Set<Peer> peersToUpdate = knownPeers.stream()
                .filter(peer -> peer.getUpdated().isBefore(updateTimeLimit))
                .collect(Collectors.toSet());
        log.info("Filtered {} peers to update", peersToUpdate.size());

        List<String> newAddresses = peers.stream()
                .filter(address -> !knownAddresses.contains(address))
                .collect(toList());
        peerService.create(newAddresses);
        peerService.update(peersToUpdate);
        log.info("Created {} new peers; updated {} peers. It takes {} ms",
                newAddresses.size(),
                peersToUpdate.size(),
                System.currentTimeMillis() - start);
    }


}
