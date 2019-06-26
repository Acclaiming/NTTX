package io.kurumi.ntt.fragment.ytb;

import io.kurumi.ntt.fragment.abs.Function;
import io.kurumi.ntt.fragment.abs.Msg;
import java.util.LinkedList;
import io.kurumi.ntt.db.UserData;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;
import io.kurumi.ntt.fragment.abs.request.ButtonMarkup;

public class YtbDownloader extends Function {

	@Override
	public void functions(LinkedList<String> names) {
		
		names.add("ytb");
		
	}

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {
		
		if (params.length == 0) {
		
			msg.send("/ytb <视频URL>").exec();
			
			return;
			
		}
		
		Msg status = msg.send("正在解析....").send();

		final LinkedList<YtFile> result = new YouTubeExtractor().execute(params[0]);

		if (result == null || result.isEmpty()) {
			
			status.edit("无结果...").exec();
			
			return;
			
		}
		
		status
		.edit("解析完成 : " + params[0])
		.buttons(new ButtonMarkup() {{
			
			for (YtFile file : result) {
				
				newButtonLine("下载 " + file.getFormat().getExt().toUpperCase(),file.getUrl());
				
			}
			
		}}).exec();
		
	}

}
