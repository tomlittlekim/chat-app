package kr.co.chat.domain.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "chat_rooms")
data class ChatRoom(
    @Id
    val id: String? = null,
    val name: String,
    val description: String? = null,
    val type: RoomType = RoomType.PUBLIC,
    val ownerId: String,
    val members: Set<String> = emptySet(),
    val maxMembers: Int = 1000,
    val isActive: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class RoomType {
    PUBLIC, PRIVATE, DIRECT_MESSAGE
} 