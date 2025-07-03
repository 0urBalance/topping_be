package org.balanceus.topping.infrastructure.service;

import org.balanceus.topping.domain.model.CollaborationProposal;
import org.balanceus.topping.domain.model.Notification;
import org.balanceus.topping.domain.model.User;
import org.balanceus.topping.domain.repository.UserRepository;
import org.balanceus.topping.infrastructure.security.Role;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {

	private final UserRepository userRepository;

	public void notifyBusinessOwnersOfNewProposal(CollaborationProposal proposal) {
		userRepository.findByRole(Role.ROLE_BUSINESS_OWNER)
				.forEach(businessOwner -> {
					sendNotification(businessOwner, 
							"새로운 협업 제안", 
							proposal.getProposer().getUsername() + "님이 '" + proposal.getTitle() + "' 협업을 제안했습니다.",
							Notification.NotificationType.COLLABORATION_PROPOSAL,
							proposal.getUuid().toString());
				});
	}

	public void notifyProposalAccepted(CollaborationProposal proposal) {
		sendNotification(proposal.getProposer(),
				"협업 제안 수락됨 🎉",
				proposal.getTargetBusinessOwner().getUsername() + "님이 귀하의 '" + proposal.getTitle() + "' 협업 제안을 수락했습니다!",
				Notification.NotificationType.PROPOSAL_ACCEPTED,
				proposal.getUuid().toString());
	}

	public void notifyProposalRejected(CollaborationProposal proposal) {
		sendNotification(proposal.getProposer(),
				"협업 제안 결과",
				proposal.getTargetBusinessOwner().getUsername() + "님이 '" + proposal.getTitle() + "' 협업 제안을 거절했습니다.",
				Notification.NotificationType.PROPOSAL_REJECTED,
				proposal.getUuid().toString());
	}

	private void sendNotification(User recipient, String title, String message, 
								 Notification.NotificationType type, String relatedEntityId) {
		// 실제 구현에서는 알림을 데이터베이스에 저장하거나 실시간으로 전송
		System.out.println("📢 알림 전송 - " + recipient.getUsername() + ": " + title + " - " + message);
	}
}