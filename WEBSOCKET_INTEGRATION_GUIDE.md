# WebSocket Integration Guide

## Overview
The backend supports real-time features using WebSocket with STOMP protocol:
- **Order Notifications** - Real-time order status updates for customers, restaurants, and drivers
- **Chat System** - Private 1-on-1 chat between driver and customer for each order

## 🔐 Authentication Required
WebSocket connections now require JWT authentication. The token must be passed as a query parameter when connecting.

## WebSocket Endpoint
- **Development**: `ws://localhost:8080/ws?token=YOUR_JWT_TOKEN`
- **Production**: `wss://eatzy-be.hoanduong.net/ws?token=YOUR_JWT_TOKEN`

## User-Specific Queues (Secure - Recommended)

### Order Notifications
All users subscribe to the same destination. Spring automatically routes messages to the correct user based on their authenticated email:

```
/user/queue/orders
```

**How it works:**
1. Frontend connects with JWT token: `new SockJS('/ws?token=eyJhbGciOi...')`
2. Backend validates token and extracts user email
3. Frontend subscribes to: `/user/queue/orders`
4. Backend sends to user via: `convertAndSendToUser(email, "/queue/orders", message)`
5. Only that specific user receives the message

### Driver Location Updates
```
/user/queue/driver-location
```

### Chat System

#### Chat Messages
```
/user/queue/chat/order/{orderId}
```

#### Typing Indicators
```
/user/queue/chat/order/{orderId}/typing
```

## Legacy Topics (Deprecated - Not Secure)

> ⚠️ **Warning**: These topic-based subscriptions are deprecated and insecure. Anyone who knows the ID can subscribe. Use user-specific queues instead.

```
/topic/restaurant/{restaurantId}/orders  (DEPRECATED)
/topic/driver/{driverId}/orders          (DEPRECATED)
/topic/customer/{customerId}/orders      (DEPRECATED)
```

## Message Formats

### Order Notification Message
```json
{
  "type": "NEW_ORDER" | "ORDER_ASSIGNED" | "ORDER_UPDATE" | "ORDER_STATUS_CHANGED",
  "orderId": 123,
  "message": "Human-readable message",
  "data": {
    // Full order details (ResOrderDTO)
  }
}
```

### Chat Message
```json
{
  "orderId": 123,
  "senderId": 1,
  "senderName": "John Doe",
  "senderType": "DRIVER" | "CUSTOMER",
  "message": "I'm arriving in 5 minutes",
  "timestamp": "2025-12-06T10:30:00Z",
  "messageType": "TEXT" | "IMAGE" | "LOCATION"
}
```

## Frontend Integration

### 1. Install Dependencies
```bash
npm install sockjs-client @stomp/stompjs
```

### 2. React Example - Secure Order Notifications (Recommended)

