// Chat Interface JavaScript
class ChatInterface {
    constructor() {
        this.selectedRoomId = null;
        this.stompClient = null;
        this.currentUser = null;
        this.isComposing = false;
        this.lastSendTime = 0;
        
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
        
        // Composition event handlers for IME input (Korean, etc.)
        document.addEventListener('compositionstart', (e) => {
            if (e.target.classList.contains('message-textarea')) {
                this.isComposing = true;
            }
        });
        
        document.addEventListener('compositionend', (e) => {
            if (e.target.classList.contains('message-textarea')) {
                this.isComposing = false;
            }
        });
        
        // Enter to send message (with IME composition handling)
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Enter' && !e.shiftKey && e.target.classList.contains('message-textarea')) {
                // Don't send message if IME composition is in progress
                if (!this.isComposing) {
                    e.preventDefault();
                    this.sendMessage();
                }
            }
        });
        
        // Proposal action buttons
        document.addEventListener('click', async (e) => {
            if (e.target.id === 'btn-proposal-submit' || e.target.closest('#btn-proposal-submit')) {
                await this.handleProposalSubmit();
            }
            if (e.target.id === 'btn-proposal-accept' || e.target.closest('#btn-proposal-accept')) {
                await this.handleProposalAccept();
            }
            if (e.target.id === 'proposalEditBtn' || e.target.closest('#proposalEditBtn')) {
                this.toggleProposalEditMode();
            }
            if (e.target.id === 'btn-proposal-save' || e.target.closest('#btn-proposal-save')) {
                await this.handleProposalSave();
            }
            if (e.target.id === 'btn-proposal-cancel' || e.target.closest('#btn-proposal-cancel')) {
                this.handleProposalCancel();
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
            // Use enhanced error handling if available
            if (error.displayUserError) {
                error.displayUserError();
            } else {
                this.showError('채팅방을 불러오는 중 오류가 발생했습니다.');
            }
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
        
        // CRITICAL: Populate proposal panel with actual data
        this.populateProposalPanel(chatData.proposalDetails);
        
        this.setupMessageInput();
        this.scrollToBottom();
    }
    
    /**
     * Populate the proposal panel with actual proposal data from the API.
     * This method updates the proposal form fields with the originating proposal information.
     */
    populateProposalPanel(proposalDetails) {
        try {
            if (!proposalDetails) {
                console.warn('No proposal details available - showing empty proposal panel');
                this.showEmptyProposalPanel();
                return;
            }
            
            console.log('Populating proposal panel with:', proposalDetails);
            
            // Update proposal header
            const proposalTitle = document.querySelector('.proposal-title');
            if (proposalTitle && proposalDetails.title) {
                proposalTitle.textContent = proposalDetails.title;
            }
            
            // Update basic proposal information
            this.updateProposalField('p-industry', this.getDisplayIndustry(proposalDetails));
            this.updateProposalField('p-products', this.getDisplayProducts(proposalDetails));
            this.updateProposalField('p-share', this.getDisplayProfitShare(proposalDetails));
            this.updateProposalField('p-duration', this.getDisplayDuration(proposalDetails));
            this.updateProposalField('p-place', this.getDisplayLocation(proposalDetails));
            
            // Update proposal images using product thumbnails
            this.updateProposalImages(proposalDetails);
            
            // Update proposal status and actions
            this.updateProposalActions(proposalDetails);
            
            // Show status information
            this.showProposalStatusInfo(proposalDetails);
            
        } catch (error) {
            console.error('Error populating proposal panel:', error);
            this.showEmptyProposalPanel();
        }
    }
    
    /**
     * Update a specific proposal form field with safe value handling
     */
    updateProposalField(fieldId, value) {
        const field = document.getElementById(fieldId);
        if (field && value !== null && value !== undefined) {
            field.value = value;
            field.disabled = true; // Make fields read-only (snapshot approach)
        }
    }
    
    /**
     * Generate display text for industry based on proposal data
     */
    getDisplayIndustry(proposal) {
        // Use store information to infer industry
        if (proposal.proposer && proposal.proposer.storeName) {
            return '업종: ' + proposal.proposer.storeName;
        }
        if (proposal.target && proposal.target.storeName) {
            return '대상: ' + proposal.target.storeName;
        }
        return '업종 정보 없음';
    }
    
    /**
     * Generate display text for products involved in the proposal
     */
    getDisplayProducts(proposal) {
        const products = [];
        
        if (proposal.proposerProduct && proposal.proposerProduct.name) {
            const proposerName = proposal.proposer && proposal.proposer.storeName ? 
                proposal.proposer.storeName : '제안자';
            products.push(`${proposerName}: ${proposal.proposerProduct.name}`);
        }
        
        if (proposal.targetProduct && proposal.targetProduct.name) {
            const targetName = proposal.target && proposal.target.storeName ? 
                proposal.target.storeName : '대상';
            products.push(`${targetName}: ${proposal.targetProduct.name}`);
        }
        
        return products.length > 0 ? products.join('\n') : '상품 정보 없음';
    }
    
    /**
     * Generate display text for profit sharing
     */
    getDisplayProfitShare(proposal) {
        if (proposal.profitShare && proposal.profitShare.trim()) {
            return proposal.profitShare.trim();
        }
        return '수익 배분: 협의 필요';
    }
    
    /**
     * Generate display text for collaboration duration
     */
    getDisplayDuration(proposal) {
        if (proposal.proposedStart && proposal.proposedEnd) {
            const start = new Date(proposal.proposedStart).toLocaleDateString('ko-KR');
            const end = new Date(proposal.proposedEnd).toLocaleDateString('ko-KR');
            return `${start} ~ ${end}`;
        }
        return '기간: 협의 필요';
    }
    
    /**
     * Generate display text for collaboration location
     */
    getDisplayLocation(proposal) {
        if (proposal.location && proposal.location.trim()) {
            return proposal.location.trim();
        }
        return '장소: 협의 필요';
    }
    
    /**
     * Update proposal action buttons based on proposal status
     */
    updateProposalActions(proposal) {
        const submitBtn = document.getElementById('btn-proposal-submit');
        const acceptBtn = document.getElementById('btn-proposal-accept');
        
        if (submitBtn && acceptBtn) {
            switch (proposal.status) {
                case 'PENDING':
                    submitBtn.style.display = 'none';
                    acceptBtn.textContent = '수락 대기중';
                    acceptBtn.disabled = true;
                    break;
                case 'ACCEPTED':
                    submitBtn.style.display = 'none';
                    acceptBtn.textContent = '수락됨';
                    acceptBtn.disabled = true;
                    acceptBtn.classList.add('btn-success');
                    break;
                case 'REJECTED':
                    submitBtn.style.display = 'none';
                    acceptBtn.textContent = '거절됨';
                    acceptBtn.disabled = true;
                    acceptBtn.classList.add('btn-danger');
                    break;
                default:
                    submitBtn.style.display = 'none';
                    acceptBtn.style.display = 'none';
            }
        }
    }
    
    /**
     * Show proposal status information at the top of the panel
     */
    showProposalStatusInfo(proposal) {
        const proposalContent = document.querySelector('.proposal-panel-content');
        if (proposalContent) {
            const statusInfo = document.createElement('div');
            statusInfo.className = 'proposal-status-info';
            statusInfo.innerHTML = `
                <div class="status-badge status-${proposal.status.toLowerCase()}">
                    ${this.getStatusDisplayText(proposal.status)}
                </div>
                <div class="proposal-meta">
                    <small>제안일: ${new Date(proposal.createdAt).toLocaleDateString('ko-KR')}</small>
                    ${proposal.updatedAt && proposal.updatedAt !== proposal.createdAt ? 
                        `<small>수정일: ${new Date(proposal.updatedAt).toLocaleDateString('ko-KR')}</small>` : ''}
                </div>
            `;
            proposalContent.insertBefore(statusInfo, proposalContent.firstChild);
        }
    }
    
    /**
     * Get display text for proposal status
     */
    getStatusDisplayText(status) {
        const statusMap = {
            'PENDING': '대기중',
            'ACCEPTED': '수락됨',
            'REJECTED': '거절됨',
            'CANCELLED': '취소됨',
            'ENDED': '완료됨'
        };
        return statusMap[status] || status;
    }
    
    /**
     * Show empty proposal panel when no proposal data is available
     */
    showEmptyProposalPanel() {
        const proposalContent = document.querySelector('.proposal-panel-content');
        if (proposalContent) {
            proposalContent.innerHTML = `
                <div class="empty-proposal">
                    <div class="empty-icon">📋</div>
                    <h4>제안서 정보 없음</h4>
                    <p>이 채팅방에 연결된 제안서가 없습니다.</p>
                </div>
            `;
        }
        
        // Also show default placeholder images
        this.showDefaultImages();
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
        
        // Prevent duplicate sends with debounce logic
        const now = Date.now();
        if (now - this.lastSendTime < 500) {
            console.log('Duplicate message send prevented (debounced)');
            return;
        }
        
        // Skip if currently composing (additional safety check)
        if (this.isComposing) {
            console.log('Message send prevented: IME composition in progress');
            return;
        }
        
        this.lastSendTime = now;
        
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
            if (error.displayUserError) {
                error.displayUserError();
            } else {
                this.showError('메시지 전송에 실패했습니다.');
            }
        }
    }
    
    appendMessage(messageData) {
        const chatBody = document.getElementById('chatBody');
        if (!chatBody) return;
        
        // Check if we need a date separator by comparing with the last message's date
        const messageDate = this.formatDate(messageData.createdAt);
        let needDateSeparator = true;
        
        // Check if the last element is a date separator with the same date
        const lastElement = chatBody.lastElementChild;
        if (lastElement) {
            if (lastElement.classList.contains('date-separator')) {
                // Last element is a date separator, check if it's the same date
                const lastDateText = lastElement.querySelector('span')?.textContent;
                if (lastDateText === messageDate) {
                    needDateSeparator = false;
                }
            } else {
                // Last element is a message, check if there's a recent date separator for today
                const recentDateSeparator = chatBody.querySelector('.date-separator:last-of-type span');
                if (recentDateSeparator && recentDateSeparator.textContent === messageDate) {
                    needDateSeparator = false;
                }
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
        // Use the global showError function from common.js instead of alert
        if (typeof showError === 'function') {
            showError(message);
        } else {
            // Fallback to alert if common.js is not loaded
            alert(message);
        }
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
            // Proposal data is now loaded via populateProposalPanel() from chat room data
            // which includes product thumbnails in the proposalDetails object
            console.log('Proposal data loading is handled by populateProposalPanel()');
            
        } catch (error) {
            console.error('Error in loadProposalData:', error);
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
    
    /**
     * Update proposal images using product thumbnails from proposal details
     */
    updateProposalImages(proposalDetails) {
        const photosContainer = document.getElementById('proposalPhotos');
        if (!photosContainer) return;
        
        // DEBUG: Log proposal details to understand data structure
        console.log('=== PROPOSAL IMAGE DEBUG ===');
        console.log('Full proposalDetails:', proposalDetails);
        console.log('proposerProduct:', proposalDetails.proposerProduct);
        console.log('targetProduct:', proposalDetails.targetProduct);
        
        const images = [];
        
        // Collect product thumbnails from proposal details
        if (proposalDetails.proposerProduct) {
            console.log('proposerProduct exists:', proposalDetails.proposerProduct);
            console.log('proposerProduct.thumbnailPath:', proposalDetails.proposerProduct.thumbnailPath);
            
            if (proposalDetails.proposerProduct.thumbnailPath) {
                images.push({
                    src: proposalDetails.proposerProduct.thumbnailPath,
                    alt: proposalDetails.proposerProduct.name || '제안자 상품',
                    productName: proposalDetails.proposerProduct.name
                });
                console.log('Added proposer product image:', proposalDetails.proposerProduct.thumbnailPath);
            } else {
                console.log('proposerProduct has no thumbnailPath');
            }
        } else {
            console.log('proposerProduct is null/undefined');
        }
        
        if (proposalDetails.targetProduct) {
            console.log('targetProduct exists:', proposalDetails.targetProduct);
            console.log('targetProduct.thumbnailPath:', proposalDetails.targetProduct.thumbnailPath);
            
            if (proposalDetails.targetProduct.thumbnailPath) {
                images.push({
                    src: proposalDetails.targetProduct.thumbnailPath,
                    alt: proposalDetails.targetProduct.name || '대상 상품',
                    productName: proposalDetails.targetProduct.name
                });
                console.log('Added target product image:', proposalDetails.targetProduct.thumbnailPath);
            } else {
                console.log('targetProduct has no thumbnailPath');
            }
        } else {
            console.log('targetProduct is null/undefined');
        }
        
        console.log('Total images collected:', images.length, images);
        
        // Clear container and render images
        photosContainer.innerHTML = '';
        
        // Show up to 2 images (as per design)
        for (let i = 0; i < 2; i++) {
            const img = document.createElement('img');
            img.className = 'proposal-photo';
            
            if (i < images.length) {
                // Use actual product thumbnail
                img.src = images[i].src;
                img.alt = images[i].alt;
                img.title = images[i].productName; // Tooltip with product name
                
                console.log(`Rendering image ${i + 1}: ${images[i].src} (${images[i].productName})`);
                
                // Fallback to placeholder if thumbnail fails to load
                img.onerror = () => {
                    console.log(`Image failed to load: ${images[i].src}, using placeholder`);
                    img.src = '/image/placeholder-food.jpg';
                    img.alt = `상품 이미지 ${i + 1}`;
                };
            } else {
                // Use placeholder for missing images
                img.src = '/image/placeholder-food.jpg';
                img.alt = `상품 이미지 ${i + 1}`;
                console.log(`Using placeholder for image ${i + 1} (no product data)`);
            }
            
            photosContainer.appendChild(img);
        }
        
        // Provide summary of what was rendered
        if (images.length === 0) {
            console.log('No product images available - showing 2 placeholders');
        } else if (images.length === 1) {
            console.log('Only 1 product image available - showing 1 product + 1 placeholder');
        } else {
            console.log('2 product images available - showing both products');
        }
        console.log('=== PROPOSAL IMAGE UPDATE COMPLETE ===');
    }
    
    populateProposalForm(proposal) {
        // Populate form fields with proposal data
        if (proposal.title) {
            const industryField = document.getElementById('p-industry');
            if (industryField) industryField.value = proposal.title;
        }
        
        if (proposal.description) {
            const productsField = document.getElementById('p-products');
            if (productsField) productsField.value = proposal.description;
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
        
        // Refresh proposal data immediately to get latest changes
        this.refreshProposalData();
        
        // Show a subtle notification about the update
        this.showProposalUpdateNotification(updateData);
        
        // If this is a status change, handle it specifically
        if (updateData.updateType === 'PROPOSAL_ACCEPTED' || 
            updateData.updateType === 'PROPOSAL_REJECTED' ||
            updateData.updateType === 'PROPOSAL_STATUS_CHANGE') {
            this.handleProposalStatusChange(updateData);
        }
    }
    
    /**
     * Handle specific proposal status changes with immediate UI updates
     */
    async handleProposalStatusChange(updateData) {
        try {
            // Reload the entire chat room data to get updated proposal details
            if (this.selectedRoomId) {
                const response = await fetch(`/chat/room/${this.selectedRoomId}/data`);
                if (response.ok) {
                    const responseData = await response.json();
                    const chatData = responseData.data || responseData;
                    
                    // Update proposal panel with latest data
                    if (chatData.proposalDetails) {
                        this.populateProposalPanel(chatData.proposalDetails);
                        
                        // Show status change notification
                        this.showStatusChangeNotification(chatData.proposalDetails.status, updateData.updatedBy);
                    }
                }
            }
        } catch (error) {
            console.error('Error handling proposal status change:', error);
        }
    }
    
    /**
     * Show a notification for proposal status changes
     */
    showStatusChangeNotification(newStatus, updatedBy) {
        const statusMessages = {
            'ACCEPTED': `✅ ${updatedBy}님이 제안서를 수락했습니다!`,
            'REJECTED': `❌ ${updatedBy}님이 제안서를 거절했습니다.`,
            'PENDING': `📝 ${updatedBy}님이 제안서를 수정했습니다.`,
            'CANCELLED': `🚫 ${updatedBy}님이 제안서를 취소했습니다.`
        };
        
        const message = statusMessages[newStatus] || `🔄 ${updatedBy}님이 제안서 상태를 변경했습니다.`;
        
        // Create and show notification
        const notification = document.createElement('div');
        notification.className = 'proposal-status-notification';
        notification.setAttribute('data-status', newStatus);
        notification.innerHTML = `
            <div class="notification-content">
                <span class="notification-icon">${newStatus === 'ACCEPTED' ? '✅' : newStatus === 'REJECTED' ? '❌' : '📝'}</span>
                <span class="notification-message">${message}</span>
            </div>
        `;
        
        // Add to chat container
        const chatMain = document.getElementById('chatMain');
        if (chatMain) {
            chatMain.appendChild(notification);
            
            // Auto-remove after 5 seconds
            setTimeout(() => {
                if (notification.parentNode) {
                    notification.parentNode.removeChild(notification);
                }
            }, 5000);
        }
    }
    
    /**
     * Refresh proposal data by reloading from API
     */
    async refreshProposalData() {
        if (!this.selectedRoomId) return;
        
        try {
            const response = await fetch(`/chat/room/${this.selectedRoomId}/data`);
            if (response.ok) {
                const responseData = await response.json();
                const chatData = responseData.data || responseData;
                
                // Update proposal panel if it exists and is visible
                const proposalPanel = document.getElementById('proposalPanel');
                if (proposalPanel && chatData.proposalDetails) {
                    this.populateProposalPanel(chatData.proposalDetails);
                }
            }
        } catch (error) {
            console.error('Error refreshing proposal data:', error);
        }
    }
    
    /**
     * Toggle between edit and view mode for proposal
     */
    toggleProposalEditMode() {
        const proposalPanel = document.getElementById('proposalPanel');
        const editBtn = document.getElementById('proposalEditBtn');
        const formFields = document.querySelectorAll('.field-input');
        const footer = document.querySelector('.proposal-sheet-footer');
        
        if (!proposalPanel || !editBtn) return;
        
        const isEditMode = proposalPanel.classList.contains('edit-mode');
        
        if (isEditMode) {
            // Switch to view mode
            this.setProposalViewMode();
        } else {
            // Switch to edit mode
            this.setProposalEditMode();
        }
    }
    
    /**
     * Set proposal panel to edit mode
     */
    setProposalEditMode() {
        const proposalPanel = document.getElementById('proposalPanel');
        const editBtn = document.getElementById('proposalEditBtn');
        const formFields = document.querySelectorAll('.field-input');
        const footer = document.querySelector('.proposal-sheet-footer');
        
        // Add edit mode class
        proposalPanel.classList.add('edit-mode');
        
        // Change edit button to indicate edit mode
        editBtn.innerHTML = '<span class="material-symbols-outlined">close</span>';
        editBtn.setAttribute('aria-label', '편집 취소');
        editBtn.title = '편집 취소';
        
        // Enable form fields
        formFields.forEach(field => {
            field.disabled = false;
            field.classList.add('editable');
        });
        
        // Store original values for cancel functionality
        this.originalValues = {};
        formFields.forEach(field => {
            this.originalValues[field.id] = field.value;
        });
        
        // Update footer with save/cancel buttons
        if (footer) {
            footer.innerHTML = `
                <button class="btn btn-outline" id="btn-proposal-cancel">취소</button>
                <button class="btn btn-solid" id="btn-proposal-save">저장</button>
            `;
        }
        
        // Focus on first editable field
        const firstField = document.querySelector('.field-input:not([disabled])');
        if (firstField) {
            firstField.focus();
        }
    }
    
    /**
     * Set proposal panel to view mode
     */
    setProposalViewMode() {
        const proposalPanel = document.getElementById('proposalPanel');
        const editBtn = document.getElementById('proposalEditBtn');
        const formFields = document.querySelectorAll('.field-input');
        const footer = document.querySelector('.proposal-sheet-footer');
        
        // Remove edit mode class
        proposalPanel.classList.remove('edit-mode');
        
        // Change edit button back to edit icon
        editBtn.innerHTML = '<span class="material-symbols-outlined">edit</span>';
        editBtn.setAttribute('aria-label', '편집');
        editBtn.title = '편집';
        
        // Disable form fields
        formFields.forEach(field => {
            field.disabled = true;
            field.classList.remove('editable');
        });
        
        // Restore original footer buttons
        if (footer) {
            footer.innerHTML = `
                <button class="btn btn-outline" id="btn-proposal-submit">제안하기</button>
                <button class="btn btn-solid" id="btn-proposal-accept">수락하기</button>
            `;
        }
        
        // Clear stored original values
        this.originalValues = null;
    }
    
    /**
     * Handle proposal save
     */
    async handleProposalSave() {
        try {
            // Get form values
            const formData = this.getProposalFormData();
            
            // Validate form data
            if (!this.validateProposalForm(formData)) {
                return;
            }
            
            // Get proposal ID from current room data
            const proposalId = await this.getCurrentProposalId();
            if (!proposalId) {
                this.showError('제안서 ID를 찾을 수 없습니다.');
                return;
            }
            
            // Send update request
            const response = await fetch(`/chat/proposal/${proposalId}`, {
                method: 'PATCH',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(formData)
            });
            
            if (response.ok) {
                const responseData = await response.json();
                
                // Update proposal panel with new data
                if (responseData.data) {
                    this.populateProposalPanel(responseData.data);
                }
                
                // Switch back to view mode
                this.setProposalViewMode();
                
                this.showSuccess('제안서가 성공적으로 수정되었습니다.');
                
            } else {
                const errorData = await response.json();
                this.showError(errorData.message || '제안서 수정에 실패했습니다.');
            }
            
        } catch (error) {
            console.error('Error saving proposal:', error);
            this.showError('제안서 저장 중 오류가 발생했습니다.');
        }
    }
    
    /**
     * Handle proposal cancel - restore original values
     */
    handleProposalCancel() {
        if (this.originalValues) {
            // Restore original values
            Object.keys(this.originalValues).forEach(fieldId => {
                const field = document.getElementById(fieldId);
                if (field) {
                    field.value = this.originalValues[fieldId];
                }
            });
        }
        
        // Switch back to view mode
        this.setProposalViewMode();
    }
    
    /**
     * Get form data for proposal update
     */
    getProposalFormData() {
        return {
            title: document.getElementById('p-industry')?.value || '',
            description: document.getElementById('p-products')?.value || '',
            duration: document.getElementById('p-duration')?.value || '',
            profitShare: document.getElementById('p-share')?.value || '',
            location: document.getElementById('p-place')?.value || ''
        };
    }
    
    /**
     * Validate proposal form data
     */
    validateProposalForm(formData) {
        if (!formData.title.trim()) {
            this.showError('업종을 입력해주세요.');
            document.getElementById('p-industry')?.focus();
            return false;
        }
        
        if (!formData.description.trim()) {
            this.showError('상품 정보를 입력해주세요.');
            document.getElementById('p-products')?.focus();
            return false;
        }
        
        // Validate duration field if provided
        if (formData.duration && !formData.duration.trim()) {
            this.showError('기간을 입력해주세요.');
            document.getElementById('p-duration')?.focus();
            return false;
        }
        
        // Validate profit share field if provided
        if (formData.profitShare && !formData.profitShare.trim()) {
            this.showError('수익 배분 구조를 입력해주세요.');
            document.getElementById('p-share')?.focus();
            return false;
        }
        
        // Validate location field if provided
        if (formData.location && !formData.location.trim()) {
            this.showError('콜라보 장소를 입력해주세요.');
            document.getElementById('p-place')?.focus();
            return false;
        }
        
        return true;
    }
    
    /**
     * Get current proposal ID from room data
     */
    async getCurrentProposalId() {
        try {
            if (!this.selectedRoomId) return null;
            
            const response = await fetch(`/chat/room/${this.selectedRoomId}/proposal`);
            if (response.ok) {
                const data = await response.json();
                return data.data?.proposalId || null;
            }
        } catch (error) {
            console.error('Error getting proposal ID:', error);
        }
        return null;
    }
    
    /**
     * Show success message
     */
    showSuccess(message) {
        // Use the existing error display method but with success styling
        this.showError(message, 'success');
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
        if (!chatData.collaborationId || !chatData.proposalDetails) {
            return '';
        }
        
        const proposalDetails = chatData.proposalDetails;
        
        // Ensure proposalDetails is valid before proceeding
        if (!proposalDetails || typeof proposalDetails !== 'object') {
            console.warn('Invalid proposalDetails:', proposalDetails);
            return '';
        }
        
        // Generate real proposal data using existing helper methods
        const industryType = this.getDisplayIndustry(proposalDetails);
        const products = this.getDisplayProducts(proposalDetails);
        const profitShare = this.getDisplayProfitShare(proposalDetails);
        const duration = this.getDisplayDuration(proposalDetails);
        
        // Generate product images HTML
        const proposalPhotosHtml = this.generateProposalPhotosHtml(proposalDetails);
        
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
                        ${proposalPhotosHtml}
                    </div>
                    
                    <div class="proposal-summary-details">
                        <div class="summary-field">
                            <label>협업 유형</label>
                            <span>${this.escapeHtml(industryType)}</span>
                        </div>
                        <div class="summary-field">
                            <label>상품</label>
                            <span style="white-space: pre-line;">${this.escapeHtml(products)}</span>
                        </div>
                        <div class="summary-field">
                            <label>수익 배분</label>
                            <span>${this.escapeHtml(profitShare)}</span>
                        </div>
                        <div class="summary-field">
                            <label>진행 기간</label>
                            <span>${this.escapeHtml(duration)}</span>
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
    
    /**
     * Generate HTML for proposal photos using real product thumbnails
     */
    generateProposalPhotosHtml(proposalDetails) {
        const images = [];
        
        // Safety check for proposalDetails
        if (!proposalDetails || typeof proposalDetails !== 'object') {
            console.warn('Invalid proposalDetails in generateProposalPhotosHtml:', proposalDetails);
            return `
                <div class="summary-photo-placeholder">
                    <span class="material-symbols-outlined">image</span>
                </div>
                <div class="summary-photo-placeholder">
                    <span class="material-symbols-outlined">image</span>
                </div>
            `;
        }
        
        // Collect product thumbnails from proposal details
        if (proposalDetails.proposerProduct && proposalDetails.proposerProduct.thumbnailPath) {
            images.push({
                src: proposalDetails.proposerProduct.thumbnailPath,
                alt: proposalDetails.proposerProduct.name || '제안자 상품',
                productName: proposalDetails.proposerProduct.name
            });
        }
        
        if (proposalDetails.targetProduct && proposalDetails.targetProduct.thumbnailPath) {
            images.push({
                src: proposalDetails.targetProduct.thumbnailPath,
                alt: proposalDetails.targetProduct.name || '대상 상품',
                productName: proposalDetails.targetProduct.name
            });
        }
        
        // Generate HTML for images or placeholders
        let photosHtml = '';
        
        for (let i = 0; i < 2; i++) {
            if (i < images.length) {
                // Show actual product image
                const image = images[i];
                photosHtml += `
                    <div class="summary-photo-item">
                        <img src="${this.escapeHtml(image.src)}" 
                             alt="${this.escapeHtml(image.alt)}" 
                             title="${this.escapeHtml(image.productName || image.alt)}"
                             onerror="this.style.display='none'; this.nextElementSibling.style.display='flex';">
                        <div class="summary-photo-placeholder" style="display: none;">
                            <span class="material-symbols-outlined">image</span>
                        </div>
                    </div>
                `;
            } else {
                // Show placeholder when no image available
                photosHtml += `
                    <div class="summary-photo-placeholder">
                        <span class="material-symbols-outlined">image</span>
                    </div>
                `;
            }
        }
        
        return photosHtml;
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