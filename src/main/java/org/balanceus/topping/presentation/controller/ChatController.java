package org.balanceus.topping.presentation.controller;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.balanceus.topping.domain.model.ChatMessage;
import org.balanceus.topping.domain.model.ChatRoom;
import org.balanceus.topping.domain.model.CollaborationProposal;
import org.balanceus.topping.domain.model.User;
import org.balanceus.topping.domain.repository.ChatMessageRepository;
import org.balanceus.topping.domain.repository.ChatRoomRepository;
import org.balanceus.topping.domain.repository.CollaborationProposalRepository;
import org.balanceus.topping.domain.repository.UserRepository;
import org.balanceus.topping.infrastructure.response.ApiResponseData;
import org.balanceus.topping.infrastructure.response.Code;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

	private final ChatRoomRepository chatRoomRepository;
	private final ChatMessageRepository chatMessageRepository;
	private final CollaborationProposalRepository proposalRepository;
	private final UserRepository userRepository;

	@PostMapping("/room/create/{proposalId}")
	@ResponseBody
	public ApiResponseData<ChatRoom> createChatRoom(
			@PathVariable UUID proposalId,
			Principal principal) {

		CollaborationProposal proposal = proposalRepository.findById(proposalId)
				.orElseThrow(() -> new RuntimeException("Proposal not found"));

		if (proposal.getStatus() != CollaborationProposal.ProposalStatus.ACCEPTED) {
			throw new RuntimeException("Only accepted proposals can have chat rooms");
		}

		ChatRoom existingRoom = chatRoomRepository.findByCollaborationProposal(proposal).orElse(null);
		if (existingRoom != null) {
			return ApiResponseData.success(Code.SUCCESS, existingRoom);
		}

		ChatRoom chatRoom = new ChatRoom();
		chatRoom.setCollaborationProposal(proposal);
		chatRoom.setRoomName(proposal.getTitle() + " 협업 채팅방");
		chatRoom.setIsActive(true);

		ChatRoom saved = chatRoomRepository.save(chatRoom);
		return ApiResponseData.success(Code.SUCCESS, saved);
	}

	@GetMapping("/room/{roomId}")
	public String chatRoom(@PathVariable UUID roomId, Model model, Principal principal) {
		ChatRoom chatRoom = chatRoomRepository.findById(roomId)
				.orElseThrow(() -> new RuntimeException("Chat room not found"));

		List<ChatMessage> messages = chatMessageRepository.findByChatRoomOrderByCreatedAtAsc(chatRoom);

		User currentUser = userRepository.findByEmail(principal.getName())
				.orElseThrow(() -> new RuntimeException("User not found"));

		model.addAttribute("chatRoom", chatRoom);
		model.addAttribute("messages", messages);
		model.addAttribute("currentUser", currentUser);
		return "chat/room";
	}

	@PostMapping("/message/send")
	@ResponseBody
	public ApiResponseData<ChatMessage> sendMessage(
			@RequestParam UUID roomId,
			@RequestParam String message,
			Principal principal) {

		ChatRoom chatRoom = chatRoomRepository.findById(roomId)
				.orElseThrow(() -> new RuntimeException("Chat room not found"));

		User sender = userRepository.findByEmail(principal.getName())
				.orElseThrow(() -> new RuntimeException("User not found"));

		ChatMessage chatMessage = new ChatMessage();
		chatMessage.setChatRoom(chatRoom);
		chatMessage.setSender(sender);
		chatMessage.setMessage(message);

		ChatMessage saved = chatMessageRepository.save(chatMessage);
		return ApiResponseData.success(Code.SUCCESS, saved);
	}

	@MessageMapping("/chat/{roomId}")
	@SendTo("/topic/chat/{roomId}")
	public ChatMessage handleMessage(ChatMessage message) {
		return chatMessageRepository.save(message);
	}

	@GetMapping("/rooms")
	public String listChatRooms(Model model, Principal principal) {
		User user = userRepository.findByEmail(principal.getName())
				.orElseThrow(() -> new RuntimeException("User not found"));

		List<ChatRoom> activeChatRooms = chatRoomRepository.findByIsActiveTrue();
		
		List<ChatRoom> userChatRooms = activeChatRooms.stream()
				.filter(room -> {
					CollaborationProposal proposal = room.getCollaborationProposal();
					return proposal.getProposer().equals(user) || 
						   (proposal.getTargetBusinessOwner() != null && proposal.getTargetBusinessOwner().equals(user));
				})
				.toList();

		model.addAttribute("chatRooms", userChatRooms);
		return "chat/rooms";
	}
}