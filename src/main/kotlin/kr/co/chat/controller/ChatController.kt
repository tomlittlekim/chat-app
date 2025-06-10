package kr.co.chat.controller

import kr.co.chat.domain.model.ChatRoom
import kr.co.chat.domain.model.Message
import kr.co.chat.domain.model.RoomType
import kr.co.chat.domain.model.User
import kr.co.chat.service.ChatService
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = ["*"])
class ChatController(
    private val chatService: ChatService
) {

    /**
     * 사용자 생성
     */
    @PostMapping("/users")
    fun createUser(@RequestBody createUserRequest: CreateUserRequest): ResponseEntity<User> {
        val user = User(
            username = createUserRequest.username,
            email = createUserRequest.email,
            displayName = createUserRequest.displayName,
            avatar = createUserRequest.avatar
        )
        
        return try {
            val createdUser = chatService.createUser(user)
            ResponseEntity.ok(createdUser)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().build()
        }
    }

    /**
     * 사용자 조회
     */
    @GetMapping("/users/{userId}")
    fun getUser(@PathVariable userId: String): ResponseEntity<User> {
        val user = chatService.getUser(userId)
        return if (user != null) {
            ResponseEntity.ok(user)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    /**
     * 사용자명으로 사용자 조회
     */
    @GetMapping("/users/by-username/{username}")
    fun getUserByUsername(@PathVariable username: String): ResponseEntity<User> {
        val user = chatService.getUserByUsername(username)
        return if (user != null) {
            ResponseEntity.ok(user)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    /**
     * 채팅방 생성
     */
    @PostMapping("/rooms")
    fun createChatRoom(@RequestBody createRoomRequest: CreateRoomRequest): ResponseEntity<ChatRoom> {
        val chatRoom = ChatRoom(
            name = createRoomRequest.name,
            description = createRoomRequest.description,
            type = RoomType.valueOf(createRoomRequest.type.uppercase()),
            ownerId = createRoomRequest.ownerId,
            maxMembers = createRoomRequest.maxMembers ?: 1000
        )
        
        val createdRoom = chatService.createChatRoom(chatRoom)
        return ResponseEntity.ok(createdRoom)
    }

    /**
     * 공개 채팅방 목록 조회
     */
    @GetMapping("/rooms/public")
    fun getPublicChatRooms(): ResponseEntity<List<ChatRoom>> {
        val rooms = chatService.getPublicChatRooms()
        return ResponseEntity.ok(rooms)
    }

    /**
     * 사용자 채팅방 목록 조회
     */
    @GetMapping("/users/{userId}/rooms")
    fun getUserChatRooms(@PathVariable userId: String): ResponseEntity<List<ChatRoom>> {
        val rooms = chatService.getUserChatRooms(userId)
        return ResponseEntity.ok(rooms)
    }

    /**
     * 채팅방 검색
     */
    @GetMapping("/rooms/search")
    fun searchChatRooms(@RequestParam keyword: String): ResponseEntity<List<ChatRoom>> {
        val rooms = chatService.searchChatRooms(keyword)
        return ResponseEntity.ok(rooms)
    }

    /**
     * 채팅방 참여
     */
    @PostMapping("/rooms/{roomId}/join")
    fun joinChatRoom(
        @PathVariable roomId: String,
        @RequestBody joinRoomRequest: JoinRoomRequest
    ): ResponseEntity<ChatRoom> {
        return try {
            val room = chatService.joinChatRoom(roomId, joinRoomRequest.userId)
            if (room != null) {
                ResponseEntity.ok(room)
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: IllegalStateException) {
            ResponseEntity.badRequest().build()
        }
    }

    /**
     * 채팅방 나가기
     */
    @PostMapping("/rooms/{roomId}/leave")
    fun leaveChatRoom(
        @PathVariable roomId: String,
        @RequestBody leaveRoomRequest: LeaveRoomRequest
    ): ResponseEntity<ChatRoom> {
        val room = chatService.leaveChatRoom(roomId, leaveRoomRequest.userId)
        return if (room != null) {
            ResponseEntity.ok(room)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    /**
     * 채팅방 메시지 조회 (페이징)
     */
    @GetMapping("/rooms/{roomId}/messages")
    fun getRoomMessages(
        @PathVariable roomId: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "50") size: Int
    ): ResponseEntity<Page<Message>> {
        val pageable = PageRequest.of(page, size, Sort.by("timestamp").descending())
        val messages = chatService.getMessages(roomId, pageable)
        return ResponseEntity.ok(messages)
    }

    /**
     * 메시지 검색
     */
    @GetMapping("/messages/search")
    fun searchMessages(@RequestParam keyword: String): ResponseEntity<List<Message>> {
        val messages = chatService.searchMessages(keyword)
        return ResponseEntity.ok(messages)
    }

    /**
     * 사용자 멘션 메시지 조회
     */
    @GetMapping("/users/{userId}/mentions")
    fun getUserMentions(@PathVariable userId: String): ResponseEntity<List<Message>> {
        val mentions = chatService.getUserMentions(userId)
        return ResponseEntity.ok(mentions)
    }

    /**
     * 채팅방 통계 정보
     */
    @GetMapping("/rooms/{roomId}/stats")
    fun getRoomStats(@PathVariable roomId: String): ResponseEntity<RoomStatsResponse> {
        val onlineCount = chatService.getRoomOnlineCount(roomId)
        val messageCount = chatService.getRoomMessageCount(roomId)
        
        val stats = RoomStatsResponse(
            onlineUsers = onlineCount,
            totalMessages = messageCount
        )
        
        return ResponseEntity.ok(stats)
    }

    /**
     * 전체 시스템 통계
     */
    @GetMapping("/stats")
    fun getSystemStats(): ResponseEntity<SystemStatsResponse> {
        val totalOnline = chatService.getTotalOnlineCount()
        
        val stats = SystemStatsResponse(
            totalOnlineUsers = totalOnline
        )
        
        return ResponseEntity.ok(stats)
    }

    /**
     * 사용자 상태 업데이트
     */
    @PutMapping("/users/{userId}/status")
    fun updateUserStatus(
        @PathVariable userId: String,
        @RequestBody statusRequest: UpdateStatusRequest
    ): ResponseEntity<User> {
        val user = chatService.updateUserStatus(userId, statusRequest.status)
        return if (user != null) {
            ResponseEntity.ok(user)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    /**
     * 채팅방 삭제
     */
    @DeleteMapping("/rooms/{roomId}")
    fun deleteChatRoom(
        @PathVariable roomId: String,
        @RequestParam userId: String
    ): ResponseEntity<Void> {
        return try {
            val deleted = chatService.deleteChatRoom(roomId, userId)
            if (deleted) {
                ResponseEntity.ok().build()
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: IllegalAccessException) {
            ResponseEntity.status(403).build()
        }
    }
}

// 요청/응답 데이터 클래스들
data class CreateUserRequest(
    val username: String,
    val email: String,
    val displayName: String,
    val avatar: String? = null
)

data class CreateRoomRequest(
    val name: String,
    val description: String? = null,
    val type: String = "PUBLIC",
    val ownerId: String,
    val maxMembers: Int? = null
)

data class JoinRoomRequest(
    val userId: String
)

data class LeaveRoomRequest(
    val userId: String
)

data class UpdateStatusRequest(
    val status: String
)

data class RoomStatsResponse(
    val onlineUsers: Long,
    val totalMessages: Long
)

data class SystemStatsResponse(
    val totalOnlineUsers: Long
) 