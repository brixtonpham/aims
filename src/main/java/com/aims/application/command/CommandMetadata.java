package com.aims.application.command;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;

/**
 * Metadata for command execution
 */
@Data
@Builder
public class CommandMetadata {
    private String commandType;
    private LocalDateTime timestamp;
    private String userId;
    private String sessionId;
    private String requestId;
    @Builder.Default
    private Map<String, Object> additionalData = new HashMap<>();
}
