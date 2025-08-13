package org.balanceus.topping.domain.model;

import java.time.LocalDate;
import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
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
@Table(name = "collaborations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Collaboration {

	@Id
	@GeneratedValue
	@UuidGenerator
	private UUID uuid;

	// Initiating and partner stores
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "initiator_store_uuid")
	private Store initiatorStore;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "partner_store_uuid")
	private Store partnerStore;

	// Products from each store
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "initiator_product_uuid")
	private Product initiatorProduct;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "partner_product_uuid")
	private Product partnerProduct;

	// Period and status
	private LocalDate startDate;
	private LocalDate endDate;

	@Enumerated(EnumType.STRING)
	private CollaborationStatus status;

	// Meta
	private String title;

	@Column(length = 2000)
	private String description;

	@CreationTimestamp
	private Instant createdAt;

	@UpdateTimestamp
	private Instant updatedAt;

	public enum CollaborationStatus {
		PENDING, ACCEPTED, REJECTED, CANCELLED, ENDED
	}
}