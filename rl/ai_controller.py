import requests
import time
import numpy as np
from agent import Agent

# Java 게임 서버 주소
JAVA_SERVER_URL = "http://localhost:8000"

# (중요!) AI에게 알려줄 상태(State)의 고정 크기
# 이 값은 preprocess_state 함수가 만드는 리스트의 길이와 같아야 합니다.
# 예시: 플레이어(4) + 적(12*4) + 총알(20*2) + 아이템(5*3) = 약 100~120개
STATE_SIZE = 120

def preprocess_state(state_json):
    """
    Java가 보낸 JSON 상태를 AI가 이해할 수 있는 '고정된 크기의 1차원 배열'로 변환합니다.
    """
    try:
        # 1. 고정 정보 추출 (4개)
        # (Java에서 좌표를 문자열로 보내는 경우를 대비해 float() 변환)
        player_info = [
            float(state_json['playerX']) / 448.0, # 정규화 (0~1 사이 값으로 변환 추천)
            float(state_json['playerY']) / 520.0,
            float(state_json['playerHp']) / 3.0,
            float(state_json['score']) / 10000.0
        ]

        # 2. 가변 정보 (적, 총알, 아이템) -> 고정 크기 배열로 변환 (Padding/Truncating)

        # 예: 적 정보 (최대 10마리, 각 4개 정보 [x, y, hp, type])
        MAX_ENEMIES = 10
        enemies_flat = []
        if 'enemies' in state_json:
            for enemy in state_json['enemies']:
                # enemy: [x, y, hp, type]
                # 좌표 정규화
                norm_enemy = [
                    float(enemy[0]) / 448.0,
                    float(enemy[1]) / 520.0,
                    float(enemy[2]) / 50.0,  # HP
                    float(enemy[3]) / 3.0    # Type
                ]
                enemies_flat.extend(norm_enemy)

        # 남는 공간 0으로 채우기 (Padding)
        target_len = MAX_ENEMIES * 4
        current_len = len(enemies_flat)
        if current_len < target_len:
            enemies_flat.extend([0] * (target_len - current_len))
        else:
            enemies_flat = enemies_flat[:target_len] # 넘치면 자르기

        # 3. (간단히 테스트용) 지금은 플레이어 정보 + 적 정보만 합쳐서 리턴
        # (나중에 총알, 아이템도 위와 같은 방식으로 추가하세요)
        final_state = player_info + enemies_flat

        # 크기가 부족하면 나머지를 0으로 채워서 STATE_SIZE 맞추기 (안전장치)
        if len(final_state) < STATE_SIZE:
            final_state.extend([0] * (STATE_SIZE - len(final_state)))

        return np.array(final_state[:STATE_SIZE]) # 정확히 STATE_SIZE 크기로 자름

    except Exception as e:
        print(f"전처리 오류: {e}")
        return np.zeros(STATE_SIZE) # 에러 나면 0으로 채운 배열 반환

def run_ai_controller():
    print(f"🤖 AI Controller 시작... ({JAVA_SERVER_URL} 연결 시도)")

    # ▼▼▼ [수정] state_size를 전달하여 에러 해결 ▼▼▼
    agent = Agent(state_size=STATE_SIZE)

    while True:
        try:
            # 1. 상태 요청
            response = requests.get(f"{JAVA_SERVER_URL}/state")

            if response.status_code == 200:
                state_data = response.json()

                if not state_data:
                    continue

                # 🟢 [추가] 상태를 잘 받았다고 출력 (너무 많으면 정신없으니 60번에 1번만 출력)
                # current_time = time.time()
                # if int(current_time * 60) % 60 == 0:
                #     print(f"✅ [Normal] Java로부터 상태 수신 완료 (Score: {state_data.get('score')})")

                # 2. 전처리 및 행동 결정
                processed_state = preprocess_state(state_data)
                action_packet = agent.get_action(processed_state)

                # 3. 행동 전송
                requests.post(f"{JAVA_SERVER_URL}/action", json=action_packet)

                # 🟢 [추가] 내가 보낸 행동을 출력 (디버깅용)
                # 움직임이 있을 때만 출력하면 더 보기 편합니다.
                if action_packet["moveX"] != 0 or action_packet["moveY"] != 0 or action_packet["shoot"]:
                    print(f"🚀 [Action] 이동: ({action_packet['moveX']}, {action_packet['moveY']}), 공격: {action_packet['shoot']}")

            elif response.status_code == 503:
                # 🟡 [추가] 게임 중이 아님을 알림 (1초에 한 번 정도만 출력하게 조절 가능)
                print("zzz... (게임 대기 중)")
                time.sleep(1)

            else:
                print(f"⚠️ 게임 서버 응답 이상 (Status: {response.status_code})")

        except requests.exceptions.ConnectionError:
            print("⏳ 게임 연결 대기 중... (Java 게임을 켜주세요)")
            time.sleep(2)
        except Exception as e:
            print(f"❌ 에러 발생: {e}")

        time.sleep(0.016)

if __name__ == "__main__":
    run_ai_controller()