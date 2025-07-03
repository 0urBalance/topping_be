package org.balanceus.topping.domain.model;

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

	private String title;

	private String description;

	private String category;

	@ManyToOne
	@JoinColumn(name = "proposer_uuid")
	private User proposer;

	@ManyToOne
	@JoinColumn(name = "target_business_owner_uuid")
	private User targetBusinessOwner;

	@Enumerated(EnumType.STRING)
	private ProposalStatus status;

	private String revenueSharePreference;

	private LocalDateTime startDate;

	private LocalDateTime endDate;

	private String promotionalMedia;

	private Integer trendScore;

	@CreationTimestamp
	private LocalDateTime createdAt;

	@UpdateTimestamp
	private LocalDateTime updatedAt;

	public enum ProposalStatus {
		PENDING, ACCEPTED, REJECTED, LIVE
	}
}