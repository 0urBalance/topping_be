# Chat Domain - Claude Guidance

## Overview
The Chat domain provides real-time messaging capabilities with automatic room creation for collaborations, modern UI design, and WebSocket integration.

## Core Entities

### ChatRoom
```java
@Entity
public class ChatRoom {
    @Id @GeneratedValue private UUID uuid;
    private String name;
    private UUID collaborationUuid; // Links to Collaboration or CollaborationProposal
    private LocalDateTime createdAt;
    
    // Virtual participants - computed from collaboration
    @Transient
    public List<User> getParticipants() {
        // Dynamically resolved from collaboration entities
    }
}
```

### ChatMessage
```java
@Entity
public class ChatMessage {
    @Id @GeneratedValue private UUID uuid;
    @ManyToOne private ChatRoom chatRoom;
    @ManyToOne private User sender;
    private String content;
    private LocalDateTime sentAt;
    private LocalDateTime readAt;    // ⚠️ Read tracking
    private boolean isRead = false;  // ⚠️ Read status
}
```

## Automatic Room Creation

### Service Integration
```java
@Service
public class ChatService {
    
    @Transactional
    public ChatRoom createChatRoomForCollaboration(Collaboration collaboration) {
        // Prevent duplicate rooms
        Optional<ChatRoom> existingRoom = chatRoomRepository.findByCollaborationUuid(collaboration.getUuid());
        if (existingRoom.isPresent()) {
            return existingRoom.get();
        }
        
        ChatRoom chatRoom = ChatRoom.builder()
            .name(collaboration.getTitle() + " 채팅방")
            .collaborationUuid(collaboration.getUuid())
            .build();
        
        return chatRoomRepository.save(chatRoom);
    }
    
    // Also supports CollaborationProposal
    public ChatRoom createChatRoomForProposal(CollaborationProposal proposal) {
        // Similar logic for pending proposals
    }
}
```

### Controller Integration
```java
// Automatic room creation in collaboration acceptance
@PostMapping("/collaborations/{proposalId}/accept")
public String acceptProposal(@PathVariable UUID proposalId) {
    Collaboration collaboration = collaborationService.acceptProposal(proposalId);
    
    // ⚠️ CRITICAL: Create chat room automatically
    chatService.createChatRoomForCollaboration(collaboration);
    
    return "redirect:/mypage/ongoing";
}
```

## Modern Chat Interface

### Unified Single-Page Design
```html
<!-- /chat/rooms - Complete chat interface -->
<div class="chat-container">
    <div class="chat-sidebar">
        <div class="chat-search">
            <input type="text" id="chatSearch" placeholder="채팅방 검색..." />
        </div>
        <div class="chat-rooms-list">
            <div th:each="room : ${chatRooms}" 
                 th:class="'chat-room-item' + (${room.uuid == selectedRoomId} ? ' active' : '')"
                 th:data-room-id="${room.uuid}">
                <h4 th:text="${room.name}">Room Name</h4>
                <span class="unread-badge" th:if="${room.unreadCount > 0}"
                      th:text="${room.unreadCount}"
                      th:aria-label="'읽지 않은 메시지 ' + ${room.unreadCount} + '개'">0</span>
            </div>
        </div>
    </div>
    
    <div class="chat-main">
        <!-- Main chat panel with message display -->
    </div>
</div>
```

### Message Bubble Design
```css
/* Message bubble styling */
.bubble {
    max-width: 70%;
    padding: 12px 16px;
    border-radius: 18px;
    margin-bottom: 8px;
    word-wrap: break-word;
}

.bubble.mine {
    background-color: #6B3410; /* Dark brown for own messages */
    color: white;
    margin-left: auto;
    border-bottom-right-radius: 4px;
}

.bubble.their {
    background-color: #f1f1f1; /* Light gray for received messages */
    color: #333;
    margin-right: auto;
    border-bottom-left-radius: 4px;
}
```

## WebSocket Integration

### Configuration
```java
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new ChatWebSocketHandler(), "/ws")
                .setAllowedOrigins("*")
                .withSockJS(); // Fallback for older browsers
    }
}
```

