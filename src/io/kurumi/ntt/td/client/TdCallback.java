package io.kurumi.ntt.td.client;

import io.kurumi.ntt.td.TdApi;

@FunctionalInterface
public interface TdCallback<T extends TdApi.Object> {

    void onCallback(boolean isOk, T result, TdApi.Error error);

}
