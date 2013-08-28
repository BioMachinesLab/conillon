#!/usr/bin/python
import time
import os
import subprocess

INTERVAL = 1
GLOBAL_SLEEP = 4

CPU_LOAD = 0;
COUNTER = 0;
SUM = 0;

TOP_LOAD = 0;

MIN_LOAD = 100;

def getTimeList():
    statFile = file("/proc/stat", "r")
    timeList = statFile.readline().split(" ")[2:6]
    statFile.close()
    for i in range(len(timeList))  :
        timeList[i] = int(timeList[i])
    return timeList

def deltaTime(interval):
    x = getTimeList()
    time.sleep(interval)
    y = getTimeList()
    for i in range(len(x)):
        y[i] -= x[i]
    return y

def getCpuLoad():
    dt = deltaTime(INTERVAL)
    cpuPct = 100 - (dt[len(dt) - 1] * 100.00 / sum(dt))
    return cpuPct

def get_network_bytes(interface):
    for line in open('/proc/net/dev', 'r'):
        if interface in line:
            data = line.split('%s:' % interface)[1].split()
            rx_bytes, tx_bytes = (data[0], data[8])
            return (rx_bytes, tx_bytes)

ini_rx_bytes, ini_tx_bytes = get_network_bytes('eth1');
while(1):
	os.system('clear');
	print '##########################################'
	load = getCpuLoad();
	SUM +=load;
	COUNTER+=1;
	avg = SUM/COUNTER;
	if(load > TOP_LOAD):
		TOP_LOAD = load;
	if(load < MIN_LOAD):
		MIN_LOAD = load;
	print ' - CPU LOAD: %s' % load;
	print ' - CPU TOP LOAD: %s' % TOP_LOAD;
	print ' - CPU MIN LOAD: %s' % MIN_LOAD;
	print ' - AVERAGE CPU: %s' % avg;
	rx_bytes, tx_bytes = get_network_bytes('eth1')
	
	rx_bytes1 =float(rx_bytes)-float(ini_rx_bytes)
	tx_bytes1 =float(tx_bytes)-float(ini_tx_bytes)

	rxKB = float(rx_bytes1) / 1024;
	txKB = float(tx_bytes1) / 1024;

	rxMB = float(rxKB) / 1024;
	txMB = float(txKB) / 1024;
	
	print 'Network:';
	print ' - RX: %.3f KB - %.3f MB' % (rxKB, rxMB);
	print ' - TX: %.3f KB - %.3f MB' % (txKB, txMB);
	print '\n';
	time.sleep(GLOBAL_SLEEP);

