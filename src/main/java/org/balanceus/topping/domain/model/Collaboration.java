package org.balanceus.topping.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
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

	@ManyToOne
	@JoinColumn(name = "product_uuid")
	private Product product;

	@ManyToOne
	@JoinColumn(name = "applicant_uuid")
	private User applicant;

	@Enumerated(EnumType.STRING)
	private CollaborationStatus status;

	private String message;

	@CreationTimestamp
	private LocalDateTime createdAt;

	public enum CollaborationStatus {
		PENDING, ACCEPTED, REJECTED
	}
}