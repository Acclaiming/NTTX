package io.kurumi.ntt.cqhttp.response;

public class GetFileResponse extends BaseResponse {
	
	public File data;
	
	public static class File {
		
		public String file;
		
	}
	
}
