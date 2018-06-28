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
AccessCount = [0,0,0,0,0,0,0,0,0,0,0,0] #read + write + fastpath read + fastpath write, fastpath read + fastpath write, read, fastpath read, write, fastpath write, accesses inside cs, accesses outside cs, read inside cs, read outside cs, write inside cs, write outside cs
AccessLabels = ['Acc', 'Acc FP', 'Rd', 'Rd FP', 'Wr', 'Wr FP', 'Acc InCS', 'Acc OutCS', 'Rd InCS', 'Rd OutCS', 'Wr InCS', 'Wr OutCS']
AccessXaxis = [1,2,3,4,5,6,7,8,9,10,11,12]
ReadCount = [0,0,0,0] #read, read inside cs, read outside cs, volatile read
ReadLabels = ['noFP Rd', 'InCS', 'OutCS', 'Vol Rd']
ReadXaxis = [1,2,3,4]
WriteCount = [0,0,0,0] #write, write inside cs, write outside cs, volatile write
WriteLabels = ['noFP Wr', 'InCS', 'OutCS', 'Vol Wr']
WriteXaxis = [1,2,3,4]
OtherCount = [0,0,0,0,0,0,0,0,0,0,0,0,0] #total, read, write, acquire, release, start, join, pre wait, post wait, class init, class accessed, fake fork, exit
OtherLabels = ['noFP Op', 'noFP Rd', 'noFP Wr', 'Acq', 'Rel', 'Start', 'Join', 'Pre-Wait', 'Post-Wait', 'Class Init', 'Class Acc', 'Fake Fork', 'Exit']
OtherXaxis = [1,2,3,4,5,6,7,8,9,10,11,12,13]

#Collect event counts
def gatherEvents(file_name, AccessCount, ReadCount, WriteCount, OtherCount):
	with open(file_name) as input:
		#Reset cumulative counters
		AccessCount[0] = 0
		AccessCount[1] = 0
		lines = input.readlines()
		for line in lines:
			if 'total: ' in line:
				OtherCount[0] = [int(s.strip()) for s in line.split('total: ') if s.strip().isdigit()][0]
			if 'read: ' in line:
				num = [int(s.strip()) for s in line.split('read: ') if s.strip().isdigit()][0]
				AccessCount[0] = AccessCount[0] + num
				AccessCount[2] = num
				ReadCount[0] = num
				OtherCount[1] = num
			if 'fastpath read: ' in line:
				num = [int(s.strip()) for s in line.split('fastpath read: ') if s.strip().isdigit()][0]
				AccessCount[0] = AccessCount[0] + num
				AccessCount[1] = AccessCount[1] + num
				AccessCount[3] = num
			if 'write: ' in line:
				num = [int(s.strip()) for s in line.split('write: ') if s.strip().isdigit()][0]
				AccessCount[0] = AccessCount[0] + num
				AccessCount[4] = num
				WriteCount[0] = num
				OtherCount[2] = num
			if 'fastpath write: ' in line:
				num = [int(s.strip()) for s in line.split('fastpath write: ') if s.strip().isdigit()][0]
				AccessCount[0] = AccessCount[0] + num
				AccessCount[1] = AccessCount[1] + num
				AccessCount[5] = num
			if 'accesses inside cs: ' in line:
				AccessCount[6] = [int(s.strip()) for s in line.split('accesses inside cs: ') if s.strip().isdigit()][0]
			if 'accesses outside cs: ' in line:
				AccessCount[7] = [int(s.strip()) for s in line.split('accesses outside cs: ') if s.strip().isdigit()][0]
			if 'read inside cs: ' in line:
				num = [int(s.strip()) for s in line.split('read inside cs: ') if s.strip().isdigit()][0]
				AccessCount[8] = num
				ReadCount[1] = num
			if 'read outside cs: ' in line:
				num = [int(s.strip()) for s in line.split('read outside cs: ') if s.strip().isdigit()][0]
				AccessCount[9] = num
				ReadCount[2] = num
			if 'write inside cs: ' in line:
				num = [int(s.strip()) for s in line.split('write inside cs: ') if s.strip().isdigit()][0]
				AccessCount[10] = num
				WriteCount[1] = num
			if 'write outside cs: ' in line:
				num = [int(s.strip()) for s in line.split('write outside cs: ') if s.strip().isdigit()][0]
				AccessCount[11] = num
				WriteCount[2] = num
			if 'volatile read: ' in line:
				ReadCount[3] = [int(s.strip()) for s in line.split('volatile read: ') if s.strip().isdigit()][0]
			if 'volatile write: ' in line:
				WriteCount[3] = [int(s.strip()) for s in line.split('volatile write: ') if s.strip().isdigit()][0]
			if 'acquire: ' in line:
				OtherCount[3] = [int(s.strip()) for s in line.split('acquire: ') if s.strip().isdigit()][0]
			if 'release: ' in line:
				OtherCount[4] = [int(s.strip()) for s in line.split('release: ') if s.strip().isdigit()][0]
			if 'start: ' in line:
				OtherCount[5] = [int(s.strip()) for s in line.split('start: ') if s.strip().isdigit()][0]
			if 'join: ' in line:
				OtherCount[6] = [int(s.strip()) for s in line.split('join: ') if s.strip().isdigit()][0]
			if 'pre wait: ' in line:
				OtherCount[7] = [int(s.strip()) for s in line.split('pre wait: ') if s.strip().isdigit()][0]
			if 'post wait: ' in line:
				OtherCount[8] = [int(s.strip()) for s in line.split('post wait: ') if s.strip().isdigit()][0]
			if 'class init: ' in line:
				OtherCount[9] = [int(s.strip()) for s in line.split('class init: ') if s.strip().isdigit()][0]
			if 'class accessed: ' in line:
				OtherCount[10] = [int(s.strip()) for s in line.split('class accessed: ') if s.strip().isdigit()][0]
			if 'fake fork: ' in line:
				OtherCount[11] = [int(s.strip()) for s in line.split('fake fork: ') if s.strip().isdigit()][0]
			if 'exit: ' in line:
				OtherCount[12] = [int(s.strip()) for s in line.split('exit: ') if s.strip().isdigit()][0]

