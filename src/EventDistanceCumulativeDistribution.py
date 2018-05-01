#!/usr/bin/env python3
import matplotlib.pyplot as plt
import sys
import os
import numpy as np
#This makes binning much simpler
def getLSConstLabel(numLSConst):
	if numLSConst == 0:
		return 0
	if numLSConst == 1:
		return 1
	if numLSConst == 2 or numLSConst == 3:
		return 2
	if numLSConst >= 4 and numLSConst <= 7:
		return 4
	if numLSConst >= 8 and numLSConst <= 15:
		return 8
	if numLSConst >= 16:
		return 16

def plotBench(file_name, bench, HBdistance, HBraces, HBcount, WCPdistance, WCPraces, WCPcount, DCdistance, DCraces, DCcount, LSConstraints, LoopIter):
	with open(file_name) as input:
		lines = input.readlines()
		checkingWDC = False
		numLSConst = 0
		numIter = 0
		for line in lines:
			if 'Checking HB-race' in line:
				HBdistance.append([int(s.strip()) for s in line.split('distance: ') if s.strip().isdigit()][0])
				HBcount[0] = HBcount[0] + 1
				HBraces.append(HBcount[0])
			if 'Checking WCP-race' in line:
				WCPdistance.append([int(s.strip()) for s in line.split('distance: ') if s.strip().isdigit()][0])
				WCPcount[0] = WCPcount[0] + 1
				WCPraces.append(WCPcount[0])
			if 'Checking WDC-race' in line:
				DCdistance.append([int(s.strip()) for s in line.split('distance: ') if s.strip().isdigit()][0])
				DCcount[0] = DCcount[0] + 1
				DCraces.append(DCcount[0])
				if checkingWDC:
					LSConstraints.append(getLSConstLabel(numLSConst))
					LoopIter.append(numIter)
					numLSConst = 0
					numIter = 0
				else:
					checkingWDC = True
			if 'Iteration = ' in line:
				numIter = numIter + 1
			if 'Found acq->rel' in line:
				numLSConst = numLSConst + 1
			if 'Found rel->acq' in line:
				numLSConst = numLSConst + 1
		if checkingWDC:
			LSConstraints.append(getLSConstLabel(numLSConst))
			LoopIter.append(numIter)

ResultDir = "result_stats_3trials_retest/"
HomeDir = os.path.expanduser('~')
Trials = 3
Benchmarks = ["avrora", "h2", "jython", "luindex", "lusearch", "pmd", "sunflow", "tomcat", "xalan"]
HBdistance = []
HBraces = []
HBcount = [0]
WCPdistance = []
WCPraces = []
WCPcount = [0]
DCdistance = []
DCraces = []
DCcount = [0]
LSConstraints = []
LoopIter = []
for bench in Benchmarks:
	for trial in range(1,Trials+1):
		if bench is "lusearch":
			plotBench(HomeDir+"/exp-output/"+ResultDir+bench+"9-fixed/adapt/gc_default/rr_wdc/var/"+str(trial)+"/output.txt", bench, HBdistance, HBraces, HBcount, WCPdistance, WCPraces, WCPcount, DCdistance, DCraces, DCcount, LSConstraints, LoopIter)
		else:
			plotBench(HomeDir+"/exp-output/"+ResultDir+bench+"9/adapt/gc_default/rr_wdc/var/"+str(trial)+"/output.txt", bench, HBdistance, HBraces, HBcount, WCPdistance, WCPraces, WCPcount, DCdistance, DCraces, DCcount, LSConstraints, LoopIter)

HBdistance.sort()
WCPdistance.sort()
DCdistance.sort()
LSConstraints.sort(reverse=True)
LoopIter.sort(reverse=True)
HBracePercent = [100 * (val / len(HBraces)) for val in HBraces]
WCPracePercent = [100 * (val / len(WCPraces)) for val in WCPraces]
DCracePercent = [100 * (val / len(DCraces)) for val in DCraces]
Xlabel = 'Percentage of dynamic races'
Ylabel = 'Event distance (logarithmic scale)'
dashed = '--'
dashdot = '-.'
lnwidth = 4

#DC Race Distances: race sort reverse
HBracePercent.sort(reverse=True)
WCPracePercent.sort(reverse=True)
DCracePercent.sort(reverse=True)

plt.figure('Race Distances')
plt.title('Cumulative Distribution of Event Distance')
plt.plot(HBdistance, HBracePercent, linewidth=lnwidth)
plt.plot(WCPdistance, WCPracePercent, dashed, linewidth=lnwidth)
plt.plot(DCdistance, DCracePercent, dashdot, linewidth=lnwidth)
plt.xlabel(Ylabel, fontsize=18)
plt.ylabel(Xlabel, fontsize=18)
plt.xscale('log')
plt.legend(['HB-races', 'WCP-only races', 'DC-only races'], loc='best', fontsize=18)
plt.tick_params(labelsize=16)
plt.margins(0.1, 0.1)

plt.show(block=False)

print('Characteristics of VindicateRace (Algorithm 1 in paper) for all dynamic DC-only races across all trials.')
print()

#LS Constraints Table
print('LS constraints added corresponds to lines 19-20 in Algorithm 1 in paper.')
print('LS Constraints Added | DC-Only Races')
print('    0 Added | ' + str(LSConstraints.count(0)))
print('    1 Added | ' + str(LSConstraints.count(1)))
print('  2-3 Added | ' + str(LSConstraints.count(2)))
print('  4-7 Added | ' + str(LSConstraints.count(4)))
print(' 8-15 Added | ' + str(LSConstraints.count(8)))
print('16-31 Added | ' + str(LSConstraints.count(16)))
print()

#Loop Iterations Table
print('Outer loop iterations corresponds to lines 15-21 in Algorithm 1 in paper.')
print('Outer Loop Iterations | DC-Only Races')
print('1 Iter | ' + str(LoopIter.count(1)))
print('2 Iter | ' + str(LoopIter.count(2)))
print('3 Iter | ' + str(LoopIter.count(3)))
print('4 Iter | ' + str(LoopIter.count(4)))
print('5 Iter | ' + str(LoopIter.count(5)))

plt.show()
