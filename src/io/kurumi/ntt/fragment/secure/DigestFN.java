package io.kurumi.ntt.fragment.secure;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.Msg;

public class DigestFN extends Fragment {

    @Override
    public void init(BotFragment origin) {

        super.init(origin);

        registerFunction("md5", "sha1", "sha256");

    }

    @Override
    public void onFunction(UserData user, Msg msg, String function, String[] params) {

        if (StrUtil.isBlank(msg.param())) {

            msg.invalidParams("文本...").async();

            return;

        }

        if ("md5".equals(function)) {

            msg.send(SecureUtil.md5(msg.param())).async();

        } else if ("sha1".equals(function)) {

            msg.send(SecureUtil.sha1(msg.param())).async();

        } else if ("sha256".equals(function)) {

            msg.send(SecureUtil.sha256(msg.param())).async();

        }

    }

}
