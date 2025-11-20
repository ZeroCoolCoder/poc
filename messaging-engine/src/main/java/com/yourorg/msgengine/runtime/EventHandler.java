package com.yourorg.msgengine.runtime;

public interface EventHandler {
    String getId();
    String getName();
    HandlerResult handle(MessageContext ctx) throws Exception;
}
