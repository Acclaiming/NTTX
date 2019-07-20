package io.kurumi.ntt.db;

import cn.hutool.core.io.*;
import cn.hutool.json.*;
import io.kurumi.ntt.*;

import java.io.*;
import java.util.*;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.request.SendAnimation;

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


public class GroupData {

    public static CachedData<GroupData> data = new CachedData<GroupData>(GroupData.class);

		static {
				
				data.collection.updateMany(eq("del_service_msg",true),set("del_service_msg",1));
				data.collection.updateMany(eq("delete_channel_msg",true),set("delete_channel_msg",1));
				
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


    public static GroupData get(Chat chat) {

        synchronized (data.idIndex) {

            if (data.idIndex.size() > 1000) {

                data.idIndex.clear();

            } else if (data.idIndex.containsKey(chat.id())) return data.idIndex.get(chat.id());

            GroupData group = data.getNoCache(chat.id());

            if (group == null) {

                group = new GroupData();

                group.id = chat.id();

            }

						group.title = chat.title();

            data.idIndex.put(chat.id(),group);

            return group;

        }

    }

    public long id;

		public String title;

		public Long owner;

    public List<Long> admins;
		public List<Long> full_admins;

		public Boolean not_trust_admin;
		public Integer delete_service_msg;
		public Integer last_service_msg;

 		public Integer delete_channel_msg;
		
		public Boolean join_captcha;
		public Boolean passive_mode;

		public Map<String,Integer> passive_msg;

		public Map<String,Integer> captchaFailed;
		public List<Long> waitForCaptcha;


		public Integer ft_count;
		public Integer captcha_time;

		public Integer last_join_msg;

		public String parse_time() { return parse_time(captcha_time); }

		public String parse_time(Integer time) {

				if (time == null) {

						return "50s";

				} else if (time < 60) {

						return time + "s";

				} else {

						return time / 60 + "m" + (time % 60 == 0 ? "" : " " + time % 60 + "s");

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

		public Integer no_sticker;
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

		public Map<String,Integer> restWarn;

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

		public Boolean dynamic_join;
		
		public Integer type; 

		// 0 允许所有人加入

		public Boolean default_msg;
		
		/*
		
		// 1 管理员确认

		public Boolean wait_msg;
		
		public String dynamic_question;
		
		// 2 回答问题并由管理员确认

		public Boolean question_wait_msg;
		
		// 3 正确回答问题
		
		public List<String> dynamic_kw;

		// 4 不允许任何人加入
		
		public String not_allow_msg;
		
		*/
		
		public Integer link_valid_time;
		
		public Boolean dynamic_strict;
		
		public Boolean dynamic_no_trust_admin;

		

}
