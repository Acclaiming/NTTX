package io.kurumi.ntt.td.client;

import cn.hutool.core.util.StrUtil;
import io.kurumi.ntt.Env;
import io.kurumi.ntt.td.TdApi;

public class TdBot extends TdClient {

    private String botToken;

    public TdBot(String botToken) {

        this(botToken, new TdOptions().databaseDirectory(Env.CACHE_DIR.getPath() + "/td/" + StrUtil.subBefore(botToken, ":", false)));

    }

    public TdBot(String botToken, TdOptions options) {

        super(options);

        this.botToken = botToken;

    }

    @Override
    public void onAuthorizationState(TdApi.UpdateAuthorizationState state) {

        super.onAuthorizationState(state);

        TdApi.AuthorizationState authState = state.authorizationState;

        if (authState instanceof TdApi.AuthorizationStateWaitPhoneNumber) {

            send(new TdApi.CheckAuthenticationBotToken(botToken));

        }

    }

}
