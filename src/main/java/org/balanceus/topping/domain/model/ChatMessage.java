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
import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "chat_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

	@Id
	@GeneratedValue
	@UuidGenerator
	private UUID uuid;

	@ManyToOne
	@JoinColumn(name = "chat_room_uuid")
	private ChatRoom chatRoom;

	@ManyToOne
	@JoinColumn(name = "sender_uuid")
	private User sender;

	private String message;

	@Column(nullable = true, name = "message_type")
	@Enumerated(EnumType.STRING)
	private MessageType messageType = MessageType.TEXT;

	@Column(length = 2000)
	private String proposalData;

	@ManyToOne
	@JoinColumn(name = "collaboration_proposal_uuid")
	private CollaborationProposal collaborationProposal;

	@CreationTimestamp
	private LocalDateTime createdAt;

	private LocalDateTime readAt;

	private boolean isRead = false;

	public enum MessageType {
		TEXT("일반 메시지"),
		PROPOSAL_SHARE("제안서 공유"),
		PROPOSAL_UPDATE("제안서 업데이트"),
		PROPOSAL_ACCEPTED("제안서 수락"),
		PROPOSAL_REJECTED("제안서 거절"),
		PROPOSAL_MODIFIED("제안서 수정"),
		PROPOSAL_STATUS_CHANGE("제안서 상태 변경");

		private final String displayName;

		MessageType(String displayName) {
			this.displayName = displayName;
		}

		public String getDisplayName() {
			return displayName;
		}
	}
	
	@PostLoad
	@PrePersist
	public void ensureMessageType() {
		if (this.messageType == null) {
			this.messageType = MessageType.TEXT;
		}
	}
}