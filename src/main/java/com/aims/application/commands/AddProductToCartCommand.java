package com.aims.application.commands;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Command for adding product to cart
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddProductToCartCommand {
    private String customerId;
    private Long productId;
    private Integer quantity;
}
