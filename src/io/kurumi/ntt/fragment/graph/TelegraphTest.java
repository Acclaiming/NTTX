package io.kurumi.ntt.fragment.graph;

import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.telegraph.Telegraph;
import java.util.LinkedList;
import io.kurumi.telegraph.model.Node;
import io.kurumi.telegraph.model.Page;

public class TelegraphTest extends Fragment {

		@Override
		public void init(BotFragment origin) {
				// TODO: Implement this method
				super.init(origin);
				registerFunction("graph");
		}

		
		
		@Override
		public void onFunction(UserData user,Msg msg,String function,String[] params) {
				
				TelegraphAccount account =TelegraphAccount.get(user);

				msg.send("你的账号 : " + account.access_token).async();
				
				LinkedList<Node> content = new LinkedList<>();

				content.add(new Node() {{
						
						text = "文本内容测试喵";
						
				}});
				
				Page page = Telegraph.createPage(account.access_token,"喵",user.name(),account.author_url,content,false);

				msg.send("测试完成 : " + page.url).exec();
				

		}
		
}
