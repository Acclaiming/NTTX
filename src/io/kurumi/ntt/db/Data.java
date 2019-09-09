package io.kurumi.ntt.db;

import java.util.*;

public class Data<T> extends AbsData<Long, T> {

    public Data(Class<T> clazz) {

        super(clazz);

    }

    public Data(String collectionName, Class<T> clazz) {

        super(collectionName, clazz);

    }

}
