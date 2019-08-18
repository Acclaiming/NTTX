package io.kurumi.ntt.fragment.sticker;

import cn.hutool.core.util.ArrayUtil;
import com.pengrad.telegrambot.model.Sticker;
import com.pengrad.telegrambot.model.StickerSet;
import com.pengrad.telegrambot.request.GetStickerSet;
import com.pengrad.telegrambot.request.SetStickerPositionInSet;
import com.pengrad.telegrambot.response.BaseResponse;
import com.pengrad.telegrambot.response.GetStickerSetResponse;
import io.kurumi.ntt.db.PointData;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.inline.ShowSticker;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.model.request.ButtonMarkup;
import io.kurumi.ntt.model.request.Keyboard;
import io.kurumi.ntt.model.request.KeyboradButtonLine;
import io.kurumi.ntt.utils.NTT;
import java.util.List;

public class MoveSticker extends Fragment {

    @Override
    public void init(BotFragment origin) {

        super.init(origin);

        registerFunction("move_sticker");
        registerPoint(POINT_MOVE_STICKER);

    }

    final String POINT_MOVE_STICKER = "move_sticker";

    class StickerMove extends PointData {

        int type;
        String setName;
        StickerSet set;
        Sticker from;

        @Override
        public void onCancel(UserData user,Msg msg) {

            ShowSticker.current.remove(user.id);

			super.onCancel(user,msg);

        }

		StickerMove(Msg command) {
			super(command);
		}

    }

    @Override
    public void onFunction(UserData user,Msg msg,String function,String[] params) {

        final List<PackOwner> all = PackOwner.getAll(user.id);

        if (all.isEmpty()) {

            msg.send("你没有使用NTT创建过贴纸包....\n使用 /new_sticker_set 创建").exec();

            return;

        }


        PointData data = new StickerMove(msg);

        setPrivatePoint(user,POINT_MOVE_STICKER,data);

        msg.send("请选择贴纸包 / 或直接发送要移动的贴纸")
			.keyboard(new Keyboard() {{

                    KeyboradButtonLine line = null;

                    for (PackOwner pack : all) {

                        if (line == null) {

                            line = newButtonLine();

                            line.newButton(pack.id);

                        } else {

                            line.newButton(pack.id);

                            line = null;

                        }

                    }

                }})
			.withCancel().exec(data);


    }

    @Override
    public void onPoint(UserData user,Msg msg,String point,PointData data) {

        final StickerMove move = (StickerMove) data.with(msg);

        if (move.type == 0 && msg.sticker() == null) {

            String target = msg.text();

            if (target == null || !PackOwner.data.fieldEquals(target,"owner",user.id)) {

                msg.send("请选择你的贴纸包").withCancel().exec(data);

                return;

            }

            final GetStickerSetResponse set = bot().execute(new GetStickerSet(target));

            if (!set.isOk()) {

                msg.send("无法读取贴纸包 ，已删除本地记录 " + target + " : " + set.description()).exec(data);

                PackOwner.data.deleteById(target);

                return;

            }

            move.type = 1;
            move.set = set.stickerSet();
            move.setName = target;

            ShowSticker.current.put(user.id,set.stickerSet());

            msg.send("请选择要移动的贴纸或直接发送")
				.buttons(new ButtonMarkup() {{

                        newCurrentInlineButtonLine("选择贴纸",ShowSticker.PREFIX);

                    }})
				.withCancel().exec(data);

        } else if (move.type < 2) {

            if (msg.sticker() == null) {

                msg.send("请选择或发送要修改位置的贴纸").withCancel().exec(data);

                return;

            } else if (msg.sticker().setName() == null) {

                msg.send("这个贴纸没有贴纸包").withCancel().exec(data);

                return;

            } else if (!msg.sticker().setName().toLowerCase().endsWith("_by_" + origin.me.username().toLowerCase())) {

                msg.send("根据 " + NewStickerSet.DOC + " ，BOT只能操作由自己创建的贴纸包....").html().withCancel().exec(data);

                return;

            }

            if (move.set == null) {

                final GetStickerSetResponse set = bot().execute(new GetStickerSet(msg.sticker().setName()));

                if (!set.isOk()) {

                    msg.send("无法读取贴纸包 ，请重试 : " + set.description()).exec(data);

                    return;

                }

                move.setName = set.stickerSet().name();
                move.set = set.stickerSet();

                ShowSticker.current.put(user.id,set.stickerSet());

            }

            move.from = msg.sticker();

            move.type = 3;

            msg.send("请选择 / 发送要互换的位置的贴纸")
				.buttons(new ButtonMarkup() {{

                        newCurrentInlineButtonLine("选择贴纸",ShowSticker.PREFIX);

                    }})
				.withCancel().exec(data);


        } else if (move.type == 3) {

            if (msg.sticker() == null) {

                msg.send("请选择 / 发送要互换的位置的贴纸").withCancel().exec(data);

                return;

            } else if (!msg.sticker().setName().equals(move.setName)) {

                msg.send("请选择贴纸包 " + move.setName + " 的贴纸").withCancel().exec(data);

                return;

            }

            int index = ArrayUtil.indexOf(move.set.stickers(),msg.sticker());

            clearPrivatePoint(user);

            BaseResponse resp = execute(new SetStickerPositionInSet(move.from.fileId(),index));

            if (resp.isOk()) {

                msg.send("修改成功！").buttons(new ButtonMarkup() {{

							newCurrentInlineButtonLine("查看贴纸包",ShowSticker.PREFIX + " " + move.setName);

						}}).exec();


            } else {

                msg.send("修改失败！\n\n{}",resp.description());

            }

        }

    }


}
