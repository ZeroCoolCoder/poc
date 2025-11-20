package com.yourorg.msgengine.engine;

import com.yourorg.msgengine.runtime.HandlerChainExecutor;
import com.yourorg.msgengine.runtime.MessageContext;
import com.yourorg.msgengine.handlers.LoggingHandler;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MessageEngineBootstrap {

    private static final Logger log = LoggerFactory.getLogger(MessageEngineBootstrap.class);

    @PostConstruct
    public void init() throws Exception {
        log.info("Message Engine bootstrap starting...");

        // demo only: build a simple chain with a single logging handler
        HandlerChainExecutor exec = new HandlerChainExecutor(
                List.of(new LoggingHandler("H_LOG", "LoggingHandler"))
        );

        // demo context
        MessageContext ctx = new MessageContext("COMP_1", "hello-world");
        exec.execute(ctx);

        log.info("Message Engine bootstrap complete.");
    }
}
