package org.balanceus.topping.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "product_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductImage {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID uuid;

    @Column(nullable = false)
    @NotBlank(message = "Image path is required")
    private String imagePath;

    @Column(nullable = false)
    @NotBlank(message = "Original filename is required")
    private String originalFilename;

    @Column(nullable = false)
    @NotNull(message = "File size is required")
    private Long fileSize;

    @Column(nullable = false)
    @NotBlank(message = "Content type is required")
    private String contentType;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ImageType imageType = ImageType.GALLERY;

    private Integer displayOrder = 0;

    @ManyToOne
    @JoinColumn(name = "product_uuid", nullable = false)
    private Product product;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public enum ImageType {
        MAIN("대표 이미지"),
        GALLERY("갤러리 이미지"),
        DETAIL("상세 이미지"),
        THUMBNAIL("썸네일 이미지");

        private final String displayName;

        ImageType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}