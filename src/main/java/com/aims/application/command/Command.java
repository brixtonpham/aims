package com.aims.application.command;

/**
 * Base Command interface for all commands in the system
 * Implements Command Pattern for standardized request handling
 */
public interface Command<T> {
    
    /**
     * Execute the command and return result
     * @return Command execution result
     */
    T execute();
    
    /**
     * Validate command before execution
     * @throws CommandValidationException if validation fails
     */
    default void validate() {
        // Default implementation - subclasses can override
    }
    
    /**
     * Get command type for logging and routing
     * @return Command type identifier
     */
    String getCommandType();
    
    /**
     * Get command metadata for audit trail
     * @return Command metadata
     */
    default CommandMetadata getMetadata() {
        return CommandMetadata.builder()
            .commandType(getCommandType())
            .timestamp(java.time.LocalDateTime.now())
            .build();
    }
}
