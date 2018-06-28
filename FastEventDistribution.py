#!/usr/bin/env python3
import matplotlib.pyplot as plt
import sys
import os
import numpy as np

#Manage font type
def SetRCParams():
	plt.rcParams['text.usetex'] = True
	plt.rcParams['text.latex.preamble'] = [r'\usepackage{libertine}', r'\scshape']
	plt.rcParams['font.family'] = 'libertine'

#Initialize arrays for event counts
AccessCount = [0,0,0,0,0,0] #total access ops, total fast path taken, total reads, read fast path taken, total writes, write fast path taken
AccessLabels = ['Acc', 'Acc FP',  'Rd', 'Rd FP', 'Wr', 'Wr FP']
AccessXaxis = [1,2,3,4,5,6]
ReadCount = [0,0,0,0,0,0] #total reads - read fast path taken, read same epoch, read shared same epoch, read exclusive, read share, read shared
ReadLabels = ['noFP Rd', 'Sa Ep', 'Shd Sa Ep', 'Excl', 'Sh', 'Shd']
ReadXaxis = [1,2,3,4,5,6]
WriteCount = [0,0,0,0] #total writes - write fast path taken, write same epoch, write exclusive, write shared
WriteLabels = ['noFP Wr', 'Same Ep', 'Exc', 'Shd']
WriteXaxis = [1,2,3,4]
OtherCount = [0,0,0,0,0,0,0,0,0,0,0,0] #total ops - total fast path taken, total reads - read fast path taken, total writes - write fast path taken, acquire, release, fork, join, pre wait, post wait, volatile, class init, class accessed
OtherLabels = ['noFP Op', 'noFP Rd', 'noFP Wr', 'Acq', 'Rel', 'Fork', 'Join', 'Pre-Wait', 'Post-Wait', 'Volatile', 'Class Init', 'Class Acc']
OtherXaxis = [1,2,3,4,5,6,7,8,9,10,11,12]
RaceTypeCount = [0,0,0,0] #write-read race, write-write race, read-write race, read(shared)-write race
RaceTypeLabels = ['Wr-Rd Race', 'Wr-Wr Race', 'Rd-Wr Race', 'Rd(Sh)-Wr Race']
RaceTypeXaxis = [1,2,3,4]

