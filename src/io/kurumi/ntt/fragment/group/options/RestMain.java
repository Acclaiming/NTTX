package io.kurumi.ntt.fragment.group.options;

import cn.hutool.core.util.NumberUtil;
import io.kurumi.ntt.db.GroupData;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.Callback;
import io.kurumi.ntt.model.request.ButtonMarkup;

public class RestMain extends Fragment {

    public static String POINT_REST = "group_rest";

    @Override
    public void init(BotFragment origin) {

        super.init(origin);

        registerCallback(POINT_REST);

    }

    @Override
    public void onCallback(UserData user, Callback callback, String point, String[] params) {

        if (params.length == 0 || !NumberUtil.isNumber(params[0])) {

            callback.invalidQuery();

            return;

        }

        final GroupData data = GroupData.data.getById(NumberUtil.parseLong(params[0]));

        if (data == null) {

            callback.invalidQuery();

            return;

        }

        if (params.length == 1) {

            String message = "限制成员进行某些操作. ";

            message += "\n\n注意 : 当设置了 🗑 (删除) 时 不计入警告计数。\n对于禁止邀请用户/机器人 : 🗑 表示仅移除被邀请者。";

            message += "\n\n" + OptionsMain.doc;

            callback.edit(message).buttons(restMenu(data)).html().async();

            return;

        }

        if ("invite_user".equals(params[1])) {

            if (data.no_invite_user == null) {

                data.no_invite_user = 0;

                callback.text("📝  仅移除被邀请用户");

            } else if (data.no_invite_user == 0) {

                data.no_invite_user = 1;

                callback.text("📝  移除被邀请用户并警告");


            } else {

                data.no_invite_user = null;

                callback.text("📝  不处理");

            }

        } else if ("invite_bot".equals(params[1])) {

            if (data.no_invite_bot == null) {

                data.no_invite_bot = 0;

                callback.text("📝  仅移除机器人");

            } else if (data.no_invite_bot == 0) {

                data.no_invite_bot = 1;

                callback.text("📝  移除机器人并警告");


            } else {

                data.no_invite_bot = null;

                callback.text("📝  不处理");

            }

        } else if ("esu_words".equals(params[1])) {

            if (data.no_esu_words == null) {

                data.no_esu_words = 0;

                callback.text("📝  仅删除");

            } else if (data.no_esu_words == 0) {

                data.no_esu_words = 1;

                callback.text("📝  删除并警告");

            } else {

                data.no_esu_words = null;

                callback.text("📝  不处理");

            }

        } else if ("esu_stickers".equals(params[1])) {

            if (data.no_esu_stickers == null) {

                data.no_esu_stickers = 0;

                callback.text("📝  仅删除");

            } else if (data.no_esu_stickers == 0) {

                data.no_esu_stickers = 1;

                callback.text("📝  删除并警告");

            } else {

                data.no_esu_stickers = null;

                callback.text("📝  不处理");

            }

        } else if ("sticker".equals(params[1])) {

            if (data.no_sticker == null) {

                data.no_sticker = 0;

                callback.text("📝  仅删除");

            } else if (data.no_sticker == 0) {

                data.no_sticker = 1;

                callback.text("📝  删除并警告");

            } else {

                data.no_sticker = null;

                callback.text("📝  不处理");

            }

        } else if ("animated".equals(params[1])) {

            if (data.no_animated_sticker == null) {

                data.no_animated_sticker = 0;

                callback.text("📝  仅删除");

            } else if (data.no_animated_sticker == 0) {

                data.no_animated_sticker = 1;

                callback.text("📝  删除并警告");

            } else {

                data.no_animated_sticker = null;

                callback.text("📝  不处理");

            }


        } else if ("image".equals(params[1])) {

            if (data.no_image == null) {

                data.no_image = 0;

                callback.text("📝  仅删除");

            } else if (data.no_image == 0) {

                data.no_image = 1;

                callback.text("📝  删除并警告");

            } else {

                data.no_image = null;

                callback.text("📝  不处理");

            }

        } else if ("animation".equals(params[1])) {

            if (data.no_animation == null) {

                data.no_animation = 0;

                callback.text("📝  仅删除");

            } else if (data.no_animation == 0) {

                data.no_animation = 1;

                callback.text("📝  删除并警告");

            } else {

                data.no_animation = null;

                callback.text("📝  不处理");

            }

        } else if ("audio".equals(params[1])) {

            if (data.no_audio == null) {

                data.no_audio = 0;

                callback.text("📝  仅删除");

            } else if (data.no_audio == 0) {

                data.no_audio = 1;

                callback.text("📝  删除并警告");

            } else {

                data.no_audio = null;

                callback.text("📝  不处理");

            }

        } else if ("video".equals(params[1])) {

            if (data.no_video == null) {

                data.no_video = 0;

                callback.text("📝  仅删除");

            } else if (data.no_video == 0) {

                data.no_video = 1;

                callback.text("📝  删除并警告");

            } else {

                data.no_video = null;

                callback.text("📝  不处理");

            }

        } else if ("video_note".equals(params[1])) {

            if (data.no_video_note == null) {

                data.no_video_note = 0;

                callback.text("📝  仅删除");

            } else if (data.no_video_note == 0) {

                data.no_video_note = 1;

                callback.text("📝  删除并警告");

            } else {

                data.no_video_note = null;

                callback.text("📝  不处理");

            }

        } else if ("contact".equals(params[1])) {

            if (data.no_contact == null) {

                data.no_contact = 0;

                callback.text("📝  仅删除");

            } else if (data.no_contact == 0) {

                data.no_contact = 1;

                callback.text("📝  删除并警告");

            } else {

                data.no_contact = null;

                callback.text("📝  不处理");

            }

        } else if ("location".equals(params[1])) {

            if (data.no_location == null) {

                data.no_location = 0;

                callback.text("📝  仅删除");

            } else if (data.no_location == 0) {

                data.no_location = 1;

                callback.text("📝  删除并警告");

            } else {

                data.no_location = null;

                callback.text("📝  不处理");

            }

        } else if ("game".equals(params[1])) {

            if (data.no_game == null) {

                data.no_game = 0;

                callback.text("📝  仅删除");

            } else if (data.no_game == 0) {

                data.no_game = 1;

                callback.text("📝  删除并警告");

            } else {

                data.no_game = null;

                callback.text("📝  不处理");

            }

        } else if ("voice".equals(params[1])) {

            if (data.no_voice == null) {

                data.no_voice = 0;

                callback.text("📝  仅删除");

            } else if (data.no_voice == 0) {

                data.no_voice = 1;

                callback.text("📝  删除并警告");

            } else {

                data.no_voice = null;

                callback.text("📝  不处理");

            }

        } else if ("file".equals(params[1])) {

            if (data.no_file == null) {

                data.no_file = 0;

                callback.text("📝  仅删除");

            } else if (data.no_file == 0) {

                data.no_file = 1;

                callback.text("📝  删除并警告");

            } else {

                data.no_file = null;

                callback.text("📝  不处理");

            }

        } else if ("action".equals(params[1])) {

            if (data.rest_action == null) {

                data.rest_action = 0;

                callback.text("📝  禁言该用户");

            } else if (data.rest_action == 0) {

                data.rest_action = 1;

                callback.text("📝  封锁该用户");

            } else {

                data.rest_action = null;

                callback.text("📝  限制非文本发送");

            }

        } else if ("inc".equals(params[1])) {

            if (data.max_count != null && data.max_count > 11) {

                callback.text("📝  新数值太高 (> 12)");

                return;

            }

            if (data.max_count == null) {

                data.max_count = 1;

            }

            callback.text("📝  " + data.max_count + " -> " + (data.max_count = data.max_count + 1));

        } else if ("dec".equals(params[1])) {

            if (data.max_count == null) {

                callback.text("📝  再低就没了 (ﾟ⊿ﾟ)ﾂ");

                return;

            }

            callback.text("📝  " + data.max_count + " -> " + (data.max_count = data.max_count - 1));

            if (data.max_count == 1) {

                data.max_count = null;

            }

        }

        callback.editMarkup(restMenu(data));

    }

