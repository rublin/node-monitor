package org.rublin.nodemonitorbot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static java.time.ZoneOffset.UTC;

@Configuration
@SpringBootApplication
public class NodeMonitorBotApplication {

    @Value("${timeout}")
    private int timeout;

    public static void main(String[] args) {
        ApiContextInitializer.init();
        SpringApplication.run(NodeMonitorBotApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplateInfo(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofMillis(timeout))
                .setReadTimeout(Duration.ofMillis(timeout))
                .build();
    }

    @Bean
    public TelegramBotsApi telegramBotsApi() {
        return new TelegramBotsApi();
    }

    @Bean
    public MongoCustomConversions customConversions() {
        List<Converter<?,?>> converters = new ArrayList<>();
        converters.add(new ZonedDateTimeWriteConverter());
        converters.add(new ZonedDateTimeReadConverter());
        return new MongoCustomConversions(Collections.unmodifiableList(converters));
    }

    class ZonedDateTimeReadConverter implements Converter<Date, ZonedDateTime> {

        @Override
        public ZonedDateTime convert(Date date) {
            return date.toInstant().atZone(UTC);
        }
    }

    class ZonedDateTimeWriteConverter implements Converter<ZonedDateTime, Date> {
        @Override
        public Date convert(ZonedDateTime zonedDateTime) {
            return Date.from(zonedDateTime.toInstant());
        }
    }
}
