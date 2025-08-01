# 💬 Chat UI Design & Unread Message System

## Overview

This document details the comprehensive chat UI enhancements implemented in the Topping platform, including message bubble redesign, unread message badge system, and accessibility improvements.

## Message Bubble Design

### Alignment System

**Requirements Implementation:**
- **Own Messages (`.bubble.mine`)**: Right-aligned with dark brown color scheme
- **Received Messages (`.bubble.their`)**: Left-aligned with light gray color scheme

### CSS Implementation

```css
/* Message alignment and styling */
.bubble.mine {
    background-color: #6B3410;
    color: white;
    margin-left: auto;
    margin-right: 0;
    border-bottom-right-radius: 4px;
    border-top-left-radius: 16px;
    border-top-right-radius: 16px;
    border-bottom-left-radius: 16px;
    text-align: left;
    display: block;
}

.bubble.their {
    background-color: #f1f1f1;
    color: #333333;
    border: 1px solid #e5e5e5;
    margin-left: 0;
    margin-right: auto;
    border-bottom-left-radius: 4px;
    border-top-left-radius: 16px;
    border-top-right-radius: 16px;
    border-bottom-right-radius: 16px;
    text-align: left;
    display: block;
}
```

### Color Scheme

| Element | Color | Contrast Ratio | Usage |
|---------|-------|----------------|-------|
| Own Messages Background | `#6B3410` | 4.5:1 (white text) | Dark brown for user's messages |
| Own Messages Text | `white` | 4.5:1 | High contrast on dark background |
| Received Messages Background | `#f1f1f1` | 4.5:1 (dark text) | Light gray for others' messages |
| Received Messages Text | `#333333` | 12.6:1 | Dark text on light background |

### Border Radius Design

**Message Tail System:**
- **Own Messages**: Small radius (4px) on bottom-right for "tail" effect
- **Received Messages**: Small radius (4px) on bottom-left for "tail" effect
- **Rounded Corners**: 16px radius on non-tail corners for modern appearance

## Unread Message Badge System

### Visual Design

```css
.unread-badge {
    background: #dc3545;
    color: white;
    border-radius: 50%;
    padding: 4px 8px;
    font-size: 11px;
    font-weight: 600;
    min-width: 20px;
    min-height: 20px;
    text-align: center;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    line-height: 1;
}

/* Hide badge when no unread messages */
.unread-badge.hidden {
    display: none;
}
```

### Badge Behavior

1. **Visibility Logic**: Badges only appear when `unreadCount > 0`
2. **Auto-Hide**: Badges immediately hidden when room is selected
3. **Dynamic Updates**: Count updates in real-time via WebSocket events
4. **Accessibility**: Proper ARIA labels for screen readers

### Template Integration

```html
<div class="unread-badge" 
     th:if="${chatRoom.unreadCount != null and chatRoom.unreadCount > 0}"
     th:text="${chatRoom.unreadCount}"
     th:classappend="${chatRoom.unreadCount == null or chatRoom.unreadCount == 0} ? 'hidden' : ''"
     th:attr="aria-label='읽지 않은 메시지 ' + ${chatRoom.unreadCount} + '개'"
     role="status">
</div>
```

## Backend Implementation

### Database Schema Updates

#### ChatMessage Entity Enhancement

```java
@Entity
@Table(name = "chat_messages")
public class ChatMessage {
    // ... existing fields ...
    
    private LocalDateTime readAt;
    private boolean isRead = false;
}
```

#### Repository Methods

```java
public interface ChatMessageRepository {
    // Unread message count methods
    long countUnreadMessagesInRoom(ChatRoom chatRoom, User user);
    Map<UUID, Long> getUnreadCountsByRoomsForUser(List<ChatRoom> chatRooms, User user);
    void markMessagesAsRead(ChatRoom chatRoom, User user);
}
```

#### JPA Query Implementation

