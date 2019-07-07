package io.kurumi.ntt.db;

import io.kurumi.ntt.model.Msg;
import java.util.LinkedList;

public class PointData {

	public LinkedList<Msg> context = new LinkedList<>();
	
	public String point;
	public Object data;

	public <T> T data() {
		
		return (T)data;
		
	}
	
	public void onFinish() {
		
		for (Msg toDelete : context) toDelete.delete();
		
	}
	
	public void onCancel(UserData user,Msg msg) {
	}
	
}
