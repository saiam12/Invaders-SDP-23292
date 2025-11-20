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

                # 3. Send action (POST)
                # action_packet : {"moveX": 1, "moveY": 0, "shoot": true}
                requests.post(f"{JAVA_SERVER_URL}/action", json=action_packet)

                # Print sent action (for debugging)
                # Only print when there is actual movement or shooting to reduce log noise.
                if action_packet["moveX"] != 0 or action_packet["moveY"] != 0 or action_packet["shoot"]:
                    print(f"🔥 [Action] Move: ({action_packet['moveX']}, {action_packet['moveY']}), Shoot: {action_packet['shoot']}")

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

if __name__ == "__main__":
    run_ai_controller()