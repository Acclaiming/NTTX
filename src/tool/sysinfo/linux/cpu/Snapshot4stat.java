package tool.sysinfo.linux.cpu;

/**
 * Read file /proc/stat and keep its information
 * @author ZHJIE
 *
 */
public class Snapshot4stat {
	private long stat_user;
	private long stat_nice;
	private long stat_system;
	private long stat_idle;
	private long stat_iowait;
	private long stat_irq;
	private long stat_softirq;
	private long stat_steal;
	private long stat_guest;
	
	public long getStat_user() {
		return stat_user;
	}
	
	public void setStat_user(long stat_user) {
		this.stat_user = stat_user;
	}
	
	public long getStat_nice() {
		return stat_nice;
	}
	
	public void setStat_nice(long stat_nice) {
		this.stat_nice = stat_nice;
	}
	
	public long getStat_system() {
		return stat_system;
	}
	
	public void setStat_system(long stat_system) {
		this.stat_system = stat_system;
	}
	
	public long getStat_idle() {
		return stat_idle;
	}
	
	public void setStat_idle(long stat_idle) {
		this.stat_idle = stat_idle;
	}
	
	public long getStat_iowait() {
		return stat_iowait;
	}
	
	public void setStat_iowait(long stat_iowait) {
		this.stat_iowait = stat_iowait;
	}
	
	public long getStat_irq() {
		return stat_irq;
	}
	
	public void setStat_irq(long stat_irq) {
		this.stat_irq = stat_irq;
	}
	
	public long getStat_softirq() {
		return stat_softirq;
	}
	
	public void setStat_softirq(long stat_softirq) {
		this.stat_softirq = stat_softirq;
	}
	
	public long getStat_steal() {
		return stat_steal;
	}
	
	public void setStat_steal(long stat_steal) {
		this.stat_steal = stat_steal;
	}
	
	public long getStat_guest() {
		return stat_guest;
	}
	
	public void setStat_guest(long stat_guest) {
		this.stat_guest = stat_guest;
	}
	
	/**
	 * <ul>Parameter number means set specific object member's value
	 *     <li>1 - user<li>
	 *     <li>2 - nice<li>
	 *     <li>3 - system<li>
	 *     <li>4 - idle<li>
	 *     <li>5 - iowait<li>
	 *     <li>6 - irq<li>
	 *     <li>7 - softirq<li>
	 *     <li>8 - steal<li>
	 *     <li>9 - guest<li>
	 * </ul>
	 * 
	 * @param num
	 */
	public void setValues(int num, long value) {
		switch (num) {
		case 1:
			setStat_user(value);
			break;
		case 2:
			setStat_nice(value);
			break;
		case 3:
			setStat_system(value);
			break;
		case 4:
			setStat_idle(value);
			break;
		case 5:
			setStat_iowait(value);
			break;
		case 6:
			setStat_irq(value);
			break;
		case 7:
			setStat_softirq(value);
			break;
		case 8:
			setStat_steal(value);
			break;
		case 9:
			setStat_guest(value);
			break;

		default:
			// System.err.println("Illegal parameter num");
			break;
		}
	}
	
}
