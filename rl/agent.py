import torch
import torch.nn as nn
import torch.optim as optim
import torch.nn.functional as F
import random
import numpy as np
from collections import deque
import os

# --- define neural network ---
class DQN(nn.Module):
    def __init__(self, state_size):
        """
        Defines a 3-head DQN neural network compliant with the API specification (ActionPacket).

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

        # set target network update cycle
        self.target_update_frequency = 1000
        self.train_count = 0

        # generate (DQN + Target Network)
        self.model = DQN(state_size)
        self.target_model = DQN(state_size)
        self.target_model.load_state_dict(self.model.state_dict())

        # set optimizer, loss_function
        self.optimizer = optim.Adam(self.model.parameters(), lr=0.001)
        self.criterion = nn.MSELoss() #

        # set relay buffer
        self.memory = deque(maxlen=100000)

        # set epsilon
        self.epsilon = 1.0 # start with 100% discovery
        self.epsilon_min = 0.01 # at least 1% discovery
        self.epsilon_decay = 0.9995 # Reduce probability of discovery every time
        self.train_start = 1000 # Start training after 1000 samples

        print("reset Agent complete")

    def get_action(self, state):
        # 1. Explore random action
        if np.random.rand() <= self.epsilon:
            return {
                "moveX": random.choice([-1, 0, 1]),
                "moveY": random.choice([-1, 0, 1]),
                "shoot": random.choice([True, False])
            }
        # 2. Explore model prediction
        state_tensor = self._state_to_tensor(state)

        with torch.no_grad():
            q_x, q_y, q_s = self.model(state_tensor)

        # Select index with max Q-value from each head
        moveX_idx = torch.argmax(q_x).item() # 0, 1, 2
        moveY_idx = torch.argmax(q_y).item() # 0, 1, 2
        shoot_idx = torch.argmax(q_s).item() # 0, 1

        # Convert indices to ActionPacket values
        return {
            "moveX": moveX_idx - 1,
            "moveY": moveY_idx - 1,
            "shoot": bool(shoot_idx)
        }

    def append_sample(self, state, action_dict, reward, next_state, done):
        action = [
            action_dict["moveX"] + 1,
            action_dict["moveY"] + 1,
            int(action_dict["shoot"])
        ]
        self.memory.append((state, action, reward, next_state, done))

    def train_model(self):
        """ Samples from buffer and trains the network. """
        if len(self.memory) < self.train_start:
            return

        # Random sampling
        batch = random.sample(self.memory, self.batch_size)

        states      = torch.FloatTensor(np.array([x[0] for x in batch]))
        actions     = torch.LongTensor(np.array([x[1] for x in batch]))
        rewards     = torch.FloatTensor(np.array([x[2] for x in batch]))
        next_states = torch.FloatTensor(np.array([x[3] for x in batch]))
        dones       = torch.FloatTensor(np.array([float(x[4]) for x in batch]))

        # Calculate Current Q (Prediction)
        curr_q_x, curr_q_y, curr_q_s = self.model(states)

        # Gather Q-values for the specific actions taken
        q_x = curr_q_x.gather(1, actions[:, 0].unsqueeze(1)).squeeze(1)
        q_y = curr_q_y.gather(1, actions[:, 1].unsqueeze(1)).squeeze(1)
        q_s = curr_q_s.gather(1, actions[:, 2].unsqueeze(1)).squeeze(1)

        # Calculate Target Q (Label)
        with torch.no_grad():
            next_q_x, next_q_y, next_q_s = self.target_model(next_states)

        max_next_x = next_q_x.max(1)[0]
        max_next_y = next_q_y.max(1)[0]
        max_next_s = next_q_s.max(1)[0]

        # Average next Q-values for simplified target
        max_next_avg = (max_next_x + max_next_y + max_next_s) / 3.0

        target_q = rewards + (1 - dones) * self.gamma * max_next_avg

        # Loss & Optimize
        curr_q_avg = (q_x + q_y + q_s) / 3.0
        loss = self.criterion(curr_q_avg, target_q)

        self.optimizer.zero_grad()
        loss.backward()
        self.optimizer.step()

        # Update Target Model periodically(every 50 times)
        self.train_count += 1
        if self.train_count % self.target_update_frequency == 0:
            self.update_target_model()
            print(f"Target model updated (Train count: {self.train_count})")

        # Decay Epsilon
        if self.epsilon > self.epsilon_min:
            self.epsilon *= self.epsilon_decay

    def _state_to_tensor(self, state):
        state_tensor = torch.FloatTensor(state)
        return state_tensor.unsqueeze(0)

    def remember(self, *args):
        self.append_sample(*args)

    def train_step(self):
        self.train_model()

    def update_target_model(self):
        new_state = self.model.state_dict()
        self.target_model.load_state_dict(new_state)

    def load_model(self, filepath='model.pth'):
        if os.path.exists(filepath):
            self.model.load_state_dict(torch.load(filepath))
            self.update_target_model()
            self.epsilon = self.epsilon_min # 학습된 모델 사용 시 탐험 최소화
            print(f"Model loaded from {filepath}")
        else:
            print("Saved model not found")

    def save_model(self, filepath="model.pth"):
        if not os.path.exists("./save_model"):
            os.makedirs("./save_model")
        torch.save(self.model.state_dict(), filepath)
        print(f"Model saved to {filepath}")