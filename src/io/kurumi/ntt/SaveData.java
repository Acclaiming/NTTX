package io.kurumi.ntt;

import io.kurumi.ntt.ui.ext.*;

public class SaveData implements Runnable {

    @Override
    public void run() {
        
        Constants.data.save();
        
        for (UserData userData : Constants.data.getUsers()) {
            
            userData.save();
            
        }
        
        System.out.println("数据已保存..");
        
    }
    
}
