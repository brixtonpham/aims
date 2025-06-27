package com.aims.application.command.middleware;

import com.aims.application.command.Command;
import com.aims.application.command.CommandMiddleware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Middleware for logging command execution
 * 
 * @author AIMS Development Team
 * @version 1.0
 * @since Phase 3
 */
@Component
public class LoggingCommandMiddleware implements CommandMiddleware {
    
    private static final Logger logger = LoggerFactory.getLogger(LoggingCommandMiddleware.class);
    
    @Override
    public void preProcess(Command<?> command) {
        String commandName = command.getClass().getSimpleName();
        logger.info("Starting execution of command: {}", commandName);
    }
    
    @Override
    public void postProcess(Command<?> command, Object result) {
        String commandName = command.getClass().getSimpleName();
        logger.info("Command {} completed successfully", commandName);
    }
    
    @Override
    public void onError(Command<?> command, Exception error) {
        String commandName = command.getClass().getSimpleName();
        logger.error("Command {} failed with error: {}", commandName, error.getMessage(), error);
    }
    
    @Override
    public int getOrder() {
        return 1; // First middleware to execute (logging should be outermost)
    }
}
