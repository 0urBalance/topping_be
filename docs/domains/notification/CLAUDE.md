# Notification Domain - Claude Guidance

## Overview
The Notification domain handles event-driven notifications for user actions, collaboration updates, and system events in the Topping platform.

## Core Entity

### Notification
```java
@Entity
public class Notification {
    @Id @GeneratedValue private UUID uuid;
    @ManyToOne private User recipient;
    private String title;
    private String message;
    private NotificationType type;
    private String relatedEntityId; // UUID of related entity
    private boolean isRead = false;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}
```

### Notification Types
```java
public enum NotificationType {
    COLLABORATION_PROPOSAL,    // New collaboration proposal received
    COLLABORATION_ACCEPTED,    // Your proposal was accepted
    COLLABORATION_REJECTED,    // Your proposal was rejected
    CHAT_MESSAGE,             // New chat message received
    STORE_LIKE,              // Someone liked your store
    PRODUCT_REVIEW,          // New review on your product
    SYSTEM_ANNOUNCEMENT      // System-wide announcements
}
```

## Repository Pattern
- **Domain Interface**: `NotificationRepository`
- **JPA Interface**: `NotificationJpaRepository extends JpaRepository<Notification, UUID>`
- **Implementation**: `NotificationRepositoryImpl implements NotificationRepository`

## Service Layer Patterns

### Event-Driven Notification Creation
```java
@Service
public class NotificationService {
    
    @Async
    public void createCollaborationProposalNotification(CollaborationProposal proposal) {
        User recipient = proposal.getPartnerStore().getOwner();
        
        Notification notification = Notification.builder()
            .recipient(recipient)
            .title("새로운 콜라보 제안")
            .message(proposal.getInitiatorStore().getName() + "에서 콜라보를 제안했습니다.")
            .type(NotificationType.COLLABORATION_PROPOSAL)
            .relatedEntityId(proposal.getUuid().toString())
            .build();
            
        notificationRepository.save(notification);
    }
    
    @Async 
    public void createChatMessageNotification(ChatMessage message) {
        ChatRoom room = message.getChatRoom();
        List<User> participants = room.getParticipants();
        
        for (User participant : participants) {
            if (!participant.equals(message.getSender())) {
                Notification notification = Notification.builder()
                    .recipient(participant)
                    .title("새 메시지")
                    .message(message.getSender().getName() + ": " + message.getContent())
                    .type(NotificationType.CHAT_MESSAGE)
                    .relatedEntityId(room.getUuid().toString())
                    .build();
                    
                notificationRepository.save(notification);
            }
        }
    }
}
```

### Notification Retrieval
```java
public List<Notification> getUserNotifications(User user, int limit) {
    return notificationRepository.findByRecipientOrderByCreatedAtDesc(user, PageRequest.of(0, limit));
}

public long countUnreadNotifications(User user) {
    return notificationRepository.countByRecipientAndIsReadFalse(user);
}

@Transactional
public void markAsRead(UUID notificationId, User user) {
    Notification notification = notificationRepository.findById(notificationId)
        .orElseThrow(() -> new EntityNotFoundException("알림을 찾을 수 없습니다"));
        
    if (!notification.getRecipient().equals(user)) {
        throw new AccessDeniedException("알림에 접근할 권한이 없습니다");
    }
    
    notification.setRead(true);
    notification.setReadAt(LocalDateTime.now());
    notificationRepository.save(notification);
}
```

## Controller Patterns

### Notification API
```java
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    
    @GetMapping
    public ResponseEntity<ApiResponseData<List<NotificationResponse>>> getNotifications(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(defaultValue = "20") int limit) {
        List<Notification> notifications = notificationService.getUserNotifications(userDetails.getUser(), limit);
        List<NotificationResponse> response = notifications.stream()
            .map(NotificationResponse::from)
            .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponseData.success(response));
    }
    
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponseData<Long>> getUnreadCount(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        long count = notificationService.countUnreadNotifications(userDetails.getUser());
        return ResponseEntity.ok(ApiResponseData.success(count));
    }
    
    @PostMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponseData<String>> markAsRead(@PathVariable UUID notificationId,
                                                            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        notificationService.markAsRead(notificationId, userDetails.getUser());
        return ResponseEntity.ok(ApiResponseData.success("알림을 읽음으로 표시했습니다"));
    }
}
```

## Template Integration

### Notification Dropdown
```html
<!-- Navbar notification dropdown -->
<div class="notification-dropdown">
    <button class="notification-trigger" onclick="toggleNotifications()">
        <i class="icon-bell"></i>
        <span class="notification-badge" id="unreadCount" style="display: none;"></span>
    </button>
    
    <div class="notification-panel" id="notificationPanel" style="display: none;">
        <div class="notification-header">
            <h3>알림</h3>
            <button onclick="markAllAsRead()">모두 읽음</button>
        </div>
        
        <div class="notification-list" id="notificationList">
            <!-- Dynamically populated via JavaScript -->
        </div>
        
        <div class="notification-footer">
            <a href="/notifications">모든 알림 보기</a>
        </div>
    </div>
</div>
```

