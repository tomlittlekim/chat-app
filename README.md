# 🚀 100만 동접 지원 실시간 채팅 시스템

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.25-7F52FF.svg)](https://kotlinlang.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.0-6DB33F.svg)](https://spring.io/projects/spring-boot)
[![Socket.io](https://img.shields.io/badge/Socket.io-2.0.9-010101.svg)](https://socket.io/)
[![MongoDB](https://img.shields.io/badge/MongoDB-7.0-47A248.svg)](https://mongodb.com/)
[![Redis](https://img.shields.io/badge/Redis-7.2-DC382D.svg)](https://redis.io/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED.svg)](https://docker.com/)
[![Kubernetes](https://img.shields.io/badge/Kubernetes-Ready-326CE5.svg)](https://kubernetes.io/)

## 📋 개요

Spring Boot + Kotlin 기반의 확장 가능한 실시간 채팅 시스템입니다. Socket.io와 Redis Pub/Sub을 활용하여 100만 동시 접속을 지원하는 고성능 아키텍처로 설계되었습니다.

## 🏗️ 시스템 아키텍처

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Web Client    │    │   Web Client    │    │   Web Client    │
│   (Socket.io)   │    │   (Socket.io)   │    │   (Socket.io)   │
└─────────┬───────┘    └─────────┬───────┘    └─────────┬───────┘
          │                      │                      │
          └──────────────────────┼──────────────────────┘
                                 │
                    ┌────────────▼────────────┐
                    │     Load Balancer       │
                    │     (Kubernetes)        │
                    └────────────┬────────────┘
                                 │
          ┌──────────────────────┼──────────────────────┐
          │                      │                      │
┌─────────▼───────┐    ┌─────────▼───────┐    ┌─────────▼───────┐
│  Spring Boot    │    │  Spring Boot    │    │  Spring Boot    │
│  + Socket.io    │    │  + Socket.io    │    │  + Socket.io    │
│    (Pod 1)      │    │    (Pod 2)      │    │    (Pod N)      │
└─────────┬───────┘    └─────────┬───────┘    └─────────┬───────┘
          │                      │                      │
          └──────────────────────┼──────────────────────┘
                                 │
                    ┌────────────▼────────────┐
                    │     Redis Pub/Sub       │
                    │   (Message Broker)      │
                    └─────────────────────────┘
                                 │
                    ┌────────────▼────────────┐
                    │    MongoDB Cluster      │
                    │   (Data Persistence)    │
                    └─────────────────────────┘
```

## ✨ 주요 기능

### 🔄 실시간 통신
- **Socket.io 기반 WebSocket 연결**
- **자동 재연결 및 폴백 지원**
- **실시간 메시지 송수신**
- **타이핑 상태 표시**
- **온라인 사용자 수 실시간 업데이트**

### 🎯 채팅 기능
- **다중 채팅방 지원**
- **사용자 상태 관리 (온라인/오프라인)**
- **메시지 히스토리 조회**
- **멘션 및 검색 기능**
- **파일 업로드 지원**
- **반응형 UI 및 스크롤 최적화**
- **부드러운 애니메이션 효과**

### 📊 확장성 & 성능
- **Redis Pub/Sub 메시지 브로커**
- **MongoDB 클러스터 데이터 저장**
- **Kubernetes 자동 확장 (HPA)**
- **100만 동접 지원 설계**
- **초당 10만 메시지 처리 목표**

### 🔐 보안 & 인증
- **JWT 토큰 기반 인증**
- **Socket.io 연결 인증**
- **Spring Security 통합**
- **CORS 설정 지원**

## 🛠️ 기술 스택

| 카테고리 | 기술 | 역할 |
|---------|------|------|
| **Backend** | Spring Boot 3.5.0 + Kotlin | 메인 애플리케이션 프레임워크 |
| **실시간 통신** | Socket.io 2.0.9 | WebSocket 기반 양방향 통신 |
| **메시지 브로커** | Redis 7.2 | Pub/Sub, 세션 관리, 캐싱 |
| **데이터베이스** | MongoDB 7.0 | 메시지, 사용자, 채팅방 데이터 |
| **컨테이너화** | Docker + Docker Compose | 개발/테스트 환경 |
| **오케스트레이션** | Kubernetes | 프로덕션 배포 및 확장 |
| **모니터링** | Prometheus + Grafana | 메트릭 수집 및 대시보드 |

## 🚀 빠른 시작

### 📋 사전 요구사항
- Java 17 이상
- Docker & Docker Compose
- Gradle 8.14 이상

### 🔧 개발 환경 설정

1. **저장소 클론**
```bash
git clone <repository-url>
cd chat
```

2. **Minikube 환경 배포 (로컬 개발용)**
```bash
# Minikube 시작
minikube start

# 전체 시스템 배포
./k8s/deploy-minikube.sh

# 포트 포워딩으로 접속
kubectl port-forward service/chat-app-service 8080:8080 9092:9092 -n chat-system
```

3. **개발 환경 실행 (Docker Compose)**
```bash
# MongoDB와 Redis 컨테이너 시작
./test-dev.sh

# Spring Boot 애플리케이션 실행 (개발 프로파일)
./gradlew bootRun --args='--spring.profiles.active=dev'
```

4. **웹 클라이언트 접속**
```
http://localhost:8080/index.html
```

### 🛑 간단한 종료 방법
```bash
# 전체 시스템 정리 (포트 포워딩 + Kubernetes 리소스)
./cleanup.sh

# 또는 수동으로
pkill -f "kubectl port-forward"
kubectl delete namespace chat-system
```

### 🐳 Docker Compose 사용법

```bash
# 전체 서비스 시작 (MongoDB + Redis + 관리 GUI)
docker compose up -d

# 개발용 서비스만 시작 (비밀번호 없는 설정)
docker compose -f docker-compose-dev.yml up -d

# 서비스 중지
docker compose down

# 데이터 볼륨까지 삭제
docker compose down -v
```

## 🌐 서비스 포트

| 서비스 | 포트 | 설명 |
|--------|------|------|
| Spring Boot | 8080 | 메인 애플리케이션 |
| Socket.io | 9092 | WebSocket 서버 |
| MongoDB | 27017 | 데이터베이스 |
| Redis | 6379 | 메시지 브로커 |
| MongoDB Express | 8081 | DB 관리 GUI |
| Redis Commander | 8082 | Redis 관리 GUI |

## 📁 프로젝트 구조

```
src/main/kotlin/kr/co/chat/
├── 📁 config/              # 설정 파일들
│   ├── RedisConfig.kt       # Redis 설정 (Pub/Sub, 직렬화)
│   ├── SocketIOConfig.kt    # Socket.io 서버 설정
│   └── SecurityConfig.kt    # Spring Security 설정
├── 📁 controller/           # REST API 컨트롤러
│   └── ChatController.kt    # 채팅 관련 REST API
├── 📁 domain/               # 도메인 모델
│   ├── 📁 model/            # 엔티티 클래스
│   │   ├── User.kt          # 사용자 모델
│   │   ├── ChatRoom.kt      # 채팅방 모델
│   │   └── Message.kt       # 메시지 모델
│   └── 📁 repository/       # 데이터 저장소 인터페이스
│       ├── UserRepository.kt
│       ├── ChatRoomRepository.kt
│       └── MessageRepository.kt
├── 📁 service/              # 비즈니스 로직
│   ├── ChatService.kt       # 채팅 핵심 로직
│   └── RedisMessageService.kt # Redis Pub/Sub 서비스
├── 📁 socket/               # Socket.io 관련
│   └── SocketIOEventHandler.kt # 이벤트 핸들러
└── ChatApplication.kt       # 메인 애플리케이션

k8s/                         # Kubernetes 배포 파일들
├── namespace.yaml           # 네임스페이스 정의
├── mongodb/                 # MongoDB 클러스터 설정
├── redis/                   # Redis 클러스터 설정
└── app/                     # 애플리케이션 배포 설정

docker-compose.yml           # 프로덕션용 Docker Compose
docker-compose-dev.yml       # 개발용 Docker Compose (간단한 설정)
cleanup.sh                   # 간단 정리 스크립트
```

## 🔧 주요 설정

### Socket.io 서버 설정 (100만 동접 최적화)
```kotlin
val config = Configuration().apply {
    hostname = "localhost"
    port = 9092
    bossThreads = 4              // I/O 처리 스레드
    workerThreads = 100          # 워커 스레드 (동접 처리)
    upgradeTimeout = 1000        # 업그레이드 타임아웃
    pingTimeout = 5000           # 핑 타임아웃
    pingInterval = 25000         # 핑 간격
}
```

### Redis Pub/Sub 채널 구조
- `chat:room:{roomId}` - 채팅방별 메시지 채널
- `user:status` - 사용자 상태 변경 채널
- `room:update` - 채팅방 업데이트 채널

## 🎨 UI/UX 개선사항

### 📱 반응형 스크롤 시스템
- **고정 높이 컨테이너**: 메시지가 많아져도 입력창이 항상 화면에 보임
- **부드러운 스크롤**: 새 메시지 추가 시 자연스러운 애니메이션
- **커스텀 스크롤바**: 세련된 스크롤바 디자인 적용
- **자동 스크롤**: 새 메시지 수신 시 자동으로 하단으로 스크롤

### 🎭 애니메이션 효과
- **메시지 페이드인**: 새 메시지에 0.3초 페이드인 효과
- **타이핑 인디케이터**: 사용자가 입력 중일 때 실시간 표시
- **호버 효과**: 버튼 및 인터랙티브 요소의 부드러운 호버 효과

### 🎯 사용자 경험 개선
- **입력창 고정**: 메시지 수량과 관계없이 항상 하단에 위치
- **레이아웃 안정성**: `flex-shrink: 0` 속성으로 레이아웃 깨짐 방지
- **모바일 최적화**: 터치 디바이스에서도 원활한 스크롤 경험

## 🚢 배포

### Kubernetes 배포

#### 로컬 개발 환경 (Minikube)
```bash
# Minikube 전용 배포 스크립트
./k8s/deploy-minikube.sh

# 포트 포워딩으로 접속
kubectl port-forward service/chat-app-service 8080:8080 9092:9092 -n chat-system

# 간단한 정리
./cleanup.sh
```

#### 프로덕션 환경
```bash
# 전체 시스템 배포
./deploy.sh

# 개별 컴포넌트 배포
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/mongodb/
kubectl apply -f k8s/redis/
kubectl apply -f k8s/app/
```

### 자동 확장 설정 (HPA)
- **최소 Pod 수**: 5개
- **최대 Pod 수**: 50개  
- **CPU 사용률 기준**: 70%
- **메모리 사용률 기준**: 80%

## 📊 성능 목표

| 지표 | 목표 값 |
|------|---------|
| **동시 접속자 수** | 1,000,000명 |
| **초당 메시지 처리** | 100,000개 |
| **평균 응답 시간** | < 50ms |
| **99%ile 응답 시간** | < 200ms |
| **시스템 가용성** | 99.9% |

## 🔍 모니터링

### Actuator Endpoints
- `/actuator/health` - 헬스체크
- `/actuator/metrics` - 애플리케이션 메트릭
- `/actuator/prometheus` - Prometheus 메트릭

### 주요 메트릭
- 온라인 사용자 수
- 실시간 메시지 처리량
- Socket.io 연결 수
- Redis 연결 상태
- MongoDB 응답 시간

## 🧪 테스트

### 단위 테스트 실행
```bash
./gradlew test
```

### 통합 테스트 실행
```bash
./gradlew integrationTest
```

### 부하 테스트
```bash
# Socket.io 연결 부하 테스트
artillery run load-test/socketio-load-test.yml
```

## 🛠️ 개발 도구

### 관리 인터페이스
- **MongoDB Express**: http://localhost:8081
- **Redis Commander**: http://localhost:8082

### API 문서
- **Swagger UI**: http://localhost:8080/swagger-ui.html

## 📝 API 문서

### REST API
- `GET /api/users` - 사용자 목록 조회
- `GET /api/rooms` - 채팅방 목록 조회
- `GET /api/rooms/{roomId}/messages` - 메시지 히스토리 조회
- `GET /api/stats` - 시스템 통계 조회

### Socket.io 이벤트
- `connect` - 연결 이벤트
- `join_room` - 채팅방 참여
- `leave_room` - 채팅방 나가기
- `send_message` - 메시지 전송
- `typing_start/stop` - 타이핑 상태
- `user_status` - 사용자 상태 변경

## 🤝 기여하기

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 라이센스

이 프로젝트는 MIT 라이센스 하에 배포됩니다. 자세한 내용은 `LICENSE` 파일을 참조하세요.

## 🙋‍♂️ 문의

프로젝트에 대한 질문이나 제안사항이 있으시면 이슈를 등록해 주세요.

---

**⚡ 100만 명이 동시에 채팅할 수 있는 시스템을 경험해보세요!** 