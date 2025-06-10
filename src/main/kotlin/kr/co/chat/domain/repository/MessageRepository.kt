package kr.co.chat.domain.repository

import kr.co.chat.domain.model.Message
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface MessageRepository : MongoRepository<Message, String> {
    
    @Query("{ 'roomId': ?0 }")
    fun findByRoomIdOrderByTimestampDesc(roomId: String, pageable: Pageable): Page<Message>
    
    @Query("{ 'roomId': ?0, 'timestamp': { '\$gte': ?1, '\$lte': ?2 } }")
    fun findByRoomIdAndTimestampBetween(
        roomId: String,
        startTime: LocalDateTime,
        endTime: LocalDateTime
    ): List<Message>
    
    @Query("{ 'senderId': ?0 }")
    fun findBySenderId(senderId: String): List<Message>
    
    @Query("{ 'mentions': { '\$in': [?0] } }")
    fun findByMentionsContaining(userId: String): List<Message>
    
    @Query("{ 'content': { '\$regex': ?0, '\$options': 'i' } }")
    fun findByContentContaining(keyword: String): List<Message>
    
    fun countByRoomId(roomId: String): Long
} 