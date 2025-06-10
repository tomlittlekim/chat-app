package kr.co.chat.config

import com.corundumstudio.socketio.Configuration
import com.corundumstudio.socketio.SocketIOServer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component

@Component
class SocketIOConfig {

    @Value("\${socketio.host:localhost}")
    private lateinit var host: String

    @Value("\${socketio.port:9092}")
    private var port: Int = 9092

    @Value("\${socketio.boss-threads:4}")
    private var bossThreads: Int = 4

    @Value("\${socketio.worker-threads:100}")
    private var workerThreads: Int = 100

    @Value("\${socketio.allow-custom-requests:true}")
    private var allowCustomRequests: Boolean = true

    @Value("\${socketio.upgrade-timeout:1000}")
    private var upgradeTimeout: Int = 1000

    @Value("\${socketio.ping-timeout:5000}")
    private var pingTimeout: Int = 5000

    @Value("\${socketio.ping-interval:25000}")
    private var pingInterval: Int = 25000

    @Bean
    fun socketIOServer(): SocketIOServer {
        val config = Configuration().apply {
            hostname = host
            port = this@SocketIOConfig.port
            bossThreads = this@SocketIOConfig.bossThreads
            workerThreads = this@SocketIOConfig.workerThreads
            isAllowCustomRequests = allowCustomRequests
            upgradeTimeout = this@SocketIOConfig.upgradeTimeout
            pingTimeout = this@SocketIOConfig.pingTimeout
            pingInterval = this@SocketIOConfig.pingInterval
            
            // CORS 설정
            setOrigin("*")
            
            // 인증 설정 (JWT 토큰 검증)
            setAuthorizationListener { handshakeData ->
                val token = handshakeData.getSingleUrlParam("token")
                // TODO: JWT 토큰 검증 로직 구현
                if (token != null) {
                    com.corundumstudio.socketio.AuthorizationResult.SUCCESSFUL_AUTHORIZATION
                } else {
                    com.corundumstudio.socketio.AuthorizationResult.FAILED_AUTHORIZATION
                }
            }
        }
        
        return SocketIOServer(config)
    }
    
    @Bean
    fun objectMapper(): ObjectMapper {
        return jacksonObjectMapper().apply {
            registerKotlinModule()
            registerModule(JavaTimeModule())
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        }
    }
}