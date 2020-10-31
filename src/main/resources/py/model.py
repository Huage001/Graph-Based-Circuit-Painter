import torch.nn as nn
import torch.nn.functional as F

class Net(nn.Module):

    def __init__(self,in_channel,num_classes):
        super(Net,self).__init__()
        self.conv1=nn.Conv2d(in_channel,10,5)
        self.conv2=nn.Conv2d(10,16,7)
        self.fc1=nn.Linear(16*4*4,80)
        self.fc2=nn.Linear(80,64)
        self.fc3=nn.Linear(64,num_classes)

    def forward(self,x):
        x=F.max_pool2d(F.leaky_relu(self.conv1(x)),(2,2))
        x=F.max_pool2d(F.leaky_relu(self.conv2(x)),(2,2))
        x=x.view(-1,self.num_flat_features(x))
        x=F.leaky_relu(self.fc1(x))
        x=F.leaky_relu(self.fc2(x))
        x=self.fc3(x)
        return x
    
    def num_flat_features(self,x):
        size=x.size()[1:]
        num_features=1
        for s in size:
            num_features*=s
        return num_features
