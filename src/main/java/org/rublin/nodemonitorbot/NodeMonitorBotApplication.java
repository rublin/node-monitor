package org.rublin.nodemonitorbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;

@Configuration
@SpringBootApplication
public class NodeMonitorBotApplication {

    public static void main(String[] args) {
        ApiContextInitializer.init();
        SpringApplication.run(NodeMonitorBotApplication.class, args);
    }

    @Bean
    public TelegramBotsApi telegramBotsApi() {
        return new TelegramBotsApi();
    }

}