```javascript
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
import { useEffect, useState } from 'react';

function OrderNotifications() {
  const [notifications, setNotifications] = useState([]);
  const [connected, setConnected] = useState(false);
  const [client, setClient] = useState(null);

  // Get JWT token from your auth context/storage
  const accessToken = localStorage.getItem('access_token');

  useEffect(() => {
    if (!accessToken) {
      console.error('No access token available');
      return;
    }

    // Create WebSocket connection WITH JWT token
    const socket = new SockJS(`http://localhost:8080/ws?token=${accessToken}`);
    
    const stompClient = new Client({
      webSocketFactory: () => socket,
      debug: (str) => console.log('STOMP:', str),
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      
      onConnect: () => {
        console.log('✅ Connected to WebSocket');
        setConnected(true);
        
        // Subscribe to user-specific queue
        // NO ID NEEDED! Spring routes to correct user based on JWT
        stompClient.subscribe('/user/queue/orders', (message) => {
          const notification = JSON.parse(message.body);
          console.log('📩 Received notification:', notification);
          handleNotification(notification);
        });

        // Subscribe to driver location updates (for customers)
        stompClient.subscribe('/user/queue/driver-location', (message) => {
          const location = JSON.parse(message.body);
          console.log('📍 Driver location:', location);
          updateDriverLocation(location);
        });
      },
      
      onDisconnect: () => {
        console.log('❌ Disconnected from WebSocket');
        setConnected(false);
      },
      
      onStompError: (frame) => {
        console.error('WebSocket error:', frame);
        // Token might be invalid - redirect to login
        if (frame.headers?.message?.includes('rejected')) {
          window.location.href = '/login';
        }
      }
    });

    stompClient.activate();
    setClient(stompClient);

    // Cleanup on unmount
    return () => {
      if (stompClient) {
        stompClient.deactivate();
      }
    };
  }, [accessToken]);

  const handleNotification = (notification) => {
    setNotifications(prev => [...prev, notification]);
    
    // Show notification toast based on type
    switch(notification.type) {
      case 'NEW_ORDER':
        showToast('🆕 New order received!', notification.message);
        playNotificationSound();
        refreshOrders();
        break;
        
      case 'ORDER_ASSIGNED':
        showModal('📦 New Order Assignment', notification.data);
        playNotificationSound();
        break;
        
      case 'ORDER_UPDATE':
      case 'ORDER_STATUS_CHANGED':
        showToast('📋 Order Update', notification.message);
        updateOrderStatus(notification.orderId, notification.data);
        break;
    }
  };

  return (
    <div>
      <span>Status: {connected ? '🟢 Connected' : '🔴 Disconnected'}</span>
      {/* Your UI components */}
    </div>
  );
}
```

### 3. React Example - Chat System

```javascript
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
import { useEffect, useState } from 'react';

function OrderChat({ orderId, userId, userType }) {
  const [messages, setMessages] = useState([]);
  const [inputMessage, setInputMessage] = useState('');
  const [isTyping, setIsTyping] = useState(false);
  const [client, setClient] = useState(null);

  useEffect(() => {
    const socket = new SockJS('http://localhost:8080/ws');
    
    const stompClient = new Client({
      webSocketFactory: () => socket,
      debug: (str) => console.log('STOMP:', str),
      reconnectDelay: 5000,
      
      onConnect: () => {
        console.log('Connected to chat');
        
        // Subscribe to chat messages
        stompClient.subscribe(`/user/queue/chat/order/${orderId}`, (message) => {
          const chatMsg = JSON.parse(message.body);
          setMessages(prev => [...prev, chatMsg]);
          playMessageSound();
        });
        
        // Subscribe to typing indicator
        stompClient.subscribe(`/user/queue/chat/order/${orderId}/typing`, (message) => {
          const typingMsg = JSON.parse(message.body);
          if (typingMsg.senderId !== userId) {
            setIsTyping(true);
            setTimeout(() => setIsTyping(false), 3000);
          }
        });
      }
    });

    stompClient.activate();
    setClient(stompClient);

    return () => {
      if (stompClient) {
        stompClient.deactivate();
      }
    };
  }, [orderId, userId]);

  const sendMessage = () => {
    if (!inputMessage.trim() || !client) return;
    
    const chatMessage = {
      orderId: orderId,
      senderId: userId,
      senderName: "Current User",
      senderType: userType.toUpperCase(), // "DRIVER" or "CUSTOMER"
      message: inputMessage,
      messageType: "TEXT"
    };
    
    client.publish({
      destination: `/app/chat/${orderId}`,
      body: JSON.stringify(chatMessage)
    });
    
    setInputMessage('');
  };

  const handleTyping = () => {
    if (!client) return;
    
    const typingMsg = {
      senderId: userId,
      senderName: "Current User",
      senderType: userType.toUpperCase(),
      messageType: "TYPING"
    };
    
    client.publish({
      destination: `/app/typing/${orderId}`,
      body: JSON.stringify(typingMsg)
    });
  };

  return (
    <div className="chat-container">
      <div className="messages">
        {messages.map((msg, index) => (
          <div key={index} className={`message ${msg.senderId === userId ? 'sent' : 'received'}`}>
            <strong>{msg.senderName}:</strong> {msg.message}
            <span className="timestamp">{new Date(msg.timestamp).toLocaleTimeString()}</span>
          </div>
        ))}
        {isTyping && <div className="typing-indicator">Typing...</div>}
      </div>
      
      <div className="input-area">
        <input
          type="text"
          value={inputMessage}
          onChange={(e) => {
            setInputMessage(e.target.value);
            handleTyping();
          }}
          onKeyPress={(e) => e.key === 'Enter' && sendMessage()}
          placeholder="Type a message..."
        />
        <button onClick={sendMessage}>Send</button>
      </div>
    </div>
  );
}
```

### 4. Vanilla JavaScript Example - Notifications

```javascript
// Import libraries
const SockJS = require('sockjs-client');
const Stomp = require('@stomp/stompjs');

