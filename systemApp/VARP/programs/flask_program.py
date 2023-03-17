from flask import Flask, jsonify, request
import screen_brightness_control as sbc
import socket

app = Flask(__name__)

connectionEstablished = False

def ipWrite():
    ip_address=""
    ip_addresses = socket.getaddrinfo(socket.gethostname(), None)
    for a in ip_addresses:
        if(a[4][0].startswith('192')):
            ip_address = a[4][0]
    host = "http://"+ip_address+":8001/"
    print(host)
    with open("connectorportal.txt", "w") as f:
        f.write(host)
    
@app.route('/', methods = ['GET'])
def test():
    cadd = ""
    with open("connecteddevices.txt", "r") as f:
        cadd = f.read()
    data=""
    if(len(cadd)>0):
        if(cadd != request.remote_addr):
            data = jsonify({"data":"Device is Connected to Another Remote"})
        else:
            data = jsonify({"data":"Already Connected"})
    else:
        data = jsonify({"data":"Test"})
    return data

@app.route('/', methods = ['POST'])
def light():
    connected = ""
    with open("connecteddevices.txt", "r") as f:
        connected = f.read()
    if(connected == request.remote_addr):
        data = request.json
        sbc.fade_brightness(int(data['luminosity']))
    else:
        data = jsonify({"data":"Request Denied"})
    return data

@app.route('/connect', methods = ['GET'])
def connect():
    global connectionEstablished
    data = request.remote_addr
    connected = ""
    with open("connecteddevices.txt", "r") as f:
        connected = f.read()
    if(len(connected)>0):
        connectionEstablished = True
    if(not connectionEstablished):
        with open("connecteddevices.txt", "w") as f:
            f.write(data)
        data = jsonify({"data":"Connected"})
        connectionEstablished = True
    elif(connected == data):
        data = jsonify({"data":"Already Connected"})
    else:
        data = jsonify({"data":"Device is Connected to Another Remote"})
    return data

@app.route('/disconnect', methods = ['GET'])
def disconnect():
    global connectionEstablished
    cadd = ""
    with open("connecteddevices.txt", "r") as f:
        cadd = f.read()
    data = ""
    if(cadd == request.remote_addr):
        with open("connecteddevices.txt", "w") as f:
            f.write(data)
        connectionEstablished = False
        data = jsonify({"data":"Disconnected"})
    else:
        data = jsonify({"data":"Request Denied"})
    return data
  
if __name__ == '__main__':
    ipWrite()
    app.run(host='0.0.0.0',port=8001,debug=False)
    
