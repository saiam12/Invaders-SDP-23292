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

        # === Bullet split ===
        bullets = state_json.get("bullets", [])
        my_bullets = []
        enemy_bullets = []
        boss_bullets = []

        for bx, by, owner in bullets:
            if owner == 2:  # AI bullet
                my_bullets.append([bx/448, by/520])
            elif owner == 0:  # Enemy bullet
                enemy_bullets.append([bx/448, by/520])
            elif owner == -1:  # Boss bullet
                boss_bullets.append([bx/448, by/520])

        def pad(arr, size=10):
            flat = []
            for (x,y) in arr[:size]:
                flat += [x, y]
            if len(arr) < size:
                flat += [0] * ((size-len(arr))*2)
            return flat

        my_b   = pad(my_bullets)
        enemy  = pad(enemy_bullets)
        boss   = pad(boss_bullets)


        final_state = player_info + my_b + enemy + boss + enemies_flat

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
                    #print(f"🔥 [Action] Move: ({action_packet['moveX']}, {action_packet['moveY']}), Shoot: {action_packet['shoot']}, train {agent.train_count} ")
                    #print(f"train {agent.train_count}")
                    pass

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

    # ---- 0. Time penalty ----
    reward -= 0.05
    # ---- 1. corner penalty ----
    px = curr['playerX']
    py = curr['playerY']
    WIDTH = 448
    ITEMS_LINE = 400

    # ---- Extract needed info ----
    prev_hp = prev['playerHp']
    curr_hp = curr['playerHp']

    prev_enemies = prev.get('enemies', [])
    curr_enemies = curr.get('enemies', [])

    prev_items = prev.get('items', [])
    curr_items = curr.get('items', [])

    prev_boss = prev.get('boss')
    curr_boss = curr.get('boss')

    # ---- 1. corner penalty ----
    margin_x = 40
    margin_top = 180
    margin_bottom = ITEMS_LINE - 20

    if px < margin_x or px > (WIDTH - margin_x):
        reward -= 0.5

    if py < margin_top or py > margin_bottom:
        reward -= 0.5

    # ---- 2. distance penalty ----
    if curr_enemies:
        enemy_center_x = sum(e[0] for e in curr_enemies) / len(curr_enemies)
        dist = abs(px - enemy_center_x) / WIDTH  # 0~1
        reward -= dist * 0.05

    # ---- 3. Bullet avoidance penalty ----
    for bx, by, owner in curr.get("bullets", []):
        if owner in [0, -1]:  # enemy or boss bullet
            dist_b = abs(px - bx) + abs(py - by)

            if dist_b < 35:
                reward -= 1.0
            if dist_b < 20:
                reward -= 3.0

    # ---- 4. Score-based kill reward ----
    prev_score = prev.get('score', 0)
    curr_score = curr.get('score', 0)
    score_delta = curr_score - prev_score
    if score_delta > 0:
        if score_delta == 100:     # ufo return only 5 points
            reward += 5
        else:
            reward += score_delta

    # ---- 5. Boss damage reward ----
    if prev_boss and curr_boss:
        prev_boss_hp = prev_boss[2]
        curr_boss_hp = curr_boss[2]
        if curr_boss_hp < prev_boss_hp:
            reward += (prev_boss_hp - curr_boss_hp) * 0.5

    # ---- 6. Boss kill reward ----
    if prev_boss is not None and curr_boss is None:
        reward += 200

    # ---- 7. Player hit (bullet damage) ----
    if curr_hp < prev_hp:
        reward -= 30

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

    # ---- 9. Stage clear reward ----
    prev_enemies = prev.get("enemies", [])
    curr_enemies = curr.get("enemies", [])
    prev_boss = prev.get("boss")
    curr_boss = curr.get("boss")

    prev_clear = (len(prev_enemies) == 0 and prev_boss is None)
    curr_clear = (len(curr_enemies) == 0 and curr_boss is None)

    if (not prev_clear) and curr_clear:
        reward += 400
        print(f"[STAGE CLEAR] +400 reward")


    # ---- 10. Death penalty ----
    if curr_hp <= 0:
        reward -= 100
    if (abs(reward) > 2) or (abs(reward) < -2):
        print(f"[BIG REWARD] reward={reward}, score={curr_score}")

    return reward

if __name__ == "__main__":
    run_ai_controller()