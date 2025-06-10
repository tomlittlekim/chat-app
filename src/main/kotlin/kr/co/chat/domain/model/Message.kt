package kr.co.chat.domain.model

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "messages")
data class Message(
    @Id
    val id: String? = null,
    val roomId: String,
    val senderId: String,
    val senderName: String,
    val content: String,
    val type: MessageType = MessageType.TEXT,
    val attachments: List<Attachment> = emptyList(),
    val mentions: Set<String> = emptySet(),
    val replyToId: String? = null,
    val isEdited: Boolean = false,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    @JsonSerialize(using = LocalDateTimeSerializer::class)
    @JsonDeserialize(using = LocalDateTimeDeserializer::class)
    val editedAt: LocalDateTime? = null,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    @JsonSerialize(using = LocalDateTimeSerializer::class)
    @JsonDeserialize(using = LocalDateTimeDeserializer::class)
    val timestamp: LocalDateTime = LocalDateTime.now()
)

enum class MessageType {
    TEXT, IMAGE, FILE, SYSTEM, EMOJI
}

data class Attachment(
    val id: String,
    val name: String,
    val url: String,
    val size: Long,
    val mimeType: String
) 