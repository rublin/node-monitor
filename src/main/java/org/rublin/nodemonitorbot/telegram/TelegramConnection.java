package org.rublin.nodemonitorbot.telegram;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.rublin.nodemonitorbot.dto.TelegramResponseDto;
import org.rublin.nodemonitorbot.exception.TelegramProcessException;
import org.rublin.nodemonitorbot.service.TelegramService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import javax.annotation.PostConstruct;

import static org.rublin.nodemonitorbot.telegram.TelegramKeyboardUtil.defaultKeyboard;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelegramConnection extends TelegramLongPollingBot {

    private final TelegramBotsApi telegramBotsApi;
    private final TelegramService telegramService;

    @Value("${telegram.bot.token}")
    private String token;

    @Value("${telegram.bot.name}")
    private String name;

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            try {
                TelegramResponseDto response = telegramService.process(update.getMessage());
                response.getMessages().forEach(message -> send(message, response.getKeyboard(), response.getId()));
            } catch (TelegramProcessException e) {
                telegramService.cleanPreviousCommand(update.getMessage().getChatId());
                send(e.getMessage(), defaultKeyboard(), update.getMessage().getChatId());
            }
        }
    }

    public void send(String message, ReplyKeyboardMarkup keyboard, long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableHtml(true);
        sendMessage.setChatId(chatId);
        sendMessage.setText(message);
        sendMessage.setReplyMarkup(keyboard);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Failed to send Telegram message to {}: ", chatId, e);
        }
    }

    @Override
    public String getBotUsername() {
        return name;
    }

    @Override
    public String getBotToken() {
        return token;
    }

    @PostConstruct
    private void init() {
        try {
            telegramBotsApi.registerBot(this);
        } catch (TelegramApiRequestException e) {
            log.error("Failed to register telegram bot", e);
        }
    }
}
