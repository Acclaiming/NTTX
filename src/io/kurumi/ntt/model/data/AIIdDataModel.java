package io.kurumi.ntt.model.data;

import io.kurumi.ntt.BotConf;

public abstract class AIIdDataModel extends IdDataModel {

    protected Factory factory;
    
    public AIIdDataModel(Factory factory,String dirName) {

        super(dirName);
        
        this.factory = factory;
        
        id = -1L;

        init();

    }
    
    public AIIdDataModel(String dirName,Long id) { super(dirName,id); }
    
    @Override
    public void save() {
        
        if (id == -1) {

            id = Long.parseLong(BotConf.getOrDefault("id." + dirName.replace("/","."),"0")) + 1L;

            BotConf.set("id." + dirName.replace("/","."),id);

        }
       
        super.save();
        
        factory.saveObj(this);
        
    }
    
    public static class Factory<T extends AIIdDataModel> extends IdDataModel.Factory<T> {
        
        public Factory(Class<T> clazz, String dirName) { super(clazz,dirName); }
        
        public T newObj() {
            
            try {

                T obj = clazz.getDeclaredConstructor(new Class[] {Factory.class,String.class}).newInstance(dirName);

                return obj;

            } catch (Exception e) {

                throw new RuntimeException(e);

            }
            
            
        }
        
    }
    
}
