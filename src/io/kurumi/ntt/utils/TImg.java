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
import net.coobird.thumbnailator.Thumbnails;
import java.io.IOException;
import io.kurumi.ntt.fragment.twitter.archive.UserArchive;
import twitter4j.Twitter;
import twitter4j.Paging;
import twitter4j.TwitterException;
import twitter4j.ResponseList;
import twitter4j.Status;
import java.util.HashMap;
import io.kurumi.ntt.utils.TImg.Score;
import java.util.Collections;

public class TImg extends TwitterFunction {

    @Override
    public void functions(LinkedList<String> names) {

        names.add("img");

    }

    final String FONT_CHS = "Noto Sans CJK SC Thin";

    @Override
    public void onFunction(UserData user, Msg msg, String function, String[] params, TAuth account) {

        BufferedImage image = new BufferedImage(1000, 800, BufferedImage.TYPE_INT_ARGB);

        Graphics2D graphics = image.createGraphics();

        File myPhoto = photoImage(account.archive().photoUrl);

        graphics.setBackground(Color.getColor("#FFFFFF"));
        graphics.clearRect(0, 0, 1000, 800);

        graphics.setColor(Color.BLACK);
        graphics.setPaint(Color.BLACK);

        graphics.setFont(new Font(FONT_CHS, Font.PLAIN, 56));

        drawCenteredString(graphics, 100, "某个图片测试 :)");

        if (myPhoto.isFile()) {

            try {

                graphics.drawImage(
                    Thumbnails.of(myPhoto)
                    .size(50, 50)
                    .asBufferedImage(), 275, 575, 50, 50, null);

            } catch (IOException e) {}

            graphics.setFont(new Font(FONT_CHS, Font.PLAIN, 10));

            graphics.drawString(account.archive().name, 275, 610);

        }
        
        LinkedList<Score> received = received(account);
        
        LinkedList<Score> sended = sended(account);
        
        LinkedList<Score> all = new LinkedList<>();
        
        all.addAll(received);
        
        for (Score score : sended) {
            
            if (all.contains(score)) all.get(all.indexOf(score)).score += score.score;
            else all.add(score);
            
        }
        
        Collections.sort(all);
        
        
        
        msg.sendUpdatingPhoto();

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {

            Thumbnails.of(image)
                .size(1000, 800)
                .outputFormat("png")
                .outputQuality(1f)
                .toOutputStream(out);

        } catch (IOException e) {}

        bot().execute(new SendPhoto(msg.chatId(), out.toByteArray()));

    }

    class Score implements Comparable {

        long id;
        int score;

        @Override
        public int compareTo(Object score) {

            return this.score - ((Score)score).score;

        }

        @Override
        public boolean equals(Object score) {
          
            return super.equals(score) || ((Score)score).id == id;
            
        }
        
       

    }

    LinkedList<Score> received(TAuth account) {

        Twitter api = account.createApi();

        HashMap<Long,Score> scores = new HashMap<>();

        try {

            ResponseList<Status> mentions =  api.getMentionsTimeline(new Paging().count(200));

            for (Status mention : mentions) {

                if (account.id.equals(mention.getUser().getId())) continue;

                long id = mention.getUser().getId();

                Score score = scores.get(id);

                if (score == null) {

                    score = new Score();

                    score.id = id;
                    score.score = 0;

                    scores.put(id, score);

                }

                score.score ++;

            }

        } catch (TwitterException e) {}

        LinkedList<TImg.Score> result = new LinkedList<Score>(scores.values());

        Collections.sort(result);

        return result;

    }
    
    LinkedList<Score> sended(TAuth account) {

        Twitter api = account.createApi();

        HashMap<Long,Score> scores = new HashMap<>();

        try {

            ResponseList<Status> statuses =  api.getUserTimeline(new Paging().count(200));

            for (Status status : statuses) {

                if (account.id.equals(status.getUser().getId())) continue;

                long id = status.getUser().getId();

                Score score = scores.get(id);

                if (score == null) {

                    score = new Score();

                    score.id = id;
                    score.score = 0;

                    scores.put(id, score);

                }

                score.score ++;

            }

        } catch (TwitterException e) {}

        LinkedList<TImg.Score> result = new LinkedList<Score>(scores.values());

        Collections.sort(result);

        return result;
       
       }

    File photoImage(String url) {

        File photo = new File(Env.CACHE_DIR, "twitter_profile_images/" + FileUtil.getName(url));

        if (!photo.isFile()) HttpUtil.downloadFile(url, photo);

        return photo;

    }

    public void drawCenteredString(Graphics2D g, int y, String text) {

        FontMetrics metrics = g.getFontMetrics();

        int x = (1000 - metrics.stringWidth(text)) / 2;

        g.drawString(text, x, y);

    }

}