### JavaScript Integration
```javascript
// Notification management
class NotificationManager {
    constructor() {
        this.loadUnreadCount();
        this.setupPolling();
    }
    
    async loadUnreadCount() {
        try {
            const response = await fetch('/api/notifications/unread-count');
            const data = await response.json();
            this.updateBadge(data.data);
        } catch (error) {
            console.error('Failed to load unread count:', error);
        }
    }
    
    updateBadge(count) {
        const badge = document.getElementById('unreadCount');
        if (count > 0) {
            badge.textContent = count > 99 ? '99+' : count;
            badge.style.display = 'inline-block';
        } else {
            badge.style.display = 'none';
        }
    }
    
    async loadNotifications() {
        try {
            const response = await fetch('/api/notifications?limit=10');
            const data = await response.json();
            this.renderNotifications(data.data);
        } catch (error) {
            console.error('Failed to load notifications:', error);
        }
    }
    
    renderNotifications(notifications) {
        const container = document.getElementById('notificationList');
        container.innerHTML = '';
        
        notifications.forEach(notification => {
            const item = this.createNotificationItem(notification);
            container.appendChild(item);
        });
    }
    
    createNotificationItem(notification) {
        const item = document.createElement('div');
        item.className = `notification-item ${!notification.isRead ? 'unread' : ''}`;
        item.innerHTML = `
            <div class="notification-content">
                <h4>${notification.title}</h4>
                <p>${notification.message}</p>
                <span class="notification-time">${this.formatTime(notification.createdAt)}</span>
            </div>
            ${!notification.isRead ? '<div class="unread-indicator"></div>' : ''}
        `;
        
        item.addEventListener('click', () => {
            this.markAsRead(notification.uuid);
            this.handleNotificationClick(notification);
        });
        
        return item;
    }
    
    async markAsRead(notificationId) {
        try {
            await fetch(`/api/notifications/${notificationId}/read`, {
                method: 'POST'
            });
            this.loadUnreadCount();
        } catch (error) {
            console.error('Failed to mark as read:', error);
        }
    }
    
    handleNotificationClick(notification) {
        // Navigate based on notification type
        switch (notification.type) {
            case 'COLLABORATION_PROPOSAL':
                window.location.href = '/mypage/received';
                break;
            case 'CHAT_MESSAGE':
                window.location.href = `/chat/rooms?room=${notification.relatedEntityId}`;
                break;
            case 'STORE_LIKE':
                window.location.href = '/mypage/store';
                break;
            default:
                break;
        }
    }
    
    setupPolling() {
        // Poll for new notifications every 30 seconds
        setInterval(() => {
            this.loadUnreadCount();
        }, 30000);
    }
}

// Initialize notification manager
document.addEventListener('DOMContentLoaded', () => {
    new NotificationManager();
});
```

## Event Integration

### Service Layer Events
```java
// In CollaborationService
@EventListener
public void handleCollaborationProposal(CollaborationProposalCreatedEvent event) {
    notificationService.createCollaborationProposalNotification(event.getProposal());
}

// In ChatService  
@EventListener
public void handleChatMessage(ChatMessageSentEvent event) {
    notificationService.createChatMessageNotification(event.getMessage());
}
```

### Async Configuration
```java
@Configuration
@EnableAsync
public class AsyncConfig {
    
    @Bean(name = "notificationExecutor")
    public Executor notificationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("notification-");
        executor.initialize();
        return executor;
    }
}
```

## Common Pitfalls
- ❌ **Blocking Operations**: Use @Async for notification creation to avoid blocking main flow
- ❌ **Missing Authorization**: Always verify notification ownership before marking as read
- ❌ **Polling Overload**: Limit polling frequency to avoid server overload
- ❌ **Notification Spam**: Implement rate limiting for chat message notifications
- ❌ **Missing Cleanup**: Implement notification cleanup for old read notifications

## Integration Points
- **Collaboration Domain**: Proposal and acceptance notifications
- **Chat Domain**: New message notifications
- **Store Domain**: Store like and review notifications
- **User Domain**: User-specific notification targeting

## Testing Patterns
```java
@ActiveProfiles("test")
class NotificationServiceTest {
    // Test async notification creation
    // Verify proper event handling
    // Test notification ownership and authorization
}
```

## Related Documentation
- [Main Claude Guidance](../../../CLAUDE.md)
- [Collaboration Domain](../collaboration/CLAUDE.md)
- [Chat Domain](../chat/CLAUDE.md)