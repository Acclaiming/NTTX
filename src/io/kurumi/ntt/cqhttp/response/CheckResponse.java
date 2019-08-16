package io.kurumi.ntt.cqhttp.response;

public class CheckResponse extends BaseResponse {
	
	public Result data;
	
	public static class Result {
		
		public boolean yes;
		
	}
	
}
