package org.rublin.nodemonitorbot.dto;

import lombok.Data;

import java.util.List;

@Data
public class PeersFromNodeDto {
    private List<String> peers;
}
