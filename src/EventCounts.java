import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class EventCounts {
	private long total;
	private long exit;
	private long fake_fork;
	private long acquire;
	private long release;
	private long write;
	private long read;
	private long fp_write;
	private long fp_read;
	private long vol_write;
	private long vol_read;
	private long start;
	private long join;
	private long pre_wait;
	private long post_wait;
	private long class_init;
	private long class_access;
	private long access_insideCS;
	private long access_outsideCS;
	private long write_insideCS;
	private long write_outsideCS;
	private long read_insideCS;
	private long read_outsideCS;
	
	private long read_same_epoch;
	private long read_shared_same_epoch;
	private long read_exclusive;
	private long read_share;
	private long read_shared;
	private long write_read_race;
	private long write_same_epoch;
	private long write_exclusive;
	private long write_shared;
	private long write_write_race;
	private long read_write_race;
	private long shared_write_race;
	private long fork;
	private long volatile_acc;
	private long read_fast_path_taken;
	private long write_fast_path_taken;
	private long total_reads;
	private long total_writes;
	private long total_access_ops;
	private long total_ops;
	private long total_fast_path_taken;
	
	private String config;
	private String bench;
	
	public EventCounts(String config, String bench) {
		this.config = config;
		this.bench = bench;
	}
	
	public void setEventCounts(String eventType, long eventCount, boolean final_count, int total_trials) {
		if (eventType.equals("Total Events")) {
			setTotal(final_count ? ((getTotal() + eventCount) / total_trials) : (getTotal() + eventCount));
		} else if (eventType.equals("Total Fast Path Taken")) {
			setTotal_fast_path_taken(final_count ? ((getTotal_fast_path_taken() + eventCount) / total_trials) : (getTotal_fast_path_taken() + eventCount));
		} else if (eventType.equals("Total Ops")) {
			setTotal_ops(final_count ? ((getTotal_ops() + eventCount) / total_trials) : (getTotal_ops() + eventCount));
		} else if (eventType.equals("Total Access Ops")) {
			setTotal_access_ops(final_count ? ((getTotal_access_ops() + eventCount) / total_trials) : (getTotal_access_ops() + eventCount));
		} else if (eventType.equals("Total Writes")) {
			setTotal_writes(final_count ? ((getTotal_writes() + eventCount) / total_trials) : (getTotal_writes() + eventCount));
		} else if (eventType.equals("Total Reads")) {
			setTotal_reads(final_count ? ((getTotal_reads() + eventCount) / total_trials) : (getTotal_reads() + eventCount));
		} else if (eventType.equals("Write Fast Path Taken")) {
			setWrite_fast_path_taken(final_count ? ((getWrite_fast_path_taken() + eventCount) / total_trials) : (getWrite_fast_path_taken() + eventCount));
		} else if (eventType.equals("Read Fast Path Taken")) {
			setRead_fast_path_taken(final_count ? ((getRead_fast_path_taken() + eventCount) / total_trials) : (getRead_fast_path_taken() + eventCount));
		} else if (eventType.equals("Fork")) {
			setFork(final_count ? ((getFork() + eventCount) / total_trials) : (getFork() + eventCount));
		} else if (eventType.equals("Shared-Write Error")) {
			setShared_write_race(final_count ? ((getShared_write_race() + eventCount) / total_trials) : (getShared_write_race() + eventCount));
		} else if (eventType.equals("Read-Write Error")) {
			setRead_write_race(final_count ? ((getRead_write_race() + eventCount) / total_trials) : (getRead_write_race() + eventCount));
		} else if (eventType.equals("Write-Write Error")) {
			setWrite_write_race(final_count ? ((getWrite_write_race() + eventCount) / total_trials) : (getWrite_write_race() + eventCount));
		} else if (eventType.equals("Write Shared")) {
			setWrite_shared(final_count ? ((getWrite_shared() + eventCount) / total_trials) : (getWrite_shared() + eventCount));
		} else if (eventType.equals("Write Exclusive")) {
			setWrite_exclusive(final_count ? ((getWrite_exclusive() + eventCount) / total_trials) : (getWrite_exclusive() + eventCount));
		} else if (eventType.equals("Write Same Epoch")) {
			setWrite_same_epoch(final_count ? ((getWrite_same_epoch() + eventCount) / total_trials) : (getWrite_same_epoch() + eventCount));
		} else if (eventType.equals("Write-Read Error")) {
			setWrite_read_race(final_count ? ((getWrite_read_race() + eventCount) / total_trials) : (getWrite_read_race() + eventCount));
		} else if (eventType.equals("Read Shared")) {
			setRead_shared(final_count ? ((getRead_shared() + eventCount) / total_trials) : (getRead_shared() + eventCount));
		} else if (eventType.equals("Read Share")) {
			setRead_share(final_count ? ((getRead_share() + eventCount) / total_trials) : (getRead_share() + eventCount));
		} else if (eventType.equals("Read Exclusive")) {
			setRead_exclusive(final_count ? ((getRead_exclusive() + eventCount) / total_trials) : (getRead_exclusive() + eventCount));
		} else if (eventType.equals("Read Shared Same Epoch")) {
			setRead_shared_same_epoch(final_count ? ((getRead_shared_same_epoch() + eventCount) / total_trials) : (getRead_shared_same_epoch() + eventCount));
		} else if (eventType.equals("Read Same Epoch")) {
			setRead_same_epoch(final_count ? ((getRead_same_epoch() + eventCount) / total_trials) : (getRead_same_epoch() + eventCount));
		} else if (eventType.equals("Exit")) {
			setExit(final_count ? ((getExit() + eventCount) / total_trials) : (getExit() + eventCount));
		} else if (eventType.equals("Fake Fork")) {
			setFake_fork(final_count ? ((getFake_fork() + eventCount) / total_trials) : (getFake_fork() + eventCount));
		} else if (eventType.equals("Acquire")) {
			setAcquire(final_count ? ((getAcquire() + eventCount) / total_trials) : (getAcquire() + eventCount));
		} else if (eventType.equals("Release")) {
			setRelease(final_count ? ((getRelease() + eventCount) / total_trials) : (getRelease() + eventCount));
		} else if (eventType.equals("Write")) {
			setWrite(final_count ? ((getWrite() + eventCount) / total_trials) : (getWrite() + eventCount));
		} else if (eventType.equals("Read")) {
			setRead(final_count ? ((getRead() + eventCount) / total_trials) : (getRead() + eventCount));
		} else if (eventType.equals("WriteFastPath")) {
			setFp_write(final_count ? ((getFp_write() + eventCount) / total_trials) : (getFp_write() + eventCount));
		} else if (eventType.equals("ReadFastPath")) {
			setFp_read(final_count ? ((getFp_read() + eventCount) / total_trials) : (getFp_read() + eventCount));
		} else if (eventType.equals("Volatile Write")) {
			setVol_write(final_count ? ((getVol_write() + eventCount) / total_trials) : (getVol_write() + eventCount));
		} else if (eventType.equals("Volatile Read")) {
			setVol_read(final_count ? ((getVol_read() + eventCount) / total_trials) : (getVol_read() + eventCount));
		} else if (eventType.equals("Start")) {
			setStart(final_count ? ((getStart() + eventCount) / total_trials) : (getStart() + eventCount));
		} else if (eventType.equals("Join")) {
			setJoin(final_count ? ((getJoin() + eventCount) / total_trials) : (getJoin() + eventCount));
		} else if (eventType.equals("Pre Wait")) {
			setPre_wait(final_count ? ((getPre_wait() + eventCount) / total_trials) : (getPre_wait() + eventCount));
		} else if (eventType.equals("Post Wait")) {
			setPost_wait(final_count ? ((getPost_wait() + eventCount) / total_trials) : (getPost_wait() + eventCount));
		} else if (eventType.equals("Class Initialized")) {
			setClass_init(final_count ? ((getClass_init() + eventCount) / total_trials) : (getClass_init() + eventCount));
		} else if (eventType.equals("Class Accessed")) {
			setClass_access(final_count ? ((getClass_access() + eventCount) / total_trials) : (getClass_access() + eventCount));
		} else if (eventType.equals("Volatile")) {
			setVolatile_acc(final_count ? ((getVolatile_acc() + eventCount) / total_trials) : (getVolatile_acc() + eventCount));
		} else if (eventType.equals("Accesses Inside Critical Sections")) {
			setAccess_insideCS(final_count ? ((getAccess_insideCS() + eventCount) / total_trials) : (getAccess_insideCS() + eventCount));
		} else if (eventType.equals("Accesses Outisde Critical Sections")) {
			setAccess_outsideCS(final_count ? ((getAccess_outsideCS() + eventCount) / total_trials) : (getAccess_outsideCS() + eventCount));
		} else if (eventType.equals("Write accesses Inside Critical Sections")) {
			setWrite_insideCS(final_count ? ((getWrite_insideCS() + eventCount) / total_trials) : (getWrite_insideCS() + eventCount));
		} else if (eventType.equals("Write accesses Outside Critical Sections")) {
			setWrite_outsideCS(final_count ? ((getWrite_outsideCS() + eventCount) / total_trials) : (getWrite_outsideCS() + eventCount));
		} else if (eventType.equals("Read accesses Inside Critical Sections")) {
			setRead_insideCS(final_count ? ((getRead_insideCS() + eventCount) / total_trials) : (getRead_insideCS() + eventCount));
		} else if (eventType.equals("Read accesses Outside Critical Sections")) {
			setRead_outsideCS(final_count ? ((getRead_outsideCS() + eventCount) / total_trials) : (getRead_outsideCS() + eventCount));
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
				input.println("total: " + getTotal());
				input.println("exit: " + getExit());
				input.println("fake fork: " + getFake_fork());
				input.println("acquire: " + getAcquire());
				input.println("release: " + getRelease());
				input.println("write: " + getWrite());
				input.println("read: " + getRead());
				input.println("fastpath write: " + getFp_write());
				input.println("fastpath read: " + getFp_read());
				input.println("volatile write: " + getVol_write());
				input.println("volatile read: " + getVol_read());
				input.println("start: " + getStart());
				input.println("join: " + getJoin());
				input.println("pre wait: " + getPre_wait());
				input.println("post wait: " + getPost_wait());
				input.println("class init: " + getClass_init());
				input.println("class accessed: " + getClass_access());
				input.println("accesses inside cs: " + getAccess_insideCS());
				input.println("accesses outside cs: " + getAccess_outsideCS());
				input.println("write inside cs: " + getWrite_insideCS());
				input.println("write outside cs: " + getWrite_outsideCS());
				input.println("read inside cs: " + getRead_insideCS());
				input.println("read outside cs: " + getRead_outsideCS());
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
				input.println("total reads: " + getTotal_reads());
				input.println("total writes: " + getTotal_writes());
				input.println("total access ops: " + getTotal_access_ops());
				input.println("total ops: " + getTotal_ops());
				input.println("total fast path taken: " + getTotal_fast_path_taken());
				input.close();
			}
		} catch (FileNotFoundException e) {e.printStackTrace();}
	}
	
	public long getTotal() {
		return total;
	}
	public void setTotal(long total) {
		this.total = total;
	}
	public long getExit() {
		return exit;
	}
	public void setExit(long exit) {
		this.exit = exit;
	}
	public long getFake_fork() {
		return fake_fork;
	}
	public void setFake_fork(long fake_fork) {
		this.fake_fork = fake_fork;
	}
	public long getAcquire() {
		return acquire;
	}
	public void setAcquire(long acquire) {
		this.acquire = acquire;
	}
	public long getRelease() {
		return release;
	}
	public void setRelease(long release) {
		this.release = release;
	}
	public long getWrite() {
		return write;
	}
	public void setWrite(long write) {
		this.write = write;
	}
	public long getRead() {
		return read;
	}
	public void setRead(long read) {
		this.read = read;
	}
	public long getFp_write() {
		return fp_write;
	}
	public void setFp_write(long fp_write) {
		this.fp_write = fp_write;
	}
	public long getFp_read() {
		return fp_read;
	}
	public void setFp_read(long fp_read) {
		this.fp_read = fp_read;
	}
	public long getVol_write() {
		return vol_write;
	}
	public void setVol_write(long vol_write) {
		this.vol_write = vol_write;
	}
	public long getVol_read() {
		return vol_read;
	}
	public void setVol_read(long vol_read) {
		this.vol_read = vol_read;
	}
	public long getStart() {
		return start;
	}
	public void setStart(long start) {
		this.start = start;
	}
	public long getJoin() {
		return join;
	}
	public void setJoin(long join) {
		this.join = join;
	}
	public long getPre_wait() {
		return pre_wait;
	}
	public void setPre_wait(long pre_wait) {
		this.pre_wait = pre_wait;
	}
	public long getPost_wait() {
		return post_wait;
	}
	public void setPost_wait(long post_wait) {
		this.post_wait = post_wait;
	}
	public long getClass_init() {
		return class_init;
	}
	public void setClass_init(long class_init) {
		this.class_init = class_init;
	}
	public long getClass_access() {
		return class_access;
	}
	public void setClass_access(long class_access) {
		this.class_access = class_access;
	}

	public long getAccess_insideCS() {
		return access_insideCS;
	}

	public void setAccess_insideCS(long access_insideCS) {
		this.access_insideCS = access_insideCS;
	}

	public long getAccess_outsideCS() {
		return access_outsideCS;
	}

	public void setAccess_outsideCS(long access_outsideCS) {
		this.access_outsideCS = access_outsideCS;
	}

	public long getRead_same_epoch() {
		return read_same_epoch;
	}

	public void setRead_same_epoch(long read_same_epoch) {
		this.read_same_epoch = read_same_epoch;
	}

	public long getRead_shared_same_epoch() {
		return read_shared_same_epoch;
	}

	public void setRead_shared_same_epoch(long read_shared_same_epoch) {
		this.read_shared_same_epoch = read_shared_same_epoch;
	}

	public long getRead_exclusive() {
		return read_exclusive;
	}

	public void setRead_exclusive(long read_exclusive) {
		this.read_exclusive = read_exclusive;
	}

	public long getRead_share() {
		return read_share;
	}

	public void setRead_share(long read_share) {
		this.read_share = read_share;
	}

	public long getRead_shared() {
		return read_shared;
	}

	public void setRead_shared(long read_shared) {
		this.read_shared = read_shared;
	}

	public long getWrite_read_race() {
		return write_read_race;
	}

	public void setWrite_read_race(long write_read_race) {
		this.write_read_race = write_read_race;
	}

	public long getWrite_same_epoch() {
		return write_same_epoch;
	}

	public void setWrite_same_epoch(long write_same_epoch) {
		this.write_same_epoch = write_same_epoch;
	}

	public long getWrite_exclusive() {
		return write_exclusive;
	}

	public void setWrite_exclusive(long write_exclusive) {
		this.write_exclusive = write_exclusive;
	}

	public long getWrite_shared() {
		return write_shared;
	}

	public void setWrite_shared(long write_shared) {
		this.write_shared = write_shared;
	}

	public long getWrite_write_race() {
		return write_write_race;
	}

	public void setWrite_write_race(long write_write_race) {
		this.write_write_race = write_write_race;
	}

	public long getRead_write_race() {
		return read_write_race;
	}

	public void setRead_write_race(long read_write_race) {
		this.read_write_race = read_write_race;
	}

	public long getShared_write_race() {
		return shared_write_race;
	}

	public void setShared_write_race(long shared_write_race) {
		this.shared_write_race = shared_write_race;
	}

	public long getFork() {
		return fork;
	}

	public void setFork(long fork) {
		this.fork = fork;
	}

	public long getRead_fast_path_taken() {
		return read_fast_path_taken;
	}

	public void setRead_fast_path_taken(long read_fast_path_taken) {
		this.read_fast_path_taken = read_fast_path_taken;
	}

	public long getWrite_fast_path_taken() {
		return write_fast_path_taken;
	}

	public void setWrite_fast_path_taken(long write_fast_path_taken) {
		this.write_fast_path_taken = write_fast_path_taken;
	}

	public long getTotal_reads() {
		return total_reads;
	}

	public void setTotal_reads(long total_reads) {
		this.total_reads = total_reads;
	}

	public long getTotal_writes() {
		return total_writes;
	}

	public void setTotal_writes(long total_writes) {
		this.total_writes = total_writes;
	}

	public long getTotal_access_ops() {
		return total_access_ops;
	}

	public void setTotal_access_ops(long total_access_ops) {
		this.total_access_ops = total_access_ops;
	}

	public long getTotal_ops() {
		return total_ops;
	}

	public void setTotal_ops(long total_ops) {
		this.total_ops = total_ops;
	}

	public long getTotal_fast_path_taken() {
		return total_fast_path_taken;
	}

	public void setTotal_fast_path_taken(long total_fast_path_taken) {
		this.total_fast_path_taken = total_fast_path_taken;
	}

	public long getWrite_insideCS() {
		return write_insideCS;
	}

	public void setWrite_insideCS(long write_insideCS) {
		this.write_insideCS = write_insideCS;
	}

	public long getWrite_outsideCS() {
		return write_outsideCS;
	}

	public void setWrite_outsideCS(long write_outsideCS) {
		this.write_outsideCS = write_outsideCS;
	}

	public long getRead_insideCS() {
		return read_insideCS;
	}

	public void setRead_insideCS(long read_insideCS) {
		this.read_insideCS = read_insideCS;
	}

	public long getRead_outsideCS() {
		return read_outsideCS;
	}

	public void setRead_outsideCS(long read_outsideCS) {
		this.read_outsideCS = read_outsideCS;
	}

	public long getVolatile_acc() {
		return volatile_acc;
	}

	public void setVolatile_acc(long volatile_acc) {
		this.volatile_acc = volatile_acc;
	}
}
