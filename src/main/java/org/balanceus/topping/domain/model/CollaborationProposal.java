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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "collaboration_proposals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CollaborationProposal {

	@Id
	@GeneratedValue
	@UuidGenerator
	private UUID uuid;

	@Enumerated(EnumType.STRING)
	private ProposalSource source;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "proposer_user_uuid")
	private User proposerUser;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "proposer_store_uuid")
	private Store proposerStore;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "target_store_uuid")
	private Store targetStore;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "proposer_product_uuid")
	private Product proposerProduct;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "target_product_uuid")
	private Product targetProduct;

	private LocalDate proposedStart;
	private LocalDate proposedEnd;
	
	@Column(length = 500)
	private String duration;
	
	@Column(length = 100)
	private String profitShare;
	
	@Column(length = 200)
	private String location;

	private String title;

	@Column(length = 2000)
	private String description;

	@Enumerated(EnumType.STRING)
	private CollaborationStatus status;

	@CreationTimestamp
	private Instant createdAt;

	@UpdateTimestamp
	private Instant updatedAt;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "collaboration_uuid")
	private Collaboration collaboration;

	public enum CollaborationStatus {
		PENDING, ACCEPTED, REJECTED, CANCELLED, ENDED
	}
}