package com.yourorg.msgengine.runtime;

import java.util.List;

public class HandlerChainExecutor {

    private final List<EventHandler> handlers;

    public HandlerChainExecutor(List<EventHandler> handlers) {
        this.handlers = List.copyOf(handlers);
    }

    public void execute(MessageContext ctx) throws Exception {
        for (EventHandler handler : handlers) {
            HandlerResult result = handler.handle(ctx);
            if (result == HandlerResult.ABORT) {
                throw new RuntimeException("Handler aborted chain: " + handler.getId());
            }
            if (result == HandlerResult.STOP) {
                break;
            }
        }
    }
}
