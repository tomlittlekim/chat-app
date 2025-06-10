package kr.co.chat.domain.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "users")
data class User(
    @Id
    val id: String? = null,
    val username: String,
    val email: String,
    val displayName: String,
    val avatar: String? = null,
    val status: UserStatus = UserStatus.OFFLINE,
    val lastSeen: LocalDateTime = LocalDateTime.now(),
    val joinedRooms: Set<String> = emptySet(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class UserStatus {
    ONLINE, OFFLINE, AWAY, BUSY
} 