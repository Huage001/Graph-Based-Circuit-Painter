import sys
import numpy as np
import matplotlib.pyplot as plt
import torch
from PIL import Image
from model import Net
import socket
import _thread
import time


def main(argv=None):
    x=[]
    y=[]

    with open(argv[0], "r") as f:
        for line in f.readlines():
            x.append(list(map(float,line.split(","))))
        f.close()
    with open(argv[1],"r") as f:
        for line in f.readlines():
            y.append(list(map(float,line.split(","))))
        f.close()

    Resolution = 32
    inf=1e4

    x_min=inf
    y_min=inf
    x_max=-inf
    y_max=-inf
    for xi in x:
        x_min=min(x_min,min(xi))
        x_max=max(x_max,max(xi))
    for yi in y:
        y_min=min(y_min,min(yi))
        y_max=max(y_max,max(yi))
    for i in range(0,len(x)):
        for j in range(0,len(x[i])):
            x[i][j]-=x_min
            y[i][j]-=y_min
    x_max=x_max-x_min
    y_max=y_max-y_min
    if x_max>y_max:
        bias=(x_max-y_max)/2
        for i in range(0,len(y)):
            for j in range(0,len(y[i])):
                y[i][j]=y[i][j]+bias
        plt.ylim([0, x_max])
    else:
        bias=(y_max-x_max)/2
        for i in range(0,len(x)):
            for j in range(0,len(x[i])):
                x[i][j]=x[i][j]+bias
        plt.xlim([0, y_max])

    for i in range(0,len(x)):
        plt.plot(x[i],y[i],linewidth=30,color='black')
    plt.axis('off')
    filename=time.time()
    plt.savefig('./src/main/resources/py/result/'+str(filename)+'.png')
    plt.close()

    im=Image.open('./src/main/resources/py/result/'+str(filename)+'.png')
    im = im.convert('L')
    im = im.resize((Resolution, Resolution), Image.ANTIALIAS)
    im = im.transpose(Image.FLIP_TOP_BOTTOM)
    im = np.array(im).reshape([-1, 1, Resolution, Resolution])
    im = (im - np.mean(im)) / np.std(im)

    with torch.no_grad():
        im = torch.from_numpy(im).to(device).float()
        outputs = net(im)
        predicts = torch.argmax(outputs, dim=1)[0]

    return predicts.item()


def run(connect):
    print('start a connection!')
    msg=connect.recv(64).decode('utf-8')
    if len(msg)==0:
        return
    fileX,fileY=msg.split(',')
    connect.send('9'.encode('UTF-8'))
    while True:
        msg=connect.recv(16).decode('utf-8')
        if len(msg)==0:
            break
        if msg=='ready':
            result=main(['./src/main/resources/py/result/'+fileX,'./src/main/resources/py/result/'+fileY])
            connect.send(str(result).encode('UTF-8'))
    print('connection closed!')


if __name__=="__main__":
    sock=socket.socket(socket.AF_INET,socket.SOCK_STREAM)
    sock.bind(('127.0.0.1',2335))
    device = torch.device("cpu")
    net = Net(1, 8)
    net = net.to(device)
    net.load_state_dict(torch.load('./src/main/resources/py/model/LeNet.pt',map_location=torch.device('cpu')))
    sock.listen()
    while True:
        connection,addr=sock.accept()
        _thread.start_new_thread(run, (connection,))
