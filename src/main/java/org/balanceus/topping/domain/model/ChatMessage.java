package org.balanceus.topping.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Entity;
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

	@CreationTimestamp
	private LocalDateTime createdAt;
}