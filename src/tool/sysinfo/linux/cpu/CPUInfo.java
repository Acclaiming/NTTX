package tool.sysinfo.linux.cpu;

/**
 * Define information, which is needed to get.
 *
 * @author ZHJIE
 */
public interface CPUInfo {

    /**
     * CPU Usage rate
     *
     * @return
     */
    double getCPUUsage(int interval) throws InterruptedException;

}
