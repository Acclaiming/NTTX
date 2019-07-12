package io.kurumi.ntt.db;

import cn.hutool.core.io.*;
import cn.hutool.json.*;
import io.kurumi.ntt.*;

import java.io.*;
import java.util.*;

public class GroupData {

	public static CachedData<GroupData> data = new CachedData<GroupData>(GroupData.class);
	
	public long id;

	public List<UserData> admins;
	
	public boolean delete_channel_msg;
	public boolean anti_esu;

	public List<String> ban_sticker;
	public List<String> ban_sticker_set;

}
