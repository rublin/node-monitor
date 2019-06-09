package org.rublin.nodemonitorbot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.rublin.nodemonitorbot.model.Node;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Slf4j
@Service
@EnableScheduling
@RequiredArgsConstructor
public class ScheduledServiceImpl implements ScheduledService {

    private final NodeService nodeService;

    @Override
    @Scheduled(cron = "${node.cron}")
    public void checkNode() {
        log.info("Cron job started");
        List<Node> allNodes = nodeService.getAll().stream()
                .map(nodeService::update)
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


}
