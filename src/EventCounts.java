import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;

import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

public class EventCounts {
	//Common counts
	private long[] acquire;
	private long[] release;
	private long[] fork;
	private long[] join;
	private long[] barrier;

	private long[] pre_wait;
	private long[] post_wait;
	private long[] class_init;
	private long[] class_access;
	
	private long[] write_accesses_inside_CS;
	private long[] write_accesses_outside_CS;
	private long[] read_accesses_inside_CS;
	private long[] read_accesses_outside_CS;
	
	private long[] other;
	
	private long[] total_reads;
	private long[] total_writes;
	private long[] total_access_ops;
	private long[] total_ops;
	private long[] total_fast_path_taken;
	
	//Slow tool counts
	private long[] read;
	private long[] write;
	private long[] volatile_write;
	private long[] volatile_read;
	
	private long[] exit;
	private long[] dummy;
	private long[] fake_fork;
	
	private long[] writeFP;
	private long[] readFP;
	
	private long[] accesses_inside_CS;
	private long[] accesses_outside_CS;
	
	private long[] total_volatiles;
	
	//Fast tool counts
	private long[] read_same_epoch;
	private long[] read_shared_same_epoch;
	private long[] read_exclusive;
	private long[] read_share;
	private long[] read_shared;
	private long[] write_read_race;
	private long[] write_same_epoch;
	private long[] write_exclusive;
	private long[] write_shared;
	private long[] write_write_race;
	private long[] read_write_race;
	private long[] shared_write_race;
	private long[] volatile_acc;
	
	private long[] read_fast_path_taken;
	private long[] write_fast_path_taken;
	
	private long[] read_same_epochFP;
	private long[] read_shared_same_epochFP;
	private long[] read_exclusiveFP;
	private long[] read_shareFP;
	private long[] read_sharedFP;
	private long[] write_same_epochFP;
	private long[] write_exclusiveFP;
	private long[] write_sharedFP;
	
	private long[] write_accesses_inside_CSFP;
	private long[] write_accesses_outside_CSFP;
	private long[] read_accesses_inside_CSFP;
	private long[] read_accesses_outside_CSFP;
	
	private long[] hold_locks;
	private long[] one_lock_held;
	private long[] two_nestedLocks_held;
	private long[] many_nestedLocks_held;
	
	private long[] read_rule_A_succeed;
	private long[] read_rule_A_total_attempts;
	private long[] write_write_rule_A_succeed;
	private long[] write_write_rule_A_total_attempts;
	private long[] write_read_rule_A_succeed;
	private long[] write_read_rule_A_total_attempts;
	
	private long[] clears_by_capo;
	private long[] read_set_size_0;
	private long[] read_set_size_1;
	private long[] read_set_size_gt_1;
	private long[] write_set_size_0;
	private long[] write_set_size_1;
	private long[] write_set_size_gt_1;
	private long[] read_map_size_0;
	private long[] read_map_size_1;
	private long[] read_map_size_10;
	private long[] read_map_size_100;
	private long[] read_map_size_1000;
	private long[] read_map_size_gt_1000;
	private long[] write_map_size_0;
	private long[] write_map_size_1;
	private long[] write_map_size_10;
	private long[] write_map_size_100;
	private long[] write_map_size_1000;
	private long[] write_map_size_gt_1000;

	private long[] rule_a_total_successes;
	private long[] rule_a_changed_a_single_thread;
	private long[] rule_a_changed_two_threads;
	private long[] rule_a_changed_more_than_two_threads;
	private long[] rule_a_changed_every_thread;
	
	private long[] total_one_lock_held;
	private long[] rule_a_success_one_lock_held;
	private long[] total_two_locks_held;
	private long[] rule_a_success_inner_lock_held;
	private long[] rule_a_success_outer_lock_held;
	private long[] total_many_locks_held;
	private long[] rule_a_success_inner_most_lock_held;
	private long[] rule_a_success_outer_most_lock_held;
	
	private String config;
	private String bench;
	
	public EventCounts(String config, String bench) {
		this.config = config;
		this.bench = bench;
	}
	
	public int failedTrials(long[] array) {
		int failures = 0;
		for (int i = 0; i < array.length; i++) {
			if (array[i] == -1) failures++;
		}
		return failures;
	}
	
	public static int failedTrials(double[] array) {
		int failures = 0;
		for (int i = 0; i < array.length; i++) {
			if (array[i] == -1) failures++;
		}
		return failures;
	}
	
	public static int failedTrials(int[] array) {
		int failures = 0;
		for (int i = 0; i < array.length; i++) {
			if (array[i] == -1) failures++;
		}
		return failures;
	}
	
	public long[] resize(long[] array, int newSize) {
		long[] resized = new long[newSize];
		int index = 0;
		for (int i = 0; i < array.length; i++) {
			if (array[i] != -1) {
				resized[index] = array[i];
				index++;
			}
		}
		return resized;
	}
	
	public static double[] resize(double[] array, int newSize) {
		double[] resized = new double[newSize];
		int index = 0;
		for (int i = 0; i < array.length; i++) {
			if (array[i] != -1) {
				resized[index] = array[i];
				index++;
			}
		}
		return resized;
	}
	
	public static int[] resize(int[] array, int newSize) {
		int[] resized = new int[newSize];
		int index = 0;
		for (int i = 0; i < array.length; i++) {
			if (array[i] != -1) {
				resized[index] = array[i];
				index++;
			}
		}
		return resized;
	}
	
	public long[] getVal(long[] val, long eventCount, int curr_trial, int total_trials) {
		if (val == null) {
			val = new long[total_trials];
			for (int i = 0; i < val.length; i++) val[i] = -1; //failed trial identifier
		}
		val[curr_trial-1] = eventCount; //trial counts start at 1
		//If this is the last trial and there were failed trials, resize the data set with only successful trials
		if (curr_trial == total_trials) {
			int failures = failedTrials(val);
			if (failures > 0) return resize(val, val.length-failures);
		}
		return val;
	}
	
