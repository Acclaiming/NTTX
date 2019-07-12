package io.kurumi.ntt.db;

import cn.hutool.core.io.*;
import cn.hutool.json.*;
import io.kurumi.ntt.*;

import java.io.*;
import java.util.*;
import com.pengrad.telegrambot.model.Chat;

public class GroupData {

	public static CachedData<GroupData> data = new CachedData<GroupData>(GroupData.class);

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

		data.idIndex.put(chat.id(),group);

		return group;
		
		}

	}

	public long id;

	public List<UserData> admins;

	public Boolean delete_channel_msg;
	//public Boolean anti_esu;

	public List<String> ban_sticker_set;

}
