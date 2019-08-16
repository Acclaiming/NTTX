package io.kurumi.ntt.cqhttp.response;

public class GetLoginInfoResponse extends BaseResponse {

	public LoginInfo data;

	public static class LoginInfo {

		Integer user_id;
		String nickname;

	}

}
