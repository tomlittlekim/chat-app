#!/bin/bash

# 색상 정의
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${BLUE}[INFO]${NC} 채팅 시스템 포트 포워딩을 시작합니다..."

# 네임스페이스 확인
if ! kubectl get namespace chat-system > /dev/null 2>&1; then
    echo -e "${YELLOW}[WARNING]${NC} chat-system 네임스페이스가 없습니다."
    echo -e "${YELLOW}[INFO]${NC} 먼저 배포를 실행하세요: ./k8s/deploy-minikube.sh"
    exit 1
fi

# 서비스 확인
if ! kubectl get service chat-app-service -n chat-system > /dev/null 2>&1; then
    echo -e "${YELLOW}[WARNING]${NC} chat-app-service가 없습니다."
    echo -e "${YELLOW}[INFO]${NC} 먼저 배포를 실행하세요: ./k8s/deploy-minikube.sh"
    exit 1
fi

echo -e "${GREEN}[SUCCESS]${NC} 포트 포워딩 시작!"
echo -e "${GREEN}[INFO]${NC} 접속 URL: http://localhost:8080"
echo -e "${GREEN}[INFO]${NC} 종료하려면 Ctrl+C를 누르세요"
echo ""

# 포트 포워딩 실행
kubectl port-forward service/chat-app-service 8080:8080 9092:9092 -n chat-system 