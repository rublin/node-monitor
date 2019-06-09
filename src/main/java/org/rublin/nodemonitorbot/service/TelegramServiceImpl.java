package org.rublin.nodemonitorbot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.rublin.nodemonitorbot.dto.TelegramResponseDto;
import org.rublin.nodemonitorbot.exception.TelegramProcessException;
import org.rublin.nodemonitorbot.model.Node;
import org.rublin.nodemonitorbot.model.TelegramUser;
import org.rublin.nodemonitorbot.telegram.TelegramCommand;
import org.rublin.nodemonitorbot.utils.AddressResolver;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.rublin.nodemonitorbot.telegram.TelegramCommand.*;
import static org.rublin.nodemonitorbot.telegram.TelegramKeyboardUtil.defaultKeyboard;
import static org.rublin.nodemonitorbot.telegram.TelegramKeyboardUtil.getAll;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramServiceImpl implements TelegramService {

    private final NodeService nodeService;
    private final TelegramUserService telegramUserService;

    private Map<Long, TelegramCommand> previousCommand = new ConcurrentHashMap<>();

    @Override
    public TelegramResponseDto process(Message message) {
        log.debug("Received message {}", message);
        if ("/start".equalsIgnoreCase(message.getText())) {
            return processNewUser(message);
        }
        Optional<TelegramCommand> optionalCommand = TelegramCommand.fromCommandName(message.getText());
        if (optionalCommand.isPresent()) {
            return processCommand(optionalCommand.get(), message);
        }
        return processNotCommand(message);
    }

    @Override
    public void cleanPreviousCommand(Long chatId) {
        previousCommand.remove(chatId);
    }

    private TelegramResponseDto processNewUser(Message message) {
        String name = getName(message);
        telegramUserService.save(new TelegramUser(null, message.getChatId(), name));
        return TelegramResponseDto.builder()
                .id(message.getChatId())
                .messages(Collections.singletonList(
                        format("Hello, %s\n\nWhat you want to do with Krabo nodes?", name)))
                .keyboard(defaultKeyboard())
                .build();
    }

    private TelegramResponseDto processCommand(TelegramCommand command, Message message) {
        log.info("Received command {} from user {}", command, message.getChatId());
        ReplyKeyboardMarkup keyboard = defaultKeyboard();
        List<String> responseMessages = new ArrayList<>();
        Long chatId = message.getChatId();
        switch (command) {
            case ADD:
                previousCommand.put(chatId, command);
                responseMessages.add("Type node IP or hostname for adding");
                keyboard = null;
                break;

            case SUBSCRIBE:
                previousCommand.put(chatId, command);
                responseMessages.add("Type node IP or hostname for subscribing");
                keyboard = null;
                break;

            case GET:
                previousCommand.put(chatId, command);
                responseMessages.add("Type node IP or select " + GET_ALL.getCommandName());
                keyboard = getAll();
                break;

            case GET_ALL:
                previousCommand.remove(chatId);
                List<String> nodes = nodeService.getAllActive().stream()
                        .map(Node::toString)
                        .collect(toList());
                responseMessages.addAll(nodes);
                break;

            case MY_SUBSCRIPTIONS:
                List<Node> mySubscriptions = nodeService.mySubscriptions(getUser(message));
                responseMessages.addAll(mySubscriptions.stream()
                        .map(Node::toString)
                        .collect(toList()));
                break;

            case RETURN:
                previousCommand.remove(chatId);
                break;

            case INFO:
                responseMessages.add("Not implemented yet");
                break;

        }
        return TelegramResponseDto.builder()
                .id(chatId)
                .keyboard(keyboard)
                .messages(responseMessages)
                .build();
    }

    private TelegramResponseDto processNotCommand(Message message) {
        ReplyKeyboardMarkup keyboard = defaultKeyboard();
        List<String> responseMessages = new ArrayList<>();
        Long chatId = message.getChatId();
        TelegramCommand command = previousCommand.get(chatId);
        if (command == ADD) {
            Node node = nodeService.registerNode(AddressResolver.getIpAddress(message.getText()));
            node = nodeService.subscribe(node, getUser(message));
            responseMessages.add("Node successfully added\n\n" + node.toString());
        } else if (command == SUBSCRIBE) {
            responseMessages.add("not supported yet");
        } else {
            throw new TelegramProcessException("Unknown command: " + message.getText());
        }

        previousCommand.remove(chatId);

        return TelegramResponseDto.builder()
                .id(chatId)
                .messages(responseMessages)
                .keyboard(keyboard)
                .build();
    }

    private TelegramUser getUser(Message message) {
        Optional<TelegramUser> optional = telegramUserService.get(message.getChatId());
        if (optional.isPresent()) {
            return optional.get();
        } else {
            log.warn("For some reason there is no user with id {}. Will create it now", message.getChatId());
            return telegramUserService.save(
                    new TelegramUser(null, message.getChatId(), getName(message)));
        }
    }

    private String getName(Message message) {
        String userName = message.getFrom().getUserName();
        String name = message.getFrom().getFirstName();
        if (Objects.nonNull(name))
            return name;
        else if (Objects.nonNull(userName))
            return userName;
        return "my anonymous friend";
    }
}
