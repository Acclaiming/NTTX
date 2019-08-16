package io.kurumi.ntt.cqhttp.response;

public class SendMessageResponse extends BaseResponse {
	
	public Message data;
	
	public static class Message {
		
		public Integer message_id;
		
	}
	
}
