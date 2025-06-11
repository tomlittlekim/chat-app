#!/bin/bash

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🚀 채팅 시스템 대용량 트래픽 테스트 시작${NC}"
echo "=================================================="

# 함수 정의
check_server() {
    echo -e "${YELLOW}📡 서버 상태 확인 중...${NC}"
    
    # Spring Boot 서버 확인
    if curl -f -s http://localhost:8080/actuator/health > /dev/null; then
        echo -e "${GREEN}✅ Spring Boot 서버 정상 (8080)${NC}"
    else
        echo -e "${RED}❌ Spring Boot 서버 연결 실패 (8080)${NC}"
        echo "서버를 먼저 시작해주세요: ./gradlew bootRun"
        exit 1
    fi
    
    # Socket.io 서버 확인
    if nc -z localhost 9092; then
        echo -e "${GREEN}✅ Socket.io 서버 정상 (9092)${NC}"
    else
        echo -e "${RED}❌ Socket.io 서버 연결 실패 (9092)${NC}"
        echo "Socket.io 서버를 확인해주세요"
        exit 1
    fi
    
    # Redis 서버 확인
    if nc -z localhost 6379; then
        echo -e "${GREEN}✅ Redis 서버 정상 (6379)${NC}"
    else
        echo -e "${RED}❌ Redis 서버 연결 실패 (6379)${NC}"
        echo "Redis 서버를 시작해주세요: docker-compose up -d redis"
        exit 1
    fi
    
    # MongoDB 서버 확인
    if nc -z localhost 27017; then
        echo -e "${GREEN}✅ MongoDB 서버 정상 (27017)${NC}"
    else
        echo -e "${RED}❌ MongoDB 서버 연결 실패 (27017)${NC}"
        echo "MongoDB 서버를 시작해주세요: docker-compose up -d mongodb"
        exit 1
    fi
    
    echo ""
}

install_artillery() {
    echo -e "${YELLOW}🔧 Artillery 확인 중...${NC}"
    
    # npx를 사용해서 Artillery 실행 가능한지 확인
    if npx artillery@latest --version > /dev/null 2>&1; then
        echo -e "${GREEN}✅ Artillery 실행 가능 (npx 사용)${NC}"
        npx artillery@latest --version | head -1
    else
        echo -e "${RED}❌ Artillery 실행 불가${NC}"
        echo "Node.js와 npm이 설치되어 있는지 확인해주세요"
        exit 1
    fi
    echo ""
}

run_test() {
    local test_type=$1
    local config_file=$2
    local description=$3
    local max_timeout=$4  # 새로운 매개변수: 최대 실행 시간(초)
    
    # 기본 타임아웃 설정 (설정되지 않은 경우)
    if [ -z "$max_timeout" ]; then
        case "$test_type" in
            "quick") max_timeout=400 ;;      # 빠른 테스트: 6분 40초
            "load") max_timeout=4500 ;;      # 표준 부하: 75분
            "stress") max_timeout=2000 ;;    # 스트레스: 33분
            *) max_timeout=600 ;;            # 기본: 10분
        esac
    fi
    
    echo -e "${BLUE}🧪 ${description} 시작...${NC}"
    echo "설정 파일: ${config_file}"
    echo "최대 실행 시간: ${max_timeout}초 ($(($max_timeout / 60))분)"
    echo "시작 시간: $(date)"
    echo ""
    
    # 결과 디렉토리 생성
    mkdir -p results
    
    # 결과 파일명 생성
    local output_file="results/${test_type}-$(date +%Y%m%d_%H%M%S).json"
    
    # 타임아웃과 함께 테스트 실행
    echo -e "${YELLOW}⏱️  타임아웃 모니터링 활성화 (최대 ${max_timeout}초)${NC}"
    
    if timeout "$max_timeout" npx artillery@latest run "${config_file}" \
        --output "$output_file" \
        --overrides '{"config":{"plugins":{"expect":{}}}}'; then
        
        echo -e "${GREEN}✅ ${description} 정상 완료${NC}"
        local exit_code=0
    else
        local exit_code=$?
        if [ $exit_code -eq 124 ]; then
            echo -e "${RED}⏰ ${description} 타임아웃 발생 (${max_timeout}초 초과)${NC}"
            echo -e "${YELLOW}💡 결과 파일이 부분적으로 생성되었을 수 있습니다: ${output_file}${NC}"
        else
            echo -e "${RED}❌ ${description} 실행 중 에러 발생 (종료 코드: $exit_code)${NC}"
        fi
    fi
    
    echo "종료 시간: $(date)"
    echo ""
    
    # 결과 파일 크기 확인
    if [ -f "$output_file" ]; then
        local file_size=$(du -h "$output_file" | cut -f1)
        echo -e "${GREEN}📄 결과 파일 생성됨: ${output_file} (${file_size})${NC}"
    else
        echo -e "${YELLOW}⚠️  결과 파일이 생성되지 않았습니다${NC}"
    fi
    
    return $exit_code
}

