package io.kurumi.ntt.cqhttp;

import io.kurumi.ntt.cqhttp.response.BaseResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.HttpResponse;
import io.kurumi.ntt.Launcher;
import io.kurumi.ntt.cqhttp.response.SendMessageResponse;
import io.kurumi.ntt.cqhttp.response.GetLoginInfoResponse;
import io.kurumi.ntt.cqhttp.response.GetStrangerInfoResponse;
import io.kurumi.ntt.cqhttp.response.GetGroupListResponse;
import io.kurumi.ntt.cqhttp.response.GetGroupMemberInfoResponse;
import io.kurumi.ntt.cqhttp.response.GetGroupMemberListResponse;
import io.kurumi.ntt.cqhttp.response.GetFileResponse;
import io.kurumi.ntt.cqhttp.response.CheckResponse;
import io.kurumi.ntt.cqhttp.response.GetGroupInfoResponse;
import io.kurumi.ntt.utils.BotLog;
import java.util.HashMap;
import io.kurumi.ntt.cqhttp.model.GroupMember;
import io.kurumi.ntt.cqhttp.model.GroupInfo;
import cn.hutool.log.StaticLog;
import org.apache.http.util.ExceptionUtils;
import cn.hutool.core.exceptions.ExceptionUtil;

public class TinxApi {

	private String url;
	
	public TinxApi(String url) {

		this.url = url;

	}
	
	public SendMessageResponse sendPrivateMsg(long user_id,String message) {

		return send("send_private_msg",params("user_id",user_id,"message",message),SendMessageResponse.class);

	}
	
	public SendMessageResponse sendPrivateMsg(long user_id,String message,boolean auto_escape) {
		
		return send("send_private_msg",params("user_id",user_id,"message",message,"auto_escape",auto_escape),SendMessageResponse.class);
		
	}
	
	public SendMessageResponse sendGroupMsg(long group_id,String message,boolean auto_escape) {

		return send("send_group_msg",params("group_id",group_id,"message",message,"auto_escape",auto_escape),SendMessageResponse.class);

	}
	
	public SendMessageResponse sendDiscussMsg(long discuss_id,String message,boolean auto_escape) {

		return send("send_discuss_msg",params("discuss_id",discuss_id,"message",message,"auto_escape",auto_escape),SendMessageResponse.class);

	}
	
	// send_msg
	
	public BaseResponse deleteMsg(int message_id) {
		
		return send("delete_msg",params("message_id",message_id),BaseResponse.class);
		
	}
	
	public BaseResponse sendLike(long user_id,int times) {
		
		return send("send_like",params("user_id",user_id,"times",times),BaseResponse.class);
		
	}
	
	public BaseResponse setGroupKick(long group_id,long user_id,boolean reject_add_request) {
		
		return send("set_group_kick",params("group_id",group_id,"user_id",user_id,"reject_add_request",reject_add_request),BaseResponse.class);
		
	}
	
	public BaseResponse setGroupBan(long group_id,long user_id,int duration) {

		return send("set_group_ban",params("group_id",group_id,"user_id",user_id,"duration",duration),BaseResponse.class);

	}
	
	public BaseResponse setGroupWholeBan(long group_id,boolean enable) {
		
		return send("set_group_whole_ban",params("group_id",group_id,"enable",enable),BaseResponse.class);
		
	}
	
	// set_group_anonymous_ban
	// set_group_admin
	// set_group_anonymous
	// set_group_card
	
	public BaseResponse setGroupLeave(long group_id) {

		return send("set_group_leave",params("group_id",group_id),BaseResponse.class);

	}
	public BaseResponse setGroupLeave(long group_id,boolean is_dismiss) {
		
		return send("set_group_leave",params("group_id",group_id,"is_dismiss",is_dismiss),BaseResponse.class);
		
	}
	
	// set_group_special_title
	
	public BaseResponse setDiscussLeave(long discuss_id) {

		return send("set_discuss_leave",params("discuss_id",discuss_id),BaseResponse.class);

	}
	
