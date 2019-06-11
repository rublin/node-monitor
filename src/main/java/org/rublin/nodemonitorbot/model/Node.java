package org.rublin.nodemonitorbot.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

import javax.validation.constraints.NotNull;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

import static java.time.ZoneOffset.UTC;

@Data
public class Node {

    @Id
    private String id;

    @NotNull
    @Indexed(name = "address_index", unique = true)
    private String address;
    private long height;
    private String version;
    private ZonedDateTime updated = ZonedDateTime.now(UTC);
    private boolean available = true;
    private boolean heightOk = true;
    private boolean versionOk = true;
    private Duration online = Duration.ZERO;
    private Set<TelegramUser> subscribers = new HashSet<>();
    private ZonedDateTime added = ZonedDateTime.now(UTC);

    public int uptime() {
        long secondsAfterAdded = Duration.between(added, ZonedDateTime.now(UTC)).getSeconds();

        return secondsAfterAdded == 0 ? 100 : (int) (online.getSeconds() * 100 / secondsAfterAdded);
    }

    @Override
    public String toString() {
        return "Node{" +
                "\naddress='" + address + '\'' +
                ", \nheight=" + height +
                ", \nversion='" + version + '\'' +
                ", \nuptime=" + uptime() + "%" +
                '}';
    }
}
