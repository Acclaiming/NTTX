package io.kurumi.ntt.utils;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ImageUtil;
import cn.hutool.http.HttpUtil;
import io.kurumi.ntt.Env;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.abs.Msg;
import io.kurumi.ntt.fragment.abs.TwitterFunction;
import io.kurumi.ntt.fragment.twitter.TAuth;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.util.LinkedList;
import java.awt.Image;
import com.pengrad.telegrambot.request.SendPhoto;
import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;

public class Image extends TwitterFunction {

    @Override
    public void functions(LinkedList<String> names) {

        names.add("img");

    }

    @Override
    public void onFunction(UserData user, Msg msg, String function, String[] params, TAuth account) {

        BufferedImage image = new BufferedImage(1000, 800, BufferedImage.TYPE_INT_RGB);

        Graphics2D graphics = image.createGraphics();

        File myPhoto = photoImage(account.archive().photoUrl);

        if (myPhoto.isFile()) {

            graphics.drawImage(ImageUtil.read(myPhoto), 450, 450, 50, 50, new ImageObserver() {

                    @Override
                    public boolean imageUpdate(Image p1, int p2, int p3, int p4, int p5, int p6) {
                        // TODO: Implement this method
                        return false;
                    }
                });

        }

        msg.sendUpdatingPhoto();
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        ImageUtil.writeJpg(image,out);
        
        bot().execute(new SendPhoto(msg.chatId(),out.toByteArray()));
        
    }

    File photoImage(String url) {

        File photo = new File(Env.CACHE_DIR, "twitter_profile_images/" + FileUtil.getName(url));

        if (photo.isFile()) HttpUtil.downloadFile(url, photo);

        return photo;

    }

}
