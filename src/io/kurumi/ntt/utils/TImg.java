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
import cn.hutool.core.util.ArrayUtil;
import java.util.Collection;

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
                    .asBufferedImage(), 75, 425, 50, 50, null);

            } catch (IOException e) {}

            graphics.setFont(new Font(FONT_CHS, Font.PLAIN, 13));

            graphics.drawString(account.archive().name, 75, 500);

        }
        
        HashMap<Long, Score> received = received(account);

        HashMap<Long,  Score> sended = sended(account);

        HashMap<Long,Score> allMap = new HashMap<>();
        
        allMap.putAll(received);
        
        for (Score score : sended.values()) {
            
            if (allMap.containsKey(score.id)) allMap.get(score.id).score += score.score;
            else allMap.put(score.id,score);
            
        }
        
        LinkedList<Score> all = new LinkedList<>(allMap.values());

        Collections.sort(all);
        
        for (int index = 0;index < 10 && index < all.size();index ++) {
            
            int x = index < 5 ? 350 : 650;
            
            int y = 50 + ((index < 5 ? index : index - 5) + 1) * 120;
            
           Score score = all.get(index);
           
           if (score.photo == null) {
               
               UserArchive target = UserArchive.show(account, score.id);
               
               score.name = target.name;
               score.photo = target.photoUrl;

           }
           
           File userPhoto = photoImage(score.photo);
           
            if (userPhoto.isFile()) {

                try {

                    graphics.drawImage(
                        Thumbnails.of(userPhoto)
                        .size(50, 50)
                        .asBufferedImage(), x,y,50, 50, null);

                } catch (IOException e) {}

             
                
                graphics.drawString(score.name, x + 75 , y + 25 - 13);

                Score rc = received.get(score.id);
                Score sc = sended.get(score.id);

                StringBuilder status = new StringBuilder();
               
                if (sc != null) {
                    
                    status.append("发出 : ").append(sc.score).append(" / ");
                    
                }
                
                if (rc != null) {
                
               status.append("收到 : ").append(rc.score);
               
               }
               
               
                
                graphics.drawString(status.toString(), x + 75, y + 25 + 13);
                
            }
            
        }
        
        msg.sendUpdatingPhoto();

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {

            Thumbnails.of(image)
                .size(1000, 800)
                .outputFormat("png")
                .outputQuality(1f)
                .toOutputStream(out);

        } catch (IOException e) {}

       // msg.send(ArrayUtil.join( received.toArray(),"\n")).exec();
        
        bot().execute(new SendPhoto(msg.chatId(), out.toByteArray()));

    }

    class Score implements Comparable {

        long id;
        int score;

        String name;
        String photo;
        
        @Override
        public int compareTo(Object score) {

            return ((Score)score).score - this.score;

        }

        @Override
        public boolean equals(Object score) {
          
            return super.equals(score) || ((Score)score).id == id;
            
        }

        @Override
        public String toString() {
            // TODO: Implement this method
            return name + " : " + score;
        }
        

    }

    HashMap<Long,Score> received(TAuth account) {

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
                    
                    score.name = mention.getUser().getName();
                    score.photo = mention.getUser().getProfileImageURLHttps();

                }

                score.score ++;
                
                scores.put(id, score);
                

            }

        } catch (TwitterException e) {}

        return scores;
        
    }
    
    HashMap<Long,Score> sended(TAuth account) {

        Twitter api = account.createApi();

        HashMap<Long,Score> scores = new HashMap<>();

        try {

            ResponseList<Status> statuses =  api.getUserTimeline(new Paging().count(200));

            for (Status status : statuses) {

                long id = status.getInReplyToUserId();

                if (id == -1) continue;
                
                Score score = scores.get(id);

                if (score == null) {

                    score = new Score();

                    score.id = id;
                    score.score = 0;

                }

                score.score ++;

                scores.put(id, score);
                

            }

        } catch (TwitterException e) {}


        return scores;
       
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
