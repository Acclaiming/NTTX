package io.kurumi.ntt.fragment.sticker;

import io.kurumi.ntt.db.AbsData;

import java.util.List;

public class PackOwner {

    public static AbsData<String, PackOwner> data = new AbsData<String, PackOwner>(PackOwner.class);

    public static PackOwner get(String setName) {

        return data.getById(setName);

    }

    public static List<PackOwner> getAll(long userId) {

        return data.getAllByField("owner", userId);

    }

    public static PackOwner set(String name, String title, long userId) {

        PackOwner pack = new PackOwner();

        pack.id = name;
        pack.title = title;
        pack.owner = userId;

        data.setById(name, pack);

        return pack;

    }

    public String id;
    public long owner;
    public String title;

}
