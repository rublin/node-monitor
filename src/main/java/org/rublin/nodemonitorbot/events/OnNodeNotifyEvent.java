package org.rublin.nodemonitorbot.events;

import lombok.Getter;
import org.rublin.nodemonitorbot.model.Node;
import org.springframework.context.ApplicationEvent;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
public class OnNodeNotifyEvent extends ApplicationEvent {
    private String message;
    private Node node;

    public OnNodeNotifyEvent(@NotBlank String message, @NotNull Node node) {
        super(message);
        this.message = message;
        this.node = node;
    }
}
