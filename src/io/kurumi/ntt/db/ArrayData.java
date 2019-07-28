package io.kurumi.ntt.db;

import com.mongodb.client.MongoCollection;

public class ArrayData<T> extends AbsData<T,DataArray<T>> {
	
	public ArrayData(Class clazz) {

        super(clazz.getSimpleName(), DataArray.class);

    }

    public ArrayData(String collectionName) {

		super(collectionName,DataArray.class);
        
    }

	public boolean add(final T data) {
		
		if (containsId(data)) return false;
		
		setById(data,new DataArray<T>() {{ id = data; }});
		
		return true;
		
	}
	
}
