package org.rublin.nodemonitorbot.events;

import lombok.Getter;
import org.rublin.nodemonitorbot.model.Node;
import org.springframework.context.ApplicationEvent;

@Getter
public class OnHeightEvent extends ApplicationEvent {
    private String message;
    private Node node;

    public OnHeightEvent(String message, Node node) {
        super(message);
        this.message = message;
        this.node = node;
    }
}
