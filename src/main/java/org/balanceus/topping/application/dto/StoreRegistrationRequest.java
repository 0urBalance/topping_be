package org.balanceus.topping.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StoreRegistrationRequest {

    @NotBlank(message = "Store name is required")
    private String name;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "Contact number is required")
    @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "Contact number must be in format 010-XXXX-XXXX")
    private String contactNumber;

    @NotBlank(message = "Business hours are required")
    private String businessHours;

    @NotBlank(message = "Category is required")
    private String category;

    private String mainImageUrl;

    private String snsOrWebsiteLink;
}