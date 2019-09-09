package io.kurumi.ntt.cqhttp.response;

public class BaseResponse {

    public String status;

    public Integer retcode;

    public boolean isOk() {

        return "ok".equals(status) || retcode == 0;

    }

    public boolean isFailed() {

        return "failed".equals(status);

    }

}
