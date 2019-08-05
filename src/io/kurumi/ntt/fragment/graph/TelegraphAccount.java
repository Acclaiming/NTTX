package io.kurumi.ntt.fragment.graph;

import io.kurumi.ntt.db.Data;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.telegraph.Telegraph;
import io.kurumi.ntt.telegraph.model.Account;

public class TelegraphAccount {

    public static Data<TelegraphAccount> data = new Data<TelegraphAccount>(TelegraphAccount.class);

    public static TelegraphAccount revokeDefaultAccount() {

        data.deleteById(-1L);

        return defaultAccount();

    }

    public static TelegraphAccount defaultAccount() {

        if (!data.containsId(-1L)) {

            Account account = Telegraph.createAccount("NTT", "NTT", "https://t.me/NTT");

            if (account == null) return null;

            TelegraphAccount auth = new TelegraphAccount();

            auth.id = -1L;

            auth.access_token = account.access_token;

            auth.short_name = account.short_name;

            auth.author_name = account.author_name;

            auth.author_url = account.author_url;

            data.setById(auth.id, auth);

            return auth;

        }

        TelegraphAccount account = data.getById(-1L);

        return account;

    }


    public static TelegraphAccount get(UserData user) {

        if (!data.containsId(user.id)) {

            Account account = Telegraph.createAccount(user.userName == null ? user.id.toString() : user.userName, user.name(), "https://" + (user.userName == null ? "NTT_X" : user.userName));

            if (account == null) return null;

            TelegraphAccount auth = new TelegraphAccount();

            auth.id = user.id;

            auth.access_token = account.access_token;

            auth.short_name = account.short_name;

            auth.author_name = account.author_name;

            auth.author_url = account.author_url;

            data.setById(auth.id, auth);

            return auth;

        }

        TelegraphAccount account = data.getById(user.id);


        return account;

    }

    public Long id;

    public String short_name;

    public String author_name;

    public String author_url;

    public String access_token;

}
