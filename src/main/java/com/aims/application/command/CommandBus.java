package com.aims.application.command;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Command Bus for routing commands to appropriate handlers
 * Implements Command Bus pattern with middleware support
 */
@Component
public class CommandBus {
    
    private static final Logger logger = LoggerFactory.getLogger(CommandBus.class);
    
    private final List<CommandHandler<?, ?>> handlers;
    private final List<CommandMiddleware> middlewares;
    
    @Autowired
    public CommandBus(List<CommandHandler<?, ?>> handlers, 
                     List<CommandMiddleware> middlewares) {
        this.handlers = handlers;
        this.middlewares = middlewares;
    }
    
    /**
     * Execute command through the command bus
     */
    @SuppressWarnings("unchecked")
    public <T> T execute(Command<T> command) {
        logger.info("Executing command: {}", command.getCommandType());
        
        try {
            // Validate command
            command.validate();
            
            // Execute pre-processing middleware
            for (CommandMiddleware middleware : middlewares) {
                middleware.preProcess(command);
            }
            
            // Find appropriate handler
            CommandHandler<Command<T>, T> handler = findHandler(command);
            if (handler == null) {
                throw new CommandHandlerNotFoundException(
                    "No handler found for command: " + command.getCommandType()
                );
            }
            
            // Execute command
            T result = handler.handle(command);
            
            // Execute post-processing middleware
            for (CommandMiddleware middleware : middlewares) {
                middleware.postProcess(command, result);
            }
            
            logger.info("Command executed successfully: {}", command.getCommandType());
            return result;
            
        } catch (Exception e) {
            logger.error("Command execution failed: {}", command.getCommandType(), e);
            
            // Execute error handling middleware
            for (CommandMiddleware middleware : middlewares) {
                middleware.onError(command, e);
            }
            
            throw new CommandExecutionException("Command execution failed", e);
        }
    }
    
    @SuppressWarnings("unchecked")
    private <T> CommandHandler<Command<T>, T> findHandler(Command<T> command) {
        return (CommandHandler<Command<T>, T>) handlers.stream()
            .filter(handler -> handler.supports(command.getCommandType()))
            .max((h1, h2) -> Integer.compare(h1.getPriority(), h2.getPriority()))
            .orElse(null);
    }
}