#Collect event counts
def gatherEvents(file_name, AccessCount, ReadCount, WriteCount, OtherCount, RaceTypeCount):
	with open(file_name) as input:
		#Reset cumulative counters
		OtherCount[0] = 0
		ReadCount[0] = 0
		WriteCount[0] = 0
		lines = input.readlines()
		for line in lines:
			if 'total ops: ' in line:
				num = [int(s.strip()) for s in line.split('total ops: ') if s.strip().isdigit()][0]
				if OtherCount[0] == 0:
					OtherCount[0] = num
				else:
					OtherCount[0] = OtherCount[0] - num
			if 'total access ops: ' in line:
				AccessCount[0] = [int(s.strip()) for s in line.split('total access ops: ') if s.strip().isdigit()][0]
			if 'total fast path taken: ' in line:
				num = [int(s.strip()) for s in line.split('total fast path taken: ') if s.strip().isdigit()][0]
				AccessCount[1] = num
				if OtherCount[0] == 0:
					OtherCount[0] = num
				else:
					OtherCount[0] = OtherCount[0] - num
			if 'total reads: ' in line:
				num = [int(s.strip()) for s in line.split('total reads: ') if s.strip().isdigit()][0]
				AccessCount[2] = num
				if ReadCount[0] == 0:
					ReadCount[0] = num
				else:
					ReadCount[0] = num - ReadCount[0]
					OtherCount[1] = ReadCount[0]
			if 'read fast path taken: ' in line:
				num = [int(s.strip()) for s in line.split('read fast path taken: ') if s.strip().isdigit()][0]
				AccessCount[3] = num
				if ReadCount[0] == 0:
					ReadCount[0] = num
				else:
					ReadCount[0] = ReadCount[0] - num
					OtherCount[1] = ReadCount[0]
			if 'total writes: ' in line:
				num = [int(s.strip()) for s in line.split('total writes: ') if s.strip().isdigit()][0]
				AccessCount[4] = num
				if WriteCount[0] == 0:
					WriteCount[0] = num
				else:
					WriteCount[0] = num - WriteCount[0]
					OtherCount[2] = WriteCount[0]
			if 'write fast path taken: ' in line:
				num = [int(s.strip()) for s in line.split('write fast path taken: ') if s.strip().isdigit()][0]
				AccessCount[5] = num
				if WriteCount[0] == 0:
					WriteCount[0] = num
				else:
					WriteCount[0] = WriteCount[0] - num
					OtherCount[2] = WriteCount[0]
			if 'read same epoch: ' in line:
				ReadCount[1] = [int(s.strip()) for s in line.split('read same epoch: ') if s.strip().isdigit()][0]
			if 'read shared same epoch: ' in line:
				ReadCount[2] = [int(s.strip()) for s in line.split('read shared same epoch: ') if s.strip().isdigit()][0]
			if 'read exclusive: ' in line:
				ReadCount[3] = [int(s.strip()) for s in line.split('read exclusive: ') if s.strip().isdigit()][0]
			if 'read share: ' in line:
				ReadCount[4] = [int(s.strip()) for s in line.split('read share: ') if s.strip().isdigit()][0]
			if 'read shared: ' in line:
				ReadCount[5] = [int(s.strip()) for s in line.split('read shared: ') if s.strip().isdigit()][0]
			if 'write same epoch: ' in line:
				WriteCount[1] = [int(s.strip()) for s in line.split('write same epoch: ') if s.strip().isdigit()][0]
			if 'write exclusive: ' in line:
				WriteCount[2] = [int(s.strip()) for s in line.split('write exclusive: ') if s.strip().isdigit()][0]
			if 'write shared: ' in line:
				WriteCount[3] = [int(s.strip()) for s in line.split('write shared: ') if s.strip().isdigit()][0]
			if 'acquire: ' in line:
				OtherCount[3] = [int(s.strip()) for s in line.split('acquire: ') if s.strip().isdigit()][0]
			if 'release: ' in line:
				OtherCount[4] = [int(s.strip()) for s in line.split('release: ') if s.strip().isdigit()][0]
			if 'fork: ' in line:
				OtherCount[5] = [int(s.strip()) for s in line.split('fork: ') if s.strip().isdigit()][0]
			if 'join: ' in line:
				OtherCount[6] = [int(s.strip()) for s in line.split('join: ') if s.strip().isdigit()][0]
			if 'pre wait: ' in line:
				OtherCount[7] = [int(s.strip()) for s in line.split('pre wait: ') if s.strip().isdigit()][0]
			if 'post wait: ' in line:
				OtherCount[8] = [int(s.strip()) for s in line.split('post wait: ') if s.strip().isdigit()][0]
			if 'volatile: ' in line:
				OtherCount[9] = [int(s.strip()) for s in line.split('volatile: ') if s.strip().isdigit()][0]
			if 'class init: ' in line:
				OtherCount[10] = [int(s.strip()) for s in line.split('class init: ') if s.strip().isdigit()][0]
			if 'class accessed: ' in line:
				OtherCount[11] = [int(s.strip()) for s in line.split('class accessed: ') if s.strip().isdigit()][0]
			if 'write-read race: ' in line:
				RaceTypeCount[0] = [int(s.strip()) for s in line.split('write-read race: ') if s.strip().isdigit()][0]
			if 'write-write race: ' in line:
				RaceTypeCount[1] = [int(s.strip()) for s in line.split('write-write race: ') if s.strip().isdigit()][0]
			if 'read-write race: ' in line:
				RaceTypeCount[2] = [int(s.strip()) for s in line.split('read-write race: ') if s.strip().isdigit()][0]
			if 'shared write race: ' in line:
				RaceTypeCount[3] = [int(s.strip()) for s in line.split('shared write race: ') if s.strip().isdigit()][0]

