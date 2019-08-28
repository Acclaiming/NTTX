package io.kurumi.ntt.td.client;

import java.util.HashMap;

public class TdPoint {
	
	public HashMap<Integer,TdPointData> privatePoints = new HashMap<>();

	public HashMap<Long,Group> groupPoints = new HashMap<>();

	public static class Group {

		public HashMap<Integer,TdPointData> points = new HashMap<>();

	}
	
}
