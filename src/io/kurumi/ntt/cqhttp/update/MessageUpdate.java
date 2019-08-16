package io.kurumi.ntt.cqhttp.update;

public class MessageUpdate extends Update {
	
	public String message_type;
	public String sub_type;
	public Integer message_id;
	public Long user_id;
	public String message;
	public Integer font;
	public Sender sender;
	
	public Long group_id;
	public Long discuss_id;
	
	public static class Anonymous {
		
		public Long id;
		public String name;
		public String flag;
		
	}

	public static class Sender {
		
		public Long user_id;
		public String nickname;
		public String sex;
		public Integer age;
		
		public String card;
		public String area;
		public String level;
		public String role;
		public String title;
		
	}
	
}