    ButtonMarkup restMenu(final GroupData data) {

        return new ButtonMarkup() {{

            newButtonLine()
                    .newButton("邀请新成员")
                    .newButton(data.no_invite_user == null ? "✅" : data.no_invite_user == 0 ? "🗑" : "❌", POINT_REST, data.id, "invite_user");

            newButtonLine()
                    .newButton("邀请机器人")
                    .newButton(data.no_invite_bot == null ? "✅" : data.no_invite_bot == 0 ? "🗑" : "❌", POINT_REST, data.id, "invite_bot");

            newButtonLine()
                    .newButton("烂俗文本")
                    .newButton(data.no_esu_words == null ? "✅" : data.no_esu_words == 0 ? "🗑" : "❌", POINT_REST, data.id, "esu_words");

            newButtonLine()
                    .newButton("烂俗贴纸")
                    .newButton(data.no_esu_stickers == null ? "✅" : data.no_esu_stickers == 0 ? "🗑" : "❌", POINT_REST, data.id, "esu_stickers");


            newButtonLine()
                    .newButton("发送贴纸")
                    .newButton(data.no_sticker == null ? "✅" : data.no_sticker == 0 ? "🗑" : "❌", POINT_REST, data.id, "sticker");

            newButtonLine()
                    .newButton("动态贴纸")
                    .newButton(data.no_animated_sticker == null ? "✅" : data.no_animated_sticker == 0 ? "🗑" : "❌", POINT_REST, data.id, "animated");

            newButtonLine()
                    .newButton("发送图片")
                    .newButton(data.no_image == null ? "✅" : data.no_image == 0 ? "🗑" : "❌", POINT_REST, data.id, "image");

            newButtonLine()
                    .newButton("发送动图")
                    .newButton(data.no_animation == null ? "✅" : data.no_animation == 0 ? "🗑" : "❌", POINT_REST, data.id, "animation");

            newButtonLine()
                    .newButton("发送音频")
                    .newButton(data.no_audio == null ? "✅" : data.no_audio == 0 ? "🗑" : "❌", POINT_REST, data.id, "audio");

            newButtonLine()
                    .newButton("录制语音")
                    .newButton(data.no_voice == null ? "✅" : data.no_voice == 0 ? "🗑" : "❌", POINT_REST, data.id, "voice");

            newButtonLine()
                    .newButton("发送视频")
                    .newButton(data.no_video == null ? "✅" : data.no_video == 0 ? "🗑" : "❌", POINT_REST, data.id, "video");

            newButtonLine()
                    .newButton("录制视频")
                    .newButton(data.no_video_note == null ? "✅" : data.no_video_note == 0 ? "🗑" : "❌", POINT_REST, data.id, "video_note");

            newButtonLine()
                    .newButton("发送名片")
                    .newButton(data.no_contact == null ? "✅" : data.no_contact == 0 ? "🗑" : "❌", POINT_REST, data.id, "contact");

            newButtonLine()
                    .newButton("发送位置")
                    .newButton(data.no_location == null ? "✅" : data.no_location == 0 ? "🗑" : "❌", POINT_REST, data.id, "location");

            newButtonLine()
                    .newButton("发送游戏")
                    .newButton(data.no_game == null ? "✅" : data.no_game == 0 ? "🗑" : "❌", POINT_REST, data.id, "game");

            newButtonLine()
                    .newButton("发送文件")
                    .newButton(data.no_file == null ? "✅" : data.no_file == 0 ? "🗑" : "❌", POINT_REST, data.id, "file");

            newButtonLine("警告 " + (data.max_count == null ? 1 : data.max_count) + " 次 : " + data.actionName(), POINT_REST, data.id, "action");

            newButtonLine().newButton("➖", POINT_REST, data.id, "dec").newButton("➕", POINT_REST, data.id, "inc");

            newButtonLine("🔙", OptionsMain.POINT_OPTIONS, data.id);

        }};


    }


}

