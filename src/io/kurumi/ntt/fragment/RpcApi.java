package io.kurumi.ntt.fragment;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.pengrad.telegrambot.response.SendResponse;
import io.kurumi.ntt.Env;
import io.kurumi.ntt.db.Data;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.twitter.TAuth;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.model.request.Send;
import io.kurumi.ntt.utils.Html;
import io.kurumi.ntt.utils.NTT;
import twitter4j.Status;
import twitter4j.TwitterException;

public class RpcApi extends Fragment {

    public static Data<RpcKey> data = new Data<>(RpcKey.class);

    public static class RpcKey {

        public Long id;

        public String uuid;

        public String key() {

            return id + "-" + uuid;

        }

    }

    @Override
    public void init(BotFragment origin) {

        super.init(origin);

        registerFunction("api_key", "api_key_regenerate", "api_key_revoke");

    }

    @Override
    public void onFunction(UserData user, Msg msg, String function, String[] params) {

        if (function.endsWith("revoke")) {

            data.deleteById(user.id);

            msg.send("已经删除令牌 .").async();

            return;

        }

        RpcKey key = data.getById(user.id);

        if (key == null || function.endsWith("regenerate")) {

            key = new RpcKey();

            key.id = user.id;

            key.uuid = UUID.fastUUID().toString(true);

            data.setById(key.id, key);

        }

        String message = user.userName() + " 你的授权令牌 :";

        message += "\n\n" + Html.code(key.key());

        message += "\n";

        message += "\n重置令牌 : /api_key_regenerate";
        message += "\n删除令牌 : /api_key_revoke";

        msg.send(message).html().async();

    }

    public static JSONObject execute(JSONObject request) {

        String apiKey = request.getStr("apiKey");

        Long user = getAuth(apiKey);

        if (user == -1L) {

            return makeError("unauthorized.");

        }

        String methodName = request.getStr("method");

        if ("send".equals(methodName)) {

            String text = request.getStr("text");

            if (text == null) {

                return makeError("empty text.");

            }

            Send send = new Send(user, text);

            String parseMode = request.getStr("parseMode", "plainText");

            if ("html".equals(parseMode.toLowerCase())) {

                send.html();

            } else if ("markdown".equals(parseMode.toLowerCase())) {

                send.markdown();

            }

            SendResponse response = send.exec();

            // log(user,"调用了发送消息 文本 : {} 解析方法 : {} 结果 : {}",text,parseMode,response.isOk() ? "成功" : "失败");

            if (response.isOk()) {

                JSONObject result = new JSONObject();

                result.put("messageId", response.message().messageId());

                return makeResult(result);

            } else {

                return makeError(response.description());

            }

        } else if ("update_status".equals(methodName)) {

            Long accountId = request.getLong("accountId");

            if (accountId == null) {

                return makeError("empty account id.");

            }

            TAuth account = TAuth.getById(accountId);

            if (account == null || !account.user.equals(user)) {

                return makeError("invalid account id.");

            }

            String text = request.getStr("text");

            if (text == null) {

                return makeError("empty status text.");

            }

            // log(user,"调用了发送推文 \n\n账号 : {}\n\n文本 : {}",account.archive().formatSimple());

            try {

                Status status = account.createApi().updateStatus(text);

                JSONObject result = new JSONObject();

                result.put("statusId", status.getId());

                return makeResult(result);

            } catch (TwitterException e) {

                return makeError(NTT.parseTwitterException(e));

            }

        } else {

            return makeError("method not found.");

        }

    }

    static void log(Long userId, String log, Object... args) {

        new Send(Env.LOG_CHANNEL, UserData.get(userId).userName() + " " + StrUtil.format(log, args)).html().async();

    }

    static JSONObject makeResult(JSONObject content) {

        JSONObject response = new JSONObject();

        response.put("ok", true);
        response.put("result", content);

        return response;

    }


    static JSONObject makeError(String content) {

        JSONObject response = new JSONObject();

        response.put("ok", false);
        response.put("description", content);

        return response;

    }

    static Long getAuth(String apiKey) {

        if (StrUtil.isBlank(apiKey) || !apiKey.contains("-")) return -1L;

        String userIdStr = StrUtil.subBefore(apiKey, "-", false);

        if (!NumberUtil.isNumber(userIdStr)) return -1L;

        Long userId = NumberUtil.parseLong(userIdStr);

        RpcKey key = data.getById(userId);

        if (key == null && !key.key().equals(apiKey)) return -1L;

        return userId;

    }

}
