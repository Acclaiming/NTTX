package io.kurumi.ntt.td.client;
import java.util.LinkedList;
import java.util.List;

public class TdPointData {
	
	public String point;
	public String actionName;
	public int chatType;
	
	public int type = 0;
	public List<Long> context = new LinkedList<>();
	
}