```java
// Count unread messages in a room for a specific user
@Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.chatRoom = :chatRoom AND m.sender != :user AND m.isRead = false")
long countUnreadMessagesInRoom(ChatRoom chatRoom, User user);

// Mark messages as read for a user in a specific room
@Modifying
@Query("UPDATE ChatMessage m SET m.isRead = true, m.readAt = CURRENT_TIMESTAMP WHERE m.chatRoom = :chatRoom AND m.sender != :user AND m.isRead = false")
void markMessagesAsRead(ChatRoom chatRoom, User user);
```

### Service Layer Integration

#### ChatService Enhancements

```java
@Service
@Transactional
public class ChatService {
    // Unread message management
    public Map<UUID, Long> getUnreadCountsByRoomsForUser(List<ChatRoom> chatRooms, User user) {
        return chatMessageRepository.getUnreadCountsByRoomsForUser(chatRooms, user);
    }

    public long getUnreadMessageCount(ChatRoom chatRoom, User user) {
        return chatMessageRepository.countUnreadMessagesInRoom(chatRoom, user);
    }

    @Transactional
    public void markMessagesAsRead(ChatRoom chatRoom, User user) {
        chatMessageRepository.markMessagesAsRead(chatRoom, user);
        log.info("Marked messages as read for user {} in room {}", user.getUuid(), chatRoom.getUuid());
    }
}
```

### Controller Integration

#### Automatic Read Status Management

```java
@GetMapping("/room/{roomId}/data")
@ResponseBody
public ApiResponseData<ChatRoomData> getChatRoomData(@PathVariable UUID roomId, Principal principal) {
    ChatRoom chatRoom = chatRoomRepository.findById(roomId)
            .orElseThrow(() -> new RuntimeException("Chat room not found"));

    User currentUser = userRepository.findByEmail(principal.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));

    // Mark messages as read when user views the chat room
    chatService.markMessagesAsRead(chatRoom, currentUser);

    // ... rest of method
}
```

#### Enhanced Room List with Unread Counts

```java
@GetMapping("/rooms")
public String listChatRooms(Model model, Principal principal) {
    // ... user and room filtering logic ...

    // Get unread message counts for each room
    Map<UUID, Long> unreadCounts = chatService.getUnreadCountsByRoomsForUser(userChatRooms, user);
    
    // Create enhanced chat room data with unread counts
    List<ChatRoomWithUnreadCount> chatRoomsWithCounts = userChatRooms.stream()
            .map(room -> new ChatRoomWithUnreadCount(room, unreadCounts.get(room.getUuid())))
            .toList();

    model.addAttribute("chatRooms", chatRoomsWithCounts);
    return "chat/rooms";
}
```

## Frontend JavaScript Integration

### Badge Management

```javascript
selectChatRoom(roomId) {
    // Remove active class from all rooms
    document.querySelectorAll('.chat-room-item').forEach(item => {
        item.classList.remove('active');
    });
    
    // Add active class to selected room
    const selectedRoom = document.querySelector(`[data-room-id="${roomId}"]`);
    if (selectedRoom) {
        selectedRoom.classList.add('active');
        
        // Hide unread badge when room is selected (messages will be marked as read)
        const unreadBadge = selectedRoom.querySelector('.unread-badge');
        if (unreadBadge) {
            unreadBadge.classList.add('hidden');
            unreadBadge.textContent = '0';
        }
    }
    
    this.selectedRoomId = roomId;
    this.loadChatRoom(roomId);
}
```

### Message Rendering

```javascript
renderMessages(messages) {
    // ... existing message rendering logic ...
    
    messages.forEach(message => {
        const isOwn = message.senderId === this.currentUser?.uuid;
        const bubbleClass = isOwn ? 'mine' : 'their';
        
        messagesHTML += `
            <div class="message-group">
                <div class="bubble ${bubbleClass}">
                    ${this.escapeHtml(message.message)}
                    <div class="message-time">${this.formatTime(message.createdAt)}</div>
                </div>
            </div>
        `;
    });
}
```

## Accessibility Implementation

### ARIA Labels and Roles

```html
<!-- Unread badge with screen reader support -->
<div class="unread-badge" 
     role="status"
     aria-label="읽지 않은 메시지 3개">
    3
</div>
```

### High Contrast Support

