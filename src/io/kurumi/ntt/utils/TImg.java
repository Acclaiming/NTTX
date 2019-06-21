package io.kurumi.ntt.utils;

import java.awt.*;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ImageUtil;
import cn.hutool.http.HttpUtil;
import com.pengrad.telegrambot.request.SendPhoto;
import io.kurumi.ntt.Env;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.abs.Msg;
import io.kurumi.ntt.fragment.abs.TwitterFunction;
import io.kurumi.ntt.fragment.twitter.TAuth;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.LinkedList;

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

        graphics.setBackground(Color.getColor("#FFFFFF"));
        graphics.clearRect(0,0,1000,800);
       
        graphics.setColor(Color.getColor("#000000"));
        
        graphics.setFont(new Font("Roboto Thin",Font.PLAIN,56));
        
       drawCenteredString(graphics,100,"Test");
       
        if (myPhoto.isFile()) {

            graphics.drawImage(ImageUtil.read(myPhoto), 475, 475, 50, 50, null);

            graphics.setFont(new Font("Roboto Thin",Font.PLAIN,13));
            
            graphics.drawString(account.archive().name,485,530);
            
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
    
    public void drawCenteredString(Graphics2D g,int y, String text) {
        
        FontMetrics metrics = g.getFontMetrics();
        
        int x = 1000 - metrics.stringWidth(text) / 2;
        
        g.drawString(text, x, y);
        
    }

}
