import random
class Agent:
    def __init__(self):
        print("Agent 초기화 완료 (현재는 깡통 상태)")

    def get_action(self, state):

        return random.randint(0, 5)
