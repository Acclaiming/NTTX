package com.pengrad.telegrambot.response;

import com.pengrad.telegrambot.model.ResponseParameters;

/**
 * stas
 * 1/13/16.
 */
public class BaseResponse {

    public transient String json;

    private boolean ok;
    private int error_code;
    private String description;
    private ResponseParameters parameters;

    BaseResponse() {
    }

	BaseResponse(String description) {
		
		this.ok = false;
		this.error_code = -1;
		this.description = description;
		
    }
	
    public boolean isOk() {
        return ok;
    }

    public int errorCode() {
        return error_code;
    }

    public String description() {
        return description;
    }

    public ResponseParameters parameters() {
        return parameters;
    }

    @Override
    public String toString() {

        return json;

    }
}
