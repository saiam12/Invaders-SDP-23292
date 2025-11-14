import random
class Agent:
    def __init__(self):
        print("Agent 초기화 완료 (현재는 깡통 상태)")

    def get_action(self, state):
        """
        [ActionPacket 규약]
        - moveX: -1 (좌), 0 (정지), 1 (우)
        - moveY: -1 (하), 0 (정지), 1 (상)
        - shoot: true (공격), false (공격 안함)
        """
        action_packet = {
            "moveX": random.choice([-1, 0, 1]),
            "moveY": random.choice([-1, 0, 1]),
            "shoot": random.choice([True, False])
        }

        move_x = random.choice([-1, 0, 1])

        # moveY 값 무작위 선택
        move_y = random.choice([-1, 0, 1])

        # shoot 값 무작위 선택
        shoot = random.choice([True, False])

        # API 규약에 맞는 딕셔너리(JSON 객체)로 반환
        action_packet = {
            "moveX": move_x,
            "moveY": move_y,
            "shoot": shoot
        }
        return action_packet