package io.kurumi.ntt.cqhttp;

import cn.hutool.json.JSONObject;
import io.kurumi.ntt.Launcher;
import io.kurumi.ntt.cqhttp.update.*;

public class Processer {

    public static Update parseUpdate(String json) {

        JSONObject obj = new JSONObject(json);

        if (obj.containsKey("retcode")) {

            // api result

            return null;

        }

        String postType = obj.getStr("post_type");

        if (Variants.POST_MESSAGE.equals(postType)) {

            return Launcher.GSON.fromJson(json, MessageUpdate.class);

        } else if (Variants.POST_NOTICE.equals(postType)) {

            String noticeType = obj.getStr("notice_type");

            if (Variants.NOTICE_GROUP_UPLOAD.equals(noticeType)) {

                return Launcher.GSON.fromJson(json, GroupUploadNotice.class);

            } else if (Variants.NOTICE_GROUP_INC.equals(noticeType)) {

                return Launcher.GSON.fromJson(json, GroupIncreaseNotice.class);

            } else if (Variants.NOTICE_GROUP_DEC.equals(noticeType)) {

                return Launcher.GSON.fromJson(json, GroupDecreaseNotice.class);

            } else if (Variants.NOTICE_GROUP_ADMIN.equals(noticeType)) {

                return Launcher.GSON.fromJson(json, GroupAdminNotice.class);

            } else if (Variants.NOTICE_FRIEND_ADD.equals(noticeType)) {

                return Launcher.GSON.fromJson(json, FriendAddNotice.class);

            } else {

                return Launcher.GSON.fromJson(json, NoticeUpdate.class);

            }

        } else if (Variants.POST_REQUEST.equals(postType)) {

            String requestType = obj.getStr("request_type");

            if (Variants.REQUEST_FRIEND.equals(requestType)) {

                return Launcher.GSON.fromJson(json, FriendRequest.class);

            } else if (Variants.REQUEST_GROUP.equals(requestType)) {

                return Launcher.GSON.fromJson(json, GroupRequest.class);

            } else {

                return Launcher.GSON.fromJson(json, RequestUpdate.class);

            }

        } else {

            return Launcher.GSON.fromJson(json, Update.class);

        }

    }

    public static void processUpdate(TinxBot bot, String updateJSON) {

        Update update = parseUpdate(updateJSON);

        if (update == null) return;

        for (TinxListener listener : bot.listeners) listener.onUpdate(update);

        if (update instanceof MessageUpdate) {

            MessageUpdate msg = (MessageUpdate) update;

            for (TinxListener listener : bot.listeners) listener.onMsg(msg);

            if (Variants.MSG_PRIVATE.equals(msg.message_type)) {

                for (TinxListener listener : bot.listeners) listener.onPrivate(msg);

            } else if (Variants.MSG_GROUP.equals(msg.message_type)) {

                for (TinxListener listener : bot.listeners) listener.onGroup(msg);

            }

        } else if (update instanceof NoticeUpdate) {

            NoticeUpdate notice = (NoticeUpdate) update;

            for (TinxListener listener : bot.listeners) listener.onNotice(notice);

            if (notice instanceof GroupUploadNotice) {

                GroupUploadNotice upload = (GroupUploadNotice) notice;

                for (TinxListener listener : bot.listeners) listener.onGroupUpload(upload);

            } else if (notice instanceof GroupAdminNotice) {

                GroupAdminNotice admin = (GroupAdminNotice) notice;

                if (Variants.GROUP_ADMIN_SET.equals(admin.sub_type)) {

                    for (TinxListener listener : bot.listeners) listener.onGroupAdminSet(admin);

                } else if (Variants.GROUP_ADMIN_UNSET.equals(admin.sub_type)) {

                    for (TinxListener listener : bot.listeners) listener.onGroupAdminUnSet(admin);

                }

            } else if (notice instanceof GroupIncreaseNotice) {

                GroupIncreaseNotice inc = (GroupIncreaseNotice) notice;

                for (TinxListener listener : bot.listeners) listener.onGroupIncrease(inc);

                if (Variants.GROUP_INC_INVITE.equals(inc.sub_type)) {

                    for (TinxListener listener : bot.listeners) listener.onGroupInviteMember(inc);

                } else if (Variants.GROUP_INC_APPROVE.equals(inc.sub_type)) {

                    for (TinxListener listener : bot.listeners) listener.onGroupApproveMember(inc);

                }

            } else if (notice instanceof GroupDecreaseNotice) {

                GroupDecreaseNotice dec = (GroupDecreaseNotice) notice;

                for (TinxListener listener : bot.listeners) listener.onGroupDecrease(dec);

                if (Variants.GROUP_DEC_LEAVE.equals(dec.sub_type)) {

                    for (TinxListener listener : bot.listeners) listener.onGroupLeftMember(dec);

                } else if (Variants.GROUP_DEC_KICK.equals(dec.sub_type)) {

                    for (TinxListener listener : bot.listeners) listener.onGroupKickMember(dec);

                } else if (Variants.GROUP_DEC_KICK_ME.equals(dec.sub_type)) {

                    for (TinxListener listener : bot.listeners) listener.onGroupKickMe(dec);

                }

            } else if (notice instanceof FriendAddNotice) {

                FriendAddNotice add = (FriendAddNotice) notice;

                for (TinxListener listener : bot.listeners) listener.onFriendAdd(add);

            }

        } else if (update instanceof RequestUpdate) {

            RequestUpdate request = (RequestUpdate) update;

            for (TinxListener listener : bot.listeners) listener.onUpdate(request);

            if (request instanceof GroupRequest) {

                GroupRequest group = (GroupRequest) update;

                for (TinxListener listener : bot.listeners) listener.onGroupRequest(group);

                if (Variants.GR_ADD.equals(group.sub_type)) {

                    for (TinxListener listener : bot.listeners) listener.onGroupAddRequest(group);

                } else if (Variants.GR_INVITE.equals(group.sub_type)) {

                    for (TinxListener listener : bot.listeners) listener.onGroupInviteRequest(group);

                }

            } else if (request instanceof FriendRequest) {

                FriendRequest friend = (FriendRequest) request;

                for (TinxListener listener : bot.listeners) listener.onFriendAddRequest(friend);

            }

        }

    }

}
