package com.aims.domain.order.dto;

import java.util.List;
import java.util.ArrayList;

/**
 * Value object representing the result of order validation.
 * 
 * Contains:
 * - Overall validation status
 * - List of validation errors if any
 * - Warning messages for business concerns
 * - Suggestions for fixing validation issues
 * 
 * @author AIMS Development Team
 * @version 1.0
 * @since Phase 1 - Foundation
 */
public class OrderValidationResult {
    
    private static final String VALIDATION_FAILED_MESSAGE = "Order validation failed";
    
    private final boolean isValid;
    private final List<ValidationError> errors;
    private final List<ValidationWarning> warnings;
    private final String summary;
    
    public OrderValidationResult(boolean isValid, List<ValidationError> errors, 
                               List<ValidationWarning> warnings, String summary) {
        this.isValid = isValid;
        this.errors = errors != null ? new ArrayList<>(errors) : new ArrayList<>();
        this.warnings = warnings != null ? new ArrayList<>(warnings) : new ArrayList<>();
        this.summary = summary;
    }
    
    // Factory methods for common scenarios
    
    public static OrderValidationResult valid() {
        return new OrderValidationResult(true, null, null, "Order validation passed");
    }
    
    public static OrderValidationResult valid(List<ValidationWarning> warnings) {
        return new OrderValidationResult(true, null, warnings, "Order validation passed with warnings");
    }
    
    public static OrderValidationResult invalid(List<ValidationError> errors) {
        return new OrderValidationResult(false, errors, null, VALIDATION_FAILED_MESSAGE);
    }
    
    public static OrderValidationResult invalid(List<ValidationError> errors, List<ValidationWarning> warnings) {
        return new OrderValidationResult(false, errors, warnings, VALIDATION_FAILED_MESSAGE);
    }
    
    // Getters
    
    public boolean isValid() {
        return isValid;
    }
    
    public List<ValidationError> getErrors() {
        return new ArrayList<>(errors);
    }
    
    public List<ValidationWarning> getWarnings() {
        return new ArrayList<>(warnings);
    }
    
    public String getSummary() {
        return summary;
    }
    
    // Business methods
    
    public boolean hasErrors() {
        return !errors.isEmpty();
    }
    
    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }
    
    public int getErrorCount() {
        return errors.size();
    }
    
    public int getWarningCount() {
        return warnings.size();
    }
    
    public List<String> getErrorMessages() {
        return errors.stream().map(ValidationError::getMessage).toList();
    }
    
    public List<String> getWarningMessages() {
        return warnings.stream().map(ValidationWarning::getMessage).toList();
    }
    
    // Convenience methods for OrderDomainServiceImpl compatibility
    public String getErrorMessage() {
        if (errors.isEmpty()) {
            return null;
        }
        return errors.stream()
            .map(ValidationError::getMessage)
            .reduce((a, b) -> a + ", " + b)
            .orElse("Validation failed");
    }
    
    public static OrderValidationResult invalid(String errorMessage) {
        List<ValidationError> errors = new ArrayList<>();
        errors.add(new ValidationError("general", errorMessage));
        return new OrderValidationResult(false, errors, null, VALIDATION_FAILED_MESSAGE);
    }
    
    // Nested classes for validation issues
    
    public static class ValidationError {
        private final String field;
        private final String code;
        private final String message;
        private final String suggestion;
        
        public ValidationError(String field, String code, String message, String suggestion) {
            this.field = field;
            this.code = code;
            this.message = message;
            this.suggestion = suggestion;
        }
        
        public ValidationError(String field, String message) {
            this(field, "VALIDATION_ERROR", message, null);
        }
        
        public String getField() {
            return field;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getMessage() {
            return message;
        }
        
        public String getSuggestion() {
            return suggestion;
        }
        
        @Override
        public String toString() {
            return String.format("%s: %s", field, message);
        }
    }
    
    public static class ValidationWarning {
        private final String field;
        private final String message;
        private final String recommendation;
        
        public ValidationWarning(String field, String message, String recommendation) {
            this.field = field;
            this.message = message;
            this.recommendation = recommendation;
        }
        
        public ValidationWarning(String field, String message) {
            this(field, message, null);
        }
        
        public String getField() {
            return field;
        }
        
        public String getMessage() {
            return message;
        }
        
        public String getRecommendation() {
            return recommendation;
        }
        
        @Override
        public String toString() {
            return String.format("%s: %s", field, message);
        }
    }
    
    @Override
    public String toString() {
        return String.format("OrderValidationResult{valid=%s, errors=%d, warnings=%d, summary='%s'}", 
                           isValid, errors.size(), warnings.size(), summary);
    }
}
