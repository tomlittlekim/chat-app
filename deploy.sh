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

log_info "실시간 채팅 시스템 배포를 시작합니다..."

# Docker 이미지 빌드
log_info "Docker 이미지를 빌드합니다..."
docker build -t chat-system:latest .
log_success "Docker 이미지 빌드 완료"

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
kubectl wait --for=condition=ready pod -l app=mongodb -n chat-system --timeout=300s
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

# 서비스 URL 출력
echo ""
log_info "서비스 접속 정보:"
LOADBALANCER_IP=$(kubectl get service chat-app-loadbalancer -n chat-system -o jsonpath='{.status.loadBalancer.ingress[0].ip}' 2>/dev/null || echo "Pending...")

if [ "$LOADBALANCER_IP" != "Pending..." ] && [ -n "$LOADBALANCER_IP" ]; then
    echo "HTTP API: http://$LOADBALANCER_IP"
    echo "Socket.IO: http://$LOADBALANCER_IP:9092"
else
    log_warning "LoadBalancer IP가 아직 할당되지 않았습니다. 다음 명령어로 확인하세요:"
    echo "kubectl get service chat-app-loadbalancer -n chat-system"
    
    # 포트 포워딩 옵션 제공
    log_info "로컬 테스트를 위한 포트 포워딩:"
    echo "kubectl port-forward service/chat-app-service 8080:8080 9092:9092 -n chat-system"
fi

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
echo "서비스 삭제: kubectl delete namespace chat-system" 