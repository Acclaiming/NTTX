package io.kurumi.ntt.fragment.group.mamage;

import cn.hutool.core.util.StrUtil;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.twitter.archive.StatusArchive;
import io.kurumi.ntt.fragment.twitter.ext.StatusGetter;
import io.kurumi.ntt.fragment.twitter.status.SavedSearch;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.model.request.ButtonLine;
import io.kurumi.ntt.model.request.ButtonMarkup;
import io.kurumi.ntt.utils.Html;
import io.kurumi.ntt.db.GroupData;
import io.kurumi.ntt.model.Callback;
import cn.hutool.core.util.NumberUtil;

public class GroupList extends Fragment {

	final String POINT_SHOW_PAGE = "_groups";
	
	@Override
	public void init(BotFragment origin) {
		
		super.init(origin);
		
		registerAdminFunction("_group_list");
		registerCallback(POINT_SHOW_PAGE);
		
	}

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {
		
		GroupData.data.saveAll();
		
		String message = exportContent(1);
		
		msg.send(message).buttons(makeButtons(GroupData.data.collection.countDocuments(),1)).html().async();
	
	}

	@Override
	public void onCallback(UserData user,Callback callback,String point,String[] params) {
		
		GroupData.data.saveAll();
		
		int cursor = NumberUtil.parseInt(params[0]);

        callback.text("正在加载...");

        long count = GroupData.data.collection.countDocuments();

        callback.edit(exportContent(cursor)).buttons(makeButtons(count,cursor)).html().exec();
		
	}

    String exportContent(int cursor) {

		String message = "";
  
        for (GroupData group : GroupData.data.collection.find().skip((cursor - 1) * 10).limit(10)) {

			message += "\n";
			
			if (group.username != null) {
				
				message += "🌐";
				
			} else {
				
				message += "🔒";
				
			}
			
			message += " | ";
			
			message += Html.code(group.id);
            
			message += " | ";
	
			if (group.username != null) {
				
				message += Html.a(group.title,"@" + group.username);
				
			} else if (!StrUtil.isBlank(group.link)) {
				
				message += Html.a(group.title,group.link);
				
			} else {
				
				message += group.title;
				
			}
			
        }

        return message;
		
    }

    ButtonMarkup makeButtons(final long count,final long current) {

        return new ButtonMarkup() {{

				ButtonLine line = newButtonLine();

				if (current > 1) {

					line.newButton(" □ ",POINT_SHOW_PAGE,1);

					line.newButton(" << ",POINT_SHOW_PAGE,current - 1);

				}

				int max = (int) count / 10;

				if (count % 10 != 0) {

					max++;

				}

				line.newButton(current + " / " + max,"null");

				if (current < max) {

					line.newButton(" >> ",POINT_SHOW_PAGE,current + 1);

					line.newButton(" ■ ",POINT_SHOW_PAGE,max);

				}

			}};

    }
	
}
