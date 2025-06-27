package com.aims.domain.payment.dto;

import java.util.List;
import java.util.ArrayList;

/**
 * Result DTO for payment validation operations.
 * Similar to OrderValidationResult but specific to payments.
 * 
 * @author AIMS Development Team
 * @version 1.0
 * @since Phase 1 - Foundation
 */
public class PaymentValidationResult {
    
    private final boolean isValid;
    private final List<ValidationError> errors;
    private final List<ValidationWarning> warnings;
    private final String summary;
    
    public PaymentValidationResult(boolean isValid, List<ValidationError> errors, 
                                 List<ValidationWarning> warnings, String summary) {
        this.isValid = isValid;
        this.errors = errors != null ? new ArrayList<>(errors) : new ArrayList<>();
        this.warnings = warnings != null ? new ArrayList<>(warnings) : new ArrayList<>();
        this.summary = summary;
    }
    
    // Factory methods
    public static PaymentValidationResult valid() {
        return new PaymentValidationResult(true, null, null, "Payment validation passed");
    }
    
    public static PaymentValidationResult invalid(List<ValidationError> errors) {
        return new PaymentValidationResult(false, errors, null, "Payment validation failed");
    }
    
    // Getters
    public boolean isValid() { return isValid; }
    public List<ValidationError> getErrors() { return new ArrayList<>(errors); }
    public List<ValidationWarning> getWarnings() { return new ArrayList<>(warnings); }
    public String getSummary() { return summary; }
    
    // Nested classes
    public static class ValidationError {
        private final String field;
        private final String message;
        
        public ValidationError(String field, String message) {
            this.field = field;
            this.message = message;
        }
        
        public String getField() { return field; }
        public String getMessage() { return message; }
    }
    
    public static class ValidationWarning {
        private final String field;
        private final String message;
        
        public ValidationWarning(String field, String message) {
            this.field = field;
            this.message = message;
        }
        
        public String getField() { return field; }
        public String getMessage() { return message; }
    }
}
