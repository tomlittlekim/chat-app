package kr.co.chat.domain.repository

import kr.co.chat.domain.model.ChatRoom
import kr.co.chat.domain.model.RoomType
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ChatRoomRepository : MongoRepository<ChatRoom, String> {
    fun findByType(type: RoomType): List<ChatRoom>
    fun findByOwnerId(ownerId: String): List<ChatRoom>
    
    @Query("{ 'members': { '\$in': [?0] }, 'isActive': true }")
    fun findActiveRoomsByUserId(userId: String): List<ChatRoom>
    
    @Query("{ 'type': ?0, 'isActive': true }")
    fun findActiveRoomsByType(type: RoomType): List<ChatRoom>
    
    @Query("{ 'name': { '\$regex': ?0, '\$options': 'i' }, 'isActive': true }")
    fun findActiveRoomsByNameContaining(name: String): List<ChatRoom>
} 