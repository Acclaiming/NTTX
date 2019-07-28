package io.kurumi.ntt.db;

import com.mongodb.client.MongoCollection;

public class LongArrayData extends AbsData<Long,DataLongArray> {
	
	public LongArrayData(Class clazz) {

        super(clazz.getSimpleName(),DataLongArray.class);

    }

    public LongArrayData(String collectionName) {

		super(collectionName,DataLongArray.class);

    }

	public boolean add(final Long data) {

		if (containsId(data)) return false;

		setById(data,new DataLongArray() {{ id = data; }});

		return true;

	}

}
