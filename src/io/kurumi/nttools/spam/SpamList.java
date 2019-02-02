package io.kurumi.nttools.spam;

import java.util.LinkedList;
import cn.hutool.json.JSONObject;

public class SpamList {
    
    public Long ownerId;
    public Long ownerTwitterId;
    
    public String name;
    
    public Boolean isPublic;
    
    public LinkedList<UserSpam> users;
    
}