// Connect to WebSocket
const socket = new SockJS('http://localhost:8080/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
  console.log('Connected: ' + frame);
  
  // Subscribe to restaurant orders
  const restaurantId = 1;
  stompClient.subscribe(`/topic/restaurant/${restaurantId}/orders`, function(message) {
    const notification = JSON.parse(message.body);
    console.log('New notification:', notification);
    
    // Handle the notification
    if (notification.type === 'NEW_ORDER') {
      alert('New order #' + notification.orderId);
      // Update your order list UI
      addOrderToList(notification.data);
    }
  });
});

// Disconnect when needed
function disconnect() {
  if (stompClient !== null) {
    stompClient.disconnect();
  }
  console.log('Disconnected');
}
```

### 5. Vanilla JavaScript Example - Chat

```javascript
const SockJS = require('sockjs-client');
const Stomp = require('@stomp/stompjs');

const socket = new SockJS('http://localhost:8080/ws');
const stompClient = Stomp.over(socket);

const orderId = 123;
const userId = 1;
const userType = 'DRIVER'; // or 'CUSTOMER'

stompClient.connect({}, function(frame) {
  console.log('Connected to chat');
  
  // Subscribe to receive chat messages
  stompClient.subscribe(`/user/queue/chat/order/${orderId}`, function(message) {
    const chatMsg = JSON.parse(message.body);
    console.log('New message:', chatMsg);
    displayMessage(chatMsg);
  });
  
  // Subscribe to typing indicator
  stompClient.subscribe(`/user/queue/chat/order/${orderId}/typing`, function(message) {
    const typingMsg = JSON.parse(message.body);
    if (typingMsg.senderId !== userId) {
      showTypingIndicator();
    }
  });
});

// Send chat message
function sendChatMessage(messageText) {
  const chatMessage = {
    orderId: orderId,
    senderId: userId,
    senderName: "John Doe",
    senderType: userType,
    message: messageText,
    messageType: "TEXT"
  };
  
  stompClient.send(`/app/chat/${orderId}`, {}, JSON.stringify(chatMessage));
}

// Send typing indicator
function sendTypingIndicator() {
  const typingMsg = {
    senderId: userId,
    senderName: "John Doe",
    senderType: userType,
    messageType: "TYPING"
  };
  
  stompClient.send(`/app/typing/${orderId}`, {}, JSON.stringify(typingMsg));
}

// Display message in UI
function displayMessage(chatMsg) {
  const messageDiv = document.createElement('div');
  messageDiv.className = chatMsg.senderId === userId ? 'sent' : 'received';
  messageDiv.innerHTML = `<strong>${chatMsg.senderName}:</strong> ${chatMsg.message}`;
  document.getElementById('chat-messages').appendChild(messageDiv);
}
```

## Workflows

### Order Notification Flow

#### Customer Creates Order
1. Customer submits order
2. ✅ Restaurant receives **NEW_ORDER** notification
3. Order appears in "Chờ xác nhận" (Pending) column

#### Restaurant Accepts Order
1. Restaurant clicks accept
2. ✅ Customer receives **ORDER_UPDATE** notification: "Your order has been accepted"
3. System automatically calls `assignDriver()`
4. ✅ Driver receives **ORDER_ASSIGNED** notification with popup to accept/reject

#### Driver Accepts Order
1. Driver clicks accept
2. ✅ Customer receives **ORDER_UPDATE**: "Driver has accepted your order"
3. ✅ All parties receive **ORDER_STATUS_CHANGED** broadcast

#### Order Status Updates
- **Restaurant marks as READY**: Customer receives notification
- **Driver picks up order**: Customer receives "Your order has been picked up and is on the way"
- **Driver arrives**: Customer receives "Your order has arrived!"
- **Driver delivers**: Customer receives "Your order has been delivered successfully!"

### Chat Flow

#### Starting a Chat
1. After driver accepts order, both Driver and Customer can start chatting
2. Each subscribes to `/user/queue/chat/order/{orderId}`
3. Messages are private between Driver and Customer only

#### Sending Messages
1. Driver types message → Sends to `/app/chat/{orderId}`
2. Server receives and forwards to both Driver and Customer queues
3. Customer receives message in real-time
4. Vice versa for Customer → Driver

#### Typing Indicators
1. When user types, send typing indicator to `/app/typing/{orderId}`
2. Other party receives notification in `/user/queue/chat/order/{orderId}/typing`
3. Show "Typing..." indicator for 3 seconds

#### Use Cases
- **Customer**: "I'm at the lobby, please come upstairs"
- **Driver**: "I'm 2 minutes away"
- **Customer**: "Could you wait a moment?"
- **Driver**: "Your order has been delivered at the door"

## Security Considerations

### Add WebSocket Security (Optional)
If you want to secure WebSocket connections:

```java
@Configuration
public class WebSocketSecurityConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer {
    
