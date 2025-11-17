import random
import torch
import torch.nn as nn
import torch.optim as optim
import random
import numpy as np

class DQN(nn.Module):
    def __init__(self, state_size):
        """
        API 규약(ActionPacket)에 맞춘 3-헤드(3-Head) DQN 신경망을 정의합니다.

        """
        super(DQN, self).__init__()

        # 공통 은닉층
        self.common_layers = nn.Sequential(
            nn.Linear(state_size, 128),
            nn.ReLU(),
            nn.Linear(128, 128),
            nn.ReLU()
        )

        # 1. moveX (좌/정지/우) 헤드
        self.moveX_head = nn.Sequential(
            nn.Linear(128, 32),
            nn.ReLU(),
            nn.Linear(32, 3)  # 3개의 행동 (좌, 정지, 우)
        )

        # 2. moveY (하/정지/상) 헤드
        self.moveY_head = nn.Sequential(
            nn.Linear(128, 32),
            nn.ReLU(),
            nn.Linear(32, 3)  # 3개의 행동 (하, 정지, 상)
        )

        # 3. shoot (No/Yes) 헤드
        self.shoot_head = nn.Sequential(
            nn.Linear(128, 32),
            nn.ReLU(),
            nn.Linear(32, 2)  # 2개의 행동 (공격 안함, 공격)
        )

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

    def _state_to_tensor(self, state):
        pass

    def get_action(self, state):
        pass

    def remember(self, state, action_packet, reward, next_state, is_game_over):
        pass

    def train_step(self):
        pass

    def update_target_model(self):
        pass

    def load_model(self, filepath='model.pth'):
        pass

    def save_model(self, filepath="model.pth"):
        pass