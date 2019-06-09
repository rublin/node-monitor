package org.rublin.nodemonitorbot.telegram;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

@Getter
@RequiredArgsConstructor
public enum TelegramCommand {
    INFO("\u2139 Info"),
    ADD("Add"),
    SUBSCRIBE("Subscribe"),
    MY_SUBSCRIPTIONS("My subscriptions"),
    GET("Get"),
    GET_ALL("Get all"),
    RETURN("\uD83D\uDD19 Return");

    private final String commandName;

    public static Optional<TelegramCommand> fromCommandName(String commandName) {
        return Arrays.stream(values())
                .filter(command -> command.getCommandName().equalsIgnoreCase(commandName))
                .findFirst();
    }
}
