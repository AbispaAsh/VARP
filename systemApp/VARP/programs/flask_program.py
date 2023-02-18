from flask import Flask, jsonify, request
import screen_brightness_control as sbc
import socket

app = Flask(__name__)

def ipWrite():
    ip_address = socket.gethostbyname(socket.gethostname())
    host = "http://"+ip_address+":8001/"
    with open("connectorportal.txt", "w") as f:
        f.write(host)
    
@app.route('/', methods = ['GET'])
def test():
    data = jsonify({"data":"Test"})
    #sbc.set_brightness(int(data['luminosity']))
    return data

@app.route('/', methods = ['POST'])
def light():
    data = request.json
    sbc.fade_brightness(int(data['luminosity']))
    return data

@app.route('/connect', methods = ['GET'])
def connect():
    data = request.remote_addr
    with open("connecteddevices.txt", "w") as f:
        f.write(data)
    data = jsonify({"data":"Connected"})
    return data

@app.route('/disconnect', methods = ['GET'])
def disconnect():
    data = ""
    with open("connecteddevices.txt", "w") as f:
        f.write(data)
    data = jsonify({"data":"Disconnected"})
    return data
  
if __name__ == '__main__':
    ipWrite()
    app.run(host='0.0.0.0',port=8001,debug=False)
    
