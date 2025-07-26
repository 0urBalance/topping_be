package org.balanceus.topping.domain.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "stores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Store {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID uuid;

    @Column(unique = true, nullable = false)
    @NotBlank(message = "Store name is required")
    private String name;

    @Column(nullable = false)
    @NotBlank(message = "Address is required")
    private String address;

    @Column(nullable = false)
    @NotBlank(message = "Contact number is required")
    @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "Contact number must be in format 010-XXXX-XXXX")
    private String contactNumber;

    @Column(nullable = false)
    @NotBlank(message = "Business hours are required")
    private String businessHours;

    @Column(nullable = false)
    @NotBlank(message = "Category is required")
    private String category;

    private String mainImageUrl;

    private String snsOrWebsiteLink;

    @OneToOne
    @JoinColumn(name = "user_uuid", nullable = false, unique = true)
    private User user;

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC, createdAt ASC")
    private List<StoreImage> images = new ArrayList<>();

    // Helper methods for image management
    public void addImage(StoreImage image) {
        images.add(image);
        image.setStore(this);
    }

    public void removeImage(StoreImage image) {
        images.remove(image);
        image.setStore(null);
    }

    public List<String> getImagePaths() {
        return images.stream()
                .map(StoreImage::getImagePath)
                .toList();
    }

    public List<StoreImage> getGalleryImages() {
        return images.stream()
                .filter(img -> img.getImageType() == StoreImage.ImageType.GALLERY)
                .toList();
    }

    public StoreImage getMainImage() {
        return images.stream()
                .filter(img -> img.getImageType() == StoreImage.ImageType.MAIN)
                .findFirst()
                .orElse(null);
    }
}