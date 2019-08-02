package io.kurumi.ntt.db;

import io.kurumi.ntt.model.Msg;

import java.util.LinkedList;
import java.util.HashMap;

public class PointData {

    public int type;
	
	public int step = 0;
 
    public LinkedList<Msg> context = new LinkedList<>();

    public String point;
    public Object data;
    public boolean cancelable = true;

    public PointData with(Msg msg) {

        context.add(msg);

        return this;

    }

    public <T> T data() {

        return (T) data;

    }

    public void onFinish() {

        for (Msg toDelete : context) toDelete.delete();

    }

    public void onCancel(UserData user, Msg msg) {
    }

}
