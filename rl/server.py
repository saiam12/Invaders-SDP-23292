from flask import Flask, request, jsonify
from agent import Agent # agent.py 에서 가져옴


app = Flask(__name__)
agent = Agent()

@app.route("/get_action", methods=['POST'])
def get_action():
    data = request.json # java로부터 json(state) 데이터를 받음
    state = data.get('state')

    action = agent.get_action(state)

    return jsonify({
        "action": action # 결정된 행동을 json 으로 반환
    })

def run_server():
    app.run(host="0.0.0.0", port=5000)