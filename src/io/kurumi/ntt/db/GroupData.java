package io.kurumi.ntt.db;

import com.pengrad.telegrambot.model.*;
import io.kurumi.ntt.*;
import com.pengrad.telegrambot.request.*;
import com.pengrad.telegrambot.response.*;

public class GroupData {

    public static Data<GroupData> data = new Data<GroupData>(GroupData.class);
    public Long id;
    public String name;
    public String userName;
    public String inviteLink;

    public static GroupData get(Long id) {

        return data.getById(id);

    }

    public static GroupData save(Chat group) {

        GroupData saved = data.getById(group.id());

        if (saved == null) {

            saved = new GroupData();
            saved.id = group.id();

        }

        saved.read(group);

        data.setById(group.id(), saved);

        return saved;

    }

    public boolean exportInviteLink() {

        StringResponse resp = Launcher.INSTANCE.bot().execute(new ExportChatInviteLink(id));

        if (resp.isOk()) {

            inviteLink = resp.result();

        }

        return resp.isOk();

    }

    public void read(Chat group) {

        name = group.title();

        userName = group.username();

        inviteLink = group.inviteLink();

    }

}
