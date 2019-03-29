package io.kurumi.ntt.db;

import cn.hutool.json.JSONObject;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.request.GetChat;
import com.pengrad.telegrambot.response.GetChatResponse;
import io.kurumi.ntt.Env;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.utils.CData;
import java.util.HashMap;
import java.util.LinkedList;
import io.kurumi.ntt.model.data.*;

public class UserData extends IdDataModel {

	public static Factory<UserData> INSTANCE = new Factory<UserData>(UserData.class,"users");

    public String firstName;
    public String lastName;
    public String userName;
    public boolean isBot;

	public JSONObject ext;

	public UserData(String dirName, long id) { super(dirName,id); }

	@Override
	protected void init() {

		ext = new JSONObject();

		isBot = false;

	}

	@Override
	protected void load(JSONObject obj) {

        this.userName = obj.getStr("user_name");

        this.firstName = obj.getStr("first_name");

        this.lastName = obj.getStr("last_name");

        this.isBot = obj.getBool("is_bot", false);

		final JSONObject ext;

		this.ext = ((ext = obj.getJSONObject("ext_data")) != null) ? ext : this.ext;

    }

	@Override
	protected void save(JSONObject obj) {

		obj.put("user_name", userName);

        obj.put("first_name", firstName);

        obj.put("last_name", lastName);

        obj.put("is_bot", isBot);

		obj.put("ext_data",ext);

	}

    public String formattedName() {

        return name() + " (" + userName() + ") ";

    }

    public String name() {

        String name = firstName;

        if (lastName != null) {

            name = lastName + " " + name;

        }

        return name;

    }

    public String userName() {

        return userName != null ? "@" + userName : name();

    }

    public boolean isAdmin() {

        return Env.FOUNDER.equals(userName);

    }

	public CData point = UserPoint.get(this);

    public void savePoint() {

        UserPoint.set(this, point);

    }
    
    public static UserData get(User u) {

        if (INSTANCE.idIndex.containsKey(u.id())) return INSTANCE.idIndex.get(u.id());
        
        UserData user = new UserData(INSTANCE.dirName,u.id());

        user.refresh(u);

        INSTANCE.idIndex.put(u.id().longValue(),user);
        
        return user;

    }


    public boolean refresh(Fragment fragment) {

        GetChatResponse chat = fragment.bot().execute(new GetChat(id));

        if (!chat.isOk()) return false;

        userName = chat.chat().username();

        firstName = chat.chat().firstName();

        lastName = chat.chat().lastName();

        save();

        return true;

    }

    public void refresh(User u) {

        userName = u.username();

        firstName = u.firstName();

        lastName = u.lastName();

        save();

    }
    

}
