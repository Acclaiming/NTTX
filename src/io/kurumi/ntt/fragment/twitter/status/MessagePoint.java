package io.kurumi.ntt.fragment.twitter.status;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.NumberUtil;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import io.kurumi.ntt.db.AbsData;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.*;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.gt;
import static com.mongodb.client.model.Filters.not;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;

public class MessagePoint {

    public static AbsData<Integer, MessagePoint> data = new AbsData<Integer, MessagePoint>(MessagePoint.class) {{

			collection.deleteMany(exists("createAt",false));
			collection.deleteMany(lt("createAt",System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L));

		}};

    public int id;
    public int type;

	public long accountId;
    public long userId;
    public long targetId;

    public long createAt;

    public static MessagePoint setDM(final int messageId,long userId,long dmId) {

        MessagePoint point = new MessagePoint();

        point.createAt = System.currentTimeMillis();

        point.id = messageId;

        point.type = 2;

        point.targetId = dmId;

        data.setById(messageId,point);

        return point;


    }

    public static MessagePoint set(final int messageId,int type,long targetId) {

        MessagePoint point = new MessagePoint();

        point.createAt = System.currentTimeMillis();

        point.id = messageId;

        point.type = type;

        point.targetId = targetId;

        data.setById(messageId,point);

        return point;

    }

    // 0 : user
    // 1 : status
    // 2 : dm

    public static MessagePoint get(int messageId) {

        return data.getById(messageId);

    }

	public static MessagePoint getFromStatus(Message msg) {

		InlineKeyboardMarkup markup = msg.replyMarkup();

		if (markup == null) return null;

		MessagePoint point = new MessagePoint();

		point.type = 1;

		InlineKeyboardButton[] buttons = markup.inlineKeyboard()[0];

		String[] params = ArrayUtil.remove(buttons[2].callbackData().split(","),0);

		if (params.length != 3) {

			return null;

		}

		point.accountId = NumberUtil.parseLong(ArrayUtil.remove(buttons[0].callbackData().split(","),0)[0]);
		point.targetId = NumberUtil.parseLong(ArrayUtil.remove(buttons[1].callbackData().split(","),0)[0]);
		
		return point;

    }

}