	public BaseResponse setDiscussLeave(long discuss_id,boolean is_dismiss) {

		return send("set_discuss_leave",params("discuss_id",discuss_id,"is_dismiss",is_dismiss),BaseResponse.class);

	}
	
	
	public BaseResponse setFriendAddRequest(String flag,boolean approve,String remark) {
		
		return send("set_friend_add_request",params("flag",flag,"approve",approve,"remark",remark),BaseResponse.class);
		
	}
	
	public BaseResponse setGroupAddRequest(String flag,String type,boolean approve,String reason) {
		
		return send("set_group_add_request",params("flag",flag,"type",type,"approve",approve,"reason",reason),BaseResponse.class);
		
	}
	
	public GetLoginInfoResponse getLoginInfo() {
		
		return send("get_login_info",params(),GetLoginInfoResponse.class);
		
	}
	
	public GetStrangerInfoResponse getStrangerInfo(long user_id) {

		return send("get_stranger_info",params("user_id",user_id),GetStrangerInfoResponse.class);

	}
	
	public GetStrangerInfoResponse getStrangerInfo(long user_id,boolean no_cache) {
		
		return send("get_stranger_info",params("user_id",user_id,"no_cache",no_cache),GetStrangerInfoResponse.class);
		
	}
	
	public GetGroupListResponse getGroupList() {
		
		return send("get_group_list",params(),GetGroupListResponse.class);
		
	}
	
	public GetGroupMemberInfoResponse getGroupMenberInfo(long group_id,long user_id) {

		return send("get_group_member_info",params("group_id",group_id,"user_id",user_id),GetGroupMemberInfoResponse.class);

	}
	
	public GetGroupMemberInfoResponse getGroupMenberInfo(long group_id,long user_id,boolean no_cache) {
		
		return send("get_group_member_info",params("group_id",group_id,"user_id",user_id,"no_cache",no_cache),GetGroupMemberInfoResponse.class);
		
	}
	
	public GetGroupMemberListResponse getGroupMemberList(long group_id) {
		
		return send("get_group_member_list",params("group_id",group_id),GetGroupMemberListResponse.class);
		
	}
	
	// get_cookies
	// get_csrf_token
	// get_credentials
	
	public GetFileResponse getRecord(String file,String out_format,boolean full_path) {
		
		return send("get_record",params("file",file,"out_format",out_format,"full_path",full_path),GetFileResponse.class);
		
	}
	
	public GetFileResponse getImage(String file) {

		return send("get_image",params("file",file),GetFileResponse.class);

	}
	
	public CheckResponse canSendImage() {
		
		return send("can_send_image",params(),CheckResponse.class);
		
	}
	
	public CheckResponse canSendRecoed() {

		return send("can_send_record",params(),CheckResponse.class);

	}
	
	// get_status
	// get_version_info
	// set_restart_plugin
	// clean_data_dir
	// clean_plugin_log
	
	public GetGroupInfoResponse _getGroupInfo(long group_id) {
		
		return send("_get_group_info",params("group_id",group_id),GetGroupInfoResponse.class);
		
	}
	
	// _get_vip_info
	// _get_group_notice
	// _send_group_notice
	// _set_restart
	
	// .check_update
	// .handle_quick_operation
	
	// ASYNC API
	
	public void sendPrivateMsgAsync(long user_id,String message) {

		sendAsync("send_private_msg",params("user_id",user_id,"message",message));

	}

	public void sendPrivateMsgAsync(long user_id,String message,boolean auto_escape) {

		sendAsync("send_private_msg",params("user_id",user_id,"message",message,"auto_escape",auto_escape));

	}

	public void sendGroupMsgAsync(long group_id,String message,boolean auto_escape) {

		sendAsync("send_group_msg",params("group_id",group_id,"message",message,"auto_escape",auto_escape));

	}

	public void sendDiscussMsgAync(long discuss_id,String message,boolean auto_escape) {

		sendAsync("send_discuss_msg",params("discuss_id",discuss_id,"message",message,"auto_escape",auto_escape));

	}

	// send_msg

	public void deleteMsgAsync(int message_id) {

		sendAsync("delete_msg",params("message_id",message_id));

	}

