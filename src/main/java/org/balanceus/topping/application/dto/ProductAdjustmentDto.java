package org.balanceus.topping.application.dto;

import java.math.BigDecimal;

import org.balanceus.topping.domain.model.Product.ProductCategory;
import org.balanceus.topping.domain.model.Product.ProductType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProductAdjustmentDto {

	@NotNull(message = "Price is required")
	@DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
	private BigDecimal price;

	@NotNull(message = "Availability status is required")
	private Boolean isAvailable;

	@NotNull(message = "Active status is required") 
	private Boolean isActive;

	@NotNull(message = "Product type is required")
	private ProductType productType;

	@NotNull(message = "Product category is required")
	private ProductCategory category;

	private String adjustmentReason;
}