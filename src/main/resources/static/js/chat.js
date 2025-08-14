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
        this.loadUserInfo().then(() => {
            console.log('Chat interface initialized with user:', this.currentUser);
        });
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
        
        // Proposal action buttons
        document.addEventListener('click', async (e) => {
            if (e.target.id === 'btn-proposal-submit') {
                await this.handleProposalSubmit();
            }
            if (e.target.id === 'btn-proposal-accept') {
                await this.handleProposalAccept();
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
                    <button class="proposal-btn" id="toggleProposalBtn" onclick="chatInterface.toggleProposalPanel()">
                        <span class="material-symbols-outlined">description</span>
                        제안서 보기
                    </button>
                </div>
            </div>
            
            <!-- Proposal Panel (Right Drawer) -->
            <div class="proposal-panel" id="proposalPanel" aria-hidden="true">
              <div class="proposal-sheet">
                <header class="proposal-sheet-header">
                  <h4 class="proposal-title">제안서</h4>
                  <div class="proposal-header-actions">
                    <button class="icon-btn" id="proposalEditBtn" aria-label="편집">
                      <span class="material-symbols-outlined">edit</span>
                    </button>
                    <button class="icon-btn" id="proposalCloseBtn" aria-label="닫기" onclick="chatInterface.toggleProposalPanel()">
                      <span class="material-symbols-outlined">close</span>
                    </button>
                  </div>
                </header>

                <div class="proposal-panel-content">
                  <div class="proposal-photos" id="proposalPhotos">
                    <!-- Images will be loaded dynamically -->
                  </div>

                  <div class="proposal-form">
                    <div class="field-row">
                      <label class="field-label">업종</label>
                      <input class="field-input" id="p-industry" placeholder="음식점" value="음식점">
                    </div>

                    <div class="field-row">
                      <label class="field-label">상품</label>
                      <textarea class="field-input" id="p-products" rows="2" placeholder="예: a가게 치킨 + b가게 맥주">a가게 치킨
b가게 맥주</textarea>
                    </div>

                    <div class="field-row">
                      <label class="field-label">수익 배분 구조</label>
                      <input class="field-input" id="p-share" placeholder="예: 5:5"> 
                    </div>

                    <div class="field-row">
                      <label class="field-label">콜라보 진행 기간</label>
                      <input class="field-input" id="p-duration" placeholder="예: 1개월" value="1개월">
                    </div>

                    <div class="field-row">
                      <label class="field-label">콜라보 장소</label>
                      <input class="field-input" id="p-place" placeholder="예: 각자 픽업" value="각각 픽업">
                    </div>
                  </div>
                </div>

                <footer class="proposal-sheet-footer">
                  <button class="btn btn-outline" id="btn-proposal-submit">제안하기</button>
                  <button class="btn btn-solid" id="btn-proposal-accept">수락하기</button>
                </footer>
              </div>
            </div>
            
            <div class="chat-body" id="chatBody">
                ${this.renderProposalSummaryIfNeeded(messages, chatData)}
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
            
            // Check if this is a system message
            const messageType = message.messageType || 'TEXT';
            if (this.isSystemMessageType(messageType)) {
                messagesHTML += this.renderSystemMessage(message);
            } else {
                const isOwn = message.senderId === this.currentUser?.uuid;
                const bubbleClass = isOwn ? 'mine' : 'their';
                
                // Debug logging for message alignment
                console.log('Message bubble alignment:', {
                    messageId: message.messageId,
                    senderId: message.senderId,
                    currentUserId: this.currentUser?.uuid,
                    isOwn: isOwn,
                    bubbleClass: bubbleClass,
                    createdAt: message.createdAt
                });
                
                messagesHTML += `
                    <div class="message-group ${bubbleClass}">
                        <div class="bubble ${bubbleClass}">
                            ${this.escapeHtml(message.message)}
                            <div class="message-time">${this.formatTime(message.createdAt)}</div>
                        </div>
                    </div>
                `;
            }
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
                    try {
                        const messageData = JSON.parse(message.body);
                        console.log('Received WebSocket message:', messageData);
                        this.appendMessage(messageData);
                    } catch (error) {
                        console.error('Error parsing WebSocket message:', error, message.body);
                    }
                });
                
                // Subscribe to proposal updates for this room
                this.stompClient.subscribe(`/topic/proposal/${roomId}`, (message) => {
                    try {
                        const proposalUpdateData = JSON.parse(message.body);
                        console.log('Received proposal update:', proposalUpdateData);
                        this.handleProposalUpdate(proposalUpdateData);
                    } catch (error) {
                        console.error('Error parsing proposal update:', error, message.body);
                    }
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
        const lastMessage = chatBody.querySelector('.message-group:last-child, .system-message:last-child');
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
        
        // Check if this is a system message (proposal status change)
        const messageType = messageData.messageType || 'TEXT';
        if (this.isSystemMessageType(messageType)) {
            messageHTML += this.renderSystemMessage(messageData);
        } else {
            const isOwn = messageData.sender?.uuid === this.currentUser?.uuid;
            const bubbleClass = isOwn ? 'mine' : 'their';
            
            // Debug logging for real-time message alignment
            console.log('Real-time message bubble alignment:', {
                senderUuid: messageData.sender?.uuid,
                currentUserId: this.currentUser?.uuid,
                isOwn: isOwn,
                bubbleClass: bubbleClass,
                messageData: messageData
            });
            
            messageHTML += `
                <div class="message-group ${bubbleClass}">
                    <div class="bubble ${bubbleClass}">
                        ${this.escapeHtml(messageData.message)}
                        <div class="message-time">${this.formatTime(messageData.createdAt)}</div>
                    </div>
                </div>
            `;
        }
        
        chatBody.insertAdjacentHTML('beforeend', messageHTML);
        this.scrollToBottom();
        
        // Update unread badge for other rooms (if this message is from a different room)
        if (messageType === 'TEXT' && !(messageData.sender?.uuid === this.currentUser?.uuid)) {
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
                const responseData = await response.json();
                console.log('User session API response:', responseData);
                
                // Extract user data from ApiResponseData wrapper
                this.currentUser = responseData.data || responseData;
                console.log('Current user loaded:', this.currentUser);
            } else {
                console.error('Failed to load user info, status:', response.status);
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
        if (!timestamp) {
            console.warn('formatDate called with null/undefined timestamp');
            return '날짜 오류';
        }
        
        try {
            const date = new Date(timestamp);
            
            // Check if date is valid
            if (isNaN(date.getTime())) {
                console.warn('Invalid timestamp format for date:', timestamp);
                return '날짜 오류';
            }
            
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
        } catch (error) {
            console.error('Error formatting date:', error, timestamp);
            return '날짜 오류';
        }
    }
    
    formatTime(timestamp) {
        if (!timestamp) {
            console.warn('formatTime called with null/undefined timestamp');
            return '';
        }
        
        try {
            // Handle various timestamp formats from server
            const date = new Date(timestamp);
            
            // Check if date is valid
            if (isNaN(date.getTime())) {
                console.warn('Invalid timestamp format:', timestamp);
                return '시간 오류';
            }
            
            const hours = date.getHours();
            const minutes = date.getMinutes().toString().padStart(2, '0');
            const period = hours < 12 ? '오전' : '오후';
            const displayHours = hours === 0 ? 12 : hours > 12 ? hours - 12 : hours;
            
            const formattedTime = `${period} ${displayHours}:${minutes}`;
            console.log('Formatted time:', { timestamp, date, formattedTime });
            return formattedTime;
        } catch (error) {
            console.error('Error formatting time:', error, timestamp);
            return '시간 오류';
        }
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
    
    toggleProposalPanel() {
        const panel = document.getElementById('proposalPanel');
        const toggleBtn = document.getElementById('toggleProposalBtn');
        if (!panel || !toggleBtn) return;

        const willOpen = !panel.classList.contains('open');

        panel.classList.toggle('open', willOpen);
        panel.setAttribute('aria-hidden', String(!willOpen));
        toggleBtn.setAttribute('aria-expanded', String(willOpen));
        toggleBtn.innerHTML = willOpen
            ? `<span class="material-symbols-outlined">close</span> 제안서 닫기`
            : `<span class="material-symbols-outlined">description</span> 제안서 보기`;

        // Load proposal data and focusing & scrolling
        if (willOpen) {
            this.loadProposalData();
            requestAnimationFrame(() => document.getElementById('p-industry')?.focus());
        }
        this.scrollToBottom();
    }
    
    async handleProposalSubmit() {
        const payload = {
            roomId: this.selectedRoomId,
            industry: document.getElementById('p-industry')?.value,
            products: document.getElementById('p-products')?.value,
            share: document.getElementById('p-share')?.value,
            duration: document.getElementById('p-duration')?.value,
            place: document.getElementById('p-place')?.value,
        };
        
        try {
            const response = await fetch('/api/proposals', { 
                method: 'POST', 
                headers: { 'Content-Type': 'application/json' }, 
                body: JSON.stringify(payload) 
            });
            
            if (response.ok) {
                this.showError('제안서가 전송되었습니다.');
                this.toggleProposalPanel();
            } else {
                this.showError('제안서 전송에 실패했습니다.');
            }
        } catch (error) {
            console.error('Error submitting proposal:', error);
            this.showError('제안서 전송 중 오류가 발생했습니다.');
        }
    }
    
    async handleProposalAccept() {
        const payload = { roomId: this.selectedRoomId };
        
        try {
            const response = await fetch('/api/proposals/accept', { 
                method: 'POST', 
                headers: { 'Content-Type': 'application/json' }, 
                body: JSON.stringify(payload) 
            });
            
            if (response.ok) {
                this.showError('제안서를 수락했습니다.');
                this.toggleProposalPanel();
            } else {
                this.showError('제안서 수락에 실패했습니다.');
            }
        } catch (error) {
            console.error('Error accepting proposal:', error);
            this.showError('제안서 수락 중 오류가 발생했습니다.');
        }
    }
    
    async loadProposalData() {
        if (!this.selectedRoomId) return;
        
        try {
            // First, try to get proposal ID from chat room data
            const proposalId = await this.getProposalIdFromRoom(this.selectedRoomId);
            if (!proposalId) {
                console.warn('No proposal found for this chat room');
                this.showDefaultProposalData();
                return;
            }
            
            // Load proposal images
            await this.loadProposalImages(proposalId);
            
            // Load proposal details (if needed)
            await this.loadProposalDetails(proposalId);
            
        } catch (error) {
            console.error('Error loading proposal data:', error);
            this.showDefaultProposalData();
        }
    }
    
    async getProposalIdFromRoom(roomId) {
        // This would need to be implemented based on how you store the proposal reference
        // For now, we'll make a simple API call to get room details
        try {
            const response = await fetch(`/chat/room/${roomId}/proposal`);
            if (response.ok) {
                const data = await response.json();
                return data.proposalId;
            }
        } catch (error) {
            console.error('Error getting proposal ID from room:', error);
        }
        return null;
    }
    
    async loadProposalImages(proposalId) {
        try {
            const response = await fetch(`/proposals/${proposalId}/images`);
            if (response.ok) {
                const responseData = await response.json();
                const images = responseData.data || [];
                this.renderProposalImages(images);
            } else {
                this.showDefaultImages();
            }
        } catch (error) {
            console.error('Error loading proposal images:', error);
            this.showDefaultImages();
        }
    }
    
    async loadProposalDetails(proposalId) {
        try {
            const response = await fetch(`/proposals/${proposalId}`);
            if (response.ok) {
                const responseData = await response.json();
                const proposal = responseData.data || responseData;
                this.populateProposalForm(proposal);
            }
        } catch (error) {
            console.error('Error loading proposal details:', error);
        }
    }
    
    renderProposalImages(images) {
        const photosContainer = document.getElementById('proposalPhotos');
        if (!photosContainer) return;
        
        if (images && images.length > 0) {
            photosContainer.innerHTML = '';
            
            // Show up to 2 images (as per design)
            const imagesToShow = images.slice(0, 2);
            imagesToShow.forEach((image, index) => {
                const img = document.createElement('img');
                img.src = image.imagePath;
                img.alt = `상품 이미지 ${index + 1}`;
                img.className = 'proposal-photo';
                img.onerror = () => {
                    img.src = '/image/placeholder-food.jpg'; // Fallback image
                };
                photosContainer.appendChild(img);
            });
            
            // If we have less than 2 images, add placeholders
            for (let i = images.length; i < 2; i++) {
                const img = document.createElement('img');
                img.src = '/image/placeholder-food.jpg';
                img.alt = `상품 이미지 ${i + 1}`;
                img.className = 'proposal-photo';
                photosContainer.appendChild(img);
            }
        } else {
            this.showDefaultImages();
        }
    }
    
    showDefaultImages() {
        const photosContainer = document.getElementById('proposalPhotos');
        if (!photosContainer) return;
        
        photosContainer.innerHTML = `
            <img src="/image/placeholder-food.jpg" alt="상품 이미지 1" class="proposal-photo">
            <img src="/image/placeholder-food.jpg" alt="상품 이미지 2" class="proposal-photo">
        `;
    }
    
    populateProposalForm(proposal) {
        // Populate form fields with proposal data
        if (proposal.industry) {
            const industryField = document.getElementById('p-industry');
            if (industryField) industryField.value = proposal.industry;
        }
        
        if (proposal.products) {
            const productsField = document.getElementById('p-products');
            if (productsField) productsField.value = proposal.products;
        }
        
        if (proposal.profitShare) {
            const shareField = document.getElementById('p-share');
            if (shareField) shareField.value = proposal.profitShare;
        }
        
        if (proposal.duration) {
            const durationField = document.getElementById('p-duration');
            if (durationField) durationField.value = proposal.duration;
        }
        
        if (proposal.location) {
            const locationField = document.getElementById('p-place');
            if (locationField) locationField.value = proposal.location;
        }
    }
    
    showDefaultProposalData() {
        // Show default placeholder data
        this.showDefaultImages();
        // Form fields will keep their default values from the HTML template
    }
    
    // System message handling
    isSystemMessageType(messageType) {
        return ['PROPOSAL_ACCEPTED', 'PROPOSAL_REJECTED', 'PROPOSAL_MODIFIED', 'PROPOSAL_STATUS_CHANGE', 'PROPOSAL_UPDATE'].includes(messageType);
    }
    
    renderSystemMessage(messageData) {
        const messageType = messageData.messageType;
        let iconClass = 'info';
        let bgClass = 'info';
        
        switch (messageType) {
            case 'PROPOSAL_ACCEPTED':
                iconClass = 'check_circle';
                bgClass = 'success';
                break;
            case 'PROPOSAL_REJECTED':
                iconClass = 'cancel';
                bgClass = 'error';
                break;
            case 'PROPOSAL_MODIFIED':
            case 'PROPOSAL_UPDATE':
                iconClass = 'edit';
                bgClass = 'warning';
                break;
            default:
                iconClass = 'info';
                bgClass = 'info';
        }
        
        return `
            <div class="system-message ${bgClass}" data-message-type="${messageType}">
                <div class="system-message-content">
                    <span class="material-symbols-outlined system-icon">${iconClass}</span>
                    <span class="system-text">${this.escapeHtml(messageData.message)}</span>
                    <button class="system-action-btn" onclick="chatInterface.handleSystemMessageClick('${messageType}', '${messageData.messageId}')">
                        <span class="material-symbols-outlined">arrow_forward</span>
                    </button>
                </div>
                <div class="system-time">${this.formatTime(messageData.createdAt)}</div>
            </div>
        `;
    }
    
    handleSystemMessageClick(messageType, messageId) {
        console.log('System message clicked:', messageType, messageId);
        
        // Open proposal panel for proposal-related system messages
        if (this.isSystemMessageType(messageType)) {
            const proposalPanel = document.getElementById('proposalPanel');
            if (proposalPanel && !proposalPanel.classList.contains('open')) {
                this.toggleProposalPanel();
            }
            
            // Refresh proposal data to show latest changes
            this.loadProposalData();
        }
    }
    
    handleProposalUpdate(updateData) {
        console.log('Handling proposal update:', updateData);
        
        // If proposal panel is open, refresh its data
        const proposalPanel = document.getElementById('proposalPanel');
        if (proposalPanel && proposalPanel.classList.contains('open')) {
            this.loadProposalData();
        }
        
        // Show a subtle notification about the update
        this.showProposalUpdateNotification(updateData);
    }
    
    showProposalUpdateNotification(updateData) {
        // Simple notification - could be enhanced with toast notifications
        const notification = document.createElement('div');
        notification.className = 'proposal-update-notification';
        notification.innerHTML = `
            <span class="material-symbols-outlined">update</span>
            <span>${updateData.updatedBy}님이 제안서를 업데이트했습니다.</span>
        `;
        
        // Add to chat body temporarily
        const chatBody = document.getElementById('chatBody');
        if (chatBody) {
            chatBody.appendChild(notification);
            
            // Remove after 3 seconds
            setTimeout(() => {
                if (notification.parentNode) {
                    notification.parentNode.removeChild(notification);
                }
            }, 3000);
        }
        
        this.scrollToBottom();
    }
    
    // Proposal Summary Card (shows at the top for first conversation)
    renderProposalSummaryIfNeeded(messages, chatData) {
        // Only show if this is the first conversation (no messages or only system messages)
        const hasRegularMessages = messages && messages.some(msg => 
            !msg.messageType || msg.messageType === 'TEXT'
        );
        
        if (hasRegularMessages) {
            return ''; // Don't show proposal summary if there are already regular messages
        }
        
        // Check if we have collaboration proposal data
        if (!chatData.collaborationId) {
            return '';
        }
        
        return `
            <div class="proposal-summary-card" id="proposalSummaryCard">
                <div class="proposal-summary-header">
                    <span class="material-symbols-outlined">description</span>
                    <h4>제안서 요약</h4>
                    <button class="proposal-summary-close" onclick="chatInterface.hideProposalSummary()">
                        <span class="material-symbols-outlined">close</span>
                    </button>
                </div>
                
                <div class="proposal-summary-content">
                    <div class="proposal-summary-photos">
                        <div class="summary-photo-placeholder">
                            <span class="material-symbols-outlined">image</span>
                        </div>
                        <div class="summary-photo-placeholder">
                            <span class="material-symbols-outlined">image</span>
                        </div>
                    </div>
                    
                    <div class="proposal-summary-details">
                        <div class="summary-field">
                            <label>협업 유형</label>
                            <span>음식점 상품 협업</span>
                        </div>
                        <div class="summary-field">
                            <label>상품</label>
                            <span>a가게 치킨 + b가게 맥주</span>
                        </div>
                        <div class="summary-field">
                            <label>수익 배분</label>
                            <span>5:5</span>
                        </div>
                        <div class="summary-field">
                            <label>진행 기간</label>
                            <span>1개월</span>
                        </div>
                    </div>
                    
                    <div class="proposal-summary-actions">
                        <button class="btn btn-outline" onclick="chatInterface.toggleProposalPanel()">
                            상세 보기
                        </button>
                        <button class="btn btn-solid" onclick="chatInterface.quickAcceptProposal()">
                            수락하기
                        </button>
                    </div>
                </div>
            </div>
        `;
    }
    
    hideProposalSummary() {
        const summaryCard = document.getElementById('proposalSummaryCard');
        if (summaryCard) {
            summaryCard.style.display = 'none';
        }
    }
    
    async quickAcceptProposal() {
        try {
            const response = await fetch('/api/proposals/accept', { 
                method: 'POST', 
                headers: { 'Content-Type': 'application/json' }, 
                body: JSON.stringify({ roomId: this.selectedRoomId }) 
            });
            
            if (response.ok) {
                this.showError('제안서를 수락했습니다!');
                this.hideProposalSummary();
                
                // Trigger proposal update broadcast
                await fetch(`/chat/room/${this.selectedRoomId}/proposal/update`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ updateType: 'PROPOSAL_ACCEPTED' })
                });
            } else {
                this.showError('제안서 수락에 실패했습니다.');
            }
        } catch (error) {
            console.error('Error accepting proposal:', error);
            this.showError('제안서 수락 중 오류가 발생했습니다.');
        }
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

// Legacy function - now replaced by ChatInterface.toggleProposalPanel()
// Kept for backward compatibility if needed
function viewProposal(collaborationId) {
    if (window.chatInterface) {
        window.chatInterface.toggleProposalPanel();
    }
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