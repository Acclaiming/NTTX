package io.kurumi.ntt.utils;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ImageUtil;
import cn.hutool.http.HttpUtil;
import com.pengrad.telegrambot.request.SendPhoto;
import io.kurumi.ntt.Env;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.abs.Msg;
import io.kurumi.ntt.fragment.abs.TwitterFunction;
import io.kurumi.ntt.fragment.twitter.TAuth;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.LinkedList;
import java.awt.Paint;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;

public class TImg extends TwitterFunction {

    @Override
    public void functions(LinkedList<String> names) {

        names.add("img");

    }

    @Override
    public void onFunction(UserData user, Msg msg, String function, String[] params, TAuth account) {

        BufferedImage image = new BufferedImage(1000, 800, BufferedImage.TYPE_INT_RGB);

        Graphics2D graphics = image.createGraphics();

        File myPhoto = photoImage(account.archive().photoUrl);

        graphics.setBackground(Color.WHITE);
        graphics.clearRect(0,0,1000,800);
       
        graphics.setFont(new Font("Roboto-Thin",Font.PLAIN,56));
        
        graphics.drawString("喵.....", 450, 100);
        
        if (myPhoto.isFile()) {

            graphics.drawImage(ImageUtil.read(myPhoto), 475, 475, 50, 50, null);

            graphics.setFont(new Font("Roboto-Thin",Font.PLAIN,13));
            
            graphics.drawString(account.archive().name,475,500);
            
        }

        msg.sendUpdatingPhoto();
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        ImageUtil.writeJpg(image,out);
        
        bot().execute(new SendPhoto(msg.chatId(),out.toByteArray()));
        
    }

    File photoImage(String url) {

        File photo = new File(Env.CACHE_DIR, "twitter_profile_images/" + FileUtil.getName(url));

        if (!photo.isFile()) HttpUtil.downloadFile(url, photo);

        return photo;

    }

}
