#!/bin/bash

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 로그 함수
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 에러 발생 시 스크립트 종료
set -e

log_info "Minikube에서 실시간 채팅 시스템 배포를 시작합니다..."

# Minikube 클러스터 상태 확인
log_info "Minikube 클러스터 상태를 확인합니다..."
if ! minikube status > /dev/null 2>&1; then
    log_error "Minikube 클러스터가 실행되지 않고 있습니다. 'minikube start'를 실행해주세요."
    exit 1
fi
log_success "Minikube 클러스터가 정상 작동 중입니다."

# Docker 환경 설정
log_info "Minikube Docker 환경을 설정합니다..."
eval $(minikube docker-env)

# Docker 이미지 빌드 (이미 빌드되어 있으면 스킵)
if ! docker images | grep -q "chat-system.*latest"; then
    log_info "Docker 이미지를 빌드합니다..."
    docker build -t chat-system:latest .
    log_success "Docker 이미지 빌드 완료"
else
    log_info "Docker 이미지가 이미 존재합니다. 빌드를 스킵합니다."
fi

# Kubernetes 네임스페이스 생성
log_info "Kubernetes 네임스페이스를 생성합니다..."
kubectl apply -f k8s/namespace.yaml
log_success "네임스페이스 생성 완료"

# MongoDB 배포
log_info "MongoDB 클러스터를 배포합니다..."
kubectl apply -f k8s/mongodb.yaml
log_success "MongoDB 배포 완료"

# Redis 배포
log_info "Redis 클러스터를 배포합니다..."
kubectl apply -f k8s/redis.yaml
log_success "Redis 배포 완료"

# MongoDB와 Redis가 준비될 때까지 대기
log_info "데이터베이스 서비스가 준비될 때까지 대기합니다..."
log_info "MongoDB 대기 중..."
kubectl wait --for=condition=ready pod -l app=mongodb -n chat-system --timeout=300s

log_info "Redis 대기 중..."
kubectl wait --for=condition=ready pod -l app=redis -n chat-system --timeout=300s
log_success "데이터베이스 서비스 준비 완료"

# 채팅 애플리케이션 배포
log_info "채팅 애플리케이션을 배포합니다..."
kubectl apply -f k8s/chat-app.yaml
log_success "채팅 애플리케이션 배포 완료"

# 애플리케이션이 준비될 때까지 대기
log_info "애플리케이션이 준비될 때까지 대기합니다..."
kubectl wait --for=condition=ready pod -l app=chat-app -n chat-system --timeout=300s
log_success "애플리케이션 준비 완료"

# 배포 상태 확인
log_info "배포 상태를 확인합니다..."
echo ""
echo "=== Pods 상태 ==="
kubectl get pods -n chat-system

echo ""
echo "=== Services 상태 ==="
kubectl get services -n chat-system

echo ""
echo "=== HPA 상태 ==="
kubectl get hpa -n chat-system

# Minikube 서비스 URL 출력
echo ""
log_info "Minikube 서비스 접속 정보:"

# minikube service로 URL 가져오기
log_info "서비스 URL을 확인합니다..."
echo ""
echo "HTTP API 서비스:"
minikube service chat-app-loadbalancer -n chat-system --url=true | head -1

echo ""
echo "Socket.IO 서비스:"
minikube service chat-app-loadbalancer -n chat-system --url=true | tail -1

echo ""
log_info "브라우저에서 서비스 열기:"
echo "HTTP API: minikube service chat-app-loadbalancer -n chat-system"
echo "또는"
echo "minikube service chat-app-service -n chat-system (내부 서비스)"

echo ""
log_success "실시간 채팅 시스템 배포가 완료되었습니다!"

# 모니터링 정보
echo ""
log_info "모니터링 엔드포인트:"
echo "Health Check: /actuator/health"
echo "Metrics: /actuator/metrics"
echo "Prometheus: /actuator/prometheus"

# 유용한 명령어들
echo ""
log_info "유용한 명령어들:"
echo "로그 확인: kubectl logs -f deployment/chat-app -n chat-system"
echo "Pod 상태 확인: kubectl get pods -n chat-system -w"
echo "HPA 상태 확인: kubectl get hpa -n chat-system -w"
echo "서비스 URL: minikube service chat-app-loadbalancer -n chat-system --url"
echo "서비스 접속: minikube service chat-app-loadbalancer -n chat-system"
echo "포트 포워딩: kubectl port-forward service/chat-app-service 8080:8080 9092:9092 -n chat-system"
echo "터널 시작: minikube tunnel (별도 터미널에서)"
echo "클러스터 대시보드: minikube dashboard"
echo "서비스 삭제: kubectl delete namespace chat-system" 