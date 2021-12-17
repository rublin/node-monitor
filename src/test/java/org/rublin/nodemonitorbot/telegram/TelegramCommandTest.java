package org.rublin.nodemonitorbot.telegram;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.rublin.nodemonitorbot.telegram.TelegramCommand.SUBSCRIBE;
import static org.rublin.nodemonitorbot.telegram.TelegramCommand.UNSUBSCRIBE;
import static org.rublin.nodemonitorbot.telegram.TelegramCommand.fromCommandName;

public class TelegramCommandTest {

    @Test
    public void fromCommandNameTest() {
        assertEquals(SUBSCRIBE, fromCommandName("/subscribe_1254").get());
        assertEquals(UNSUBSCRIBE, fromCommandName("/unsubscribe_1254").get());
        assertFalse(fromCommandName("qwer").isPresent());
    }
}