# 🧪 대용량 트래픽 테스트 가이드

이 디렉토리는 채팅 시스템의 대용량 트래픽 테스트를 위한 설정과 도구들을 포함합니다.

## 📋 테스트 도구

### Artillery.io
Socket.io WebSocket 연결 테스트에 최적화된 부하 테스트 도구입니다.

## 🚀 빠른 시작

### 1. 필수 조건 확인
```bash
# 모든 서비스가 실행 중인지 확인
docker-compose up -d        # MongoDB, Redis 시작
./gradlew bootRun          # Spring Boot 애플리케이션 시작
```

### 2. 테스트 실행
```bash
cd load-test
./run-tests.sh
```

## 📁 파일 구조

| 파일 | 설명 |
|------|------|
| `run-tests.sh` | **메인 테스트 실행 스크립트** |
| `quick-test.yml` | 빠른 테스트 (5분, ~100 동접) |
| `artillery-config.yml` | 표준 부하 테스트 (1시간, ~50000 동접) |
| `stress-test.yml` | 스트레스 테스트 (30분, 극한 부하) |
| `package.json` | NPM 설정 및 스크립트 |

## 🎯 테스트 유형별 가이드

### 1️⃣ 빠른 테스트 (추천 시작점)
```bash
# 직접 실행
artillery run quick-test.yml

# 또는 스크립트 사용
./run-tests.sh
# 메뉴에서 1번 선택
```
- **시간**: 5분
- **최대 동접**: ~100명
- **목적**: 기본 기능 검증

### 2️⃣ 표준 부하 테스트
```bash
artillery run artillery-config.yml
```
- **시간**: 1시간
- **최대 동접**: ~50,000명
- **목적**: 실제 운영 환경 시뮬레이션

### 3️⃣ 스트레스 테스트
```bash
artillery run stress-test.yml
```
- **시간**: 30분
- **최대 동접**: 50,000명 이상
- **목적**: 시스템 한계점 찾기

### 4️⃣ 사용자 정의 테스트
스크립트에서 4번을 선택하면 원하는 조건으로 테스트 가능:
- 동접자 수
- 테스트 시간
- 채팅방 개수

## 📊 모니터링 방법

### 실시간 모니터링
테스트 중 다음 엔드포인트들을 확인하세요:

```bash
# 시스템 상태
curl http://localhost:8080/actuator/health

# 메트릭 확인
curl http://localhost:8080/actuator/metrics

# 프로메테우스 메트릭
curl http://localhost:8080/actuator/prometheus
```

### 관리 도구
- **MongoDB Express**: http://localhost:8081
- **Redis Commander**: http://localhost:8082

### 시스템 리소스 모니터링
```bash
# CPU, 메모리 사용률
htop

# 네트워크 연결 상태
netstat -an | grep :9092 | wc -l  # Socket.io 연결 수

# Docker 컨테이너 리소스
docker stats
```

## 📈 결과 분석

### Artillery 출력 해석
```
All VUs finished. Total time: 5 minutes
Summary report:
  Scenarios launched:  1000
  Scenarios completed: 985
  Requests completed:  98500
  Mean response/sec:   330.33
  Response time (ms):
    min: 15
    max: 1205
    median: 45
    p95: 180
    p99: 450
```

**주요 지표 설명:**
- **Scenarios completed**: 성공한 사용자 세션 비율 (98.5% = 양호)
- **Mean response/sec**: 초당 처리량 (330/sec)
- **Response time p95**: 95% 사용자가 경험한 최대 응답시간 (180ms)
- **Response time p99**: 99% 사용자가 경험한 최대 응답시간 (450ms)

### 성능 기준
| 지표 | 양호 | 보통 | 위험 |
|------|------|------|------|
| **성공률** | >95% | 90-95% | <90% |
| **평균 응답시간** | <50ms | 50-200ms | >200ms |
| **p99 응답시간** | <200ms | 200-500ms | >500ms |
| **에러율** | <1% | 1-5% | >5% |

## 🔧 고급 설정

### 환경별 타겟 변경
```bash
# 프로덕션 환경 테스트
artillery run artillery-config.yml --environment prod

# 사용자 정의 타겟
artillery run artillery-config.yml \
  --overrides '{"config":{"target":"http://production-server:9092"}}'
```

### 결과 리포트 생성
```bash
# JSON 리포트 저장
artillery run artillery-config.yml --output test-results.json

# HTML 리포트 생성
artillery report test-results.json --output test-report.html
```

### 분산 테스트 (여러 머신 사용)
```bash
# 머신 1
artillery run artillery-config.yml --count 1 --machine-id 1

# 머신 2  
artillery run artillery-config.yml --count 1 --machine-id 2
```

## ⚠️ 주의사항

### 테스트 전 체크리스트
- [ ] 모든 서비스 (Spring Boot, Redis, MongoDB) 실행 확인
- [ ] 충분한 시스템 리소스 (CPU, 메모리, 네트워크) 확보
- [ ] 로그 레벨 조정 (INFO 이상으로 설정)
- [ ] 테스트용 DB 사용 (운영 DB 보호)

### 시스템 최적화
```bash
# 파일 디스크립터 한계 증가
ulimit -n 65536

# TCP 연결 최적화
echo 'net.core.somaxconn=65536' >> /etc/sysctl.conf
echo 'net.ipv4.tcp_max_syn_backlog=65536' >> /etc/sysctl.conf
sysctl -p
```

### 문제 해결

#### "연결 거부" 에러
```bash
# 포트 사용 확인
netstat -tulpn | grep :9092

# 서버 로그 확인
docker-compose logs app
```

#### "타임아웃" 에러
- Artillery 설정에서 `timeout` 값 증가
- 서버의 `pingTimeout`, `pingInterval` 조정

#### "메모리 부족" 에러
- Docker 메모리 제한 증가
- JVM 힙 크기 조정: `-Xmx4g`

## 🎯 100만 동접 도전

### 단계별 접근
1. **1,000명** → 기본 기능 검증
2. **10,000명** → 첫 번째 병목 지점 발견
3. **100,000명** → 시스템 최적화 적용
4. **1,000,000명** → 클러스터 확장 및 최종 목표

### 클러스터 환경에서 테스트
```bash
# Kubernetes 환경
kubectl apply -f ../k8s/

# 여러 클라이언트에서 분산 테스트
for i in {1..10}; do
  artillery run artillery-config.yml --count 1 &
done
```

## 📞 문의 및 지원

테스트 중 문제가 발생하면:
1. `results/` 디렉토리의 로그 파일 확인
2. 서버 로그 검토
3. 시스템 리소스 모니터링

---

**🚀 성공적인 대용량 트래픽 테스트를 위해 차근차근 진행하세요!** 