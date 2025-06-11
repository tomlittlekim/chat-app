#!/usr/bin/env python3
import json
from datetime import datetime

def analyze_test_results(file_path):
    # JSON 파일 읽기
    with open(file_path, 'r', encoding='utf-8') as f:
        data = json.load(f)

    aggregate = data['aggregate']
    counters = aggregate['counters']

    # 테스트 기간 계산
    start_time = aggregate['firstCounterAt'] / 1000
    end_time = aggregate['lastCounterAt'] / 1000
    duration = end_time - start_time

    print('🧪 대용량 트래픽 테스트 결과 분석')
    print('=' * 50)
    print(f'📅 테스트 일시: {datetime.fromtimestamp(start_time).strftime("%Y-%m-%d %H:%M:%S")}')
    print(f'⏱️ 테스트 기간: {duration:.1f}초 ({duration/60:.1f}분)')
    print()

    print('📊 주요 지표:')
    total_created = counters.get("vusers.created", 0)
    total_failed = counters.get("vusers.failed", 0)
    websocket_errors = counters.get("errors.Error: websocket error", 0)
    
    success_rate = ((total_created - total_failed) / total_created * 100) if total_created > 0 else 0
    
    print(f'• 총 생성된 가상 사용자: {total_created:,}명')
    print(f'• 성공한 연결: {total_created - total_failed:,}명 ({success_rate:.1f}%)')
    print(f'• 실패한 연결: {total_failed:,}명 ({(total_failed/total_created*100):.1f}%)')
    print(f'• WebSocket 에러: {websocket_errors:,}건')
    print()

    print('⚡ 성능 지표:')
    print(f'• 평균 초당 연결 시도: {total_created / duration:.1f} 연결/초')
    print(f'• 평균 초당 에러: {websocket_errors / duration:.1f} 에러/초')
    print()

    print('📈 시간대별 트래픽 분석:')
    intermediate = data.get('intermediate', [])
    if intermediate:
        for i, period in enumerate(intermediate[:10]):  # 처음 10개 구간만 표시
            period_counters = period['counters']
            period_created = period_counters.get('vusers.created', 0)
            period_failed = period_counters.get('vusers.failed', 0)
            print(f'  {i+1:2d}번째 10초: 시도 {period_created:3d}건, 실패 {period_failed:3d}건')
    print()

    print('🔍 문제점 분석:')
    if success_rate == 0:
        print('🔴 심각한 문제 발견:')
        print('• 성공률: 0% - 모든 연결이 실패')
        print('• 모든 WebSocket 연결이 실패하여 실제 채팅 테스트 불가')
        print('• 서버의 Socket.io 연결 처리에 문제가 있는 것으로 추정')
        print()
        print('🛠️ 해결 방안:')
        print('1. Socket.io 서버 설정 확인')
        print('2. CORS 설정 검토')
        print('3. 서버 로그 확인')
        print('4. 방화벽/포트 설정 검토')
        print('5. 서버의 WebSocket 핸들러 구현 확인')
    elif success_rate < 95:
        print(f'🟡 성능 개선 필요: 성공률 {success_rate:.1f}%')
        print('• 일부 연결이 실패하고 있어 시스템 최적화 필요')
    else:
        print(f'🟢 양호한 성능: 성공률 {success_rate:.1f}%')

if __name__ == "__main__":
    analyze_test_results("results/quick-20250610_145807.json") 