package io.kurumi.ntt.fragment.group.options;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.pengrad.telegrambot.request.EditMessageReplyMarkup;
import io.kurumi.ntt.db.GroupData;
import io.kurumi.ntt.db.PointData;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.Callback;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.model.request.ButtonMarkup;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class CaptchaMain extends Fragment {

	public static String POINT_CAPTCHA = "group_join";

	final String POINT_CUSTOM = "group_cus";

	@Override
	public void init(BotFragment origin) {

		super.init(origin);

		registerCallback(POINT_CAPTCHA,POINT_CUSTOM);
		registerPoint(POINT_CUSTOM);

	}

	@Override
	public void onCallback(UserData user,Callback callback,String point,String[] params) {

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

			String message = "编辑群组的新成员加群验证设置.";

			message += "\n\n" + OptionsMain.doc;

			callback.edit(message).buttons(joinMenu(data)).html().async();

			return;

		}

		if (POINT_CAPTCHA.equals(point)) {

			if ("enable".equals(params[1])) {

				if (data.join_captcha == null) {

					data.join_captcha = true;

					callback.text("🚪  已开启");

				} else {

					data.join_captcha = null;

					callback.text("🚪  已关闭");

				}

			} else if ("passive".equals(params[1])) {

				if (data.passive_mode == null) {

					data.passive_mode = true;

					callback.text("🚪  已开启");

				} else {

					data.passive_mode = null;

					callback.text("🚪  已关闭");

				}

			} else if ("ft_inc".equals(params[1])) {

				if (data.ft_count != null && data.ft_count >= 5) {

					callback.text("🚪  新数值太高 (> 5)");

					return;

				}

				if (data.ft_count == null) {

					data.ft_count = 0;

				}

				callback.text("🚪  " + data.ft_count + " -> " + (data.ft_count = data.ft_count + 1));

			} else if ("captcha_del".equals(params[1])) {

				if (data.captcha_del == null) {

					data.captcha_del = 0;

					callback.text("🚪  保留一条");

				} else if (data.captcha_del == 0) {

					data.captcha_del = 1;

					callback.text("🚪  全部保留");

				} else {

					data.captcha_del = null;

					callback.text("🚪  延时删除");

				}


			} else if ("ft_dec".equals(params[1])) {

				if (data.ft_count == null) {

					callback.text("🚪  再低就没了 (ﾟ⊿ﾟ)ﾂ");

					return;

				}

				callback.text("🚪  " + data.ft_count + " -> " + (data.ft_count = data.ft_count - 1));

				if (data.ft_count == 0) {

					data.ft_count = null;

				}

			} else if ("jt_inc".equals(params[1])) {

				if (data.captcha_time != null && (data.captcha_time >= 5 * 60)) {

					callback.text("🚪  新数值太高 (> 5min)");

					return;

				}

				if (data.captcha_time == null) {

					data.captcha_time = 50;

				}

				callback.text("🚪  " + data.parse_time() + " -> " + (data.parse_time(data.captcha_time = data.captcha_time + 10)));

				if (data.captcha_time == 50) {

					data.captcha_time = null;

				}

			} else if ("jt_inc_t".equals(params[1])) {

				if (data.captcha_time != null && (data.captcha_time >= 5 * 60)) {

					callback.text("🚪  新数值太高 (> 5min)");

					return;

				}

				if (data.captcha_time == null) {

					data.captcha_time = 50;

				}

				int time = data.captcha_time;

				if (time + 30 > 5 * 60) {

					data.captcha_time = 5 * 60;

				} else {

					data.captcha_time = time + 30;

				}

				callback.text("🚪  " + data.parse_time(time) + " -> " + data.parse_time());

				if (data.captcha_time == 50) {

					data.captcha_time = null;

				}

			} else if ("jt_dec".equals(params[1])) {

				if (data.captcha_time != null && data.captcha_time < 21) {

					callback.text("🚪  再低还能验证吗 (ﾟ⊿ﾟ)ﾂ");

					return;

				}

				if (data.captcha_time == null) {

					data.captcha_time = 50;

				}

				callback.text("🚪  " + data.parse_time() + " -> " + data.parse_time(data.captcha_time = data.captcha_time - 10));

				if (data.captcha_time == 50) {

					data.captcha_time = null;

				}


			} else if ("jt_dec_t".equals(params[1])) {

				if (data.captcha_time != null && data.captcha_time < 21) {

					callback.text("🚪  再低还能验证吗 (ﾟ⊿ﾟ)ﾂ");

					return;

				}

				if (data.captcha_time == null) {

					data.captcha_time = 50;

				}

				int time = data.captcha_time;

				if (time - 30 > 20) {

					data.captcha_time = time - 30;

				} else {

					data.captcha_time = 20;

				}

				callback.text("🚪  " + data.parse_time(time) + " -> " + data.parse_time());

				if (data.captcha_time == 50) {

					data.captcha_time = null;

				}


			} else if ("fail_ban".equals(params[1])) {

				if (data.fail_ban == null) {

					data.fail_ban = true;

					callback.text("🚪  封锁该用户");

				} else {

					data.fail_ban = null;

					callback.text("🚪  移除该用户");

				}


			} else if ("mode_def".equals(params[1])) {

				callback.text("🚪  默认模式");

				if (data.captcha_mode == null) {

					return;

				}

				data.captcha_mode = null;

			} else if ("mode_code".equals(params[1])) {

				callback.text("🚪  验证码验证");

				if (((Integer) 0).equals(data.captcha_mode)) {

					return;

				}

				data.captcha_mode = 0;

			} else if ("mode_math".equals(params[1])) {

				callback.text("🚪  算数验证");

				if (((Integer) 1).equals(data.captcha_mode)) {

					return;

				}

				data.captcha_mode = 1;

			} else if ("with_image".equals(params[1])) {

				if (data.with_image == null) {

					data.with_image = true;

					callback.text("🚪  以图片显示问题");

				} else {

					data.with_image = null;

					callback.text("🚪  以文字显示问题");

				}

			} else if ("interfere".equals(params[1])) {

				if (data.interfere == null) {

					data.interfere = true;

					callback.text("🚪  开启按钮干扰");

				} else {

					data.interfere = null;

					callback.text("🚪  关闭按钮干扰");

				}

			} else if ("require_input".equals(params[1])) {

				if (data.require_input == null) {

					if (((Integer) 2).equals(data.captcha_mode) && (data.custom_a_question == null || data.custom_kw == null)) {

						callback.alert(

							"你正在使用自定义验证模式",

							"需要设定回答模式的问题与答案才能开启回答模式"

						);

						return;

					}

					data.require_input = true;

					callback.text("🚪  要求输入答案");


				} else {

					if (((Integer) 2).equals(data.captcha_mode) && (data.custom_i_question == null || data.custom_items == null)) {

						callback.alert(

							"你正在使用自定义验证模式",

							"需要设定选择模式的问题与选项才能关闭回答模式"

						);

						return;

					}

					data.require_input = null;

					callback.text("🚪  要求选择答案");

				}

			} else if ("invite_user".equals(params[1])) {

				if (data.invite_user_ban == null) {

					data.invite_user_ban = true;

					callback.text("🚪  封锁");

				} else {

					data.invite_user_ban = null;

					callback.text("🚪  移除");

				}

			} else if ("invite_bot".equals(params[1])) {

				if (data.invite_bot_ban == null) {

					data.invite_bot_ban = true;

					callback.text("🚪  封锁");

				} else {

					data.invite_bot_ban = null;

					callback.text("🚪  移除");

				}

			} else if ("mode_cus".equals(params[1])) {

				callback.edit("编辑自定义问题. 对错选项或正确内容.\n\n" + cusStats(data) + "\n\n" + OptionsMain.doc).buttons(cusMenu(data)).async();

				return;

			}

			callback.editMarkup(joinMenu(data));


		} else {

			if ("enable_cus".equals(params[1])) {

				if (((Integer) 2).equals(data.captcha_mode)) {

					callback.text("🚪  已关闭");

					data.captcha_mode = null;

				} else if (data.require_input == null && (data.custom_i_question == null || data.custom_items == null)) {

					callback.alert(

						"你正在使用选项模式",

						"需要设定选项模式的问题与选项才能继续"


					);

					return;

				} else if (data.require_input != null && (data.custom_a_question == null || data.custom_kw == null)) {

					callback.alert(

						"你正在使用回答模式",

						"需要设定回答模式的问题与正确回答才能继续"


					);

					return;

				} else {

					callback.text("🚪  已开启");

					data.captcha_mode = 2;

				}

				callback.edit("编辑自定义问题. 对错选项或正确内容.\n\n" + OptionsMain.doc,cusStats(data)).buttons(cusMenu(data)).async();

			} else if ("reset_i_question".equals(params[1])) {

				callback.confirm();

				EditCustom edit = new EditCustom(0,callback,data);

				callback.send("现在发送问题 :").exec(edit);

				setPrivatePoint(user,POINT_CUSTOM,edit);

			} else if ("reset_items".equals(params[1])) {

				callback.confirm();

				EditCustom edit = new EditCustom(1,callback,data);

				callback.send("现在发送选项 每行一个 至少一个 最多六个 正确答案以 + 号开头 :").exec(edit);

				setPrivatePoint(user,POINT_CUSTOM,edit);

			} else if ("reset_a_question".equals(params[1])) {

				callback.confirm();

				EditCustom edit = new EditCustom(2,callback,data);

				callback.send("现在发送问题 :").exec(edit);

				setPrivatePoint(user,POINT_CUSTOM,edit);

			} else if ("reset_answer".equals(params[1])) {

				callback.confirm();

				EditCustom edit = new EditCustom(3,callback,data);

				callback.send("现在发送正确关键字 每行一个 :").exec(edit);

				setPrivatePoint(user,POINT_CUSTOM,edit);

			}

		}


	}

	class EditCustom extends PointData {

        int type;
        Callback origin;
        GroupData data;

        public EditCustom(int type,Callback origin,GroupData data) {

			this.type = type;
            this.origin = origin;
            this.data = data;

        }

        @Override
        public void onFinish() {

			super.onFinish();

			origin.edit("编辑自定义问题. 对错选项或正确内容.\n\n" + OptionsMain.doc,cusStats(data)).buttons(cusMenu(data)).async();

        }

    }

	@Override
    public void onPoint(UserData user,Msg msg,String point,PointData data) {

        EditCustom edit = (EditCustom) data.with(msg);

        if (edit.type == 0) {

            if (!msg.hasText()) {

                msg.send("请输入新的选择模式问题 :").withCancel().exec(data);

                return;

            }

            edit.data.custom_i_question = msg.text();

            clearPrivatePoint(user);

        } else if (edit.type == 1) {

            if (!msg.hasText()) {

                msg.send("请输入新的选择模式选项 :").withCancel().exec(data);

                return;

            }

            List<GroupData.CustomItem> items = new LinkedList<>();

            boolean valid = false;

            ArrayList<String> buttons = new ArrayList<>();

            for (final String line : msg.text().split("\n")) {

                if (buttons.contains(line)) {

                    msg.send("选项重复 : " + line).withCancel().exec(data);

                    return;

                }


                if (line.startsWith("+")) {

                    buttons.add(line.substring(1));

                    valid = true;

                    items.add(new GroupData.CustomItem() {{

								this.isValid = true;
								this.text = line.substring(1);

							}});

                } else {

                    buttons.add(line);

                    items.add(new GroupData.CustomItem() {{

								this.isValid = false;
								this.text = line;

							}});

                }

            }

            if (items.isEmpty()) {

                msg.send("选项为空 请重试").withCancel().exec(data);

                return;

            } else if (items.size() > 6) {

                msg.send("选项太多 (> 6)").exec(data);

                return;

            } else if (!valid) {

                msg.send("没有包含一个正确选项 :\n\n每行一个选项，正确选项以+开头").exec(data);

                return;

            }

            edit.data.custom_items = items;

            clearPrivatePoint(user);

        } else if (edit.type == 2) {

            if (!msg.hasText()) {

                msg.send("请输入新的回答模式问题 :").withCancel().exec(data);

                return;

            }

            edit.data.custom_a_question = msg.text();

            clearPrivatePoint(user);

        } else if (edit.type == 3) {

            if (StrUtil.isBlank(msg.text())) {

                msg.send("请输入新的回答模式答案 :").withCancel().exec(data);

                return;

            }

            LinkedList<String> custom_kw = new LinkedList<>();

            for (String kw : msg.text().split("\n")) {

                if (!StrUtil.isBlank(kw)) {

                    custom_kw.add(kw);

                }

            }

            if (custom_kw.isEmpty()) {

                msg.send("为空 请重试！").withCancel().exec(data);

                return;

            }

            edit.data.custom_kw = custom_kw;

            clearPrivatePoint(user);

		}

	}


    ButtonMarkup joinMenu(final GroupData data) {

        return new ButtonMarkup() {{

				newButtonLine()
                    .newButton("开启验证")
                    .newButton(data.join_captcha != null ? "✅" : "☑",POINT_CAPTCHA,data.id,"enable");

				newButtonLine()
                    .newButton("被动模式")
                    .newButton(data.passive_mode != null ? "✅" : "☑",POINT_CAPTCHA,data.id,"passive");

				newButtonLine("容错次数 : " + (data.ft_count == null ? 0 : data.ft_count),"null");

				newButtonLine().newButton("➖",POINT_CAPTCHA,data.id,"ft_dec").newButton("➕",POINT_CAPTCHA,data.id,"ft_inc");

				newButtonLine("时间上限 : " + data.parse_time(),"null");

				newButtonLine()
                    .newButton("➖",POINT_CAPTCHA,data.id,"jt_dec")
                    .newButton("➖➖",POINT_CAPTCHA,data.id,"jt_dec_t")
                    .newButton("➕",POINT_CAPTCHA,data.id,"jt_inc")
                    .newButton("➕➕",POINT_CAPTCHA,data.id,"jt_inc_t");

				newButtonLine()
                    .newButton("验证失败")
                    .newButton(data.fail_ban == null ? "移除" : "封锁",POINT_CAPTCHA,data.id,"fail_ban");

				newButtonLine()
                    .newButton("保留验证消息")
                    .newButton(data.captcha_del == null ? "延时删除" : data.captcha_del == 0 ? "保留一条" : "全部保留",POINT_CAPTCHA,data.id,"captcha_del");


				newButtonLine("验证期间邀请用户");

				newButtonLine()
                    .newButton("邀请用户")
                    .newButton(data.invite_user_ban == null ? "移除" : "封锁",POINT_CAPTCHA,data.id,"invite_user");

				newButtonLine()
                    .newButton("邀请机器人")
                    .newButton(data.invite_bot_ban == null ? "移除" : "封锁",POINT_CAPTCHA,data.id,"invite_bot");

				newButtonLine("审核模式","null");

				newButtonLine()
                    .newButton("默认模式")
                    .newButton(data.captcha_mode == null ? "●" : "○",POINT_CAPTCHA,data.id,"mode_def");

				newButtonLine()
                    .newButton("验证码")
                    .newButton(((Integer) 0).equals(data.captcha_mode) ? "●" : "○",POINT_CAPTCHA,data.id,"mode_code");

				newButtonLine()
                    .newButton("算数题")
                    .newButton(((Integer) 1).equals(data.captcha_mode) ? "●" : "○",POINT_CAPTCHA,data.id,"mode_math");

				newButtonLine()
                    .newButton("自定义")
                    .newButton(((Integer) 2).equals(data.captcha_mode) ? "●" : "○",POINT_CAPTCHA,data.id,"mode_cus");

				newButtonLine()
                    .newButton("图片描述")
                    .newButton(data.with_image != null ? "✅" : "☑",POINT_CAPTCHA,data.id,"with_image");

				newButtonLine()
                    .newButton("干扰按钮")
                    .newButton(data.interfere != null ? "✅" : "☑",POINT_CAPTCHA,data.id,"interfere");

				newButtonLine()
                    .newButton("回答模式")
                    .newButton(data.require_input != null ? "✅" : "☑",POINT_CAPTCHA,data.id,"require_input");

				newButtonLine("🔙",OptionsMain.POINT_OPTIONS,data.id);

			}};

    }

    String cusStats(GroupData data) {

        StringBuilder stats = new StringBuilder();

        stats.append("选择模式问题 : ");

        if (data.custom_i_question == null) {

            stats.append("未设定");

        } else {

            stats.append(data.custom_i_question);

        }

        stats.append("\n选择模式选项 : ");

        if (data.custom_items == null) {

            stats.append("未设定");

        } else {

            stats.append("\n").append(ArrayUtil.join(data.custom_items.toArray(),"\n"));

        }

        stats.append("\n\n");

        stats.append("回答模式问题 : ");

        if (data.custom_a_question == null) {

            stats.append("未设定");

        } else {

            stats.append(data.custom_a_question);

        }

        stats.append("\n正确关键字 : ");

        if (data.custom_kw == null) {

            stats.append("未设定");

        } else {

            stats.append(ArrayUtil.join(data.custom_kw.toArray(),"\n"));

        }

        return stats.toString();

    }

    ButtonMarkup cusMenu(final GroupData data) {

        return new ButtonMarkup() {{

				newButtonLine()
                    .newButton("使用自定义问题")
                    .newButton(((Integer) 2).equals(data.captcha_mode) ? "✅" : "☑",POINT_CUSTOM,data.id,"enable_cus");

				newButtonLine("设置选择模式问题",POINT_CUSTOM,data.id,"reset_i_question");
				newButtonLine("设置选择模式选项",POINT_CUSTOM,data.id,"reset_items");

				newButtonLine("设置回答模式问题",POINT_CUSTOM,data.id,"reset_a_question");
				newButtonLine("设置回答模式答案",POINT_CUSTOM,data.id,"reset_answer");

				newButtonLine("🔙",POINT_CAPTCHA,data.id);

			}};

    }

}
