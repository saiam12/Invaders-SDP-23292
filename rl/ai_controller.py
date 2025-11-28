import requests
import time
import numpy as np
from agent import Agent

# Java game server URL
JAVA_SERVER_URL = "http://localhost:8000"

# fix state size
STATE_SIZE = 120

def preprocess_state(state_json):
    """
    Converts the JSON state sent by Java into a 'fixed-size 1D array' that the AI can understand.
    """
    try:
        # 1. Extract fixed information (4 items)
        player_info = [
            float(state_json['playerX']) / 448.0, # Normalization
            float(state_json['playerY']) / 520.0,
            float(state_json['playerHp']) / 3.0,
            float(state_json['score']) / 10000.0
        ]

        # 2. Convert variable information (Enemies, Bullets, Items) -> Fixed size array (Padding/Truncating)
        MAX_ENEMIES = 10
        enemies_flat = []
        if 'enemies' in state_json:
            for enemy in state_json['enemies']:
                # enemy: [x, y, hp, type]
                # Normalize coordinates
                norm_enemy = [
                    float(enemy[0]) / 448.0,
                    float(enemy[1]) / 520.0,
                    float(enemy[2]) / 50.0,  # HP
                    float(enemy[3]) / 3.0    # Type
                ]
                enemies_flat.extend(norm_enemy)

        # Fill remaining space with 0s (Padding)
        target_len = MAX_ENEMIES * 4
        current_len = len(enemies_flat)
        if current_len < target_len:
            enemies_flat.extend([0] * (target_len - current_len))
        else:
            enemies_flat = enemies_flat[:target_len] # Truncate if overflows

        # 3. (Simple test purpose) Currently combining only player info + enemy info
        # (Later, add bullets and items in the same way as above)
        final_state = player_info + enemies_flat

        # If size is insufficient, fill the rest with 0s to match STATE_SIZE (Safety mechanism)
        if len(final_state) < STATE_SIZE:
            final_state.extend([0] * (STATE_SIZE - len(final_state)))

        return np.array(final_state[:STATE_SIZE]) # Cut to exactly STATE_SIZE length

    except Exception as e:
        print(f"Preprocessing error: {e}")
        return np.zeros(STATE_SIZE) # Return array filled with 0s on error

def run_ai_controller():
    print(f"AI Controller starting... (Attempting to connect to {JAVA_SERVER_URL})")

    agent = Agent(state_size=STATE_SIZE)

    # Variables for remembering the previous state
    prev_state = None
    prev_raw_state = None
    prev_action = None
    prev_score = 0
    prev_lives = 3

    while True:
        try:
            # 1. Request game state (GET)
            response = requests.get(f"{JAVA_SERVER_URL}/state")

            if response.status_code == 200:
                state_data = response.json()

                if not state_data:
                    continue

                # 2. Preprocess and determine action
                processed_state = preprocess_state(state_data)
                action_packet = agent.get_action(processed_state)

                curr_score = state_data['score']
                curr_lives = state_data['playerHp']
                done = (curr_lives <= 0)

                if prev_state is not None:
                    # (1) calc reward by compairing now-post
                    reward = calc_reward(prev_raw_state, state_data, prev_action)

                    # (2) Save memories: (Old state, old behavior, rewards, now state, game over?)
                    agent.append_sample(prev_state, prev_action, reward, processed_state, done)

                    # (3) train model
                    agent.train_model()


                # 3. Send action (POST)
                # action_packet : {"moveX": 1, "moveY": 0, "shoot": true}
                requests.post(f"{JAVA_SERVER_URL}/action", json=action_packet)

                # Update current status to 'previous status' (for next turn)
                prev_state = processed_state
                prev_raw_state = state_data
                prev_action = action_packet
                prev_score = curr_score
                prev_lives = curr_lives

                # save model
                if done:
                    agent.save_model(f"./save_model/episode_model.pth")

                if agent.train_count % 50000 == 0 and agent.train_count > 0:
                    agent.save_model(f"./save_model/model_{agent.train_count}.pth")



                # Print sent action (for debugging)
                # Only print when there is actual movement or shooting to reduce log noise.
                if action_packet["moveX"] != 0 or action_packet["moveY"] != 0 or action_packet["shoot"]:
                    print(f"🔥 [Action] Move: ({action_packet['moveX']}, {action_packet['moveY']}), Shoot: {action_packet['shoot']}, train {agent.train_count} ")

            elif response.status_code == 503:
                # Notify that the game is not currently active
                print("zzz... (Waiting for game to start)")
                time.sleep(1)

            else:
                print(f"⚠️ Abnormal game server response (Status: {response.status_code})")

        except requests.exceptions.ConnectionError:
            # Error occurring when Java game is closed
            print("⏳ Waiting for connection... (Please start the Java game)")
            time.sleep(2) # Wait 2 seconds and retry connection
        except Exception as e:
            print(f"❌ Error occurred: {e}")

        # Prevent too fast communication (Target approx 60 FPS)
        time.sleep(0.016)
def calc_reward(prev, curr, prev_action):
    reward = 0.0

    # ---- 1. Survival reward ----
    reward += 0.1

    # ---- 2. Shooting reward ----
    if prev_action and prev_action["shoot"]:
        reward += 0.03

    # ---- Extract needed info ----
    prev_hp = prev['playerHp']
    curr_hp = curr['playerHp']

    prev_enemies = prev.get('enemies', [])
    curr_enemies = curr.get('enemies', [])

    prev_items = prev.get('items', [])
    curr_items = curr.get('items', [])

    prev_boss = prev.get('boss')
    curr_boss = curr.get('boss')

    # Convert enemies to sets to detect killed enemies
    prev_enemy_set = {(e[0], e[1], e[3]) for e in prev_enemies}
    curr_enemy_set = {(e[0], e[1], e[3]) for e in curr_enemies}

    killed = prev_enemy_set - curr_enemy_set

    # ---- 3. Enemy kill rewards (type-specific) ----
    for (_, _, t) in killed:
        if t == 1:          # enemyA (small)
            reward += 10
        elif t == 2:        # enemyB (medium)
            reward += 25
        elif t == 3:        # enemyC (strong)
            reward += 40
        elif t == 0:        # UFO / special → low reward
            reward += 5

    # ---- 4. Boss damage reward ----
    if prev_boss and curr_boss:
        prev_boss_hp = prev_boss[2]
        curr_boss_hp = curr_boss[2]
        if curr_boss_hp < prev_boss_hp:
            reward += (prev_boss_hp - curr_boss_hp) * 0.5

    # ---- 5. Boss kill reward ----
    if prev_boss is not None and curr_boss is None:
        reward += 200

    # ---- 6. Player hit (bullet damage) ----
    if curr_hp < prev_hp:
        reward -= 30

    # ---- 7. Collision detection ----
    px, py = curr['playerX'], curr['playerY']
    for ex, ey, _, _ in curr_enemies:
        if abs(px - ex) < 20 and abs(py - ey) < 20:
            reward -= 50
            break

    # ---- 8. Item pickup (item disappears) ----
    prev_item_set = {(i[0], i[1], i[2]) for i in prev_items}
    curr_item_set = {(i[0], i[1], i[2]) for i in curr_items}

    picked = prev_item_set - curr_item_set

    for (_, _, t) in picked:
        if t == "HP":
            reward += 15
        elif t == "POWER":
            reward += 20
        elif t == "SPEED":
            reward += 10
        elif t == "BOMB":
            reward += 40

    # ---- 9. Death penalty ----
    if curr_hp <= 0:
        reward -= 100

    return reward

if __name__ == "__main__":
    run_ai_controller()