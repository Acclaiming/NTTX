package io.kurumi.ntt.td.client;

import io.kurumi.ntt.td.TdApi;

public class TdListener implements ITdListener {

	public TdClient client;

	@Override
	public void onInit(TdClient client) {

		this.client = client;

	}

	@Override
	public void onEvent(TdApi.Object event) {

		if (event instanceof TdApi.UpdateAuthorizationState) {

			onUpdateAuthorizationState((TdApi.UpdateAuthorizationState)event);

		} 

	}

	public void onUpdateAuthorizationState(TdApi.UpdateAuthorizationState state) {
	}

}
