package io.kurumi.ntt.cqhttp.model;

import java.util.List;
import io.kurumi.ntt.Launcher;
import io.kurumi.ntt.cqhttp.response.GetGroupInfoResponse;
import java.util.HashMap;

public class GroupInfo {
	
	private static HashMap<Long,GroupInfo> cache = new HashMap<>();
	
	public static GroupInfo get(long groupId) {
		
		if (cache.containsKey(groupId)) {
		
			return cache.get(groupId);
			
		}
			
		GetGroupInfoResponse info = Launcher.TINX.api._getGroupInfo(groupId);

		if (info.isOk()) {
			
			cache.put(groupId,info.data);
			
			return info.data;
			
		}
		
		return null;
		
	}
	
	public GroupMember getMember(long userId) {
		
		return GroupMember.get(group_id,userId,false);
		
	}
	
	public Long group_id;
	public String group_name;
	public Long create_time;
	public Long category;
	public Integer member_count;
	public Integer max_member_count;
	public String introduction;
	public List<Admin> admins;
	public Integer admin_count;
	
	public static class Admin {
		
		public Integer user_id;
		public String nickname;
		public String role;
		
	}
	
}
