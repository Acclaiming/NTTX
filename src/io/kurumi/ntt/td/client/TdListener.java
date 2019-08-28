package io.kurumi.ntt.td.client;

import io.kurumi.ntt.td.TdApi;
import io.kurumi.ntt.td.TdApi.Function;
import io.kurumi.ntt.td.TdApi.User;
import io.kurumi.ntt.td.model.TMsg;
import io.kurumi.ntt.db.PointData;
import io.kurumi.ntt.td.TdApi.Object;

public class TdListener extends TdHandler {
	
	public void onMessage(User user,TMsg msg) {
	}
	
	public void onFunction(User user,TMsg msg,String function,String[] params) {
	}
	
	public void onPoint(User user,TMsg message,String point,TdPointData data) {
	}
	
	public void onPayload(User user,TMsg msg,String payload,String[] params) {
	}
	
}
