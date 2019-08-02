package io.kurumi.ntt.fragment.mstd;

import com.sys1yagi.mastodon4j.MastodonClient;
import com.sys1yagi.mastodon4j.api.method.Apps;
import io.kurumi.ntt.Launcher;
import com.sys1yagi.mastodon4j.api.method.Accounts;
import com.sys1yagi.mastodon4j.api.method.Blocks;
import com.sys1yagi.mastodon4j.api.method.Favourites;

public class MstdApi {
	
	public final MastodonClient client;
	
	public MstdApi(String app) {

		client = new MastodonClient.Builder(app,Launcher.OKHTTP,Launcher.GSON).build();

	}
	
	public MstdApi(MstdApp app) {
		
		client = new MastodonClient.Builder(app.id,Launcher.OKHTTP,Launcher.GSON).build();
		
	}
	
	public MstdApi(MstdAuth auth) {
	
		client = new MastodonClient.Builder(auth.appId,Launcher.OKHTTP,Launcher.GSON).accessToken(auth.accessToken).build();

	}
	
	private Accounts accounts;

	public Accounts accounts() {

		if (accounts == null) accounts = new Accounts(client);

		return accounts;

	}

	private Apps apps;
	
	public Apps apps() {
		
		if (apps == null) apps = new Apps(client);
		
		return apps;
		
	}
	
	private Blocks blocks;

	public Blocks blocks() {

		if (blocks == null) blocks = new Blocks(client);

		return blocks;

	}
	
	private Favourites favs;
	
	public Favourites favs() {
		
		if (favs == null) favs = new Favourites(client);
		
		return favs;
		
	}
	
}
