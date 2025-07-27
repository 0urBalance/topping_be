package org.balanceus.topping.application.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StoreForm {

    @NotBlank(message = "Store name is required")
    @Size(max = 100, message = "Store name must not exceed 100 characters")
    private String name;

    @NotBlank(message = "Category is required")
    private String category;

    @NotBlank(message = "Address is required")
    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @NotBlank(message = "Contact number is required")
    @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "Contact number must be in format 010-XXXX-XXXX")
    private String contactNumber;

    @NotBlank(message = "Business hours are required")
    @Size(max = 500, message = "Business hours must not exceed 500 characters")
    private String businessHours;

    private String mainImageUrl;

    private String snsOrWebsiteLink;

    // Tags as comma-separated string for form binding
    private String tags;

    // Collaboration status (optional)
    private Boolean isCollaborationOpen = true;

    // Helper method to convert tags string to list
    public List<String> getTagsList() {
        if (tags == null || tags.trim().isEmpty()) {
            return List.of();
        }
        return List.of(tags.split(","))
                .stream()
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .map(tag -> tag.startsWith("#") ? tag : "#" + tag)
                .toList();
    }

    // Helper method to set tags from list
    public void setTagsFromList(List<String> tagsList) {
        if (tagsList == null || tagsList.isEmpty()) {
            this.tags = "";
            return;
        }
        this.tags = String.join(", ", tagsList.stream()
                .map(tag -> tag.startsWith("#") ? tag.substring(1) : tag)
                .toList());
    }
}