show_menu() {
    echo -e "${YELLOW}테스트 유형을 선택하세요:${NC}"
    echo "1) 빠른 테스트 (5분, ~100 동접)"
    echo "2) 표준 부하 테스트 (1시간, ~50000 동접)"
    echo "3) 스트레스 테스트 (30분, ~50000+ 동접)"
    echo "4) 사용자 정의 테스트"
    echo "5) 모든 테스트 순차 실행"
    echo "0) 종료"
    echo ""
    read -p "선택 (0-5): " choice
}

run_custom_test() {
    echo -e "${YELLOW}사용자 정의 테스트 설정:${NC}"
    
    read -p "최대 동접자 수: " max_users
    read -p "테스트 시간(분): " duration_min
    read -p "채팅방 개수: " room_count
    
    # 임시 설정 파일 생성
    cat > custom-test.yml << EOF
config:
  target: 'http://localhost:9092'
  phases:
    - duration: $((duration_min * 60))
      arrivalRate: 1
      rampTo: $((max_users / 10))
    - duration: $((duration_min * 60))
      arrivalRate: $((max_users / 10))

  socketio:
    transports: ['websocket']

scenarios:
  - name: "사용자 정의 테스트"
    engine: socketio
    flow:
      - emit:
          channel: "connect"
          data:
            userId: "custom_{{ \$randomInt(1, ${max_users}) }}"
            nickname: "CustomUser_{{ \$randomInt(1, ${max_users}) }}"
      
      - emit:
          channel: "join_room"
          data:
            roomId: "custom_room_{{ \$randomInt(1, ${room_count}) }}"
            userId: "{{ userId }}"
      
      - loop:
          - emit:
              channel: "send_message"
              data:
                roomId: "custom_room_{{ \$randomInt(1, ${room_count}) }}"
                message: "사용자 정의 메시지 {{ \$randomInt(1, 10000) }}"
                userId: "{{ userId }}"
          - think: {{ \$randomInt(1, 10) }}
        count: 10
      
      - emit:
          channel: "disconnect"
EOF

    run_test "custom" "custom-test.yml" "사용자 정의 테스트 (${max_users}명, ${duration_min}분)"
    rm -f custom-test.yml
}

# 메인 실행 부분
check_server
install_artillery

while true; do
    show_menu
    
    case $choice in
        1)
            run_test "quick" "quick-test.yml" "빠른 테스트"
            ;;
        2)
            run_test "load" "artillery-config.yml" "표준 부하 테스트"
            ;;
        3)
            run_test "stress" "stress-test.yml" "스트레스 테스트"
            ;;
        4)
            run_custom_test
            ;;
        5)
            echo -e "${BLUE}🔄 모든 테스트 순차 실행...${NC}"
            run_test "quick" "quick-test.yml" "빠른 테스트"
            sleep 30
            run_test "load" "artillery-config.yml" "표준 부하 테스트"
            sleep 60
            run_test "stress" "stress-test.yml" "스트레스 테스트"
            echo -e "${GREEN}🎉 모든 테스트 완료!${NC}"
            ;;
        0)
            echo -e "${GREEN}👋 테스트 종료${NC}"
            exit 0
            ;;
        *)
            echo -e "${RED}❌ 잘못된 선택입니다${NC}"
            ;;
    esac
    
    echo ""
    read -p "다른 테스트를 실행하시겠습니까? (y/n): " continue_test
    if [[ $continue_test != "y" && $continue_test != "Y" ]]; then
        break
    fi
    echo ""
done

echo -e "${GREEN}🎉 테스트 완료! 결과는 results/ 디렉토리에서 확인하세요${NC}" 