### STOMP Messaging
```javascript
// Modern WebSocket client implementation
class ChatWebSocket {
    constructor() {
        this.stompClient = null;
        this.currentRoomId = null;
    }
    
    connect() {
        const socket = new SockJS('/ws');
        this.stompClient = Stomp.over(socket);
        
        this.stompClient.connect({}, (frame) => {
            console.log('Connected: ' + frame);
            this.subscribeToRoom(this.currentRoomId);
        });
    }
    
    subscribeToRoom(roomId) {
        if (this.stompClient && roomId) {
            this.stompClient.subscribe(`/topic/chat/${roomId}`, (message) => {
                this.displayMessage(JSON.parse(message.body));
            });
        }
    }
    
    sendMessage(roomId, content) {
        this.stompClient.send(`/app/chat/message`, {}, JSON.stringify({
            chatRoomId: roomId,
            content: content
        }));
    }
}
```

## API Endpoints

### Chat Room Data
```java
@GetMapping("/chat/room/{roomId}/data")
public ResponseEntity<ChatRoomDataResponse> getChatRoomData(@PathVariable UUID roomId,
                                                           @AuthenticationPrincipal UserDetailsImpl userDetails) {
    // Get room, messages, and participants
    ChatRoomDataResponse response = chatService.getChatRoomData(roomId, userDetails.getUser());
    
    // ⚠️ Auto-mark messages as read when room is accessed
    chatService.markMessagesAsRead(roomId, userDetails.getUser());
    
    return ResponseEntity.ok(response);
}
```

### Message Sending
```java
@PostMapping("/chat/message/send")
public ResponseEntity<ApiResponseData<String>> sendMessage(@RequestBody ChatMessageRequest request,
                                                         @AuthenticationPrincipal UserDetailsImpl userDetails) {
    // Save message to database
    ChatMessage message = chatService.saveMessage(request, userDetails.getUser());
    
    // ⚠️ CRITICAL: Broadcast via WebSocket immediately
    simpMessagingTemplate.convertAndSend("/topic/chat/" + request.getChatRoomId(), message);
    
    return ResponseEntity.ok(ApiResponseData.success("메시지 전송 완료"));
}
```

### Session User Info
```java
@GetMapping("/api/session/user")
public ResponseEntity<ApiResponseData<SessionUserInfo>> getCurrentUser(@AuthenticationPrincipal UserDetailsImpl userDetails) {
    // ⚠️ Returns wrapped response for proper UUID extraction
    SessionUserInfo userInfo = SessionUserInfo.from(userDetails.getUser());
    return ResponseEntity.ok(ApiResponseData.success(userInfo));
}
```

## Unread Message System

### Database Tracking
```java
// Service method for read status management
@Transactional
public void markMessagesAsRead(UUID roomId, User user) {
    List<ChatMessage> unreadMessages = chatMessageRepository.findUnreadMessagesInRoom(roomId, user);
    
    for (ChatMessage message : unreadMessages) {
        message.setRead(true);
        message.setReadAt(LocalDateTime.now());
    }
    
    chatMessageRepository.saveAll(unreadMessages);
}

// Count unread messages for badge display
public long countUnreadMessages(UUID roomId, User user) {
    return chatMessageRepository.countUnreadMessagesInRoom(roomId, user);
}
```

### UI Badge Management
```javascript
// Auto-hide badges when room is selected
function selectChatRoom(roomId) {
    // Mark room as active
    document.querySelectorAll('.chat-room-item').forEach(item => {
        item.classList.remove('active');
    });
    
    const selectedRoom = document.querySelector(`[data-room-id="${roomId}"]`);
    selectedRoom.classList.add('active');
    
    // Hide unread badge
    const badge = selectedRoom.querySelector('.unread-badge');
    if (badge) {
        badge.classList.add('hidden');
    }
    
    // Load room data and mark as read
    loadChatRoomData(roomId);
}
```

## Message Alignment & User Identification