#Specify Data Set, Trials, Benchmarks, etc.
HomeDir = os.path.expanduser('~')
Benchmarks = ['avrora', 'batik', 'htwo', 'jython', 'luindex', 'lusearch', 'pmd', 'sunflow', 'tomcat', 'xalan']
Configurations = ['hb', 'capo_exc']

for bench in Benchmarks:
	for config in Configurations:
		gatherEvents(HomeDir+"/git/Parser/event-output/"+bench+"/"+config+"/slow_event_counts.txt", AccessCount, ReadCount, WriteCount, OtherCount)

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
		plt.subplot(221)
		plt.title('Access Counts')
		plt.bar(AccessXaxis, AccessCount, width=BarWidth, align=BarAlign)
		plt.xticks(AccessXaxis, AccessLabels, rotation=Rotation)
		plt.ylabel(Ylabel, fontsize=LabelFontSize, fontname='Libertine')
		plt.autoscale(tight=True)
		plt.tick_params(labelsize=TickFontSize)
		plt.margins(0.1, 0.1)

#Display Read Event Count Plot
		plt.subplot(222)
		plt.title('Read Counts')
		plt.bar(ReadXaxis, ReadCount, width=BarWidth, align=BarAlign)
		plt.xticks(ReadXaxis, ReadLabels, rotation=Rotation)
		plt.ylabel(Ylabel, fontsize=LabelFontSize, fontname='Libertine')
		plt.autoscale(tight=True)
		plt.tick_params(labelsize=TickFontSize)
		plt.margins(0.1, 0.1)

#Display Write Event Count Plot
		plt.subplot(223)
		plt.title('Write Counts')
		plt.bar(WriteXaxis, WriteCount, width=BarWidth, align=BarAlign)
		plt.xticks(WriteXaxis, WriteLabels, rotation=Rotation)
		plt.ylabel(Ylabel, fontsize=LabelFontSize, fontname='Libertine')
		plt.autoscale(tight=True)
		plt.tick_params(labelsize=TickFontSize)
		plt.margins(0.1, 0.1)

#Display Other Event Count Plot
		plt.subplot(224)
		plt.title('Other Counts')
		plt.bar(OtherXaxis, OtherCount, width=BarWidth, align=BarAlign)
		plt.xticks(OtherXaxis, OtherLabels, rotation=Rotation)
		plt.ylabel(Ylabel, fontsize=LabelFontSize, fontname='Libertine')
		plt.autoscale(tight=True)
		plt.tick_params(labelsize=TickFontSize)
		plt.margins(0.1, 0.1)

		mng = plt.get_current_fig_manager()
		mng.resize(*mng.window.maxsize())
		plt.show(block=False)
		fig.savefig(HomeDir+"/git/Parser/event-output/"+bench+"/"+config+"/slow_plot.pdf")