    @Override
    protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
        messages
            .simpDestMatchers("/app/**").authenticated()
            .simpSubscribeDestMatchers("/topic/**", "/queue/**").authenticated()
            .anyMessage().authenticated();
    }
    
    @Override
    protected boolean sameOriginDisabled() {
        return true; // Disable CSRF for WebSocket
    }
}
```

### Send Authentication Token
```javascript
const stompClient = new Client({
  webSocketFactory: () => socket,
  connectHeaders: {
    Authorization: 'Bearer ' + localStorage.getItem('token')
  },
  // ... other config
});
```

## Testing with Postman or WebSocket Client

### Using Chrome Extension: Smart Websocket Client
1. Install "Smart Websocket Client" extension
2. Connect to: `ws://localhost:8080/ws`
3. Use SockJS protocol
4. Subscribe to topic: `/topic/restaurant/1/orders`
5. Create an order via REST API and watch for notifications

### Using wscat (Command Line)
```bash
npm install -g wscat
wscat -c ws://localhost:8080/ws
```

## Troubleshooting

### Connection Fails
- Check if Spring Boot application is running
- Verify WebSocket endpoint: `/ws`
- Check browser console for errors
- Ensure CORS is properly configured

### Not Receiving Messages
- Verify you're subscribed to the correct topic
- Check userId matches the entity (restaurant, driver, customer)
- Look for errors in Spring Boot logs
- Ensure WebSocketService is properly injected

### Connection Drops
- Implement reconnection logic with exponential backoff
- Check network stability
- Verify heartbeat configuration

## Chat Features to Implement

### Basic Features
- ✅ Real-time text messaging
- ✅ Typing indicators
- ✅ Message timestamps
- ✅ Sender identification

### Advanced Features (Optional)
- 📸 Image sharing (set `messageType: "IMAGE"` and send image URL)
- 📍 Location sharing (set `messageType: "LOCATION"` with coordinates)
- 🔔 Push notifications for offline users
- 💾 Message persistence (save to database)
- ✓ Read receipts
- 🔍 Message search
- 📎 File attachments

## Best Practices

### General
1. **Always handle disconnections**: Implement automatic reconnection
2. **Show connection status**: Display "Connected" / "Disconnected" indicator
3. **Error handling**: Gracefully handle WebSocket errors
4. **Cleanup**: Always disconnect when component unmounts

### Notifications
5. **Sound notifications**: Play different sounds for different notification types
6. **Badge counts**: Update notification badges in real-time
7. **Auto-refresh lists**: Refresh order lists when receiving notifications
8. **Queue messages offline**: Store notifications when offline, sync when reconnected

### Chat
9. **Message persistence**: Save chat history to database
10. **Typing debounce**: Don't send typing indicator on every keystroke
11. **Message length limit**: Validate message length before sending
12. **Auto-scroll**: Scroll to bottom when new message arrives
13. **Time grouping**: Group messages by time (e.g., "Today", "Yesterday")
14. **User online status**: Show if other party is online/offline

## Support
For issues or questions, contact the development team.
