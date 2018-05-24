
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
	private String config;
	private String bench;
	
	public EventCounts(String config, String bench) {
		this.config = config;
		this.bench = bench;
	}
	
	public void setEventCounts(String eventType, long eventCount, boolean final_count, int total_trials) {
		if (eventType.equals("Total Events")) {
			setTotal(final_count ? ((getTotal() + eventCount) / total_trials) : (getTotal() + eventCount));
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
		} else if (eventType.equals("Accesses Inside Critical Sections")) {
			setAccess_insideCS(final_count ? ((getAccess_insideCS() + eventCount) / total_trials) : (getAccess_insideCS() + eventCount));
		} else if (eventType.equals("Accesses Outisde Critical Sections")) {
			setAccess_outsideCS(final_count ? ((getAccess_outsideCS() + eventCount) / total_trials) : (getAccess_outsideCS() + eventCount));
			
		}
	}
	
	public void printCounts() {
		System.out.println(bench + " " + config);
//		System.out.println("total: " + getTotal());
//		System.out.println("exit: " + getExit());
//		System.out.println("fake fork: " + getFake_fork());
//		System.out.println("acquire: " + getAcquire());
//		System.out.println("release: " + getRelease());
//		System.out.println("write: " + getWrite());
//		System.out.println("read: " + getRead());
//		System.out.println("fastpath write: " + getFp_write());
//		System.out.println("fastpath read: " + getFp_read());
//		System.out.println("volatile write: " + getVol_write());
//		System.out.println("volatile read: " + getVol_read());
//		System.out.println("start: " + getStart());
//		System.out.println("join: " + getJoin());
//		System.out.println("pre wait: " + getPre_wait());
//		System.out.println("post wait: " + getPost_wait());
//		System.out.println("class init: " + getClass_init());
//		System.out.println("class accessed: " + getClass_access());
		System.out.println("accesses inside cs: " + getAccess_insideCS());
		System.out.println("accesses outside cs: " + getAccess_outsideCS());
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
}
