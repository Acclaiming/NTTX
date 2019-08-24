package io.kurumi.ntt.db;

import cn.hutool.core.util.ArrayUtil;
import com.pengrad.telegrambot.model.Chat;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.request.Send;
import java.util.List;
import java.util.Map;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.*;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.gt;
import static com.mongodb.client.model.Filters.not;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;
import static java.util.Arrays.asList;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import io.kurumi.ntt.utils.BotLog;
import com.pengrad.telegrambot.request.ExportChatInviteLink;
import com.pengrad.telegrambot.response.StringResponse;

public class GroupData {

    public static CachedData<GroupData> data = new CachedData<GroupData>(GroupData.class);

    public static void delete(long id) {

        synchronized (data.idIndex) {

            data.deleteById(id);

            data.idIndex.remove(id);

        }

    }

    public static GroupData get(long id) {

        synchronized (data.idIndex) {

            if (data.idIndex.size() > 1000) {

                data.idIndex.clear();

            } else if (data.idIndex.containsKey(id)) return data.idIndex.get(id);

            GroupData group = data.getNoCache(id);

            if (group == null) {

                group = new GroupData();

                group.id = id;

            }

            data.idIndex.put(id,group);

            return group;

        }

    }


    public static GroupData get(Fragment fragment,Chat chat) {

        synchronized (data.idIndex) {

            if (data.idIndex.size() > 1000) {

                data.idIndex.clear();

            } else if (data.idIndex.containsKey(chat.id().longValue())) return data.idIndex.get(chat.id().longValue());

            GroupData group = data.getNoCache(chat.id().longValue());

            if (group == null) {

                group = new GroupData();

                group.id = chat.id();

            }

            group.title = chat.title();
			group.username = chat.username();
			group.bot = fragment.origin.me.id();
			group.last = System.currentTimeMillis();
			
			if (group.username == null && group.link == null) {
				
				StringResponse exported = fragment.execute(new ExportChatInviteLink(group.id));

				if (exported.isOk()) {
					
					group.link = exported.result();
					
				} else {
					
					group.link = "";
					
				}
				
			}

            data.idIndex.put(chat.id().longValue(),group);

            return group;

        }

    }

    public long id;

    public String title;
	public String link;
	public String username;
	public Long bot;
	public Boolean bot_admin;
	public Long last;
	
    public Long owner;

    public List<Long> admins;
    public List<Long> full_admins;

    public Boolean not_trust_admin;
    public Integer delete_service_msg;
    public Integer last_service_msg;

    public Integer delete_channel_msg;

    public Boolean join_captcha;
    public Boolean passive_mode;

    public Map<String, Integer> passive_msg;

    public Map<String, Integer> captchaFailed;
    public List<Long> waitForCaptcha;


    public Integer ft_count;
    public Integer captcha_time;

    public Integer last_join_msg;

    public String parse_time() {
        return parse_time(captcha_time);
    }

    public String parse_time(Integer time) {

        if (time == null) {

            return "50秒";

        } else if (time < 60) {

            return time + "秒";

		} else if (time % 60 == 0) {
			
			return (time / 60) + "分钟";
			
        } else {

            return time / 60 + "分 " + (time % 60) + "秒";

        }

    }

    public Boolean with_image;
    public Boolean interfere;
    public Boolean require_input;

    public Boolean fail_ban;

    public Boolean invite_user_ban;
    public Boolean invite_bot_ban;

    public Integer captcha_mode;
    public Integer captcha_del;

    public String custom_i_question;
    public String custom_a_question;

    public List<CustomItem> custom_items;

    public static class CustomItem {

        public Boolean isValid;
        public String text;

        @Override
        public String toString() {

            if (isValid == null || text == null) return super.toString();

            return (isValid ? "[正确选项]" : "[错误选项]") + " " + text;

        }

    }

    public static List<String> custom_kw;

    public Integer no_invite_user;
    public Integer no_invite_bot;

	public Integer no_esu_words;
	public Integer no_esu_stickers;

    public Integer no_sticker;
    public Integer no_animated_sticker;
    public Integer no_image;
    public Integer no_animation;
    public Integer no_audio;
    public Integer no_video;
    public Integer no_video_note;
    public Integer no_contact;
    public Integer no_location;
    public Integer no_game;
    public Integer no_voice;
    public Integer no_file;

    public Integer max_count;
    public Integer rest_action;

    public Integer last_warn_msg;

    public Map<String, Integer> restWarn;

    public String actionName() {

        return rest_action == null ? "限制" :
			rest_action == 0 ? "禁言" :
			/* rest_action == 1 ? */"封锁";

    }

    //public Boolean anti_esu;

    public List<String> ban_sticker_set;

    public Integer welcome;

    public String welcomeMessage;

    public String welcomeSticker;
    public List<String> welcomeSet;

    public Boolean del_welcome_msg;
    public Integer last_welcome_msg;
    public Integer last_welcome_msg_2;

    public Boolean anti_halal;
    public Boolean cas_spam;
    public Boolean backhole;

	public Boolean enable_log;

	public Long log_channel;

	public void log(Fragment fragment,Object... str) {
		
		if (enable_log != null && log_channel != null) {

			new Send(fragment,log_channel,ArrayUtil.join(str,"\n")).html().async();

		}

	}

}
