package org.rublin.nodemonitorbot.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.ZonedDateTime;

@Getter
@Builder
public class NodeDto implements Comparable<NodeDto> {
    private String address;
    private String version;
    private ZonedDateTime updated;
    private int uptime;
    private long height;

    @Override
    public int compareTo(NodeDto nodeDto) {
        return nodeDto.getUptime() - this.getUptime();
    }
}
