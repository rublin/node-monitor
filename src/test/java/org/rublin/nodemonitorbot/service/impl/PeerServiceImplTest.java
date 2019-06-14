package org.rublin.nodemonitorbot.service.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.rublin.nodemonitorbot.repository.PeerRepository;
import org.rublin.nodemonitorbot.service.NodeService;
import org.springframework.web.client.RestTemplate;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class PeerServiceImplTest {

    @Mock
    private PeerRepository repository;
    @Mock
    private NodeService nodeService;
    @Mock
    private RestTemplate restTemplate;
    @InjectMocks
    private PeerServiceImpl peerService;

    @Test
    public void preparePeerAddress() {
        assertEquals("37.59.43.88", peerService.preparePeerAddress("37.59.43.88:32347"));
    }
}