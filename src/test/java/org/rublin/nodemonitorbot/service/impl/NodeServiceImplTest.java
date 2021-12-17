package org.rublin.nodemonitorbot.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.rublin.nodemonitorbot.model.Node;
import org.rublin.nodemonitorbot.repository.NodeRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class NodeServiceImplTest {

    @Mock
    private NodeRepository nodeRepository;
    @Mock
    private RestTemplate restTemplate;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private NodeServiceImpl nodeService;

    @BeforeEach
    public void init() {
        ReflectionTestUtils.setField(nodeService, "heightLimit", 5L);
    }

    @Test
    public void nodeVersionTest() {
        Node node = createNode();

        assertTrue(nodeService.isNodeVersionNeedsToUpdate(node, "1243"));
        assertFalse(node.isVersionOk());

        node.setVersionOk(true);
        assertTrue(node.isVersionOk());
        assertFalse(nodeService.isNodeVersionNeedsToUpdate(node, "1.6.5.860 (41b1aab)"));
        assertFalse(nodeService.isNodeVersionNeedsToUpdate(node, "1.6.5.860"));
        assertFalse(nodeService.isNodeVersionNeedsToUpdate(node, "1.6.5"));
        assertTrue(node.isVersionOk());
    }

    @Test
    public void nodeHeightTest() {
        Node node = createNode();

        assertTrue(nodeService.isNodeHeightNeedsToCorrect(node, 129L));
        assertFalse(node.isHeightOk());

        node.setHeightOk(true);
        assertFalse(nodeService.isNodeHeightNeedsToCorrect(node, 123L));
        assertTrue(node.isHeightOk());

        assertFalse(nodeService.isNodeHeightNeedsToCorrect(node, 124L));
        assertTrue(node.isHeightOk());
    }

    @Test
    public void update() {
        Node node = createNode();
        when(nodeRepository.save(node)).thenReturn(node);

        Node updated = nodeService.update(node, 123, "1.6.5.860 (41b1aab)");
        verifyNoMoreInteractions(nodeRepository);
        verifyNoMoreInteractions(eventPublisher);
        assertEquals(node.isVersionOk(), updated.isVersionOk());
        assertEquals(node.isHeightOk(), updated.isHeightOk());

        updated = nodeService.update(node, 1234, "124");
        verify(nodeRepository).save(node);
        verify(eventPublisher).publishEvent(any());
        assertFalse(node.isHeightOk());
        assertFalse(node.isVersionOk());

        nodeService.update(node, 1234, "1243");

        verifyNoMoreInteractions(nodeRepository);
        verifyNoMoreInteractions(eventPublisher);
        assertEquals(node.isVersionOk(), updated.isVersionOk());
        assertEquals(node.isHeightOk(), updated.isHeightOk());
    }

    @Test
    public void heightReturnToOkTest() {
        Node node = createNode();
        when(nodeRepository.save(node)).thenReturn(node);
        node.setHeightOk(false);

        Node updated = nodeService.update(node, 123, "1.6.5.860 (41b1aab)");
        verify(nodeRepository).save(node);
        verify(eventPublisher).publishEvent(any());
        assertTrue(updated.isHeightOk());
        assertTrue(updated.isVersionOk());
    }

    @Test
    public void versionReturnToOkTest() {
        Node node = createNode();
        when(nodeRepository.save(node)).thenReturn(node);

        node.setVersionOk(false);
        Node updated = nodeService.update(node, 123, "1.6.5.860");
        verify(nodeRepository).save(node);
        verify(eventPublisher).publishEvent(any());
        assertTrue(updated.isVersionOk());
        assertTrue(updated.isHeightOk());
    }

    private Node createNode() {
        Node node = new Node();
        node.setAddress("test node address");
        node.setVersionOk(true);
        node.setHeightOk(true);
        node.setVersion("1.6.5.860 ()");
        node.setHeight(123);
        return node;
    }
}