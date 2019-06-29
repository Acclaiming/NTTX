package tool.sysinfo.linux.memory;

/**
 * Snapshot of /proc/meminfo.
 * <br>Member variable's unit: kB
 * @author ZHJIE
 *
 */
public class Snapshot4meminfo {
	private long memTotal;
	private long memFree;
	private long buffers;
	private long cached;
	
	public long getMemTotal() {
		return memTotal;
	}
	
	public void setMemTotal(long memTotal) {
		this.memTotal = memTotal;
	}
	
	public long getMemFree() {
		return memFree;
	}
	
	public void setMemFree(long memFree) {
		this.memFree = memFree;
	}
	
	public long getBuffers() {
		return buffers;
	}
	
	public void setBuffers(long buffers) {
		this.buffers = buffers;
	}
	
	public long getCached() {
		return cached;
	}
	
	public void setCached(long cached) {
		this.cached = cached;
	}
	
}
