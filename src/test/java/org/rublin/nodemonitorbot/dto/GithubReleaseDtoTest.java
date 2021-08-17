package org.rublin.nodemonitorbot.dto;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GithubReleaseDtoTest {

    @Test
    public void getVersion() {
        GithubReleaseDto dto = new GithubReleaseDto();
        dto.setTagName("v.1.8.1");
        assertEquals("1.8.1", dto.getVersion());

        dto.setTagName("v.1.8.0");
        assertEquals("1.8.0", dto.getVersion());

        dto.setTagName("v.1.7.7");
        assertEquals("1.7.7", dto.getVersion());

        dto.setTagName("v.1.7.0");
        assertEquals("1.7.0", dto.getVersion());

        dto.setTagName("v.1.5.3");
        assertEquals("1.5.3", dto.getVersion());
    }
}