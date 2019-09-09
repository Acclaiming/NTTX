package io.kurumi.ntt.fragment.twitter.spam;

import io.kurumi.ntt.db.AbsData;

import java.util.HashMap;
import java.util.List;

public class SpamTag {

    public static AbsData<String, SpamTag> data = new AbsData<>(SpamTag.class);

    public String id;

    public String description;

    public HashMap<String, Integer> records;

    public List<Long> subscribers;

}
