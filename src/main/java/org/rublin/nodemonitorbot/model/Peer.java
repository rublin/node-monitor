package org.rublin.nodemonitorbot.model;

import lombok.Data;
import org.springframework.data.annotation.Id;

import javax.validation.constraints.NotNull;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@Data
public class Peer {

    @Id
    private String id;

    @NotNull
    private String address;

    private ZonedDateTime updated = ZonedDateTime.now(ZoneOffset.UTC);
}
