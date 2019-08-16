package io.kurumi.ntt.cqhttp.response;

import java.util.HashMap;
import cn.hutool.json.JSONObject;

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
