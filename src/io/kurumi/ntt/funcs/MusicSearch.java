package io.kurumi.ntt.funcs;

import cn.hutool.http.*;
import cn.hutool.json.*;
import com.pengrad.telegrambot.request.*;
import io.kurumi.ntt.db.*;
import io.kurumi.ntt.fragment.*;
import io.kurumi.ntt.model.*;
import java.io.*;
import io.kurumi.ntt.*;
import cn.hutool.core.io.*;
import java.net.*;

public class MusicSearch extends Fragment {

	public static MusicSearch INSTANCE = new MusicSearch();
	
    String apiUrlLocal = "http://127.0.0.1:11212/";
    String apiUrl = "https://napi.kurumi.io/"; {
		
		if (HttpUtil.createGet(apiUrlLocal).execute().isOk()) {
			
			apiUrl = apiUrlLocal;
			
		}
		
	}
	
    Integer searchId(String keywords) {

        
        return null;

    }

	String getUrl(int id) {

		HttpResponse resp = HttpUtil.createGet(apiUrl + "song/url").form("id",id).execute();

		if (resp.isOk()) {

			JSONObject data = new JSONObject(resp.body());

			return data.getByPath("data.0.url",String.class);

		}

		return null;

	}

	String getLyric(int id) {

		HttpResponse resp = HttpUtil.createGet(apiUrl + "lyric").form("id",id).execute();

		if (resp.isOk()) {

			JSONObject data = new JSONObject(resp.body());

			if (data.getBool("nolyric",false)) {

				return "暂无歌词 ~";

			}

			String lyric = data.getByPath("lrc.lyric",String.class);

			if (lyric == null) return null;
			
			return lyric.replaceAll("\\[..:.....\\]","");

		}

		return null;

	}

    @Override
    public boolean onMsg(UserData user,Msg msg) {

		if (msg.hasText() && (msg.text().startsWith("我要听") || msg.text().startsWith("我要聽"))) {

			String keywords = msg.text().substring(3);

			doMusicSearch(user,msg,keywords);

		}

		return false;

    }

	void doMusicSearch(UserData user,Msg msg,String keywords) {

		msg.sendTyping();
		
		Integer id = null;
		String name = null;
		
		HttpResponse resp = HttpUtil.createGet(apiUrl + "search").form("keywords",keywords).execute();

        if (resp.isOk()) {

            JSONObject data = new JSONObject(resp.body());

            if (data.getByPath("result.songCount",Integer.class) > 0) {

               id =  data.getByPath("result.songs.0.id",Integer.class);
			   name = data.getByPath("result.songs.0.name",String.class);
				//  for (int index = 0;index < songs.size();index ++) {}


            }

        }
		
		if (id == null) {

			msg.reply("暂无结果 :)").exec();

			return;

		}

		String url = getUrl(id);

		if (url == null) {

			msg.reply("版权限制 (ﾟ〇ﾟ ;)").exec();

			return;

		}
		
		msg.sendUpdatingAudio();

		File cache = new File(Env.CACHE_DIR,"music/" + id);
		File lrcCache = new File(Env.CACHE_DIR,"music/lrc/" + id);
		
		if (!cache.isFile()) {

			HttpRequest get = HttpUtil.createGet(url);
			
			if (apiUrl == apiUrlLocal) {
			
			get.setProxy(new Proxy(Proxy.Type.SOCKS,new InetSocketAddress("127.0.0.1",1080)));
			get.execute().writeBody(cache);
			
			}

		}
		
		String lrc;
		
		if (!lrcCache.isFile()) {
			
		 lrc = getLyric(id);
			
			if (lrc != null) {
				
				FileUtil.writeUtf8String(lrc,lrcCache);
				
				lrc = "暂无歌词 ~";
				
			}
			
			
		} else {
			
			lrc = FileUtil.readUtf8String(lrcCache);
			
		}

		msg.fragment.bot().execute(new SendAudio(msg.chatId(),cache).fileName(name + ".mp3").caption(name + " : \n\n" + lrc));

}

}
