package kr.co.chat.service

import com.corundumstudio.socketio.SocketIOServer
import com.fasterxml.jackson.databind.ObjectMapper
import kr.co.chat.domain.model.Message
import org.slf4j.LoggerFactory
import org.springframework.data.redis.connection.MessageListener
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.stereotype.Service
import jakarta.annotation.PostConstruct
import java.util.concurrent.ConcurrentHashMap

@Service
class RedisMessageListener(
    private val socketIOServer: SocketIOServer,
    private val redisMessageListenerContainer: RedisMessageListenerContainer,
    private val objectMapper: ObjectMapper
) {
    private val logger = LoggerFactory.getLogger(RedisMessageListener::class.java)
    private val subscribedChannels = ConcurrentHashMap<String, MessageListener>()

    @PostConstruct
    fun init() {
        // 기본 채널들 구독
        subscribeToChannel(RedisMessageService.USER_STATUS_CHANNEL)
        subscribeToChannel(RedisMessageService.ROOM_UPDATE_CHANNEL)
        
        // 기본 'general' 채팅방 구독
        subscribeToChatRoom("general")
        
        logger.info("Redis message listeners initialized")
    }

    /**
     * 채팅방별 Redis 채널 구독
     */
    fun subscribeToChatRoom(roomId: String) {
        val channelName = "${RedisMessageService.CHAT_CHANNEL_PREFIX}$roomId"
        
        // 이미 구독된 채널인지 확인
        if (subscribedChannels.containsKey(channelName)) {
            logger.debug("Already subscribed to channel: $channelName")
            return
        }
        
        val messageListener = MessageListener { message, channelBytes ->
            try {
                val channel = channelBytes?.let { String(it) } ?: return@MessageListener
                val messageBody = message.body?.let { String(it) } ?: return@MessageListener
                
                logger.debug("Received message from channel: $channel")
                handleChatMessage(channel, messageBody)
            } catch (e: Exception) {
                logger.error("Error processing Redis message from channel: $channelName", e)
            }
        }
        
        subscribedChannels[channelName] = messageListener
        redisMessageListenerContainer.addMessageListener(messageListener, ChannelTopic(channelName))
        logger.info("Subscribed to chat room channel: $channelName")
    }

    /**
     * 채팅방 구독 해제
     */
    fun unsubscribeFromChatRoom(roomId: String) {
        val channelName = "${RedisMessageService.CHAT_CHANNEL_PREFIX}$roomId"
        val messageListener = subscribedChannels.remove(channelName)
        
        if (messageListener != null) {
            redisMessageListenerContainer.removeMessageListener(messageListener, ChannelTopic(channelName))
            logger.info("Unsubscribed from chat room channel: $channelName")
        }
    }

    private fun subscribeToChannel(channelName: String) {
        val messageListener = MessageListener { message, channelBytes ->
            try {
                val channel = channelBytes?.let { String(it) } ?: return@MessageListener
                val messageBody = message.body?.let { String(it) } ?: return@MessageListener
                
                logger.debug("Received message from channel: $channel")
                
                when (channel) {
                    RedisMessageService.USER_STATUS_CHANNEL -> {
                        handleUserStatusUpdate(messageBody)
                    }
                    RedisMessageService.ROOM_UPDATE_CHANNEL -> {
                        handleRoomUpdate(messageBody)
                    }
                }
            } catch (e: Exception) {
                logger.error("Error processing Redis message from channel: $channelName", e)
            }
        }
        
        redisMessageListenerContainer.addMessageListener(messageListener, ChannelTopic(channelName))
        logger.info("Subscribed to channel: $channelName")
    }

    private fun handleChatMessage(channel: String, messageBody: String) {
        try {
            // 채널에서 roomId 추출 (chat:room:general -> general)
            val roomId = channel.removePrefix("chat:room:")
            
            // JSON을 Message 객체로 변환
            val message = objectMapper.readValue(messageBody, Message::class.java)
            
            // Message 객체를 Socket.io 호환 Map으로 변환
            val messageMap = mapOf(
                "id" to message.id,
                "roomId" to message.roomId,
                "senderId" to message.senderId,
                "senderName" to message.senderName,
                "content" to message.content,
                "type" to message.type.name,
                "attachments" to message.attachments,
                "mentions" to message.mentions,
                "replyToId" to message.replyToId,
                "isEdited" to message.isEdited,
                "editedAt" to message.editedAt?.toString(),
                "timestamp" to message.timestamp.toString()
            )
            
            // 해당 채팅방의 모든 클라이언트에게 메시지 전송
            socketIOServer.getRoomOperations(roomId).sendEvent("message", messageMap)
            
            logger.debug("Forwarded message to room: $roomId")
        } catch (e: Exception) {
            logger.error("Error handling chat message: $messageBody", e)
        }
    }

    private fun handleUserStatusUpdate(messageBody: String) {
        try {
            val statusUpdate = objectMapper.readValue(messageBody, Map::class.java)
            
            // 모든 클라이언트에게 사용자 상태 변경 알림
            socketIOServer.broadcastOperations.sendEvent("user_status_update", statusUpdate)
            
            logger.debug("Forwarded user status update: $statusUpdate")
        } catch (e: Exception) {
            logger.error("Error handling user status update: $messageBody", e)
        }
    }

    private fun handleRoomUpdate(messageBody: String) {
        try {
            val roomUpdate = objectMapper.readValue(messageBody, Map::class.java)
            val roomId = roomUpdate["roomId"] as String?
            
            if (roomId != null) {
                // 해당 채팅방의 모든 클라이언트에게 방 업데이트 알림
                socketIOServer.getRoomOperations(roomId).sendEvent("room_update", roomUpdate)
                
                logger.debug("Forwarded room update: $roomUpdate")
            }
        } catch (e: Exception) {
            logger.error("Error handling room update: $messageBody", e)
        }
    }
} 