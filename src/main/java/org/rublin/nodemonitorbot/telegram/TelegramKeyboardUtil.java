package org.rublin.nodemonitorbot.telegram;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.Arrays;

import static org.rublin.nodemonitorbot.telegram.TelegramCommand.*;

public class TelegramKeyboardUtil {
    public static ReplyKeyboardMarkup defaultKeyboard() {
        ReplyKeyboardMarkup replyKeyboardMarkup = returnKeyboard();
        replyKeyboardMarkup.getKeyboard().clear();
        replyKeyboardMarkup.getKeyboard().add(createKeyboardRow(
                ADD.getCommandName(),
                MY_SUBSCRIPTIONS.getCommandName(),
                GET.getCommandName()));
        replyKeyboardMarkup.getKeyboard().add(createKeyboardRow(
                INFO.getCommandName()));

        return replyKeyboardMarkup;
    }

    public static ReplyKeyboardMarkup getAll() {
        ReplyKeyboardMarkup replyKeyboardMarkup = returnKeyboard();
        replyKeyboardMarkup.getKeyboard().add(0, createKeyboardRow(GET_ALL.getCommandName()));

        return replyKeyboardMarkup;
    }

    public static ReplyKeyboardMarkup returnKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setSelective(true);
        keyboardMarkup.setOneTimeKeyboard(true);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setKeyboard(new ArrayList<>());
        keyboardMarkup.getKeyboard().add(createKeyboardRow(RETURN.getCommandName()));
        keyboardMarkup.setOneTimeKeyboard(true);
        return keyboardMarkup;
    }

    private static KeyboardRow createKeyboardRow(String... commands) {
        KeyboardRow row = new KeyboardRow();
        Arrays.stream(commands)
                .forEach(row::add);
        return row;
    }
}
