package io.kurumi.ntt.cqhttp;

import cn.hutool.json.JSONObject;
import io.kurumi.ntt.Launcher;
import io.kurumi.ntt.cqhttp.update.Update;
import io.kurumi.ntt.cqhttp.update.MessageUpdate;
import io.kurumi.ntt.cqhttp.update.GroupUploadNotice;
import io.kurumi.ntt.cqhttp.update.GroupIncreaseNotice;
import io.kurumi.ntt.cqhttp.update.GroupDecreaseNotice;
import io.kurumi.ntt.cqhttp.update.GroupAdminNotice;
import io.kurumi.ntt.cqhttp.update.FriendAddNotice;
import io.kurumi.ntt.cqhttp.update.NoticeUpdate;
import io.kurumi.ntt.cqhttp.update.FriendRequest;
import io.kurumi.ntt.cqhttp.update.GroupRequest;
import io.kurumi.ntt.cqhttp.update.RequestUpdate;

public class Processer {

	public static Update processUpdate(String json) {

		JSONObject obj = new JSONObject(json);

		String postType = obj.getStr("post_type");

		if (Variants.POST_MESSAGE.equals(postType)) {

			return Launcher.GSON.fromJson(json,MessageUpdate.class);

		} else if (Variants.POST_NOTICE.equals(postType)) {

			String noticeType = obj.getStr("notice_type");

			if (Variants.NOTICE_GROUP_UPLOAD.equals(noticeType)) {

				return Launcher.GSON.fromJson(json,GroupUploadNotice.class);

			} else if (Variants.NOTICE_GROUP_INC.equals(noticeType)) {

				return Launcher.GSON.fromJson(json,GroupIncreaseNotice.class);

			} else if (Variants.NOTICE_GROUP_DEC.equals(noticeType)) {

				return Launcher.GSON.fromJson(json,GroupDecreaseNotice.class);

			} else if (Variants.NOTICE_GROUP_ADMIN.equals(noticeType)) {

				return Launcher.GSON.fromJson(json,GroupAdminNotice.class);

			} else if (Variants.NOTICE_FRIEND_ADD.equals(noticeType)) {

				return Launcher.GSON.fromJson(json,FriendAddNotice.class);

			} else {

				return Launcher.GSON.fromJson(json,NoticeUpdate.class);

			}

		} else if (Variants.POST_REQUEST.equals(postType)) {

			String requestType = obj.getStr("request_type");

			if (Variants.REQUEST_FRIEND.equals(requestType)) {

				return Launcher.GSON.fromJson(json,FriendRequest.class);

			} else if (Variants.REQUEST_GROUP.equals(requestType)) {

				return Launcher.GSON.fromJson(json,GroupRequest.class);

			} else {

				return Launcher.GSON.fromJson(json,RequestUpdate.class);

			}

		} else {

			return Launcher.GSON.fromJson(json,Update.class);
			
		}

	}

}