#Specify Data Set, Trials, Benchmarks, etc.
HomeDir = os.path.expanduser('~')
Benchmarks = ['avrora', 'batik', 'htwo', 'jython', 'luindex', 'lusearch', 'pmd', 'sunflow', 'tomcat', 'xalan']
Configurations = ['pip_capo', 'pip_pip']

for bench in Benchmarks:
	for config in Configurations:
		gatherEvents(HomeDir+"/git/Parser/event-output/"+bench+"/"+config+"/fast_event_counts.txt", AccessCount, ReadCount, WriteCount, OtherCount, RaceTypeCount)

#Set Labels for Plot
		Xlabel = 'Event Types'
		Ylabel = 'Event Counts'

#Set Font Type for Plot
		SetRCParams()

#Set Fone Size for Plot
		LabelFontSize = 18
		LegendFontSize = 22
		TickFontSize = 16
		Rotation = -45
		BarWidth = 0.7
		BarAlign = 'center'

		fig = plt.figure(bench + ' ' + config)

#Display Access Event Count Plot
		plt.subplot(231)
		plt.title('Access Counts')
		plt.bar(AccessXaxis, AccessCount, width=BarWidth, align=BarAlign)
		plt.xticks(AccessXaxis, AccessLabels, rotation=Rotation)
		plt.ylabel(Ylabel, fontsize=LabelFontSize, fontname='Libertine')
		plt.autoscale(tight=True)
		plt.tick_params(labelsize=TickFontSize)
		plt.margins(0.1, 0.1)

#Display Read Event Count Plot
		plt.subplot(232)
		plt.title('Read Counts')
		plt.bar(ReadXaxis, ReadCount, width=BarWidth, align=BarAlign)
		plt.xticks(ReadXaxis, ReadLabels, rotation=Rotation)
		plt.ylabel(Ylabel, fontsize=LabelFontSize, fontname='Libertine')
		plt.autoscale(tight=True)
		plt.tick_params(labelsize=TickFontSize)
		plt.margins(0.1, 0.1)

#Display Write Event Count Plot
		plt.subplot(233)
		plt.title('Write Counts')
		plt.bar(WriteXaxis, WriteCount, width=BarWidth, align=BarAlign)
		plt.xticks(WriteXaxis, WriteLabels, rotation=Rotation)
		plt.ylabel(Ylabel, fontsize=LabelFontSize, fontname='Libertine')
		plt.autoscale(tight=True)
		plt.tick_params(labelsize=TickFontSize)
		plt.margins(0.1, 0.1)

#Display Other Event Count Plot
		plt.subplot(234)
		plt.title('Other Counts')
		plt.bar(OtherXaxis, OtherCount, width=BarWidth, align=BarAlign)
		plt.xticks(OtherXaxis, OtherLabels, rotation=Rotation)
		plt.ylabel(Ylabel, fontsize=LabelFontSize, fontname='Libertine')
		plt.autoscale(tight=True)
		plt.tick_params(labelsize=TickFontSize)
		plt.margins(0.1, 0.1)

#Display RaceType Event Count Plot
		plt.subplot(235)
		plt.title('Race Type Counts')
		plt.bar(RaceTypeXaxis, RaceTypeCount, width=BarWidth, align=BarAlign)
		plt.xticks(RaceTypeXaxis, RaceTypeLabels, rotation=Rotation)
		plt.ylabel(Ylabel, fontsize=LabelFontSize, fontname='Libertine')
		plt.autoscale(tight=True)
		plt.tick_params(labelsize=TickFontSize)
		plt.margins(0.1, 0.1)

		mng = plt.get_current_fig_manager()
#		mng.frame.Maximize(True)
#		mng.window.showMaximized()
		mng.resize(*mng.window.maxsize())
		plt.show(block=False)
		fig.savefig(HomeDir+"/git/Parser/event-output/"+bench+"/"+config+"/fast_plot.pdf")
