package com.aims.application.command;

/**
 * Middleware interface for command processing pipeline
 */
public interface CommandMiddleware {
    
    /**
     * Execute before command handling
     */
    void preProcess(Command<?> command);
    
    /**
     * Execute after successful command handling
     */
    void postProcess(Command<?> command, Object result);
    
    /**
     * Execute when command handling fails
     */
    void onError(Command<?> command, Exception error);
    
    /**
     * Get middleware execution order
     */
    default int getOrder() {
        return 0;
    }
}