### Proper "Mine vs Their" Logic
```javascript
// Extract user UUID from API response wrapper
function getCurrentUserUuid() {
    return fetch('/api/session/user')
        .then(response => response.json())
        .then(data => {
            // ⚠️ Extract from ApiResponseData wrapper
            return data.data.uuid; // Not data.uuid directly
        });
}

function displayMessage(message) {
    getCurrentUserUuid().then(currentUserUuid => {
        const bubble = document.createElement('div');
        bubble.className = 'bubble';
        
        // ⚠️ Proper UUID comparison for alignment
        if (message.sender.uuid === currentUserUuid) {
            bubble.classList.add('mine'); // Right-aligned, dark brown
        } else {
            bubble.classList.add('their'); // Left-aligned, light gray
        }
        
        bubble.textContent = message.content;
        document.querySelector('.messages-container').appendChild(bubble);
    });
}
```

## Timestamp Display

### Korean Formatting
```javascript
function formatTimestamp(timestamp) {
    const date = new Date(timestamp);
    const now = new Date();
    
    // Format as Korean time (오전/오후 HH:mm)
    const timeOptions = {
        hour: '2-digit',
        minute: '2-digit',
        hour12: true,
        locale: 'ko-KR'
    };
    
    const formattedTime = date.toLocaleTimeString('ko-KR', timeOptions);
    
    // Add date separators (오늘, 어제, YYYY년 MM월 DD일)
    if (isSameDay(date, now)) {
        return `오늘 ${formattedTime}`;
    } else if (isYesterday(date, now)) {
        return `어제 ${formattedTime}`;
    } else {
        const dateStr = date.toLocaleDateString('ko-KR', {
            year: 'numeric',
            month: 'long',
            day: 'numeric'
        });
        return `${dateStr} ${formattedTime}`;
    }
}
```

## Repository Pattern

### Dual Entity Support
```java
// ChatRoomRepository with collaboration lookup
public interface ChatRoomRepository extends JpaRepository<ChatRoom, UUID> {
    
    @Query("SELECT cr FROM ChatRoom cr WHERE cr.collaborationUuid = :collaborationUuid")
    Optional<ChatRoom> findByCollaborationUuid(@Param("collaborationUuid") UUID collaborationUuid);
    
    // Support both Collaboration and CollaborationProposal
    @Query("SELECT cr FROM ChatRoom cr WHERE cr.collaborationUuid IN " +
           "(SELECT c.uuid FROM Collaboration c WHERE c.initiator = :user OR c.partner = :user) OR " +
           "cr.collaborationUuid IN " +
           "(SELECT cp.uuid FROM CollaborationProposal cp WHERE cp.initiatorStore.owner = :user OR cp.partnerStore.owner = :user)")
    List<ChatRoom> findByParticipant(@Param("user") User user);
}
```

## Common Pitfalls
- ❌ **Missing Room Creation**: Always create chat rooms when collaborations are accepted
- ❌ **WebSocket Libraries**: Include SockJS and STOMP CDN libraries before custom JavaScript
- ❌ **Message Alignment**: Use proper UUID comparison from `ApiResponseData` wrapper
- ❌ **Read Status**: Remember to mark messages as read when room is accessed
- ❌ **Null Participants**: Check for null collaboration participants in room creation
- ❌ **Badge Hiding**: Use `.hidden` class, not direct `display: none`

## Integration Points
- **Collaboration Domain**: Automatic room creation for accepted collaborations
- **User Domain**: User authentication and session management
- **WebSocket**: Real-time message broadcasting with STOMP
- **MyPage**: Chat room access from collaboration management

## Testing Patterns
```java
@ActiveProfiles("test")
class ChatServiceTest {
    // Test automatic room creation
    // Verify dual entity support
    // Test read status management
    // Mock WebSocket message broadcasting
}
```

## Related Documentation
- [Main Claude Guidance](../../../CLAUDE.md)
- [Collaboration Domain](../collaboration/CLAUDE.md)
- [Chat System Integration](../../technical/chat-system-integration.md)
- [Chat UI Enhancements](../../technical/chat-ui-enhancements.md)
- [Chat Real-time Messaging](../../technical/chat-real-time-messaging.md)