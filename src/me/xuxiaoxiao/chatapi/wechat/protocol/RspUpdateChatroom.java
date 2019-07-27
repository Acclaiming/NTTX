package me.xuxiaoxiao.chatapi.wechat.protocol;

import java.util.ArrayList;

public class RspUpdateChatroom {
    public BaseResponse BaseResponse;
    public int MemberCount;
    public ArrayList<RspInit.User> MemberList;
}
