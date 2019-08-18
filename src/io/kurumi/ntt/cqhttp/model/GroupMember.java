package io.kurumi.ntt.cqhttp.model;

import io.kurumi.ntt.Launcher;
import java.util.HashMap;
import io.kurumi.ntt.cqhttp.response.GetGroupMemberInfoResponse;

public class GroupMember {

	private static HashMap<Long,GroupMember> cache = new HashMap<>();

	public static GroupMember get(long groupId,long userId,boolean noCahce) {

		if (!noCahce && cache.containsKey(groupId)) {

			return cache.get(groupId);

		}

		GetGroupMemberInfoResponse info = Launcher.TINX.api.getGroupMenberInfo(groupId,userId,noCahce);

		if (info.isOk()) {

			cache.put(groupId,info.data);

			return info.data;

		}

		return null;

	}
	
	public Integer group_id;
	public Integer user_id;
	public String nickname;
	public String card;
	public Integer age;
	public String area;
	public Integer join_time;
	public Integer last_sent_time;
	public String level;
	public String role;
	public Boolean unfriendly;
	public String title;
	public Integer title_expire_time;
	public Boolean card_changeable;
	
}
