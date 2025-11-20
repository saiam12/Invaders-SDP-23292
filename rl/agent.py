import random
import torch
import torch.nn as nn
import torch.optim as optim
import random
import numpy as np
from collections import deque

# --- define neural network ---
class DQN(nn.Module):
    def __init__(self, state_size):
        """
        API 규약(ActionPacket)에 맞춘 3-헤드(3-Head) DQN 신경망을 정의합니다.

        """
        super(DQN, self).__init__()

        # common hidded layer
        self.common_layers = nn.Sequential(
            nn.Linear(state_size, 128),
            nn.ReLU(),
            nn.Linear(128, 128),
            nn.ReLU()
        )

        # 1. moveX (left/stop/right)
        self.moveX_head = nn.Sequential(
            nn.Linear(128, 32),
            nn.ReLU(),
            nn.Linear(32, 3)  # 3개의 행동 (좌, 정지, 우)
        )

        # 2. moveY (up/stop/down)
        self.moveY_head = nn.Sequential(
            nn.Linear(128, 32),
            nn.ReLU(),
            nn.Linear(32, 3)  # 3개의 행동 (하, 정지, 상)
        )

        # 3. shoot (No/Yes)
        self.shoot_head = nn.Sequential(
            nn.Linear(128, 32),
            nn.ReLU(),
            nn.Linear(32, 2)  # 2개의 행동 (공격 안함, 공격)
        )

    def forward(self, x):
        common = self.common_layers(x)

        moveX_q = self.moveX_head(common)
        moveY_q = self.moveY_head(common)
        shoot_q = self.shoot_head(common)

        # return 3 Q-values
        return moveX_q, moveY_q, shoot_q

# --- Agent Class ---
class Agent:
    def __init__(self, state_size, batch_size=32, gamma=0.99):
        print('reset Agent ...')
        self.state_size = state_size
        self.batch_size = batch_size
        self.gamma = gamma

        # generate (DQN + Target Network)
        self.model = DQN(state_size)
        self.target_model = DQN(state_size)
        self.target_model.load_state_dict(self.model.state_dict())

        self.optimizer = optim.Adam(self.model.parameters(), lr=0.001)

        # set relay buffer
        self.memory = deque(maxlen=100000)

        # set epsilon
        self.epsilon = 1.0 # start with 100% discovery
        self.epsilon_min = 0.01 # at least 1% discovery
        self.epsilon_decay = 0.9995 # Reduce probability of discovery every time

        print("reset Agent complete")

    def get_action(self, state): # 나중에 바꿀 임시 코드
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
        state_tensor = torch.FloatTensor(state)
        return state_tensor.unsqueeze(0)

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