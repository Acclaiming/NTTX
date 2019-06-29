package tool.sysinfo.linux.cpu;

/**
 * Use linux file /proc/stat
 * @author ZHJIE
 *
 */
public class CPUInfoImpl implements CPUInfo {
	
	/**
	 * return average CPU usage between given time interval
	 * @param interval seconds
	 * @return
	 * @throws InterruptedException 
	 */
	public double getCPUUsage(int interval) throws InterruptedException {
		Handler4stat handler4stat = new Handler4stat(true);
		long total = handler4stat.getTotalCPUTime();
		long idle = handler4stat.getIdleCPUTime();
		
		Thread.sleep(interval * 1000);
		
		handler4stat.setSnapshot();
		long total2 = handler4stat.getTotalCPUTime();
		long idle2 = handler4stat .getIdleCPUTime();
		
		if (total2 == total) {
			// 总利用率未发生变化，认为这段时间利用率为0，防止除数为0
			return 0;
		}
		
		return 1 - (double)(idle2 - idle)/(double)(total2 - total);
	}

}
