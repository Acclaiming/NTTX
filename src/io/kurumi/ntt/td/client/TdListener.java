package io.kurumi.ntt.td.client;

import io.kurumi.ntt.td.TdApi;
import io.kurumi.ntt.td.TdApi.Function;
import io.kurumi.ntt.td.TdApi.User;
import io.kurumi.ntt.td.model.TMsg;
import io.kurumi.ntt.db.PointData;
import io.kurumi.ntt.td.TdApi.Object;

public class TdListener extends TdHandler {
	
	public void init() {}

	public void registerFunction(String... functions) {

        for (String function : functions) {

            client.functions.put(function,this);

        }

    }

    public void registerAdminFunction(String... functions) {

        for (String function : functions) {

            client.adminFunctions.put(function,this);

        }

    }

    public void registerPayload(String... payloads) {

        for (String payload : payloads) {

            client.payloads.put(payload,this);

        }

    }

    public void registerAdminPayload(String... payloads) {

        for (String payload : payloads) {

            client.adminPayloads.put(payload,this);

        }

    }

    public void registerPoint(String... points) {

        for (String point : points) {

            client.points.put(point,this);

        }

    }

    public void registerCallback(String... points) {

        for (String point : points) {

            client.callbackQuerys.put(point,this);

        }


    }
	
	public void onMessage(User user,TMsg msg) {
	}
	
	public void onFunction(User user,TMsg msg,String function,String[] params) {
	}
	
	public void onPoint(User user,TMsg message,String point,TdPointData data) {
	}
	
	public void onPayload(User user,TMsg msg,String payload,String[] params) {
	}
	
}
