package org.rublin.nodemonitorbot.telegram;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

@Getter
@RequiredArgsConstructor
public enum TelegramCommand {
    INFO("\u2139 Info"),
    ADD("\u2795 Add"),
    SUBSCRIBE("/subscribe_"),
    UNSUBSCRIBE("/unsubscribe_"),
    MY_SUBSCRIPTIONS("\uD83D\uDCAC My subscriptions"),
    GET("\uD83D\uDCCB Get"),
    GET_ALL("Get all"),
    RETURN("\uD83D\uDD19 Return");

    private final String commandName;

    public static Optional<TelegramCommand> fromCommandName(String commandName) {
        return Arrays.stream(values())
                .filter(command -> commandName.startsWith(command.getCommandName()))
                .findFirst();
    }
}
