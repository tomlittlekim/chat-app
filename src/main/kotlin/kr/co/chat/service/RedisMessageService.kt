package kr.co.chat.service

import com.fasterxml.jackson.databind.ObjectMapper
import kr.co.chat.domain.model.Message
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.stereotype.Service

@Service
class RedisMessageService(
    private val redisTemplate: RedisTemplate<String, Any>,
    private val redisMessageListenerContainer: RedisMessageListenerContainer,
    private val objectMapper: ObjectMapper
) {
    private val logger = LoggerFactory.getLogger(RedisMessageService::class.java)

    companion object {
        const val CHAT_CHANNEL_PREFIX = "chat:room:"
        const val USER_STATUS_CHANNEL = "user:status"
        const val ROOM_UPDATE_CHANNEL = "room:update"
    }

    /**
     * 메시지를 특정 채팅방 채널에 발행
     */
    fun publishMessage(roomId: String, message: Message) {
        try {
            val channel = "$CHAT_CHANNEL_PREFIX$roomId"
            redisTemplate.convertAndSend(channel, message)
            logger.debug("Published message to channel: $channel")
        } catch (e: Exception) {
            logger.error("Failed to publish message to room $roomId", e)
        }
    }

    /**
     * 사용자 상태 변경을 발행
     */
    fun publishUserStatusUpdate(userId: String, status: String) {
        try {
            val statusUpdate = mapOf(
                "userId" to userId,
                "status" to status,
                "timestamp" to System.currentTimeMillis()
            )
            redisTemplate.convertAndSend(USER_STATUS_CHANNEL, statusUpdate)
            logger.debug("Published user status update: $userId -> $status")
        } catch (e: Exception) {
            logger.error("Failed to publish user status update for user $userId", e)
        }
    }

    /**
     * 채팅방 업데이트를 발행
     */
    fun publishRoomUpdate(roomId: String, action: String, data: Any? = null) {
        try {
            val roomUpdate = mapOf(
                "roomId" to roomId,
                "action" to action,
                "data" to data,
                "timestamp" to System.currentTimeMillis()
            )
            redisTemplate.convertAndSend(ROOM_UPDATE_CHANNEL, roomUpdate)
            logger.debug("Published room update: $roomId -> $action")
        } catch (e: Exception) {
            logger.error("Failed to publish room update for room $roomId", e)
        }
    }

    /**
     * 특정 채널 구독
     */
    fun subscribeToChannel(channelName: String, listener: org.springframework.data.redis.connection.MessageListener) {
        val topic = ChannelTopic(channelName)
        redisMessageListenerContainer.addMessageListener(listener, topic)
        logger.info("Subscribed to channel: $channelName")
    }

    /**
     * 채널 구독 해제
     */
    fun unsubscribeFromChannel(channelName: String, listener: org.springframework.data.redis.connection.MessageListener) {
        val topic = ChannelTopic(channelName)
        redisMessageListenerContainer.removeMessageListener(listener, topic)
        logger.info("Unsubscribed from channel: $channelName")
    }

    /**
     * 현재 온라인 사용자 수 관리
     */
    fun addOnlineUser(userId: String) {
        redisTemplate.opsForSet().add("online:users", userId)
    }

    fun removeOnlineUser(userId: String) {
        redisTemplate.opsForSet().remove("online:users", userId)
    }

    fun getOnlineUsersCount(): Long {
        return redisTemplate.opsForSet().size("online:users") ?: 0L
    }

    fun isUserOnline(userId: String): Boolean {
        return redisTemplate.opsForSet().isMember("online:users", userId) ?: false
    }

    /**
     * 채팅방별 온라인 사용자 관리
     */
    fun addUserToRoom(roomId: String, userId: String) {
        redisTemplate.opsForSet().add("room:$roomId:users", userId)
    }

    fun removeUserFromRoom(roomId: String, userId: String) {
        redisTemplate.opsForSet().remove("room:$roomId:users", userId)
    }

    fun getRoomUsersCount(roomId: String): Long {
        return redisTemplate.opsForSet().size("room:$roomId:users") ?: 0L
    }
} 