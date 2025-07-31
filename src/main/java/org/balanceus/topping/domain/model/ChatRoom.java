package org.balanceus.topping.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "chat_rooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoom {

	@Id
	@GeneratedValue
	@UuidGenerator
	private UUID uuid;

	@OneToOne
	@JoinColumn(name = "collaboration_proposal_uuid")
	private CollaborationProposal collaborationProposal;

	@OneToOne
	@JoinColumn(name = "collaboration_uuid")
	private Collaboration collaboration;

	private String roomName;

	private Boolean isActive;

	@CreationTimestamp
	private LocalDateTime createdAt;

	public User getFirstParticipant() {
		if (collaborationProposal != null) {
			return collaborationProposal.getProposer();
		} else if (collaboration != null) {
			return collaboration.getApplicant();
		}
		return null;
	}

	public User getSecondParticipant() {
		if (collaborationProposal != null) {
			return collaborationProposal.getTargetBusinessOwner();
		} else if (collaboration != null) {
			return collaboration.getProduct().getCreator();
		}
		return null;
	}

	public boolean hasParticipant(User user) {
		User first = getFirstParticipant();
		User second = getSecondParticipant();
		return (first != null && first.getUuid().equals(user.getUuid())) ||
			   (second != null && second.getUuid().equals(user.getUuid()));
	}

	public String getCollaborationTitle() {
		if (collaborationProposal != null) {
			return collaborationProposal.getTitle();
		} else if (collaboration != null) {
			return collaboration.getProduct().getName() + " 협업";
		}
		return "채팅방";
	}

	public String getCollaborationStatus() {
		if (collaborationProposal != null) {
			return collaborationProposal.getStatus() != null ? 
				collaborationProposal.getStatus().name() : "UNKNOWN";
		} else if (collaboration != null) {
			return collaboration.getStatus() != null ? 
				collaboration.getStatus().name() : "UNKNOWN";
		}
		return "UNKNOWN";
	}
}