	public void sendLikeAsync(long user_id,int times) {

		sendAsync("send_like",params("user_id",user_id,"times",times));

	}

	public void setGroupKickAsync(long group_id,long user_id,boolean reject_add_request) {

		sendAsync("set_group_kick",params("group_id",group_id,"user_id",user_id,"reject_add_request",reject_add_request));

	}

	public void setGroupBanAsync(long group_id,long user_id,int duration) {

		sendAsync("set_group_ban",params("group_id",group_id,"user_id",user_id,"duration",duration));

	}

	public void setGroupWholeBanAsync(long group_id,boolean enable) {

		sendAsync("set_group_whole_ban",params("group_id",group_id,"enable",enable));

	}

	// set_group_anonymous_ban
	// set_group_admin
	// set_group_anonymous
	// set_group_card

	public void setGroupLeaveAsync(long group_id) {

		sendAsync("set_group_leave",params("group_id",group_id));

	}
	public void setGroupLeaveAsync(long group_id,boolean is_dismiss) {

		sendAsync("set_group_leave",params("group_id",group_id,"is_dismiss",is_dismiss));

	}

	// set_group_special_title

	public void setDiscussLeaveAsync(long discuss_id) {

		sendAsync("set_discuss_leave",params("discuss_id",discuss_id));

	}

	public void setDiscussLeaveAsync(long discuss_id,boolean is_dismiss) {

		sendAsync("set_discuss_leave",params("discuss_id",discuss_id,"is_dismiss",is_dismiss));

	}


	public void setFriendAddRequestAsync(String flag,boolean approve,String remark) {

		sendAsync("set_friend_add_request",params("flag",flag,"approve",approve,"remark",remark));

	}

	public void setGroupAddRequestAsync(String flag,String type,boolean approve,String reason) {

		sendAsync("set_group_add_request",params("flag",flag,"type",type,"approve",approve,"reason",reason));

	}

	public void getLoginInfoAsync() {

		sendAsync("get_login_info",params());

	}

	public void getStrangerInfoAsync(long user_id) {

		sendAsync("get_stranger_info",params("user_id",user_id));

	}

	public void getStrangerInfoAsync(long user_id,boolean no_cache) {

		sendAsync("get_stranger_info",params("user_id",user_id,"no_cache",no_cache));

	}


	// get_group_list
	// get_group_member_info
	// get_group_member_list

	// get_cookies
	// get_csrf_token
	// get_credentials

	// get_record
	// get_image
	// can_send_image
	// can_send_record

	// get_status
	// get_version_info
	// set_restart_plugin
	// clean_data_dir
	// clean_plugin_log

	// _get_group_info
	// _get_vip_info
	// _get_group_notice
	// _send_group_notice
	// _set_restart

	// .check_update
	// .handle_quick_operation
	
	JSONObject params(Object... params) {

		JSONObject body = new JSONObject();

		for (int index = 0;index < params.length;index += 2) {

			if (params[index + 1] == null) continue;
			
			body.put(params[index].toString(),params[index + 1]);

		}
		
		return body;
		
	}
	
	public void sendAsync(String method,JSONObject body) {
		
		method += "_async";
		
		JSONObject query = new JSONObject();
		
		query.put("action",method);
		query.put("params",body);
		
		if (!Launcher.TINX.send(query.toString())) {
			
			BotLog.debug("连接已关闭, 向 HTTP API 发送异步操作.");
			
			send(method,body,null);
			
		}
		
	}

	public <T extends BaseResponse> T send(String method,JSONObject body,Class<T> clazz) {

		try {

			HttpResponse result = HttpUtil.createPost(url + method).body(body).execute();
			
			if (clazz == null) return null;
			
			return Launcher.GSON.fromJson(result.body(),clazz);

		} catch (Exception httpExc) {
			
			httpExc.printStackTrace();
			
			try {

				T result = clazz.newInstance();
				result.status = "error";
				result.retcode = -1;

				return result;

			} catch (Exception e) {}

			return null;

		}

	}

}
