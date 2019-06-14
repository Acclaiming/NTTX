package io.kurumi.ntt.db;

import cn.hutool.core.util.*;
import com.pengrad.telegrambot.model.*;
import io.kurumi.ntt.utils.*;

import java.util.*;

public class UserData {

    public static Data<UserData> data = new Data<UserData>(UserData.class);

    public static HashMap<Long, UserData> userDataIndex = new HashMap<>();
    public Long id;
    public String firstName;
    public String lastName;
    public String userName;
    public Boolean contactable;

    public static UserData get(Long userId) {

        if (userDataIndex.size() > 1000) {

            userDataIndex.clear();

        } else if (userDataIndex.containsKey(userId)) return userDataIndex.get(userId);

        synchronized (userDataIndex) {

            if (userDataIndex.containsKey(userId)) {

                return userDataIndex.get(userId);
            }

            UserData userData = data.getById(userId);

            if (userData != null) {

                userDataIndex.put(userId, userData);
            }

            return userData;

        }

    }

    public static UserData get(User user) {

        if (user == null) return null;

        if (userDataIndex.size() > 1000) {

            userDataIndex.clear();

        } else if (userDataIndex.containsKey(user.id())) {

            checkUpdate(userDataIndex.get(user.id()), user);


        }

        synchronized (userDataIndex) {

            if (userDataIndex.containsKey(user.id())) {

                return checkUpdate(userDataIndex.get(user.id()), user);
            }


            if (data.containsId(user.id())) {

                UserData userData = data.getById(user.id());

                userDataIndex.put(user.id(), userData);

                return checkUpdate(userData, user);


            } else {

                UserData userData = new UserData();

                userData.id = user.id();

                userData.read(user);

                data.setById(user.id(), userData);

                userDataIndex.put(user.id(), userData);

                return userData;

            }

        }

    }

    static UserData checkUpdate(UserData userData, User user) {

        if (!ObjectUtil.equal(user.firstName(), userData.firstName) ||
                ObjectUtil.equal(user.lastName(), userData.lastName)) {

            userData.read(user);

            data.setById(userData.id, userData);

        }

        return userData;

    }

    public void read(User user) {

        userName = user.username();

        firstName = user.firstName();

        lastName = user.lastName();

    }

    public boolean contactable() {

        if (NTT.isUserContactable(id) != contactable) {

            contactable = !contactable;

            data.setById(id, this);

        }

        return contactable;

    }

    public String formattedName() {

        return name() + " (" + userName != null ? userName : id + ") ";

    }

    public String name() {

        String name = firstName;

        if (lastName != null) {

            name = name + " " + lastName;

        }

        return name;

    }


    public String userName() {

        return Html.a(StrUtil.isBlank(name()) ? "[已重置]" : name(), "tg://user?id=" + id);

    }

    public boolean developer() {

        return
                865266732 == id || // 咱
                        589593327 == id || // Peace of Mind
                        748525422 == id; // 绮 雨

    }

}
