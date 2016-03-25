'''
Created on 2015-6-12

@author: DJ
'''
import socket
from threading import Thread
import time

class subThread(Thread):
    def __init__(self):
        Thread.__init__(self)
        self.client = socket.socket(socket.AF_INET,socket.SOCK_DGRAM)
        self.client.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)
        pass
    def run(self):
        while(True):
            self.client.sendto('5548888881sfa',('255.255.255.255',8999))
            time.sleep(1)
if __name__ == '__main__':
    subT = subThread()
    subT.start()