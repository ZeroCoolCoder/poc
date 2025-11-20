package com.yourorg.msgengine.handlers;

import com.yourorg.msgengine.runtime.EventHandler;
import com.yourorg.msgengine.runtime.HandlerResult;
import com.yourorg.msgengine.runtime.MessageContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingHandler implements EventHandler {

    private static final Logger log = LoggerFactory.getLogger(LoggingHandler.class);

    private final String id;
    private final String name;

    public LoggingHandler(String id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String getId() { return id; }

    @Override
    public String getName() { return name; }

    @Override
    public HandlerResult handle(MessageContext ctx) {
        log.info("LoggingHandler [{}] - component={} payload={}", name, ctx.getComponentId(), ctx.getPayload());
        return HandlerResult.CONTINUE;
    }
}
