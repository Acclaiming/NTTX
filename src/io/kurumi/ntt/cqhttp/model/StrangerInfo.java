package io.kurumi.ntt.cqhttp.model;

import io.kurumi.ntt.Launcher;
	import io.kurumi.ntt.cqhttp.response.GetGroupMemberInfoResponse;
	import java.util.HashMap;
import io.kurumi.ntt.cqhttp.response.GetStrangerInfoResponse;

	public class StrangerInfo {
		
	public Long user_id;
	public String nickname;
	public String sex;
	public int age;
	
	private static HashMap<Long,StrangerInfo> cache = new HashMap<>();

	public static StrangerInfo get(long userId,boolean noCahce) {

		if (!noCahce && cache.containsKey(userId)) {

			return cache.get(userId);

		}

		GetStrangerInfoResponse info = Launcher.TINX.api.getStrangerInfo(userId,noCahce);

		if (info.isOk()) {

			cache.put(userId,info.data);

			return info.data;

		}

		return null;

	}
		
}
