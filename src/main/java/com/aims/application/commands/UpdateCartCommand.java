package com.aims.application.commands;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Command for updating cart item
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCartCommand {
    private Long cartItemId;
    private Integer quantity;
}
