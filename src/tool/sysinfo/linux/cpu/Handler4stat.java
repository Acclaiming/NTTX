package tool.sysinfo.linux.cpu;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Create one snapshot once new a instance or invoking {@link #setSnapshot()}, 
 * <br>And you could get informations from given methods
 * @author ZHJIE
 *
 */
public class Handler4stat {
	public Snapshot4stat shot = new Snapshot4stat();
	private static final String statPath = "/proc/stat";
	
	/**
	 * Parameter is true will create one snapshot of /proc/stat
	 * <br>If not, use {@link #setSnapshot()} manually.
	 * @param canInit
	 */
	public Handler4stat(boolean canInit) {
		if (canInit) {
			setSnapshot();
		}
	}
	
	/**
	 * Read file /proc/stat, then get and set useful information
	 */
	public void setSnapshot() {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(statPath)));
			String line;
			while ((line=br.readLine().trim()) != null) {
				if (line.startsWith("cpu ")) {
					String[] infoArray = line.split("\\s+");
					for (int i=1; i<infoArray.length; i++) {
						shot.setValues(i, Long.valueOf(infoArray[i]));
					}
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public long getTotalCPUTime() {
		return shot.getStat_user() + shot.getStat_nice() + shot.getStat_system()
				+ shot.getStat_idle() + shot.getStat_iowait() + shot.getStat_irq()
				+ shot.getStat_softirq() + shot.getStat_steal() + shot.getStat_guest();
	}
	
	public long getIdleCPUTime() {
		return shot.getStat_idle();
	}
}
