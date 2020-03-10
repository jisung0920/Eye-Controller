import argparse
import socket

parser = argparse.ArgumentParser()
parser.add_argument('--ip',required =False, default='192.168.0.21',help = 'device\'s IP address')
parser.add_argument('--port',required =False, default='8000', help = 'device\'s PORT number')
args = parser.parse_args()

cIP,cPORT = args.ip, int(args.port)
# cIP = '0.0.0.0'
socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
print('IP Address ',cIP,':',cPORT)

socket.bind((cIP,cPORT))


print('Connected with ',cIP)

try :

	
	while True :
		data = socket.recv(5000)
		print('get',data.decode())
		# model data
		# connect.sendall()
except :
	print('Connecting Error')
	pass
finally :
	print('End of the session')
