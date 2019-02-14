package io.kurumi.nttools.timer;

import cn.hutool.core.io.FileUtil;
import io.kurumi.nttools.fragments.MainFragment;
import java.io.File;
import cn.hutool.core.util.ZipUtil;
import java.util.Date;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import io.kurumi.nttools.model.request.Send;
import com.pengrad.telegrambot.request.SendDocument;

public class BackupTask implements TimerTask {

    public static final BackupTask INSTANCE = new BackupTask();
    
    @Override
    public void run(final MainFragment fragment) {

        FileUtil.del(new File(fragment.dataDir, "cache"));

        final File file = new File(fragment.dataDir.getParentFile(), "DATA " + new Date().toLocaleString() + ".zip");

        try {

            ZipFile zip = new ZipFile(file);

            ZipParameters params = new ZipParameters();

            params.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_ULTRA);

            /*

             params.setEncryptFiles(true);

             params.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);

             params.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);

             params.setPassword("");

             */

            zip.addFolder(fragment.dataDir,params);
            
            final Long id = fragment.findUserData("HiedaNaKan").id;

            fragment.main.threadPool.execute(new Runnable() {
                
                    @Override
                    public void run() {
                        
                        fragment.main.bot.execute(new SendDocument(id,file));
                        
                        FileUtil.del(file);
                        
                    }
                    
                });
            
        } catch (ZipException e) {}
        
    }

}
