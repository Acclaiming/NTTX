package io.kurumi.ntt.cqhttp.response;

public class GetStrangerInfoResponse extends BaseResponse {
	
	public StrangerInfo data;
	
	public static class StrangerInfo {
		
		public int user_id;
		public String nickname;
		public String sex;
		public int age;
		
	}
	
}
