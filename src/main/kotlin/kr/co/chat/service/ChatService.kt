package kr.co.chat.service

import kr.co.chat.domain.model.ChatRoom
import kr.co.chat.domain.model.Message
import kr.co.chat.domain.model.RoomType
import kr.co.chat.domain.model.User
import kr.co.chat.domain.repository.ChatRoomRepository
import kr.co.chat.domain.repository.MessageRepository
import kr.co.chat.domain.repository.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional
class ChatService(
    private val userRepository: UserRepository,
    private val chatRoomRepository: ChatRoomRepository,
    private val messageRepository: MessageRepository,
    private val redisMessageService: RedisMessageService
) {

    /**
     * 메시지 저장
     */
    fun saveMessage(message: Message): Message {
        return messageRepository.save(message)
    }

    /**
     * 채팅방 메시지 조회 (페이징)
     */
    fun getMessages(roomId: String, pageable: Pageable): Page<Message> {
        return messageRepository.findByRoomIdOrderByTimestampDesc(roomId, pageable)
    }

    /**
     * 채팅방 생성
     */
    fun createChatRoom(room: ChatRoom): ChatRoom {
        val savedRoom = chatRoomRepository.save(room)
        redisMessageService.publishRoomUpdate(savedRoom.id!!, "CREATED", savedRoom)
        return savedRoom
    }

    /**
     * 채팅방 참여
     */
    fun joinChatRoom(roomId: String, userId: String): ChatRoom? {
        val room = chatRoomRepository.findById(roomId).orElse(null) ?: return null
        
        if (room.members.size >= room.maxMembers) {
            throw IllegalStateException("채팅방이 가득찼습니다.")
        }
        
        val updatedRoom = room.copy(
            members = room.members + userId,
            updatedAt = LocalDateTime.now()
        )
        
        val savedRoom = chatRoomRepository.save(updatedRoom)
        redisMessageService.publishRoomUpdate(roomId, "USER_JOINED", mapOf("userId" to userId))
        
        return savedRoom
    }

    /**
     * 채팅방 나가기
     */
    fun leaveChatRoom(roomId: String, userId: String): ChatRoom? {
        val room = chatRoomRepository.findById(roomId).orElse(null) ?: return null
        
        val updatedRoom = room.copy(
            members = room.members - userId,
            updatedAt = LocalDateTime.now()
        )
        
        val savedRoom = chatRoomRepository.save(updatedRoom)
        redisMessageService.publishRoomUpdate(roomId, "USER_LEFT", mapOf("userId" to userId))
        
        return savedRoom
    }

    /**
     * 사용자별 채팅방 조회
     */
    fun getUserChatRooms(userId: String): List<ChatRoom> {
        return chatRoomRepository.findActiveRoomsByUserId(userId)
    }

    /**
     * 공개 채팅방 조회
     */
    fun getPublicChatRooms(): List<ChatRoom> {
        return chatRoomRepository.findActiveRoomsByType(RoomType.PUBLIC)
    }

    /**
     * 채팅방 검색
     */
    fun searchChatRooms(keyword: String): List<ChatRoom> {
        return chatRoomRepository.findActiveRoomsByNameContaining(keyword)
    }

    /**
     * 사용자 생성
     */
    fun createUser(user: User): User {
        if (userRepository.existsByUsername(user.username)) {
            throw IllegalArgumentException("이미 존재하는 사용자명입니다.")
        }
        if (userRepository.existsByEmail(user.email)) {
            throw IllegalArgumentException("이미 존재하는 이메일입니다.")
        }
        
        return userRepository.save(user)
    }

    /**
     * 사용자 조회
     */
    fun getUser(userId: String): User? {
        return userRepository.findById(userId).orElse(null)
    }

    /**
     * 사용자명으로 사용자 조회
     */
    fun getUserByUsername(username: String): User? {
        return userRepository.findByUsername(username)
    }

    /**
     * 사용자 상태 업데이트
     */
    fun updateUserStatus(userId: String, status: String): User? {
        val user = userRepository.findById(userId).orElse(null) ?: return null
        
        val updatedUser = user.copy(
            status = kr.co.chat.domain.model.UserStatus.valueOf(status.uppercase()),
            lastSeen = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        
        val savedUser = userRepository.save(updatedUser)
        redisMessageService.publishUserStatusUpdate(userId, status)
        
        return savedUser
    }

    /**
     * 채팅방의 온라인 사용자 수 조회
     */
    fun getRoomOnlineCount(roomId: String): Long {
        return redisMessageService.getRoomUsersCount(roomId)
    }

    /**
     * 전체 온라인 사용자 수 조회
     */
    fun getTotalOnlineCount(): Long {
        return redisMessageService.getOnlineUsersCount()
    }

    /**
     * 사용자 온라인 상태 확인
     */
    fun isUserOnline(userId: String): Boolean {
        return redisMessageService.isUserOnline(userId)
    }

    /**
     * 메시지 검색
     */
    fun searchMessages(keyword: String): List<Message> {
        return messageRepository.findByContentContaining(keyword)
    }

    /**
     * 사용자 멘션 메시지 조회
     */
    fun getUserMentions(userId: String): List<Message> {
        return messageRepository.findByMentionsContaining(userId)
    }

    /**
     * 채팅방 메시지 수 조회
     */
    fun getRoomMessageCount(roomId: String): Long {
        return messageRepository.countByRoomId(roomId)
    }

    /**
     * 채팅방 삭제 (비활성화)
     */
    fun deleteChatRoom(roomId: String, userId: String): Boolean {
        val room = chatRoomRepository.findById(roomId).orElse(null) ?: return false
        
        if (room.ownerId != userId) {
            throw IllegalAccessException("채팅방을 삭제할 권한이 없습니다.")
        }
        
        val updatedRoom = room.copy(
            isActive = false,
            updatedAt = LocalDateTime.now()
        )
        
        chatRoomRepository.save(updatedRoom)
        redisMessageService.publishRoomUpdate(roomId, "DELETED", null)
        
        return true
    }
} 