'''
Created on 2015-6-12

@author: DJ
'''
import socket
import time
if __name__ == '__main__':
    server = socket.socket(socket.AF_INET,socket.SOCK_DGRAM)
    server.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)
    server.bind(('192.168.1.5',8999))
    while True:
        time.sleep(0.2)
        data,(addr,port) = server.recvfrom(2048);
        print 'Recv',data,'From:','%s:%s'%(addr,port)
#         server.sendto('ddd',(addr,port));
    pass