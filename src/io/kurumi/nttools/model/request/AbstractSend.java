package io.kurumi.nttools.model.request;

import com.pengrad.telegrambot.response.BaseResponse;
import io.kurumi.nttools.fragments.Fragment;

public abstract class AbstractSend<T extends AbstractSend> {

    public Fragment fragment;
    
    public AbstractSend(Fragment fragment) {
        this.fragment = fragment;
    }
    
    public abstract T buttons(ButtonMarkup markup);
    
    public abstract T disableLinkPreview();
    
    public abstract T markdown();
    
    public abstract T html();
    
    public abstract BaseResponse sync();
    
    public void exec() {
        
        fragment.main.threadPool.execute(new Runnable() {

                @Override
                public void run() {
                    
                    exec();
                    
                }
                
            });
        
    }

}
