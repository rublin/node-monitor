package org.rublin.nodemonitorbot.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.rublin.nodemonitorbot.events.OnNodeNotifyEvent;
import org.rublin.nodemonitorbot.telegram.TelegramConnection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import static org.rublin.nodemonitorbot.telegram.TelegramKeyboardUtil.defaultKeyboard;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotifyEventListener {

    private final TelegramConnection telegramConnection;

    @Value("${telegram.bot.notification.delay}")
    private long delay;

    @Async
    @EventListener
    public void nodeNotifyEventListener(OnNodeNotifyEvent event) {
        event.getNode().getSubscribers().forEach(telegramUser -> {
            telegramConnection.send(event.getMessage(), defaultKeyboard(), telegramUser.getTelegramId());
            log.info("User {} will receive the notification {}", telegramUser.getTelegramId(), event.getMessage()
            );
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                log.warn("Something wrong with delay: ", e);
            }
        });
    }
}