	public void setEventCounts(String eventType, long eventCount, int curr_trial, int total_trials) {
		if (eventType.equals("Acquire")) {
			setAcquire(getVal(getAcquire(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Release")) {
			setRelease(getVal(getRelease(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Fork")) {
			setFork(getVal(getFork(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Join")) {
			setJoin(getVal(getJoin(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Barrier")) {
			setBarrier(getVal(getBarrier(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Pre Wait")) {
			setPre_wait(getVal(getPre_wait(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Post Wait")) {
			setPost_wait(getVal(getPost_wait(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Class Initialized")) {
			setClass_init(getVal(getClass_init(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Class Accessed")) {
			setClass_access(getVal(getClass_access(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Write accesses Inside Critical Sections")) {
			setWrite_accesses_inside_CS(getVal(getWrite_accesses_inside_CS(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Write accesses Outside Critical Sections")) {
			setWrite_accesses_outside_CS(getVal(getWrite_accesses_outside_CS(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Read accesses Inside Critical Sections")) {
			setRead_accesses_inside_CS(getVal(getRead_accesses_inside_CS(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Read accesses Outside Critical Sections")) {
			setRead_accesses_outside_CS(getVal(getRead_accesses_outside_CS(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Other")) {
			setOther(getVal(getOther(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Total Reads")) {
			setTotal_reads(getVal(getTotal_reads(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Total Writes")) {
			setTotal_writes(getVal(getTotal_writes(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Total Access Ops")) {
			setTotal_access_ops(getVal(getTotal_access_ops(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Total Ops")) {
			setTotal_ops(getVal(getTotal_ops(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Total Fast Path Taken")) {
			setTotal_fast_path_taken(getVal(getTotal_fast_path_taken(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Read")) {
			setRead(getVal(getRead(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Write")) {
			setWrite(getVal(getWrite(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Volatile Write")) {
			setVolatile_write(getVal(getVolatile_write(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Volatile Read")) {
			setVolatile_read(getVal(getVolatile_read(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Exit")) {
			setExit(getVal(getExit(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Dummy")) {
			setDummy(getVal(getDummy(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Fake Fork")) {
			setFake_fork(getVal(getFake_fork(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("WriteFastPath")) {
			setWriteFP(getVal(getWriteFP(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("ReadFastPath")) {
			setReadFP(getVal(getReadFP(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Accesses Inside Critical Sections")) {
			setAccesses_inside_CS(getVal(getAccesses_inside_CS(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Accesses Outside Critical Sections")) {
			setAccesses_outside_CS(getVal(getAccesses_outside_CS(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Total Volatiles")) {
			setTotal_volatiles(getVal(getTotal_volatiles(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Read Same Epoch")) {
			setRead_same_epoch(getVal(getRead_same_epoch(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Read Shared Same Epoch")) {
			setRead_shared_same_epoch(getVal(getRead_shared_same_epoch(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Read Exclusive")) {
			setRead_exclusive(getVal(getRead_exclusive(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Read Share")) {
			setRead_share(getVal(getRead_share(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Read Shared")) {
			setRead_shared(getVal(getRead_shared(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Write-Read Error")) {
			setWrite_read_race(getVal(getWrite_read_race(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Write Same Epoch")) {
			setWrite_same_epoch(getVal(getWrite_same_epoch(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Write Exclusive")) {
			setWrite_exclusive(getVal(getWrite_exclusive(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Write Shared")) {
			setWrite_shared(getVal(getWrite_shared(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Write-Write Error")) {
			setWrite_write_race(getVal(getWrite_write_race(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Read-Write Error")) {
			setRead_write_race(getVal(getRead_write_race(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Shared-Write Error")) {
			setShared_write_race(getVal(getShared_write_race(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Volatile")) {
			setVolatile_acc(getVal(getVolatile_acc(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Read Fast Path Taken")) {
			setRead_fast_path_taken(getVal(getRead_fast_path_taken(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Write Fast Path Taken")) {
			setWrite_fast_path_taken(getVal(getWrite_fast_path_taken(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Read Same Epoch FP")) {
			setRead_same_epochFP(getVal(getRead_same_epochFP(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Read Shared Same Epoch FP")) {
			setRead_shared_same_epochFP(getVal(getRead_shared_same_epochFP(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Read Exclusive FP")) {
			setRead_exclusiveFP(getVal(getRead_exclusiveFP(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Read Share FP")) {
			setRead_shareFP(getVal(getRead_shareFP(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Read Shared FP")) {
			setRead_sharedFP(getVal(getRead_sharedFP(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Write Same Epoch FP")) {
			setWrite_same_epochFP(getVal(getWrite_same_epochFP(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Write Exclusive FP")) {
			setWrite_exclusiveFP(getVal(getWrite_exclusiveFP(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Write Shared FP")) {
			setWrite_sharedFP(getVal(getWrite_sharedFP(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Write accesses Inside Critical Sections succeeding Fast Path")) {
			setWrite_accesses_inside_CSFP(getVal(getWrite_accesses_inside_CSFP(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Write accesses Outside Critical Sections succeeding Fast Path")) {
			setWrite_accesses_outside_CSFP(getVal(getWrite_accesses_outside_CSFP(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Read accesses Inside Critical Sections succeeding Fast Path")) {
			setRead_accesses_inside_CSFP(getVal(getRead_accesses_inside_CSFP(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Read accesses Outside Critical Sections succeeding Fast Path")) {
			setRead_accesses_outside_CSFP(getVal(getRead_accesses_outside_CSFP(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Holding Lock during Access Event")) {
			setHold_locks(getVal(getHold_locks(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("One Lock Held")) {
			setOne_lock_held(getVal(getOne_lock_held(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Two Nested Locks Held")) {
			setTwo_nestedLocks_held(getVal(getTwo_nestedLocks_held(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("More than two Nested Locks Held")) {
			setMany_nestedLocks_held(getVal(getMany_nestedLocks_held(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Read Rule A Succeed")) {
			setRead_rule_A_succeed(getVal(getRead_rule_A_succeed(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Read Rule A Total Attempts")) {
			setRead_rule_A_total_attempts(getVal(getRead_rule_A_total_attempts(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Write Write Rule A Succeed")) {
			setWrite_write_rule_A_succeed(getVal(getWrite_write_rule_A_succeed(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Write Write Rule A Total Attempts")) {
			setWrite_write_rule_A_total_attempts(getVal(getWrite_write_rule_A_total_attempts(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Write Read Rule A Succeed")) {
			setWrite_read_rule_A_succeed(getVal(getWrite_read_rule_A_succeed(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Write Read Rule A Total Attempts")) {
			setWrite_read_rule_A_total_attempts(getVal(getWrite_read_rule_A_total_attempts(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Clears by CAPO")) {
			setClears_by_capo(getVal(getClears_by_capo(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Read Set Size 0")) {
			setRead_set_size_0(getVal(getRead_set_size_0(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Read Set Size 1")) {
			setRead_set_size_1(getVal(getRead_set_size_1(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Read Set Size Gt 1")) {
			setRead_set_size_gt_1(getVal(getRead_set_size_gt_1(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Write Set Size 0")) {
			setWrite_set_size_0(getVal(getWrite_set_size_0(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Write Set Size 1")) {
			setWrite_set_size_1(getVal(getWrite_set_size_1(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Write Set Size Gt 1")) {
			setWrite_set_size_gt_1(getVal(getWrite_set_size_gt_1(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Read Map Size 0")) {
			setRead_map_size_0(getVal(getRead_map_size_0(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Read Map Size 1")) {
			setRead_map_size_1(getVal(getRead_map_size_1(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Read Map Size 10")) {
			setRead_map_size_10(getVal(getRead_map_size_10(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Read Map Size 100")) {
			setRead_map_size_100(getVal(getRead_map_size_100(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Read Map Size 1000")) {
			setRead_map_size_1000(getVal(getRead_map_size_1000(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Read Map Size Gt 1000")) {
			setRead_map_size_gt_1000(getVal(getRead_map_size_gt_1000(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Write Map Size 0")) {
			setWrite_map_size_0(getVal(getWrite_map_size_0(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Write Map Size 1")) {
			setWrite_map_size_1(getVal(getWrite_map_size_1(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Write Map Size 10")) {
			setWrite_map_size_10(getVal(getWrite_map_size_10(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Write Map Size 100")) {
			setWrite_map_size_100(getVal(getWrite_map_size_100(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Write Map Size 1000")) {
			setWrite_map_size_1000(getVal(getWrite_map_size_1000(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Write Map Size Gt 1000")) {
			setWrite_map_size_gt_1000(getVal(getWrite_map_size_gt_1000(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Rule A Total Successes")) {
			setRule_a_total_successes(getVal(getRule_a_total_successes(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Rule A changed a single thread")) {
			setRule_a_changed_a_single_thread(getVal(getRule_a_changed_a_single_thread(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Rule A changed two threads")) {
			setRule_a_changed_two_threads(getVal(getRule_a_changed_two_threads(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Rule A changed more than two threads")) {
			setRule_a_changed_more_than_two_threads(getVal(getRule_a_changed_more_than_two_threads(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Rule A changed every thread")) {
			setRule_a_changed_every_thread(getVal(getRule_a_changed_every_thread(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Total One Lock Held")) {
			setTotal_one_lock_held(getVal(getTotal_one_lock_held(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Rule A Success One Lock Held")) {
			setRule_a_success_one_lock_held(getVal(getRule_a_success_one_lock_held(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Total Two Locks Held")) {
			setTotal_two_locks_held(getVal(getTotal_two_locks_held(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Rule A Success Inner Lock Held")) {
			setRule_a_success_inner_lock_held(getVal(getRule_a_success_inner_lock_held(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Rule A Success Outer Lock Held")) {
			setRule_a_success_outer_lock_held(getVal(getRule_a_success_outer_lock_held(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Total Many Locks Held")) {
			setTotal_many_locks_held(getVal(getTotal_many_locks_held(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Rule A Success Inner Most Lock Held")) {
			setRule_a_success_inner_most_lock_held(getVal(getRule_a_success_inner_most_lock_held(), eventCount, curr_trial, total_trials));
		} else if (eventType.equals("Rule A Success Outer Most Lock Held")) {
			setRule_a_success_outer_most_lock_held(getVal(getRule_a_success_outer_most_lock_held(), eventCount, curr_trial, total_trials));
		}
	}
	
	public void printCounts(String tool) {
		File outputDir = new File("event-output");
		if (!outputDir.exists()) outputDir.mkdir();
		File benchDir = new File(outputDir+"/"+bench);
		if (!benchDir.exists()) benchDir.mkdir();
		File configDir = new File(benchDir+"/"+config);
		if (!configDir.exists()) configDir.mkdir();
		try {
			if (tool.equals("DC")) {
				PrintWriter input = new PrintWriter(configDir+"/slow_event_counts.txt");
				input.println(bench + " " + config);
				input.println("exit: " + getExit());
				input.println("fake fork: " + getFake_fork());
				input.println("acquire: " + getAcquire());
				input.println("release: " + getRelease());
				input.println("write: " + getWrite());
				input.println("read: " + getRead());
				input.println("fastpath write: " + getWriteFP());
				input.println("fastpath read: " + getReadFP());
				input.println("volatile write: " + getVolatile_write());
				input.println("volatile read: " + getVolatile_read());
				input.println("fork: " + getFork());
				input.println("join: " + getJoin());
				input.println("pre wait: " + getPre_wait());
				input.println("post wait: " + getPost_wait());
				input.println("class init: " + getClass_init());
				input.println("class accessed: " + getClass_access());
				input.println("accesses inside cs: " + getAccesses_inside_CS());
				input.println("accesses outside cs: " + getAccesses_outside_CS());
				input.println("write inside cs: " + getWrite_accesses_inside_CS());
				input.println("write outside cs: " + getWrite_accesses_outside_CS());
				input.println("read inside cs: " + getRead_accesses_inside_CS());
				input.println("read outside cs: " + getRead_accesses_outside_CS());
				input.close();
			}
			if (tool.equals("PIP")) {
				PrintWriter input = new PrintWriter(configDir+"/fast_event_counts.txt");
				input.println(bench + " " + config);
				input.println("read same epoch: " + getRead_same_epoch());
				input.println("read shared same epoch: " + getRead_shared_same_epoch());
				input.println("read exclusive: " + getRead_exclusive());
				input.println("read share: " + getRead_share());
				input.println("read shared: " + getRead_shared());
				input.println("write-read race: " + getWrite_read_race());
				input.println("write same epoch: " + getWrite_same_epoch());
				input.println("write exclusive: " + getWrite_exclusive());
				input.println("write shared: " + getWrite_shared());
				input.println("write-write race: " + getWrite_write_race());
				input.println("read-write race: " + getRead_write_race());
				input.println("shared write race: " + getShared_write_race());
				input.println("read same epoch FP: " + getRead_same_epochFP());
				input.println("read shared same epoch FP: " + getRead_shared_same_epochFP());
				input.println("read exclusive FP: " + getRead_exclusiveFP());
				input.println("read share FP: " + getRead_shareFP());
				input.println("read shared FP: " + getRead_sharedFP());
				input.println("write same epoch FP: " + getWrite_same_epochFP());
				input.println("write exclusive FP: " + getWrite_exclusiveFP());
				input.println("write shared FP: " + getWrite_sharedFP());
				input.println("acquire: " + getAcquire());
				input.println("release: " + getRelease());
				input.println("fork: " + getFork());
				input.println("join: " + getJoin());
				input.println("pre wait: " + getPre_wait());
				input.println("post wait: " + getPost_wait());
				input.println("class init: " + getClass_init());
				input.println("class accessed: " + getClass_access());
				input.println("volatile: " + getVolatile_acc());
				input.println("read fast path taken: " + getRead_fast_path_taken());
				input.println("write fast path taken: " + getWrite_fast_path_taken());
				input.println("read inside crit sec: " + getRead_accesses_inside_CS());
				input.println("read inside crit sec FP: " + getRead_accesses_inside_CSFP());
				input.println("read outside crit sec: " + getRead_accesses_outside_CS());
				input.println("read outside crit sec FP: " + getRead_accesses_outside_CSFP());
				input.println("write inside crit sec: " + getWrite_accesses_inside_CS());
				input.println("write inside crit sec FP: " + getWrite_accesses_inside_CSFP());
				input.println("write outside crit sec: " + getWrite_accesses_outside_CS());
				input.println("write outside crit sec FP: " + getWrite_accesses_outside_CSFP());
				input.println("total reads: " + getTotal_reads());
				input.println("total writes: " + getTotal_writes());
				input.println("total access ops: " + getTotal_access_ops());
				input.println("total ops: " + getTotal_ops());
				input.println("total fast path taken: " + getTotal_fast_path_taken());

				input.println("hold locks: " + getHold_locks());
				input.println("one lock held: " + getOne_lock_held());
				input.println("two nestedLocks held: " + getTwo_nestedLocks_held());
				input.println("many nestedLocks held: " + getMany_nestedLocks_held());
				
				input.println("read rule A succeed: " + getRead_rule_A_succeed());
				input.println("read rule A total attempts: " + getRead_rule_A_total_attempts());
				input.println("write write rule A succeed: " + getWrite_write_rule_A_succeed());
				input.println("write write rule A total attempts: " + getWrite_write_rule_A_total_attempts());
				input.println("write read rule A succeed: " + getWrite_read_rule_A_succeed());
				input.println("write read rule A total attempts: " + getWrite_read_rule_A_total_attempts());
				
				input.println("clears by CAPO: " + getClears_by_capo());
				input.println("read set size 0: " + getRead_set_size_0());
				input.println("read set size 1: " + getRead_set_size_1());
				input.println("read set size gt 1: " + getRead_set_size_gt_1());
				input.println("write set size 0: " + getWrite_set_size_0());
				input.println("write set size 1: " + getWrite_set_size_1());
				input.println("write set size gt 1: " + getWrite_set_size_gt_1());
				input.println("read map size 0: "+ getRead_map_size_0());
				input.println("read map size 1: "+ getRead_map_size_1());
				input.println("read map size 10: "+ getRead_map_size_10());
				input.println("read map size 100: "+ getRead_map_size_100());
				input.println("read map size 1000: "+ getRead_map_size_1000());
				input.println("read map size gt 1000: "+ getRead_map_size_gt_1000());
				input.println("write map size 0: " + getWrite_map_size_0());
				input.println("write map size 1: " + getWrite_map_size_1());
				input.println("write map size 10: " + getWrite_map_size_10());
				input.println("write map size 100: " + getWrite_map_size_100());
				input.println("write map size 1000: " + getWrite_map_size_1000());
				input.println("write map size gt 1000: " + getWrite_map_size_gt_1000());
				
				input.println("rule A total successes: " + getRule_a_total_successes());
				input.println("rule A changed a single thread: " + getRule_a_changed_a_single_thread());
				input.println("rule A changed two threads: " + getRule_a_changed_two_threads());
				input.println("rule A changed more than two threads: " + getRule_a_changed_more_than_two_threads());
				input.println("rule A changed every thread: " + getRule_a_changed_every_thread());
				
				input.println("total one lock held: " + getTotal_one_lock_held());
				input.println("rule A success one lock held: " + getRule_a_success_one_lock_held());
				input.println("total two locks held: " + getTotal_two_locks_held());
				input.println("rule A success inner lock held: " + getRule_a_success_inner_lock_held());
				input.println("rule A success outer lock held: " + getRule_a_success_outer_lock_held());
				input.println("total many locks held: " + getTotal_many_locks_held());
				input.println("rule A success inner most lock held: " + getRule_a_success_inner_most_lock_held());
				input.println("rule A success outer most lock held: " + getRule_a_success_outer_most_lock_held());				
				input.close();
			}
		} catch (FileNotFoundException e) {e.printStackTrace();}
	}
	
	public void recordCounts(BufferedWriter output) throws IOException {
		getEventCounts(output);
		getReadCounts(output);
		getWriteCounts(output);
		getOtherCounts(output);
		getRaceTypeCounts(output);
		
		//Extra counts
		getLocksHeldCounts(output);
		getRuleAChanges(output);
		getRuleALockSuccess(output);
	}
	
	public void getRuleALockSuccess(BufferedWriter output) throws IOException {
		long[] totalEventsHeldLocks = add(getTotal_one_lock_held(), add(getTotal_two_locks_held(), getTotal_many_locks_held()));
		if (!isZero(totalEventsHeldLocks)) {
			output.write("\\newcommand{\\" + bench + "TotalLockHeld}{" + roundTwoSigs(totalEventsHeldLocks) + "}\n");
			output.write("\\newcommand{\\" + bench + "SingleLockHeld}{" + getPercent(getTotal_one_lock_held(), totalEventsHeldLocks) + "}\n");
			output.write("\\newcommand{\\" + bench + "TwoLocksHeld}{" + getPercent(getTotal_two_locks_held(), totalEventsHeldLocks) + "}\n");
			output.write("\\newcommand{\\" + bench + "ManyLocksHeld}{" + getPercent(getTotal_many_locks_held(), totalEventsHeldLocks) + "}\n");
			
			output.write("\\newcommand{\\" + bench + "TotalSingleLockHeld}{" + roundTwoSigs(getTotal_one_lock_held()) + "}\n");
			output.write("\\newcommand{\\" + bench + "SucSingleLockHeld}{" + getPercent(getRule_a_success_one_lock_held(), getTotal_one_lock_held()) + "}\n");
			
			output.write("\\newcommand{\\" + bench + "TotalTwoLocksHeld}{" + roundTwoSigs(getTotal_two_locks_held()) + "}\n");
			double innerSucc = isZero(getTotal_two_locks_held()) ? 0 : getPercent(getRule_a_success_inner_lock_held(), getTotal_two_locks_held());
			double outerSucc = isZero(getTotal_two_locks_held()) ? 0 : getPercent(getRule_a_success_outer_lock_held(), getTotal_two_locks_held());
			output.write("\\newcommand{\\" + bench + "SucInnerLockHeld}{" + innerSucc + "}\n");
			output.write("\\newcommand{\\" + bench + "SucOuterLockHeld}{" + outerSucc + "}\n");
			
			output.write("\\newcommand{\\" + bench + "TotalManyLocksHeld}{" + roundTwoSigs(getTotal_many_locks_held()) + "}\n");
			double innerMostSucc = isZero(getTotal_many_locks_held()) ? 0 : getPercent(getRule_a_success_inner_most_lock_held(), getTotal_many_locks_held());
			double outerMostSucc = isZero(getTotal_many_locks_held()) ? 0 : getPercent(getRule_a_success_outer_most_lock_held(), getTotal_many_locks_held());
			output.write("\\newcommand{\\" + bench + "SucInnerMostLockHeld}{" + innerMostSucc + "}\n");
			output.write("\\newcommand{\\" + bench + "SucOuterMostLockHeld}{" + outerMostSucc + "}\n");
		}
	}
	
	public void getRuleAChanges(BufferedWriter output) throws IOException {
		if (!isZero(getRule_a_total_successes())) {
			output.write("\\newcommand{\\" + bench + "TotalRuleASuc}{" + roundTwoSigs(getRule_a_total_successes()) + "}\n");
			output.write("\\newcommand{\\" + bench + "OneThrChanged}{" + getPercent(getRule_a_changed_a_single_thread(), getRule_a_total_successes()) + "}\n");
			output.write("\\newcommand{\\" + bench + "TwoThrChanged}{" + getPercent(getRule_a_changed_two_threads(), getRule_a_total_successes()) + "}\n");
			output.write("\\newcommand{\\" + bench + "ManyThrChanged}{" + getPercent(getRule_a_changed_more_than_two_threads(), getRule_a_total_successes()) + "}\n");
			output.write("\\newcommand{\\" + bench + "AllThrChanged}{" + getPercent(getRule_a_changed_every_thread(), getRule_a_total_successes()) + "}\n");
		}
	}
	
	public void getLocksHeldCounts(BufferedWriter output) throws IOException {
		if (!isZero(getHold_locks())) {
			output.write("\\newcommand{\\" + bench + "HoldLocksTotal}{" + roundTwoSigs(getHold_locks()) + "}\n");
			output.write("\\newcommand{\\" + bench + "OneLockHeld}{" + getPercent(getOne_lock_held(), getHold_locks()) + "}\n");
			output.write("\\newcommand{\\" + bench + "TwoNestedLocks}{" + getPercent(getTwo_nestedLocks_held(), getHold_locks()) + "}\n");
			output.write("\\newcommand{\\" + bench + "ManyNestedLocks}{" + getPercent(getMany_nestedLocks_held(), getHold_locks()) + "}\n");
		} // else I could print \nra values
	}
	
	public void getRuleACounts(BufferedWriter output) throws IOException {
		if (!isZero(getRead_rule_A_total_attempts())) {
			output.write("\\newcommand{\\" + bench + "ReadRuleASuc}{" + getPercent(getRead_rule_A_succeed(), getRead_rule_A_total_attempts()) + "}\n");
			output.write("\\newcommand{\\" + bench + "ReadRuleATot}{" + roundTwoSigs(getRead_rule_A_total_attempts()) + "}\n");
		}
		if (!isZero(getWrite_write_rule_A_total_attempts())) {
			output.write("\\newcommand{\\" + bench + "WriteWriteRuleASuc}{" + getPercent(getWrite_write_rule_A_succeed(), getWrite_write_rule_A_total_attempts()) + "}\n");
			output.write("\\newcommand{\\" + bench + "WriteWriteRuleATot}{" + roundTwoSigs(getWrite_write_rule_A_total_attempts()) + "}\n");
		}
		if (!isZero(getWrite_read_rule_A_total_attempts())) {
			output.write("\\newcommand{\\" + bench + "WriteReadRuleASuc}{" + getPercent(getWrite_read_rule_A_succeed(), getWrite_read_rule_A_total_attempts()) + "}\n");
			output.write("\\newcommand{\\" + bench + "WriteReadRuleATot}{" + roundTwoSigs(getWrite_read_rule_A_total_attempts()) + "}\n");
		}
	}
	
	public void getCAPOSetCounts(BufferedWriter output) throws IOException {
		long[] totalClears = getClears_by_capo();
		if (!isZero(totalClears)) {
			output.write("\\newcommand{\\" + bench + "TotalSetClears}{" + roundTwoSigs(totalClears) + "}\n");
			output.write("\\newcommand{\\" + bench + "ReadSetZero}{" + getPercent(getRead_set_size_0(), totalClears) + "}\n");
			output.write("\\newcommand{\\" + bench + "ReadSetOne}{" + getPercent(getRead_set_size_1(), totalClears) + "}\n");
			output.write("\\newcommand{\\" + bench + "ReadSetGtOne}{" + getPercent(getRead_set_size_gt_1(), totalClears) + "}\n");
			output.write("\\newcommand{\\" + bench + "WriteSetZero}{" + getPercent(getWrite_set_size_0(), totalClears) + "}\n");
			output.write("\\newcommand{\\" + bench + "WriteSetOne}{" + getPercent(getWrite_set_size_1(), totalClears) + "}\n");
			output.write("\\newcommand{\\" + bench + "WriteSetGtOne}{" + getPercent(getWrite_set_size_gt_1(), totalClears) + "}\n");
		}
	}
	//Note: TotalSetClears and TotalMapClears will be equal since the clear count acts as a counter for both set and map stats.
	public void getCAPOMapCounts(BufferedWriter output) throws IOException {
		long[] totalClears = getClears_by_capo();
		if (!isZero(totalClears)) {
			output.write("\\newcommand{\\" + bench + "TotalMapClears}{" + roundTwoSigs(totalClears) + "}\n");
			output.write("\\newcommand{\\" + bench + "ReadMapZero}{" + getPercent(getRead_map_size_0(), totalClears) + "}\n");
			output.write("\\newcommand{\\" + bench + "ReadMapOne}{" + getPercent(getRead_map_size_1(), totalClears) + "}\n");
			output.write("\\newcommand{\\" + bench + "ReadMapTen}{" + getPercent(getRead_map_size_10(), totalClears) + "}\n");
			output.write("\\newcommand{\\" + bench + "ReadMapHund}{" + getPercent(getRead_map_size_100(), totalClears) + "}\n");
			output.write("\\newcommand{\\" + bench + "ReadMapThou}{" + getPercent(getRead_map_size_1000(), totalClears) + "}\n");
			output.write("\\newcommand{\\" + bench + "ReadMapGtThou}{" + getPercent(getRead_map_size_gt_1000(), totalClears) + "}\n");
			output.write("\\newcommand{\\" + bench + "WriteMapZero}{" + getPercent(getWrite_map_size_0(), totalClears) + "}\n");
			output.write("\\newcommand{\\" + bench + "WriteMapOne}{" + getPercent(getWrite_map_size_1(), totalClears) + "}\n");
			output.write("\\newcommand{\\" + bench + "WriteMapTen}{" + getPercent(getWrite_map_size_10(), totalClears) + "}\n");
			output.write("\\newcommand{\\" + bench + "WriteMapHund}{" + getPercent(getWrite_map_size_100(), totalClears) + "}\n");
			output.write("\\newcommand{\\" + bench + "WriteMapThou}{" + getPercent(getWrite_map_size_1000(), totalClears) + "}\n");
			output.write("\\newcommand{\\" + bench + "WriteMapGtThou}{" + getPercent(getWrite_map_size_gt_1000(), totalClears) + "}\n");
		}
	}
	
	public void getEventCounts(BufferedWriter output) throws IOException {
		//Needs to be a pointwise add.
		long[] totalEvents = add(getTotal_ops(), getTotal_fast_path_taken());
		//Note: total events/reads/writes include race counts. total reads + total writes add up to total access ops
		output.write("\\newcommand{\\" + bench + "EventTotal}{" + roundTwoSigs(totalEvents) + "}\n");
		output.write("\\newcommand{\\" + bench + "NoFPEventTotal}{" + roundTwoSigs(getTotal_ops()) +"}\n");
		output.write("\\newcommand{\\" + bench + "ReadTotal}{" + getPercent(getTotal_reads(), getTotal_ops()) + "}\n");
		output.write("\\newcommand{\\" + bench + "WriteTotal}{" + getPercent(getTotal_writes(), getTotal_ops()) + "}\n");
		long[] noFPRdInCS = sub(getRead_accesses_inside_CS(), getRead_accesses_inside_CSFP());
		long[] noFPWrInCS = sub(getWrite_accesses_inside_CS(), getWrite_accesses_inside_CSFP());
		long[] noFPAccessInCS = add(noFPRdInCS, noFPWrInCS);
		long[] noFPRdOutCS = sub(getRead_accesses_outside_CS(), getRead_accesses_outside_CSFP());
		long[] noFPWrOutCS = sub(getWrite_accesses_outside_CS(), getWrite_accesses_outside_CSFP());
		long[] noFPAccessOutCS = add(noFPRdOutCS, noFPWrOutCS);
		long[] honestTotalWrites = sub(getTotal_writes(), getWrite_write_race());
		long[] honestTotalAccesses = add(getTotal_reads(), honestTotalWrites);
		output.write("\\newcommand{\\" + bench + "NoFPAccessInCS}{" + getPercent(noFPAccessInCS, honestTotalAccesses) + "}\n");
		output.write("\\newcommand{\\" + bench + "NoFPAccessOutCS}{" + getPercent(noFPAccessOutCS, honestTotalAccesses) + "}\n");
		long[] acqRelEvents = add(getAcquire(), getRelease());
		output.write("\\newcommand{\\" + bench + "AcqRelTotal}{" + getPercent(acqRelEvents, getTotal_ops()) + "}\n");		
		long[] otherEvents = sub(sub(getTotal_ops(), getTotal_access_ops()), acqRelEvents);
		output.write("\\newcommand{\\" + bench + "OtherTotal}{" + getPercent(otherEvents, getTotal_ops()) + "}\n");
	}
	
	public void getReadCounts(BufferedWriter output) throws IOException {
		//Note: noFPReadTotal should be the same as readTotal, just want to distinguish getReadCounts' total from AccessCounts' read total
		output.write("\\newcommand{\\" + bench + "NoFPReadTotal}{" + roundTwoSigs(getTotal_reads()) + "}\n");
		long[] noFPRdInCS = sub(getRead_accesses_inside_CS(), getRead_accesses_inside_CSFP());
		output.write("\\newcommand{\\" + bench + "ReadInCS}{" + getPercent(noFPRdInCS, getTotal_reads()) + "}\n");
		long[] noFPRdOutCS = sub(getRead_accesses_outside_CS(), getRead_accesses_outside_CSFP());
		output.write("\\newcommand{\\" + bench + "ReadOutCS}{" + getPercent(noFPRdOutCS, getTotal_reads()) + "}\n");
		output.write("\\newcommand{\\" + bench + "ReadSameEp}{" + getPercent(getRead_same_epoch(), getTotal_reads()) + "}\n");
		output.write("\\newcommand{\\" + bench + "ReadSharedSameEp}{" + getPercent(getRead_shared_same_epoch(), getTotal_reads()) + "}\n");
		output.write("\\newcommand{\\" + bench + "ReadExclusive}{" + getPercent(getRead_exclusive(), getTotal_reads()) + "}\n");
		output.write("\\newcommand{\\" + bench + "ReadShare}{" + getPercent(getRead_share(), getTotal_reads()) + "}\n");
		output.write("\\newcommand{\\" + bench + "ReadShared}{" + getPercent(getRead_shared(), getTotal_reads()) + "}\n");
	}
	
	public void getWriteCounts(BufferedWriter output) throws IOException {
		long[] honestTotalWrites = sub(getTotal_writes(), getWrite_write_race());
		output.write("\\newcommand{\\" + bench + "NoFPHonestWriteTotal}{" + roundTwoSigs(honestTotalWrites) + "}\n");
		long[] noFPWrInCS = sub(getWrite_accesses_inside_CS(), getWrite_accesses_inside_CSFP());
		output.write("\\newcommand{\\" + bench + "WriteInCS}{" + getPercent(noFPWrInCS, honestTotalWrites) + "}\n");
		long[] noFPWrOutCS = sub(getWrite_accesses_outside_CS(), getWrite_accesses_outside_CSFP());
		output.write("\\newcommand{\\" + bench + "WriteOutCS}{" + getPercent(noFPWrOutCS, honestTotalWrites) + "}\n");
		//Note: noFPWriteTotal should be the same as writeTotal, just want to distinguish getWriteCounts' total from AccessCounts' write total
		output.write("\\newcommand{\\" + bench + "NoFPWriteTotal}{" + roundTwoSigs(getTotal_writes()) + "}\n");
		output.write("\\newcommand{\\" + bench + "WriteSameEp}{" + getPercent(getWrite_same_epoch(), getTotal_writes()) + "}\n");
		output.write("\\newcommand{\\" + bench + "WriteExclusive}{" + getPercent(getWrite_exclusive(), getTotal_writes()) + "}\n");
		output.write("\\newcommand{\\" + bench + "WriteShared}{" + getPercent(getWrite_shared(), getTotal_writes()) + "}\n");
	}
	
	public void getOtherCounts(BufferedWriter output) throws IOException {
		//Note: noFPOtherTotal should be the same as otherTotal, just want to distinguish getOtherCounts' total from AccessCounts' other total
		long[] otherEvents = sub(getTotal_ops(), getTotal_access_ops());
		output.write("\\newcommand{\\" + bench + "NoFPOtherTotal}{" + getAvg(otherEvents) + "}\n");
		long[] acqRelEvents = add(getAcquire(), getRelease());
		output.write("\\newcommand{\\" + bench + "AcqRelOtherTotal}{" + getPercent(acqRelEvents, otherEvents) + "}\n");
		otherEvents = sub(otherEvents, acqRelEvents);
		output.write("\\newcommand{\\" + bench + "NoAcqRelOtherTotal}{" + getAvg(otherEvents) + "}\n");
		output.write("\\newcommand{\\" + bench + "Fork}{" + getPercent(getFork(), otherEvents) + "}\n");
		output.write("\\newcommand{\\" + bench + "Join}{" + getPercent(getJoin(), otherEvents) + "}\n");
		output.write("\\newcommand{\\" + bench + "PreWait}{" + getPercent(getPre_wait(), otherEvents) + "}\n");
		output.write("\\newcommand{\\" + bench + "PostWait}{" + getPercent(getPost_wait(), otherEvents) + "}\n");
		output.write("\\newcommand{\\" + bench + "VolatileTotal}{" + getPercent(getVolatile_acc(), otherEvents) + "}\n");
		output.write("\\newcommand{\\" + bench + "ClassInit}{" + getPercent(getClass_init(), otherEvents) + "}\n");
		output.write("\\newcommand{\\" + bench + "ClassAccess}{" + getPercent(getClass_access(), otherEvents) + "}\n");
	}
	
	public void getRaceTypeCounts(BufferedWriter output) throws IOException {
		long[] raceTotal = add(add(add(getWrite_read_race(), getWrite_write_race()), getRead_write_race()), getShared_write_race());
		output.write("\\newcommand{\\" + bench + "RaceTotal}{" + (isZero(raceTotal) ? "\\rna" : getAvg(raceTotal)) + "}\n");
		output.write("\\newcommand{\\" + bench + "WrRdRace}{" + (isZero(raceTotal) ? "\\rna" : getPercent(getWrite_read_race(), raceTotal)) + "}\n");
		output.write("\\newcommand{\\" + bench + "WrWrRace}{" + (isZero(raceTotal) ? "\\rna" : getPercent(getWrite_write_race(), raceTotal)) + "}\n");
		output.write("\\newcommand{\\" + bench + "RdWrRace}{" + (isZero(raceTotal) ? "\\rna" : getPercent(getRead_write_race(), raceTotal)) + "}\n");
		output.write("\\newcommand{\\" + bench + "RdShWrRace}{" + (isZero(raceTotal) ? "\\rna" : getPercent(getShared_write_race(), raceTotal)) + "}\n");
	}
	
	public static double calcCI(double[] data) {
		if (data == null || isZero(data) || data.length == 1) return 0;
		
		SummaryStatistics stats = new SummaryStatistics();
        for (double val : data) {
            stats.addValue(val);
        }

        // Calculate 95% confidence interval
        return calcMeanCI(stats, 0.95);
	}
	public static double calcCI(long[] data) {
		if (data == null || isZero(data) || data.length == 1) return 0;
		
		SummaryStatistics stats = new SummaryStatistics();
        for (long val : data) {
            stats.addValue(val);
        }

        // Calculate 95% confidence interval
        return calcMeanCI(stats, 0.95);
	}
	
	private static double calcMeanCI(SummaryStatistics stats, double level) {
        try {
            // Create T Distribution with N-1 degrees of freedom
            TDistribution tDist = new TDistribution(stats.getN() - 1);
            // Calculate critical value
            double critVal = 1.95996;//95% confidence intervals//tDist.inverseCumulativeProbability(1.0 - (1 - level) / 2);
            // Calculate confidence interval
            return critVal * stats.getStandardDeviation() / Math.sqrt(stats.getN());
        } catch (MathIllegalArgumentException e) {
            return Double.NaN;
        }
    }
	
	public static long getAvg(long[] array) {
		double avg = 0; 
		for (int i = 0; i < array.length; i++) {
			avg += array[i];
		}
		avg /= (double) array.length;
		if ((avg - Math.floor(avg)) >= 0.5) {
			avg = Math.floor(avg) + 1;
		} else {
			avg = Math.floor(avg);
		}
		return (long) avg;
	}
	
	public static double getAvg(double[] array) {
		double avg = 0;
		for (int i = 0; i < array.length; i++) {
			avg += array[i];
		}
		avg /= (double) array.length;
		return avg;
	}
	
	public static int getAvg(int[] array) {
		double avg = 0;
		for (int i = 0; i < array.length; i++) {
			avg += array[i];
		}
		avg /= (double) array.length;
		if ((avg - Math.floor(avg)) >= 0.5) {
			avg = Math.floor(avg) + 1;
		} else {
			avg = Math.floor(avg);
		}
		return (int) avg;
	}
	
	public double getPercent(long[] val, long[] total) {
		return BenchmarkInfo.getThreeSigsDouble(((getAvg(val)/(double)getAvg(total))*100));
	}
	
	public String roundTwoSigs(long[] val) {
		double rounded = BenchmarkInfo.getTwoSigsDouble(getAvg(val)/(double)1000000);
		if (rounded < 1) {
			return Double.toString(rounded);
		}
		String roundedString = Double.toString(rounded);
		return roundedString.length() > 3 ? BenchmarkInfo.getParenthesis(roundedString.substring(0, roundedString.length()-2)) : roundedString;
	}
	
	public static long[] add(long[] array1, long[] array2) {
		if (isZero(array1)) return array2;
		if (isZero(array2)) return array1;
		long[] arrayAdd = new long[array1.length];
		for(int i = 0; i < array1.length; i++) {
			arrayAdd[i] = array1[i] + array2[i];
		}
		return arrayAdd;
	}
	
	public static long[] sub(long[] array1, long[] array2) {
		long[] arraySub = new long[array1.length];
		for(int i = 0; i < array1.length; i++) {
			arraySub[i] = array1[i] - array2[i];
		}
		return arraySub;
	}
	
	public static boolean isZero(long[] array) {
		if (array == null) return true;
		for (int i = 0; i < array.length; i++) {
			if (array[i] != 0) {
				return false;
			}
		}
		return true;
	}
	
	public static boolean isZero(double[] array) {
		if (array == null) return true;
		for (int i = 0; i < array.length; i++) {
			if (array[i] != 0) {
				return false;
			}
		}
		return true;
	}

	public long[] getAcquire() {
		return acquire;
	}

	public void setAcquire(long[] acquire) {
		this.acquire = acquire;
	}

	public long[] getRelease() {
		return release;
	}

	public void setRelease(long[] release) {
		this.release = release;
	}

	public long[] getFork() {
		return fork;
	}

	public void setFork(long[] fork) {
		this.fork = fork;
	}

	public long[] getJoin() {
		return join;
	}

	public void setJoin(long[] join) {
		this.join = join;
	}

	public long[] getBarrier() {
		return barrier;
	}

	public void setBarrier(long[] barrier) {
		this.barrier = barrier;
	}

	public long[] getPre_wait() {
		return pre_wait;
	}

	public void setPre_wait(long[] pre_wait) {
		this.pre_wait = pre_wait;
	}

	public long[] getPost_wait() {
		return post_wait;
	}

	public void setPost_wait(long[] post_wait) {
		this.post_wait = post_wait;
	}

	public long[] getClass_init() {
		return class_init;
	}

	public void setClass_init(long[] class_init) {
		this.class_init = class_init;
	}

	public long[] getClass_access() {
		return class_access;
	}

	public void setClass_access(long[] class_access) {
		this.class_access = class_access;
	}

	public long[] getWrite_accesses_inside_CS() {
		return write_accesses_inside_CS;
	}

	public void setWrite_accesses_inside_CS(long[] write_accesses_inside_CS) {
		this.write_accesses_inside_CS = write_accesses_inside_CS;
	}

	public long[] getWrite_accesses_outside_CS() {
		return write_accesses_outside_CS;
	}

	public void setWrite_accesses_outside_CS(long[] write_accesses_outside_CS) {
		this.write_accesses_outside_CS = write_accesses_outside_CS;
	}

	public long[] getRead_accesses_inside_CS() {
		return read_accesses_inside_CS;
	}

	public void setRead_accesses_inside_CS(long[] read_accesses_inside_CS) {
		this.read_accesses_inside_CS = read_accesses_inside_CS;
	}

	public long[] getRead_accesses_outside_CS() {
		return read_accesses_outside_CS;
	}

	public void setRead_accesses_outside_CS(long[] read_accesses_outside_CS) {
		this.read_accesses_outside_CS = read_accesses_outside_CS;
	}

	public long[] getOther() {
		return other;
	}

	public void setOther(long[] other) {
		this.other = other;
	}

	public long[] getTotal_reads() {
		return total_reads;
	}

	public void setTotal_reads(long[] total_reads) {
		this.total_reads = total_reads;
	}

	public long[] getTotal_writes() {
		return total_writes;
	}

	public void setTotal_writes(long[] total_writes) {
		this.total_writes = total_writes;
	}

	public long[] getTotal_access_ops() {
		return total_access_ops;
	}

	public void setTotal_access_ops(long[] total_access_ops) {
		this.total_access_ops = total_access_ops;
	}

	public long[] getTotal_ops() {
		return total_ops;
	}

	public void setTotal_ops(long[] total_ops) {
		this.total_ops = total_ops;
	}

	public long[] getTotal_fast_path_taken() {
		return total_fast_path_taken;
	}

	public void setTotal_fast_path_taken(long[] total_fast_path_taken) {
		this.total_fast_path_taken = total_fast_path_taken;
	}

	public long[] getRead() {
		return read;
	}

	public void setRead(long[] read) {
		this.read = read;
	}

	public long[] getWrite() {
		return write;
	}

	public void setWrite(long[] write) {
		this.write = write;
	}

	public long[] getVolatile_write() {
		return volatile_write;
	}

	public void setVolatile_write(long[] volatile_write) {
		this.volatile_write = volatile_write;
	}

	public long[] getVolatile_read() {
		return volatile_read;
	}

	public void setVolatile_read(long[] volatile_read) {
		this.volatile_read = volatile_read;
	}

	public long[] getExit() {
		return exit;
	}

	public void setExit(long[] exit) {
		this.exit = exit;
	}

	public long[] getDummy() {
		return dummy;
	}

	public void setDummy(long[] dummy) {
		this.dummy = dummy;
	}

	public long[] getFake_fork() {
		return fake_fork;
	}

	public void setFake_fork(long[] fake_fork) {
		this.fake_fork = fake_fork;
	}

	public long[] getWriteFP() {
		return writeFP;
	}

	public void setWriteFP(long[] writeFP) {
		this.writeFP = writeFP;
	}

	public long[] getReadFP() {
		return readFP;
	}

	public void setReadFP(long[] readFP) {
		this.readFP = readFP;
	}

	public long[] getAccesses_inside_CS() {
		return accesses_inside_CS;
	}

	public void setAccesses_inside_CS(long[] accesses_inside_CS) {
		this.accesses_inside_CS = accesses_inside_CS;
	}

	public long[] getAccesses_outside_CS() {
		return accesses_outside_CS;
	}

	public void setAccesses_outside_CS(long[] accesses_outside_CS) {
		this.accesses_outside_CS = accesses_outside_CS;
	}

	public long[] getTotal_volatiles() {
		return total_volatiles;
	}

	public void setTotal_volatiles(long[] total_volatiles) {
		this.total_volatiles = total_volatiles;
	}

	public long[] getRead_same_epoch() {
		return read_same_epoch;
	}

	public void setRead_same_epoch(long[] read_same_epoch) {
		this.read_same_epoch = read_same_epoch;
	}

	public long[] getRead_shared_same_epoch() {
		return read_shared_same_epoch;
	}

	public void setRead_shared_same_epoch(long[] read_shared_same_epoch) {
		this.read_shared_same_epoch = read_shared_same_epoch;
	}

	public long[] getRead_exclusive() {
		return read_exclusive;
	}

	public void setRead_exclusive(long[] read_exclusive) {
		this.read_exclusive = read_exclusive;
	}

	public long[] getRead_share() {
		return read_share;
	}

	public void setRead_share(long[] read_share) {
		this.read_share = read_share;
	}

	public long[] getRead_shared() {
		return read_shared;
	}

	public void setRead_shared(long[] read_shared) {
		this.read_shared = read_shared;
	}

	public long[] getWrite_read_race() {
		return write_read_race;
	}

	public void setWrite_read_race(long[] write_read_race) {
		this.write_read_race = write_read_race;
	}

	public long[] getWrite_same_epoch() {
		return write_same_epoch;
	}

	public void setWrite_same_epoch(long[] write_same_epoch) {
		this.write_same_epoch = write_same_epoch;
	}

	public long[] getWrite_exclusive() {
		return write_exclusive;
	}

	public void setWrite_exclusive(long[] write_exclusive) {
		this.write_exclusive = write_exclusive;
	}

	public long[] getWrite_shared() {
		return write_shared;
	}

	public void setWrite_shared(long[] write_shared) {
		this.write_shared = write_shared;
	}

	public long[] getWrite_write_race() {
		return write_write_race;
	}

	public void setWrite_write_race(long[] write_write_race) {
		this.write_write_race = write_write_race;
	}

	public long[] getRead_write_race() {
		return read_write_race;
	}

	public void setRead_write_race(long[] read_write_race) {
		this.read_write_race = read_write_race;
	}

	public long[] getShared_write_race() {
		return shared_write_race;
	}

	public void setShared_write_race(long[] shared_write_race) {
		this.shared_write_race = shared_write_race;
	}

	public long[] getVolatile_acc() {
		return volatile_acc;
	}

	public void setVolatile_acc(long[] volatile_acc) {
		this.volatile_acc = volatile_acc;
	}

	public long[] getRead_fast_path_taken() {
		return read_fast_path_taken;
	}

	public void setRead_fast_path_taken(long[] read_fast_path_taken) {
		this.read_fast_path_taken = read_fast_path_taken;
	}

	public long[] getWrite_fast_path_taken() {
		return write_fast_path_taken;
	}

	public void setWrite_fast_path_taken(long[] write_fast_path_taken) {
		this.write_fast_path_taken = write_fast_path_taken;
	}

	public long[] getRead_same_epochFP() {
		return read_same_epochFP;
	}

	public void setRead_same_epochFP(long[] read_same_epochFP) {
		this.read_same_epochFP = read_same_epochFP;
	}

	public long[] getRead_shared_same_epochFP() {
		return read_shared_same_epochFP;
	}

	public void setRead_shared_same_epochFP(long[] read_shared_same_epochFP) {
		this.read_shared_same_epochFP = read_shared_same_epochFP;
	}

	public long[] getRead_exclusiveFP() {
		return read_exclusiveFP;
	}

	public void setRead_exclusiveFP(long[] read_exclusiveFP) {
		this.read_exclusiveFP = read_exclusiveFP;
	}

	public long[] getRead_shareFP() {
		return read_shareFP;
	}

	public void setRead_shareFP(long[] read_shareFP) {
		this.read_shareFP = read_shareFP;
	}

	public long[] getRead_sharedFP() {
		return read_sharedFP;
	}

	public void setRead_sharedFP(long[] read_sharedFP) {
		this.read_sharedFP = read_sharedFP;
	}

	public long[] getWrite_same_epochFP() {
		return write_same_epochFP;
	}

	public void setWrite_same_epochFP(long[] write_same_epochFP) {
		this.write_same_epochFP = write_same_epochFP;
	}

	public long[] getWrite_exclusiveFP() {
		return write_exclusiveFP;
	}

	public void setWrite_exclusiveFP(long[] write_exclusiveFP) {
		this.write_exclusiveFP = write_exclusiveFP;
	}

	public long[] getWrite_sharedFP() {
		return write_sharedFP;
	}

	public void setWrite_sharedFP(long[] write_sharedFP) {
		this.write_sharedFP = write_sharedFP;
	}

	public long[] getWrite_accesses_inside_CSFP() {
		return write_accesses_inside_CSFP;
	}

	public void setWrite_accesses_inside_CSFP(long[] write_accesses_inside_CSFP) {
		this.write_accesses_inside_CSFP = write_accesses_inside_CSFP;
	}

	public long[] getWrite_accesses_outside_CSFP() {
		return write_accesses_outside_CSFP;
	}

	public void setWrite_accesses_outside_CSFP(long[] write_accesses_outside_CSFP) {
		this.write_accesses_outside_CSFP = write_accesses_outside_CSFP;
	}

	public long[] getRead_accesses_inside_CSFP() {
		return read_accesses_inside_CSFP;
	}

	public void setRead_accesses_inside_CSFP(long[] read_accesses_inside_CSFP) {
		this.read_accesses_inside_CSFP = read_accesses_inside_CSFP;
	}

	public long[] getRead_accesses_outside_CSFP() {
		return read_accesses_outside_CSFP;
	}

	public void setRead_accesses_outside_CSFP(long[] read_accesses_outside_CSFP) {
		this.read_accesses_outside_CSFP = read_accesses_outside_CSFP;
	}

	public long[] getHold_locks() {
		return hold_locks;
	}

	public void setHold_locks(long[] hold_locks) {
		this.hold_locks = hold_locks;
	}

	public long[] getOne_lock_held() {
		return one_lock_held;
	}

	public void setOne_lock_held(long[] one_lock_held) {
		this.one_lock_held = one_lock_held;
	}

	public long[] getTwo_nestedLocks_held() {
		return two_nestedLocks_held;
	}

	public void setTwo_nestedLocks_held(long[] two_nestedLocks_held) {
		this.two_nestedLocks_held = two_nestedLocks_held;
	}

	public long[] getMany_nestedLocks_held() {
		return many_nestedLocks_held;
	}

	public void setMany_nestedLocks_held(long[] many_nestedLocks_held) {
		this.many_nestedLocks_held = many_nestedLocks_held;
	}

	public long[] getRead_rule_A_succeed() {
		return read_rule_A_succeed;
	}

	public void setRead_rule_A_succeed(long[] read_rule_A_succeed) {
		this.read_rule_A_succeed = read_rule_A_succeed;
	}

	public long[] getRead_rule_A_total_attempts() {
		return read_rule_A_total_attempts;
	}

	public void setRead_rule_A_total_attempts(long[] read_rule_A_total_attempts) {
		this.read_rule_A_total_attempts = read_rule_A_total_attempts;
	}

	public long[] getWrite_write_rule_A_succeed() {
		return write_write_rule_A_succeed;
	}

	public void setWrite_write_rule_A_succeed(long[] write_write_rule_A_succeed) {
		this.write_write_rule_A_succeed = write_write_rule_A_succeed;
	}

	public long[] getWrite_write_rule_A_total_attempts() {
		return write_write_rule_A_total_attempts;
	}

	public void setWrite_write_rule_A_total_attempts(long[] write_write_rule_A_total_attempts) {
		this.write_write_rule_A_total_attempts = write_write_rule_A_total_attempts;
	}

	public long[] getWrite_read_rule_A_succeed() {
		return write_read_rule_A_succeed;
	}

	public void setWrite_read_rule_A_succeed(long[] write_read_rule_A_succeed) {
		this.write_read_rule_A_succeed = write_read_rule_A_succeed;
	}

	public long[] getWrite_read_rule_A_total_attempts() {
		return write_read_rule_A_total_attempts;
	}

	public void setWrite_read_rule_A_total_attempts(long[] write_read_rule_A_total_attempts) {
		this.write_read_rule_A_total_attempts = write_read_rule_A_total_attempts;
	}

	public long[] getClears_by_capo() {
		return clears_by_capo;
	}

	public void setClears_by_capo(long[] clears_by_capo) {
		this.clears_by_capo = clears_by_capo;
	}

	public long[] getRead_set_size_0() {
		return read_set_size_0;
	}

	public void setRead_set_size_0(long[] read_set_size_0) {
		this.read_set_size_0 = read_set_size_0;
	}

	public long[] getRead_set_size_1() {
		return read_set_size_1;
	}

	public void setRead_set_size_1(long[] read_set_size_1) {
		this.read_set_size_1 = read_set_size_1;
	}

	public long[] getRead_set_size_gt_1() {
		return read_set_size_gt_1;
	}

	public void setRead_set_size_gt_1(long[] read_set_size_gt_1) {
		this.read_set_size_gt_1 = read_set_size_gt_1;
	}

	public long[] getWrite_set_size_0() {
		return write_set_size_0;
	}

	public void setWrite_set_size_0(long[] write_set_size_0) {
		this.write_set_size_0 = write_set_size_0;
	}

	public long[] getWrite_set_size_1() {
		return write_set_size_1;
	}

	public void setWrite_set_size_1(long[] write_set_size_1) {
		this.write_set_size_1 = write_set_size_1;
	}

	public long[] getWrite_set_size_gt_1() {
		return write_set_size_gt_1;
	}

	public void setWrite_set_size_gt_1(long[] write_set_size_gt_1) {
		this.write_set_size_gt_1 = write_set_size_gt_1;
	}

	public long[] getRead_map_size_0() {
		return read_map_size_0;
	}

	public void setRead_map_size_0(long[] read_map_size_0) {
		this.read_map_size_0 = read_map_size_0;
	}

	public long[] getRead_map_size_1() {
		return read_map_size_1;
	}

	public void setRead_map_size_1(long[] read_map_size_1) {
		this.read_map_size_1 = read_map_size_1;
	}

	public long[] getRead_map_size_10() {
		return read_map_size_10;
	}

	public void setRead_map_size_10(long[] read_map_size_10) {
		this.read_map_size_10 = read_map_size_10;
	}

	public long[] getRead_map_size_100() {
		return read_map_size_100;
	}

	public void setRead_map_size_100(long[] read_map_size_100) {
		this.read_map_size_100 = read_map_size_100;
	}

	public long[] getRead_map_size_1000() {
		return read_map_size_1000;
	}

	public void setRead_map_size_1000(long[] read_map_size_1000) {
		this.read_map_size_1000 = read_map_size_1000;
	}

	public long[] getRead_map_size_gt_1000() {
		return read_map_size_gt_1000;
	}

	public void setRead_map_size_gt_1000(long[] read_map_size_gt_1000) {
		this.read_map_size_gt_1000 = read_map_size_gt_1000;
	}

	public long[] getWrite_map_size_0() {
		return write_map_size_0;
	}

	public void setWrite_map_size_0(long[] write_map_size_0) {
		this.write_map_size_0 = write_map_size_0;
	}

	public long[] getWrite_map_size_1() {
		return write_map_size_1;
	}

	public void setWrite_map_size_1(long[] write_map_size_1) {
		this.write_map_size_1 = write_map_size_1;
	}

	public long[] getWrite_map_size_10() {
		return write_map_size_10;
	}

	public void setWrite_map_size_10(long[] write_map_size_10) {
		this.write_map_size_10 = write_map_size_10;
	}

	public long[] getWrite_map_size_100() {
		return write_map_size_100;
	}

	public void setWrite_map_size_100(long[] write_map_size_100) {
		this.write_map_size_100 = write_map_size_100;
	}

	public long[] getWrite_map_size_1000() {
		return write_map_size_1000;
	}

	public void setWrite_map_size_1000(long[] write_map_size_1000) {
		this.write_map_size_1000 = write_map_size_1000;
	}

	public long[] getWrite_map_size_gt_1000() {
		return write_map_size_gt_1000;
	}

	public void setWrite_map_size_gt_1000(long[] write_map_size_gt_1000) {
		this.write_map_size_gt_1000 = write_map_size_gt_1000;
	}
	
	public long[] getRule_a_total_successes() {
		return rule_a_total_successes;
	}

	public void setRule_a_total_successes(long[] rule_a_total_successes) {
		this.rule_a_total_successes = rule_a_total_successes;
	}

	public long[] getRule_a_changed_a_single_thread() {
		return rule_a_changed_a_single_thread;
	}

	public void setRule_a_changed_a_single_thread(long[] rule_a_changed_a_single_thread) {
		this.rule_a_changed_a_single_thread = rule_a_changed_a_single_thread;
	}

	public long[] getRule_a_changed_two_threads() {
		return rule_a_changed_two_threads;
	}

	public void setRule_a_changed_two_threads(long[] rule_a_changed_two_threads) {
		this.rule_a_changed_two_threads = rule_a_changed_two_threads;
	}

	public long[] getRule_a_changed_more_than_two_threads() {
		return rule_a_changed_more_than_two_threads;
	}

	public void setRule_a_changed_more_than_two_threads(long[] rule_a_changed_more_than_two_threads) {
		this.rule_a_changed_more_than_two_threads = rule_a_changed_more_than_two_threads;
	}

	public long[] getRule_a_changed_every_thread() {
		return rule_a_changed_every_thread;
	}

	public void setRule_a_changed_every_thread(long[] rule_a_changed_every_thread) {
		this.rule_a_changed_every_thread = rule_a_changed_every_thread;
	}

	public long[] getTotal_one_lock_held() {
		return total_one_lock_held;
	}

	public void setTotal_one_lock_held(long[] total_one_lock_held) {
		this.total_one_lock_held = total_one_lock_held;
	}

	public long[] getRule_a_success_one_lock_held() {
		return rule_a_success_one_lock_held;
	}

	public void setRule_a_success_one_lock_held(long[] rule_a_success_one_lock_held) {
		this.rule_a_success_one_lock_held = rule_a_success_one_lock_held;
	}

	public long[] getTotal_two_locks_held() {
		return total_two_locks_held;
	}

	public void setTotal_two_locks_held(long[] total_two_locks_held) {
		this.total_two_locks_held = total_two_locks_held;
	}

	public long[] getRule_a_success_inner_lock_held() {
		return rule_a_success_inner_lock_held;
	}

	public void setRule_a_success_inner_lock_held(long[] rule_a_success_inner_lock_held) {
		this.rule_a_success_inner_lock_held = rule_a_success_inner_lock_held;
	}

	public long[] getRule_a_success_outer_lock_held() {
		return rule_a_success_outer_lock_held;
	}

	public void setRule_a_success_outer_lock_held(long[] rule_a_success_outer_lock_held) {
		this.rule_a_success_outer_lock_held = rule_a_success_outer_lock_held;
	}

	public long[] getTotal_many_locks_held() {
		return total_many_locks_held;
	}

	public void setTotal_many_locks_held(long[] total_many_locks_held) {
		this.total_many_locks_held = total_many_locks_held;
	}

	public long[] getRule_a_success_inner_most_lock_held() {
		return rule_a_success_inner_most_lock_held;
	}

	public void setRule_a_success_inner_most_lock_held(long[] rule_a_success_inner_most_lock_held) {
		this.rule_a_success_inner_most_lock_held = rule_a_success_inner_most_lock_held;
	}

	public long[] getRule_a_success_outer_most_lock_held() {
		return rule_a_success_outer_most_lock_held;
	}

	public void setRule_a_success_outer_most_lock_held(long[] rule_a_success_outer_most_lock_held) {
		this.rule_a_success_outer_most_lock_held = rule_a_success_outer_most_lock_held;
	}
	
}
