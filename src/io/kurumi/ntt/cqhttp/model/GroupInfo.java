package io.kurumi.ntt.cqhttp.model;

import java.util.List;

public class GroupInfo {
	
	public Integer group_id;
	public String group_name;
	public Integer create_time;
	public Integer category;
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
