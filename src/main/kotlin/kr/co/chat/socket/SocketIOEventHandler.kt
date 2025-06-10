package kr.co.chat.socket

import com.corundumstudio.socketio.SocketIOServer
import com.corundumstudio.socketio.listener.ConnectListener
import com.corundumstudio.socketio.listener.DataListener
import com.corundumstudio.socketio.listener.DisconnectListener
import kr.co.chat.domain.model.Message
import kr.co.chat.domain.model.MessageType
import kr.co.chat.service.ChatService
import kr.co.chat.service.RedisMessageService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@Component
class SocketIOEventHandler(
    private val socketIOServer: SocketIOServer,
    private val chatService: ChatService,
    private val redisMessageService: RedisMessageService
) {
    private val logger = LoggerFactory.getLogger(SocketIOEventHandler::class.java)

    @PostConstruct
    fun init() {
        socketIOServer.addConnectListener(onConnect())
        socketIOServer.addDisconnectListener(onDisconnect())
        
        // 메시지 관련 이벤트
        socketIOServer.addEventListener("join_room", String::class.java, onJoinRoom())
        socketIOServer.addEventListener("leave_room", String::class.java, onLeaveRoom())
        socketIOServer.addEventListener("send_message", MessageData::class.java, onSendMessage())
        socketIOServer.addEventListener("typing_start", TypingData::class.java, onTypingStart())
        socketIOServer.addEventListener("typing_stop", TypingData::class.java, onTypingStop())
        
        // 사용자 상태 관련 이벤트
        socketIOServer.addEventListener("user_status", UserStatusData::class.java, onUserStatus())
        
        socketIOServer.start()
        logger.info("Socket.IO server started on port: ${socketIOServer.configuration.port}")
    }

    @PreDestroy
    fun destroy() {
        socketIOServer.stop()
        logger.info("Socket.IO server stopped")
    }

    private fun onConnect(): ConnectListener {
        return ConnectListener { client ->
            val userId = client.handshakeData.getSingleUrlParam("userId")
            
            if (userId != null) {
                client.set("userId", userId)
                redisMessageService.addOnlineUser(userId)
                redisMessageService.publishUserStatusUpdate(userId, "ONLINE")
                
                logger.info("User connected: $userId, SessionId: ${client.sessionId}")
                
                // 온라인 사용자 수 브로드캐스트
                socketIOServer.broadcastOperations.sendEvent("online_users_count", 
                    redisMessageService.getOnlineUsersCount())
            } else {
                logger.warn("Connection without userId, disconnecting: ${client.sessionId}")
                client.disconnect()
            }
        }
    }

    private fun onDisconnect(): DisconnectListener {
        return DisconnectListener { client ->
            val userId = client.get<String>("userId")
            
            if (userId != null) {
                redisMessageService.removeOnlineUser(userId)
                redisMessageService.publishUserStatusUpdate(userId, "OFFLINE")
                
                // 모든 채팅방에서 사용자 제거
                client.getAllRooms().forEach { roomId ->
                    redisMessageService.removeUserFromRoom(roomId, userId)
                }
                
                logger.info("User disconnected: $userId, SessionId: ${client.sessionId}")
                
                // 온라인 사용자 수 브로드캐스트
                socketIOServer.broadcastOperations.sendEvent("online_users_count", 
                    redisMessageService.getOnlineUsersCount())
            }
        }
    }

    private fun onJoinRoom(): DataListener<String> {
        return DataListener { client, roomId, _ ->
            val userId = client.get<String>("userId")
            
            if (userId != null) {
                client.joinRoom(roomId)
                redisMessageService.addUserToRoom(roomId, userId)
                
                // 채팅방 입장 시스템 메시지
                val systemMessage = Message(
                    roomId = roomId,
                    senderId = "system",
                    senderName = "System",
                    content = "$userId님이 입장했습니다.",
                    type = MessageType.SYSTEM,
                    timestamp = LocalDateTime.now()
                )
                
                chatService.saveMessage(systemMessage)
                redisMessageService.publishMessage(roomId, systemMessage)
                
                // 방 사용자 수 업데이트
                val roomUserCount = redisMessageService.getRoomUsersCount(roomId)
                socketIOServer.getRoomOperations(roomId).sendEvent("room_user_count", roomUserCount)
                
                logger.info("User $userId joined room: $roomId")
            }
        }
    }

    private fun onLeaveRoom(): DataListener<String> {
        return DataListener { client, roomId, _ ->
            val userId = client.get<String>("userId")
            
            if (userId != null) {
                client.leaveRoom(roomId)
                redisMessageService.removeUserFromRoom(roomId, userId)
                
                // 채팅방 퇴장 시스템 메시지
                val systemMessage = Message(
                    roomId = roomId,
                    senderId = "system",
                    senderName = "System",
                    content = "$userId님이 퇴장했습니다.",
                    type = MessageType.SYSTEM,
                    timestamp = LocalDateTime.now()
                )
                
                chatService.saveMessage(systemMessage)
                redisMessageService.publishMessage(roomId, systemMessage)
                
                // 방 사용자 수 업데이트
                val roomUserCount = redisMessageService.getRoomUsersCount(roomId)
                socketIOServer.getRoomOperations(roomId).sendEvent("room_user_count", roomUserCount)
                
                logger.info("User $userId left room: $roomId")
            }
        }
    }

    private fun onSendMessage(): DataListener<MessageData> {
        return DataListener { client, messageData, _ ->
            val userId = client.get<String>("userId")
            
            if (userId != null) {
                val message = Message(
                    roomId = messageData.roomId,
                    senderId = userId,
                    senderName = messageData.senderName,
                    content = messageData.content,
                    type = MessageType.valueOf(messageData.type.uppercase()),
                    timestamp = LocalDateTime.now()
                )
                
                // 메시지 저장 및 발행
                chatService.saveMessage(message)
                redisMessageService.publishMessage(messageData.roomId, message)
                
                logger.debug("Message sent by $userId to room ${messageData.roomId}")
            }
        }
    }

    private fun onTypingStart(): DataListener<TypingData> {
        return DataListener { client, typingData, _ ->
            val userId = client.get<String>("userId")
            
            if (userId != null) {
                client.getNamespace().getRoomOperations(typingData.roomId)
                    .sendEvent("user_typing", mapOf(
                        "userId" to userId,
                        "userName" to typingData.userName,
                        "isTyping" to true
                    ))
            }
        }
    }

    private fun onTypingStop(): DataListener<TypingData> {
        return DataListener { client, typingData, _ ->
            val userId = client.get<String>("userId")
            
            if (userId != null) {
                client.getNamespace().getRoomOperations(typingData.roomId)
                    .sendEvent("user_typing", mapOf(
                        "userId" to userId,
                        "userName" to typingData.userName,
                        "isTyping" to false
                    ))
            }
        }
    }

    private fun onUserStatus(): DataListener<UserStatusData> {
        return DataListener { client, statusData, _ ->
            val userId = client.get<String>("userId")
            
            if (userId != null) {
                redisMessageService.publishUserStatusUpdate(userId, statusData.status)
                logger.debug("User $userId status changed to ${statusData.status}")
            }
        }
    }
}

// 데이터 클래스들
data class MessageData(
    val roomId: String,
    val content: String,
    val senderName: String,
    val type: String = "TEXT"
)

data class TypingData(
    val roomId: String,
    val userName: String
)

data class UserStatusData(
    val status: String
) 