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
			// For proposals, return the proposer user or the owner of the proposer store
			if (collaborationProposal.getProposerUser() != null) {
				return collaborationProposal.getProposerUser();
			} else if (collaborationProposal.getProposerStore() != null) {
				return collaborationProposal.getProposerStore().getUser();
			}
		} else if (collaboration != null) {
			// For collaborations, return the owner of the initiator store
			if (collaboration.getInitiatorStore() != null) {
				return collaboration.getInitiatorStore().getUser();
			}
		}
		return null;
	}

	public User getSecondParticipant() {
		if (collaborationProposal != null) {
			// For proposals, return the owner of the target store
			if (collaborationProposal.getTargetStore() != null) {
				return collaborationProposal.getTargetStore().getUser();
			}
		} else if (collaboration != null) {
			// For collaborations, return the owner of the partner store
			if (collaboration.getPartnerStore() != null) {
				return collaboration.getPartnerStore().getUser();
			}
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
			return collaborationProposal.getTitle() != null ? 
				collaborationProposal.getTitle() : "협업 제안";
		} else if (collaboration != null) {
			return collaboration.getTitle() != null ? 
				collaboration.getTitle() : "협업 채팅";
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