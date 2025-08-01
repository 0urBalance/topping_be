# Mobile Navbar & Chat System Troubleshooting Guide

## Overview

This document covers troubleshooting and fixes for two critical frontend issues that were resolved:
1. **Mobile Hamburger Menu Not Working**
2. **Chat System JavaScript Errors**

## Mobile Navbar Issues

### Problem: Hamburger Menu Not Responsive

**Symptoms:**
- Hamburger menu appears on mobile but clicking does nothing
- No console errors, just no response to clicks
- Menu items not accessible on mobile devices

**Root Causes:**
1. CSS width property commented out, causing sidebar to have no dimensions
2. Mixed event handling causing conflicts between inline and JavaScript handlers
3. Z-index issues preventing click events from reaching the button

### Solution Implementation

#### 1. CSS Width Fix (`navbar.css:21`)
```css
/* BEFORE: Commented out */
/*width: var(--sidebar-width);*/

/* AFTER: Uncommented */
width: var(--sidebar-width);
```

#### 2. Enhanced Event Handling (`navbar.html:33`)
```html
<!-- Dual approach for maximum compatibility -->
<button class="mobile-menu-toggle" id="mobileMenuToggle" onclick="toggleMobileSidebar();">
```

#### 3. Immediate JavaScript Function (`navbar.html:35-65`)
```javascript
// Inline script ensures function is available immediately
window.toggleMobileSidebar = function() {
    console.log('toggleMobileSidebar called');
    const sidebar = document.getElementById('sidebar');
    // ... toggle logic with comprehensive null checks
};
```

#### 4. Z-Index Enhancement (`navbar.css:281`)
```css
/* Increased from 1001 to 9999 for maximum priority */
z-index: 9999;
```

## Chat System Issues

### Problem: JavaScript Errors Breaking Chat Functionality

**Symptoms:**
```
Error loading chat room: TypeError: Cannot read properties of undefined (reading 'username')
loadChatRoom @ chat.js:97

SockJS is not defined
connectWebSocket @ chat.js:214
```

**Root Causes:**
1. `otherUser` undefined due to null `targetBusinessOwner` in collaboration proposals
2. Missing SockJS and STOMP libraries in chat template
3. Outdated STOMP client syntax

### Solution Implementation

#### 1. Null-Safe Participant Resolution (`ChatController.java:96-122`)
```java
// Enhanced null checking with multiple fallback strategies
User otherUser = null;

if (proposal.getProposer().equals(currentUser)) {
    otherUser = proposal.getTargetBusinessOwner();
} else {
    otherUser = proposal.getProposer();
}

// Fallback to collaboration data if proposal data incomplete
if (otherUser == null && chatRoom.getCollaboration() != null) {
    Collaboration collaboration = chatRoom.getCollaboration();
    User productOwner = collaboration.getProduct() != null ? 
        collaboration.getProduct().getStore().getUser() : null;
    User applicant = collaboration.getApplicant();
    
    if (productOwner != null && applicant != null) {
        otherUser = productOwner.equals(currentUser) ? applicant : productOwner;
    }
}

// Final fallback: create placeholder user
if (otherUser == null) {
    otherUser = new User();
    otherUser.setUuid(UUID.randomUUID());
    otherUser.setUsername("Unknown User");
}
```

#### 2. WebSocket Library Integration (`rooms.html:103-104`)
```html
<!-- Added required CDN libraries -->
<script src="https://cdn.jsdelivr.net/npm/sockjs-client@1.6.1/dist/sockjs.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/@stomp/stompjs@7.0.0/bundles/stomp.umd.min.js"></script>
```

#### 3. Modern STOMP Client Implementation (`chat.js:215-240`)
```javascript
// Updated from legacy syntax to modern STOMP client
this.stompClient = new StompJs.Client({
    brokerURL: null,
    webSocketFactory: () => new SockJS('/ws'),
    onConnect: (frame) => {
        console.log('Connected to WebSocket:', frame);
        this.stompClient.subscribe(`/topic/chat/${roomId}`, (message) => {
            const messageData = JSON.parse(message.body);
            this.appendMessage(messageData);
        });
    },
    onStompError: (frame) => {
        console.error('STOMP error:', frame);
        this.showError('실시간 채팅 연결에 실패했습니다.');
    }
});

this.stompClient.activate();
```

## API Error Handling

### Problem: "Unexpected token '<'" JSON Parse Errors

**Symptoms:**
```
Failed to load user info: SyntaxError: Unexpected token '<', "<!DOCTYPE "... is not valid JSON
```

**Root Cause:**
API endpoints returning HTML (login redirects) instead of JSON when user not authenticated.

**Solution:** Content-Type Validation (`main-common.js:26-33`)
```javascript
if (response.ok) {
    const contentType = response.headers.get('content-type');
    if (contentType && contentType.includes('application/json')) {
        this.user = await response.json();
        this.updateUI();
    } else {
        console.warn('User info endpoint returned non-JSON response, user likely not authenticated');
        this.user = null;
    }
}
```

## Template Method Fixes

### Problem: Non-Existent Method Calls in Thymeleaf

**Symptoms:**
```
Method 'getFirstParticipant' not found in ChatRoom
Method 'getCollaborationTitle' not found in ChatRoom
```

**Solution:** Use Actual Entity Fields (`rooms.html:60,63`)
```html
<!-- BEFORE: Non-existent methods -->
<span th:text="${chatRoom.getFirstParticipant().username.substring(0,1)}">

<!-- AFTER: Actual fields with null safety -->
<span th:text="${chatRoom.roomName != null and !#strings.isEmpty(chatRoom.roomName) ? chatRoom.roomName.substring(0,1).toUpperCase() : 'C'}">
```

## Prevention Guidelines

### Mobile Development
1. **Always test responsive functionality on actual mobile devices**
2. **Use both inline and JavaScript event handlers for critical UI elements**
3. **Ensure CSS dimensions are properly set for mobile components**
4. **Use high z-index values for mobile overlay elements**

### Chat System Development
1. **Add comprehensive null checks for all collaboration participants**
2. **Include required WebSocket libraries before custom JavaScript**
3. **Validate API response content-types before JSON parsing**
4. **Use modern WebSocket client libraries with proper error handling**

### Entity Method Usage
1. **All entities use `getUuid()` method, not domain-specific names like `getUserId()`**
2. **Check actual entity fields before using in templates**
3. **Add null safety checks in all Thymeleaf expressions**

## Testing Checklist

### Mobile Navbar
- [ ] Hamburger menu appears on mobile (≤768px)
- [ ] Clicking hamburger opens/closes sidebar
- [ ] Sidebar overlay appears and is clickable
- [ ] Menu animations work properly
- [ ] Touch gestures work (swipe to open/close)

### Chat System
- [ ] Chat rooms list loads without errors
- [ ] Clicking chat room loads conversation
- [ ] User info displays correctly ("otherUser")
- [ ] WebSocket connection establishes successfully
- [ ] Messages send and receive in real-time
- [ ] No console errors during normal usage

## Related Documentation

- [Chat Domain README](../domains/chat/README.md) - Complete chat system architecture
- [CSS Framework Guide](../technical/css-framework.md) - UI component standards
- [CLAUDE.md](../../CLAUDE.md) - Updated project overview with recent fixes