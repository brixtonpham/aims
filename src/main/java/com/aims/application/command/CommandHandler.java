package com.aims.application.command;

/**
 * Handler interface for processing commands
 * Implements Command Handler pattern
 */
public interface CommandHandler<C extends Command<T>, T> {
    
    /**
     * Handle the command execution
     * @param command Command to handle
     * @return Execution result
     */
    T handle(C command);
    
    /**
     * Check if this handler supports the given command type
     * @param commandType Command type to check
     * @return true if handler supports this command type
     */
    boolean supports(String commandType);
    
    /**
     * Get handler priority for command routing
     * @return Handler priority (higher number = higher priority)
     */
    default int getPriority() {
        return 0;
    }
}
