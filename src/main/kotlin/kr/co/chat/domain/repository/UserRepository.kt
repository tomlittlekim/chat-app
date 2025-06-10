package kr.co.chat.domain.repository

import kr.co.chat.domain.model.User
import kr.co.chat.domain.model.UserStatus
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : MongoRepository<User, String> {
    fun findByUsername(username: String): User?
    fun findByEmail(email: String): User?
    
    @Query("{ 'status': ?0 }")
    fun findByStatus(status: UserStatus): List<User>
    
    @Query("{ 'joinedRooms': { '\$in': [?0] } }")
    fun findUsersInRoom(roomId: String): List<User>
    
    fun existsByUsername(username: String): Boolean
    fun existsByEmail(email: String): Boolean
} 