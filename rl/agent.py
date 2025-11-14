import random
class Agent:
    def __init__(self):
        print("Agent 초기화 완료 (현재는 깡통 상태)")

    def get_action(self, state):
        action_packet = {
            "moveX": random.choice([-1, 0, 1]),
            "moveY": random.choice([-1, 0, 1]),
            "shoot": random.choice([True, False])
        }
        return action_packet