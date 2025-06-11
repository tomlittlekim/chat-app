#!/bin/bash

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}[CLEANUP]${NC} 채팅 시스템을 정리합니다..."

# 포트 포워딩 프로세스 종료
echo -e "${YELLOW}[CLEANUP]${NC} 포트 포워딩 프로세스를 종료합니다..."
pkill -f "kubectl port-forward" 2>/dev/null || true

# 네임스페이스 삭제
echo -e "${YELLOW}[CLEANUP]${NC} chat-system 네임스페이스를 삭제합니다..."
kubectl delete namespace chat-system 2>/dev/null || true

echo -e "${GREEN}[SUCCESS]${NC} 정리 완료!"
echo -e "${GREEN}[INFO]${NC} 다시 배포하려면: ./k8s/deploy-minikube.sh" 