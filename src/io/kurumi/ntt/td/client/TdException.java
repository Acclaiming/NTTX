package io.kurumi.ntt.td.client;

import io.kurumi.ntt.td.TdApi;

public class TdException extends RuntimeException {

    private TdApi.Error error;

    public TdException(TdApi.Error error) {

        super(error.message);

        this.error = error;

    }

    public TdApi.Error getError() {

        return error;

    }

}
