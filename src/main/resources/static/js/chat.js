// Chat Interface JavaScript
class ChatInterface {
    constructor() {
        this.selectedRoomId = null;
        this.stompClient = null;
        this.currentUser = null;
        
        this.init();
    }
    
    init() {
        this.bindEvents();
        this.setupSearch();
        this.loadUserInfo();
    }
    
    bindEvents() {
        // Chat room selection
        document.querySelectorAll('.chat-room-item').forEach(item => {
            item.addEventListener('click', (e) => {
                const roomId = e.currentTarget.dataset.roomId;
                this.selectChatRoom(roomId);
            });
        });
        
        // Search functionality
        const searchInput = document.getElementById('chatSearch');
        if (searchInput) {
            searchInput.addEventListener('input', (e) => {
                this.filterChatRooms(e.target.value);
            });
        }
        
        // Enter to send message
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Enter' && !e.shiftKey && e.target.classList.contains('message-textarea')) {
                e.preventDefault();
                this.sendMessage();
            }
        });
    }
    
    setupSearch() {
        const searchInput = document.getElementById('chatSearch');
        if (searchInput) {
            // Add search functionality
            searchInput.addEventListener('input', debounce((e) => {
                this.filterChatRooms(e.target.value);
            }, 300));
        }
    }
    
    filterChatRooms(query) {
        const chatRooms = document.querySelectorAll('.chat-room-item');
        const lowerQuery = query.toLowerCase();
        
        chatRooms.forEach(room => {
            const roomName = room.querySelector('.room-name').textContent.toLowerCase();
            const roomPreview = room.querySelector('.room-preview').textContent.toLowerCase();
            
            if (roomName.includes(lowerQuery) || roomPreview.includes(lowerQuery)) {
                room.style.display = 'flex';
            } else {
                room.style.display = 'none';
            }
        });
    }
    
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
    
    async loadChatRoom(roomId) {
        try {
            const response = await fetch(`/chat/room/${roomId}/data`);
            if (response.ok) {
                const responseData = await response.json();
                console.log('Chat room API response:', responseData);
                
                // Extract the actual data from ApiResponseData wrapper
                const chatData = responseData.data || responseData;
                
                // Defensive check to ensure required data exists
                if (!chatData || !chatData.otherUser) {
                    console.error('Invalid chat data structure:', chatData);
                    this.showError('채팅방 데이터가 올바르지 않습니다.');
                    return;
                }
                
                this.renderChatInterface(chatData);
                this.connectWebSocket(roomId);
            } else {
                console.error('Failed to load chat room data');
                this.showError('채팅방을 불러올 수 없습니다.');
            }
        } catch (error) {
            console.error('Error loading chat room:', error);
            this.showError('채팅방을 불러오는 중 오류가 발생했습니다.');
        }
    }
    
    renderChatInterface(chatData) {
        const chatMain = document.getElementById('chatMain');
        
        // Additional defensive checks
        const otherUser = chatData.otherUser || { username: 'Unknown User' };
        const roomName = chatData.roomName || '채팅방';
        const messages = chatData.messages || [];
        
        console.log('Rendering chat interface with:', { otherUser, roomName, messagesCount: messages.length });
        
        const chatHTML = `
            <div class="chat-header">
                <div class="chat-user-info">
                    <div class="chat-user-avatar">
                        ${this.getAvatarText(otherUser.username)}
                    </div>
                    <div class="chat-user-details">
                        <h4>${otherUser.username}</h4>
                        <p class="chat-user-status">접속</p>
                    </div>
                </div>
                <div class="chat-header-actions">
                    <button class="proposal-btn" onclick="viewProposal('${chatData.collaborationId}')">
                        <span class="material-symbols-outlined">description</span>
                        제안서 보기
                    </button>
                </div>
            </div>
            
            <div class="chat-body" id="chatBody">
                ${this.renderMessages(messages)}
            </div>
            
            <div class="chat-input">
                <div class="input-container">
                    <textarea 
                        class="message-textarea" 
                        id="messageInput"
                        placeholder="메시지를 입력하세요..."
                        rows="1"></textarea>
                    <button class="send-btn" id="sendBtn" onclick="chatInterface.sendMessage()">
                        <span class="material-symbols-outlined">send</span>
                    </button>
                </div>
            </div>
        `;
        
        chatMain.innerHTML = chatHTML;
        this.setupMessageInput();
        this.scrollToBottom();
    }
    
    renderMessages(messages) {
        if (!messages || messages.length === 0) {
            return `
                <div class="chat-placeholder">
                    <div class="placeholder-icon">
                        <span class="material-symbols-outlined">chat_bubble</span>
                    </div>
                    <h3>대화를 시작해보세요</h3>
                    <p>첫 메시지를 보내서 협업에 대해 이야기해보세요.</p>
                </div>
            `;
        }
        
        let messagesHTML = '';
        let currentDate = '';
        
        messages.forEach(message => {
            // Defensive checks for message properties
            if (!message || !message.senderId || !message.message) {
                console.warn('Invalid message data:', message);
                return; // Skip invalid messages
            }
            
            const messageDate = this.formatDate(message.createdAt);
            
            // Add date separator if date changed
            if (messageDate !== currentDate) {
                messagesHTML += `
                    <div class="date-separator">
                        <span>${messageDate}</span>
                    </div>
                `;
                currentDate = messageDate;
            }
            
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
        
        return messagesHTML;
    }
    
    setupMessageInput() {
        const textarea = document.getElementById('messageInput');
        if (textarea) {
            // Auto-resize textarea
            textarea.addEventListener('input', (e) => {
                e.target.style.height = 'auto';
                e.target.style.height = Math.min(e.target.scrollHeight, 120) + 'px';
                
                // Enable/disable send button
                const sendBtn = document.getElementById('sendBtn');
                if (sendBtn) {
                    sendBtn.disabled = !e.target.value.trim();
                }
            });
        }
    }
    
    connectWebSocket(roomId) {
        if (this.stompClient) {
            this.stompClient.deactivate();
        }
        
        // Use modern STOMP client
        this.stompClient = new StompJs.Client({
            brokerURL: null, // We'll use SockJS instead
            webSocketFactory: () => new SockJS('/ws'),
            debug: (str) => {
                console.log('STOMP Debug:', str);
            },
            onConnect: (frame) => {
                console.log('Connected to WebSocket:', frame);
                
                // Subscribe to chat room messages
                this.stompClient.subscribe(`/topic/chat/${roomId}`, (message) => {
                    const messageData = JSON.parse(message.body);
                    this.appendMessage(messageData);
                });
            },
            onStompError: (frame) => {
                console.error('STOMP error:', frame);
                this.showError('실시간 채팅 연결에 실패했습니다.');
            },
            onWebSocketError: (event) => {
                console.error('WebSocket error:', event);
                this.showError('실시간 채팅 연결에 실패했습니다.');
            }
        });
        
        this.stompClient.activate();
    }
    
    async sendMessage() {
        const textarea = document.getElementById('messageInput');
        const message = textarea?.value.trim();
        
        if (!message || !this.selectedRoomId) {
            return;
        }
        
        try {
            const response = await fetch('/chat/message/send', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    roomId: this.selectedRoomId,
                    message: message
                })
            });
            
            if (response.ok) {
                textarea.value = '';
                textarea.style.height = 'auto';
                document.getElementById('sendBtn').disabled = true;
            } else {
                throw new Error('Failed to send message');
            }
        } catch (error) {
            console.error('Error sending message:', error);
            this.showError('메시지 전송에 실패했습니다.');
        }
    }
    
    appendMessage(messageData) {
        const chatBody = document.getElementById('chatBody');
        if (!chatBody) return;
        
        // Check if we need a date separator
        const lastMessage = chatBody.querySelector('.message-group:last-child');
        const messageDate = this.formatDate(messageData.createdAt);
        let needDateSeparator = true;
        
        if (lastMessage) {
            const lastDateSeparator = chatBody.querySelector('.date-separator:last-of-type span');
            if (lastDateSeparator && lastDateSeparator.textContent === messageDate) {
                needDateSeparator = false;
            }
        }
        
        let messageHTML = '';
        
        if (needDateSeparator) {
            messageHTML += `
                <div class="date-separator">
                    <span>${messageDate}</span>
                </div>
            `;
        }
        
        const isOwn = messageData.sender.uuid === this.currentUser?.uuid;
        const bubbleClass = isOwn ? 'mine' : 'their';
        
        messageHTML += `
            <div class="message-group">
                <div class="bubble ${bubbleClass}">
                    ${this.escapeHtml(messageData.message)}
                    <div class="message-time">${this.formatTime(messageData.createdAt)}</div>
                </div>
            </div>
        `;
        
        chatBody.insertAdjacentHTML('beforeend', messageHTML);
        this.scrollToBottom();
        
        // Update unread badge for other rooms (if this message is from a different room)
        if (!isOwn) {
            this.updateUnreadBadges();
        }
    }
    
    updateUnreadBadges() {
        // This could be enhanced to make an API call to get updated unread counts
        // For now, we'll just refresh the page periodically or on focus
        // In a production app, you might want to use WebSocket events for real-time updates
    }
    
    scrollToBottom() {
        const chatBody = document.getElementById('chatBody');
        if (chatBody) {
            chatBody.scrollTop = chatBody.scrollHeight;
        }
    }
    
    async loadUserInfo() {
        try {
            const response = await fetch('/api/session/user');
            if (response.ok) {
                this.currentUser = await response.json();
            }
        } catch (error) {
            console.error('Error loading user info:', error);
        }
    }
    
    showError(message) {
        // Simple error display - can be enhanced with toast notifications
        alert(message);
    }
    
    getAvatarText(username) {
        return username ? username.charAt(0).toUpperCase() : 'U';
    }
    
    formatDate(timestamp) {
        const date = new Date(timestamp);
        const today = new Date();
        const yesterday = new Date(today);
        yesterday.setDate(today.getDate() - 1);
        
        if (this.isSameDay(date, today)) {
            return '오늘';
        } else if (this.isSameDay(date, yesterday)) {
            return '어제';
        } else {
            return `${date.getFullYear()}년 ${date.getMonth() + 1}월 ${date.getDate()}일`;
        }
    }
    
    formatTime(timestamp) {
        const date = new Date(timestamp);
        const hours = date.getHours();
        const minutes = date.getMinutes().toString().padStart(2, '0');
        const period = hours < 12 ? '오전' : '오후';
        const displayHours = hours === 0 ? 12 : hours > 12 ? hours - 12 : hours;
        
        return `${period} ${displayHours}:${minutes}`;
    }
    
    isSameDay(date1, date2) {
        return date1.getFullYear() === date2.getFullYear() &&
               date1.getMonth() === date2.getMonth() &&
               date1.getDate() === date2.getDate();
    }
    
    escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }
}

// Utility function for debouncing
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

// Global function for viewing proposals
function viewProposal(collaborationId) {
    // Open proposal in modal or new page
    window.open(`/proposals/${collaborationId}`, '_blank');
}

// Initialize chat interface when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    window.chatInterface = new ChatInterface();
});

// Handle page visibility for WebSocket connection management
document.addEventListener('visibilitychange', () => {
    if (document.hidden) {
        // Page is hidden - could pause WebSocket
    } else {
        // Page is visible - ensure WebSocket is connected
        if (window.chatInterface && window.chatInterface.selectedRoomId && !window.chatInterface.stompClient?.connected) {
            window.chatInterface.connectWebSocket(window.chatInterface.selectedRoomId);
        }
    }
});