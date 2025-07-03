package org.balanceus.topping.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "collaboration_products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CollaborationProduct {

	@Id
	@GeneratedValue
	@UuidGenerator
	private UUID uuid;

	@ManyToOne
	@JoinColumn(name = "collaboration_proposal_uuid")
	private CollaborationProposal collaborationProposal;

	private String name;

	private String description;

	private BigDecimal price;

	private String imageUrl;

	private Integer stockQuantity;

	@Enumerated(EnumType.STRING)
	private ProductStatus status;

	private LocalDateTime availableFrom;

	private LocalDateTime availableUntil;

	private String specialOfferDescription;

	@CreationTimestamp
	private LocalDateTime createdAt;

	@UpdateTimestamp
	private LocalDateTime updatedAt;

	public enum ProductStatus {
		DRAFT, ACTIVE, SOLD_OUT, DISCONTINUED
	}
}