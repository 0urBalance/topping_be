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
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

	@Id
	@GeneratedValue
	@UuidGenerator
	private UUID uuid;

	@ManyToOne
	@JoinColumn(name = "recipient_uuid")
	private User recipient;

	private String title;

	private String message;

	@Enumerated(EnumType.STRING)
	private NotificationType type;

	private String relatedEntityId;

	private Boolean isRead;

	@CreationTimestamp
	private LocalDateTime createdAt;

	public enum NotificationType {
		COLLABORATION_PROPOSAL, PROPOSAL_ACCEPTED, PROPOSAL_REJECTED, CHAT_MESSAGE, PRODUCT_PUBLISHED
	}
}