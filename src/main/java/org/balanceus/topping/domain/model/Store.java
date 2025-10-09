package org.balanceus.topping.domain.model;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.ElementCollection;
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
    @Convert(converter = StoreCategoryConverter.class)
    private StoreCategory category;

    private String mainImageUrl;

    private String snsOrWebsiteLink;

    @Column(length = 1000)
    private String description;

    @Column(name = "is_collaboration_open")
    private Boolean isCollaborationOpen = true;

    @OneToOne
    @JoinColumn(name = "user_uuid", nullable = false, unique = true)
    private User user;

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC, createdAt ASC")
    private Set<StoreImage> images = new LinkedHashSet<>();

    @ElementCollection
    @CollectionTable(name = "store_tags", joinColumns = @JoinColumn(name = "store_uuid"))
    @Column(name = "tag")
    private List<String> tags = new ArrayList<>();

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("productType ASC, reviewCount DESC, name ASC")
    private List<Product> products = new ArrayList<>();

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt DESC")
    private List<Review> reviews = new ArrayList<>();

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

    // Helper methods for product management
    public void addProduct(Product product) {
        products.add(product);
        product.setStore(this);
    }

    public void removeProduct(Product product) {
        products.remove(product);
        product.setStore(null);
    }

    public List<Product> getPopularProducts() {
        return products.stream()
                .filter(product -> product.getProductType() == Product.ProductType.COLLABORATION || product.getReviewCount() > 10)
                .limit(6)
                .toList();
    }

    public List<Product> getSignatureProducts() {
        return products.stream()
                .filter(product -> product.getProductType() == Product.ProductType.SIGNATURE)
                .limit(6)
                .toList();
    }

    // Helper methods for tag management
    public void addTag(String tag) {
        if (tag != null && !tag.trim().isEmpty() && !tags.contains(tag.trim())) {
            tags.add(tag.trim());
        }
    }

    public void removeTag(String tag) {
        tags.remove(tag);
    }

    public List<String> getHashtags() {
        return tags.stream()
                .filter(tag -> tag != null && !tag.trim().isEmpty())
                .map(tag -> tag.startsWith("#") ? tag : "#" + tag)
                .toList();
    }

    // Helper methods for review management
    public void addReview(Review review) {
        reviews.add(review);
        review.setStore(this);
    }

    public void removeReview(Review review) {
        reviews.remove(review);
        review.setStore(null);
    }

    public List<Review> getActiveReviews() {
        return reviews.stream()
                .filter(review -> review.getIsActive())
                .toList();
    }

    public double getAverageRating() {
        List<Review> activeReviews = getActiveReviews();
        if (activeReviews.isEmpty()) {
            return 0.0;
        }
        return activeReviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
    }

    public int getReviewCount() {
        return (int) reviews.stream()
                .filter(review -> review.getIsActive())
                .count();
    }
}
