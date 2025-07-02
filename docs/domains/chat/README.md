# Chat Domain

## Overview
The chat system provides real-time communication capabilities for users engaged in collaborations and business discussions.

## Domain Models
- **ChatRoom**: `ChatRoom.java` - Chat room entity for organizing conversations
- **ChatMessage**: `ChatMessage.java` - Individual messages within chat rooms

## Repositories
Following the consistent three-layer pattern:
- **Domain Interfaces**: `ChatRoomRepository`, `ChatMessageRepository`
- **JPA Interfaces**: `ChatRoomJpaRepository`, `ChatMessageJpaRepository`
- **Implementations**: `ChatRoomRepositoryImpl`, `ChatMessageRepositoryImpl`

## Controllers
- `ChatController.java` - Chat functionality and WebSocket handling

## WebSocket Configuration
- **Config**: `WebSocketConfig.java` - WebSocket setup for real-time messaging
- **Real-time Features**: Live chat, message broadcasting, user presence

## Chat Features

### Chat Rooms
- **Purpose**: Organized conversations for specific collaborations or topics
- **Access Control**: Room-based permissions tied to collaboration access
- **Persistence**: Chat rooms stored and managed in database

### Messaging
- **Real-time**: WebSocket-based instant messaging
- **Message Storage**: All messages persisted for conversation history
- **Message Types**: Text messages with potential for rich content

### Integration with Collaborations
- **Automatic Creation**: Chat rooms created for accepted collaborations
- **Context Awareness**: Chat tied to specific collaboration contexts
- **Participant Management**: Room access based on collaboration participants

## WebSocket Endpoints
- WebSocket connection endpoints for real-time communication
- Message broadcasting to room participants
- Connection management and user presence tracking

## Templates and UI
- `chat/room.html` - Individual chat room interface
- `chat/rooms.html` - Chat room listing and navigation

## Technical Implementation
- **WebSocket**: Spring WebSocket for real-time communication
- **Message Broker**: Simple broker for message routing
- **Session Management**: WebSocket session handling with authentication
- **Persistence**: JPA for chat history storage

## Chat Workflow
1. **Room Creation**: Chat room created when collaboration is accepted
2. **User Connection**: Participants connect via WebSocket
3. **Real-time Messaging**: Messages sent and received instantly
4. **Message Storage**: All messages persisted for history
5. **Session Management**: Handle user connections and disconnections

## Integration Points
- **Collaboration Domain**: Chat rooms tied to active collaborations
- **User Domain**: User authentication and session management
- **Notification Domain**: Chat notifications and alerts

## Security Considerations
- **Authentication**: WebSocket connections require valid user session
- **Authorization**: Room access controlled by collaboration participation
- **Message Validation**: Input validation for chat messages
- **Rate Limiting**: Prevent spam and abuse in chat rooms

## Recent Development
- Implemented chat room system for accepted collaborations
- Added WebSocket configuration for real-time messaging
- Integrated chat with collaboration workflow