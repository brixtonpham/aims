package com.aims.application.commands;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Query for viewing cart
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ViewCartQuery {
    private String customerId;
}
