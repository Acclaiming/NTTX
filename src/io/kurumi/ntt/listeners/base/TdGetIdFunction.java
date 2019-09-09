package io.kurumi.ntt.listeners.base;

import io.kurumi.ntt.td.TdApi.User;
import io.kurumi.ntt.td.client.TdFunction;
import io.kurumi.ntt.td.model.TMsg;

public class TdGetIdFunction extends TdFunction {

    @Override
    public String functionName() {

        return "id";
    }

    @Override
    public void onFunction(User user, TMsg msg, String function, String[] params) {

        TextBuilder message = new TextBuilder();

        if (!msg.isPrivate()) {

            message.bold("CID").text(" : ").code(msg.chatId + "");

            if (!msg.isChannel()) message.text("\n");

        }

        if (!msg.isChannel()) {

            message.bold("UID").text(" : ").code(user.id + "");

        }

        send(chatId(msg.chatId).input(inputText(message)));

    }

}
