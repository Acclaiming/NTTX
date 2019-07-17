package io.kurumi.telegraph.model;
import java.util.List;

// This object represents a page on Telegraph.

public class Page {
		
		public String path;
		
		public String url;
		
		public String title;
		
		public String description;
		
		public String author_name;
		
		public String author_url;
		
		public String image_url;
		
		public List<Node> content;
		
		public Integer views;
		
		public Boolean can_edit;
		
}
