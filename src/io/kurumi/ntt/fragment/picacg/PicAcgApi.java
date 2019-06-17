package io.kurumi.ntt.fragment.picacg;
import cn.hutool.http.*;

public class PicAcgApi {

	public static final String API_KEY = "C69BAF41DA5ABD1FFEDC6D2FEA56B";
	public static final String BASE_URL = "https://picaapi.picacomic.com/";
	public static final String CERT_URL = "picaapi.picacomic.com";
	public static final String CHATROOM = "https://chat.picacomic.com";
	public static final String CHATROOM_GAME = "https://game.picacomic.com";


	public static String register(String name, String email, String password, String birthday, String gender) {

		return HttpUtil.createPost(BASE_URL + "auth/register")
			.header("Accept", "application/json; charset=UTF-8")
			.form("name", name)
			.form("email", email)
			.form("password", password)
			.form("birthday", birthday)
			.form("gender", gender).execute().toString();
	}

}
