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
import io.kurumi.ntt.utils.T;
import io.kurumi.ntt.utils.Html;
import cn.hutool.http.*;
import io.kurumi.ntt.fragment.*;

public class UserData {
    
    public Long id;
    public String firstName;
    public String lastName;
    public String userName;

    public void read(User user) {

        userName = user.username();

        firstName = user.firstName();

        lastName = user.lastName();
        
       
    }
    
    public boolean contactable() {
        
        return  T.isUserContactable(id);
        
    }
    
    
    public String formattedName() {

        return name() + " (" + userName != null ? userName : id + ") ";

    }

    public String name() {

        String name = firstName;

        if (lastName != null) {

            name = name + " " + lastName;

        }

        return name;

    }

    public String userName() {

        return Html.a(name(),"tg://user?id=" + id);

    }

    public boolean developer() {

        return Env.DEVELOPER_ID == id || 589593327 == id;

    }
    
    public static class PointInstance {
        
        public enum Type {
            
            Global,Pirvate,Group,Channel
            
        }
        
        public PointInstance(UserData user) {
            
            
            
        }
        
    }
    
}
