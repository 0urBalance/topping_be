package org.balanceus.topping.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product {

	@Id
	@GeneratedValue
	@UuidGenerator
	private UUID uuid;

	@Column(nullable = false)
	@NotBlank(message = "Product name is required")
	private String name;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Column(nullable = false, precision = 10, scale = 2)
	@NotNull(message = "Price is required")
	private BigDecimal price;

	private String thumbnailPath;

	private String imageUrl;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private ProductType productType = ProductType.REGULAR;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private ProductCategory category = ProductCategory.COLLABORATION;

	private String ingredients;

	@Column(nullable = false)
	private Boolean isAvailable = true;

	@Column(nullable = false)
	private Integer reviewCount = 0;

	@Column(nullable = false)
	private Boolean isActive = true;

	@ManyToOne
	@JoinColumn(name = "store_uuid", nullable = false)
	private Store store;

	@ManyToOne
	@JoinColumn(name = "creator_uuid")
	private User creator;

	@ManyToOne
	@JoinColumn(name = "collaboration_uuid")
	private Collaboration collaboration;

	@OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("displayOrder ASC, createdAt ASC")
	private List<ProductImage> images = new ArrayList<>();

	@CreationTimestamp
	private LocalDateTime createdAt;

	// Helper methods for image management
	public void addImage(ProductImage image) {
		images.add(image);
		image.setProduct(this);
	}

	public void removeImage(ProductImage image) {
		images.remove(image);
		image.setProduct(null);
	}

	public List<String> getImagePaths() {
		return images.stream()
				.map(ProductImage::getImagePath)
				.toList();
	}

	public ProductImage getMainImage() {
		return images.stream()
				.filter(img -> img.getImageType() == ProductImage.ImageType.MAIN)
				.findFirst()
				.orElse(null);
	}

	public List<ProductImage> getGalleryImages() {
		return images.stream()
				.filter(img -> img.getImageType() == ProductImage.ImageType.GALLERY)
				.toList();
	}

	public enum ProductType {
		REGULAR("일반 상품"),
		SIGNATURE("시그니처 상품"),
		COLLABORATION("콜라보 상품"),
		SEASONAL("시즌 상품");

		private final String displayName;

		ProductType(String displayName) {
			this.displayName = displayName;
		}

		public String getDisplayName() {
			return displayName;
		}
	}

	public enum ProductCategory {
		COLLABORATION("콜라보 상품"),
		SIGNATURE("시그니처 상품"),
		POPULAR("인기 상품"),
		NEW("신상품"),
		BEVERAGE("음료"),
		FOOD("음식"),
		DESSERT("디저트"),
		BAKERY("베이커리"),
		OTHER("기타");

		private final String displayName;

		ProductCategory(String displayName) {
			this.displayName = displayName;
		}

		public String getDisplayName() {
			return displayName;
		}
	}
}