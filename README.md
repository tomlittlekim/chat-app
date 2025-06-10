# 실시간 채팅 시스템 (100만 동접 지원)

Kotlin + Spring Boot 기반의 확장 가능한 실시간 채팅 시스템입니다.

## 🏗️ 아키텍처

```
실시간 채팅 시스템 (100만 동접)
├── WebSocket + Socket.io (실시간 통신)
├── Redis Pub/Sub (메시지 브로커)
├── MongoDB 클러스터 (데이터 저장)
└── Kubernetes 배포 (확장성)
```

## 🚀 주요 기능

- **실시간 메시징**: Socket.io를 통한 양방향 실시간 통신
- **확장 가능한 아키텍처**: Redis Pub/Sub으로 다중 서버 지원
- **고성능 데이터베이스**: MongoDB 클러스터로 대용량 데이터 처리
- **자동 확장**: Kubernetes HPA로 트래픽에 따른 자동 스케일링
- **실시간 상태 관리**: 사용자 온라인/오프라인 상태 추적
- **채팅방 관리**: 다중 채팅방 지원
- **타이핑 표시**: 실시간 타이핑 상태 표시
- **메시지 검색**: 전문 검색 기능
- **모니터링**: Prometheus 메트릭 지원

## 🛠️ 기술 스택

### Backend
- **Kotlin** + **Spring Boot 3.5**
- **Socket.io** (netty-socketio)
- **MongoDB** (Spring Data MongoDB)
- **Redis** (Spring Data Redis)
- **Spring Security**
- **Micrometer** (메트릭)

### Infrastructure
- **Docker** (컨테이너화)
- **Kubernetes** (오케스트레이션)
- **MongoDB Cluster** (3 replicas)
- **Redis Cluster** (3 replicas)
- **HPA** (Horizontal Pod Autoscaler)

## 📋 사전 요구사항

- Java 21+
- Docker
- Kubernetes (minikube, Docker Desktop, 또는 클러스터)
- kubectl

## 🚀 빠른 시작

### 1. 프로젝트 클론
```bash
git clone <repository-url>
cd chat
```

### 2. 로컬 개발 환경 실행
```bash
# MongoDB 실행
docker run -d --name mongodb -p 27017:27017 \
  -e MONGO_INITDB_ROOT_USERNAME=admin \
  -e MONGO_INITDB_ROOT_PASSWORD=password123 \
  mongo:7.0

# Redis 실행
docker run -d --name redis -p 6379:6379 redis:7.2-alpine

# 애플리케이션 실행
./gradlew bootRun
```

### 3. Kubernetes 배포
```bash
# 배포 스크립트 실행
./deploy.sh
```

## 🌐 API 엔드포인트

### REST API
- `POST /api/chat/users` - 사용자 생성
- `GET /api/chat/users/{userId}` - 사용자 조회
- `POST /api/chat/rooms` - 채팅방 생성
- `GET /api/chat/rooms/public` - 공개 채팅방 목록
- `GET /api/chat/rooms/{roomId}/messages` - 채팅방 메시지 조회
- `GET /api/chat/stats` - 시스템 통계

### Socket.io 이벤트
- `join_room` - 채팅방 입장
- `leave_room` - 채팅방 퇴장
- `send_message` - 메시지 전송
- `typing_start/stop` - 타이핑 상태
- `user_status` - 사용자 상태 변경

### 모니터링
- `GET /actuator/health` - 헬스 체크
- `GET /actuator/metrics` - 메트릭
- `GET /actuator/prometheus` - Prometheus 메트릭

## 🎯 성능 목표

- **동시 접속자**: 100만명
- **메시지 처리량**: 초당 10만 메시지
- **응답 시간**: 평균 50ms 이하
- **가용성**: 99.9% 이상

## 📊 확장성 설계

### 수평 확장
- **애플리케이션**: HPA로 5-50개 Pod 자동 확장
- **데이터베이스**: MongoDB 샤딩 지원
- **캐시**: Redis 클러스터 모드

### 부하 분산
- **LoadBalancer**: Kubernetes Service
- **Session Affinity**: Redis 기반 세션 공유
- **Message Broadcasting**: Redis Pub/Sub

## 🔧 설정

### 애플리케이션 설정 (application.yml)
```yaml
# Socket.io 설정
socketio:
  host: 0.0.0.0
  port: 9092
  worker-threads: 100

# 채팅 시스템 설정
chat:
  max-message-length: 1000
  rate-limit:
    messages-per-minute: 60
  rooms:
    max-users-per-room: 1000
```

### Kubernetes 리소스 설정
```yaml
# HPA 설정
spec:
  minReplicas: 5
  maxReplicas: 50
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        averageUtilization: 70
```

## 🧪 테스트

### 웹 클라이언트 테스트
1. 애플리케이션 실행 후 `http://localhost:8080` 접속
2. 사용자명과 채팅방 입력
3. 여러 브라우저 탭에서 동시 테스트

### API 테스트
```bash
# 사용자 생성
curl -X POST http://localhost:8080/api/chat/users \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"test@example.com","displayName":"Test User"}'

# 채팅방 생성
curl -X POST http://localhost:8080/api/chat/rooms \
  -H "Content-Type: application/json" \
  -d '{"name":"테스트방","ownerId":"testuser"}'
```

## 📈 모니터링

### 메트릭 확인
```bash
# Prometheus 메트릭
curl http://localhost:8080/actuator/prometheus

# 시스템 통계
curl http://localhost:8080/api/chat/stats
```

### 로그 확인
```bash
# Kubernetes 로그
kubectl logs -f deployment/chat-app -n chat-system

# 특정 Pod 로그
kubectl logs -f <pod-name> -n chat-system
```

## 🔍 트러블슈팅

### 일반적인 문제들

1. **Socket.io 연결 실패**
   - 방화벽 설정 확인 (9092 포트)
   - CORS 설정 확인

2. **MongoDB 연결 실패**
   - 연결 문자열 확인
   - 인증 정보 확인

3. **Redis 연결 실패**
   - Redis 서버 상태 확인
   - 네트워크 연결 확인

### 성능 최적화

1. **JVM 튜닝**
   ```bash
   JAVA_OPTS="-server -Xmx2g -Xms1g -XX:+UseG1GC"
   ```

2. **MongoDB 인덱스**
   ```javascript
   db.messages.createIndex({"roomId": 1, "timestamp": -1})
   db.users.createIndex({"username": 1})
   ```

3. **Redis 설정**
   ```
   maxmemory 2gb
   maxmemory-policy allkeys-lru
   ```

## 🤝 기여하기

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📝 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다.

## 📞 연락처

프로젝트 관련 문의사항이 있으시면 이슈를 생성해 주세요.

---

**실시간 채팅 시스템으로 100만 동접의 꿈을 실현해보세요! 🚀** 