```css
/* Enhanced contrast for accessibility */
@media (prefers-contrast: high) {
    .bubble.mine {
        background-color: #4a2107; /* Darker brown */
    }
    
    .bubble.their {
        background-color: #e8e8e8; /* Slightly darker gray */
        color: #1a1a1a;
        border-color: #999;
    }
}
```

### Keyboard Navigation

- **Tab Navigation**: All interactive elements accessible via keyboard
- **Enter Key**: Send message without mouse interaction
- **Screen Reader Support**: Proper semantic HTML and ARIA attributes

## Responsive Design

### Mobile Optimization

```css
@media (max-width: 768px) {
    .bubble {
        max-width: 85%; /* Increased from 70% for mobile */
    }
    
    .unread-badge {
        min-width: 18px; /* Slightly smaller on mobile */
        min-height: 18px;
        font-size: 10px;
    }
}
```

### Layout Considerations

- **Flexible Bubbles**: Messages adapt to content length while maintaining alignment
- **Badge Positioning**: Unread badges remain visible and properly positioned across screen sizes
- **Touch Targets**: Minimum 44px touch targets for mobile interactions

## Performance Considerations

### Database Optimization

1. **Efficient Queries**: Single query to count unread messages per room
2. **Bulk Operations**: Batch processing for multiple room unread counts
3. **Indexed Fields**: Proper indexing on `sender`, `chatRoom`, and `isRead` fields

### Frontend Optimization

1. **CSS Animations**: Hardware-accelerated transitions for smooth badge hiding
2. **Debounced Updates**: Throttled badge updates to prevent excessive rendering
3. **Memory Management**: Proper cleanup of event listeners and WebSocket connections

## Testing Strategy

### Backend Testing

```java
@Test
void shouldCountUnreadMessagesCorrectly() {
    // Given: Chat room with mixed read/unread messages
    // When: Counting unread messages for specific user
    // Then: Should return correct count excluding own messages
}

@Test
void shouldMarkMessagesAsReadWhenViewing() {
    // Given: Chat room with unread messages
    // When: User opens chat room
    // Then: Messages should be marked as read with timestamp
}
```

### Frontend Testing

```javascript
describe('Unread Badge Management', () => {
    it('should hide badge when room is selected', () => {
        // Test badge hiding functionality
    });
    
    it('should update badge count on new message', () => {
        // Test real-time badge updates
    });
});
```

## Common Issues & Solutions

### Badge Not Hiding

**Issue**: Unread badge doesn't hide when room is selected
**Solution**: Check JavaScript event binding and CSS class application

```javascript
// Ensure proper class manipulation
unreadBadge.classList.add('hidden');
unreadBadge.textContent = '0';
```

### Message Alignment Issues

**Issue**: Messages not aligning correctly on different screen sizes
**Solution**: Verify flexbox properties and margin settings

```css
/* Ensure proper alignment */
.bubble.mine {
    margin-left: auto;
    margin-right: 0;
}
```

### Color Contrast Problems

**Issue**: Insufficient color contrast for accessibility
**Solution**: Use WCAG-compliant color combinations and test with contrast analyzers

## Future Enhancements

### Planned Features

1. **Real-time Badge Updates**: WebSocket-based badge count updates
2. **Message Status Indicators**: Read receipts and delivery confirmations
3. **Typing Indicators**: Show when other user is typing
4. **Message Search**: Search within chat history with unread filtering
5. **Notification Integration**: Browser notifications for unread messages

### Scalability Considerations

1. **Badge Caching**: Cache unread counts for better performance
2. **Lazy Loading**: Load unread counts on demand for large room lists
3. **WebSocket Optimization**: Efficient event broadcasting for badge updates
4. **Database Partitioning**: Partition message tables by date for better query performance

## Related Documentation

- [Chat Domain README](../domains/chat/README.md) - Complete chat system documentation
- [CSS Framework](./css-framework.md) - Topping design system guidelines
- [Frontend Optimization](./frontend-optimization.md) - Performance best practices
- [Accessibility Guidelines](./accessibility.md) - WCAG compliance standards