package org.rublin.nodemonitorbot.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.rublin.nodemonitorbot.dto.GithubReleaseDto;
import org.rublin.nodemonitorbot.service.VersionService;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class VersionServiceImpl implements VersionService {

    private final RestTemplate restTemplate;

    @Override
    public String latestVersion() {
        GithubReleaseDto latestRelease = restTemplate.getForObject("https://api.github.com/repos/seredat/karbowanec/releases/latest", GithubReleaseDto.class);

        return latestRelease.getVersion();
    }
}
