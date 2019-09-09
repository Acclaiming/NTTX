package io.kurumi.ntt.fragment.group.codes;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;

public class MathCode extends VerifyCode {

    boolean img;

    public MathCode(boolean input, boolean img) {
        super(input);
        this.img = img;
    }

    int left = RandomUtil.randomInt(101);
    int right = RandomUtil.randomInt(31);

    int type = RandomUtil.randomInt(2);

    @Override
    public String question() {

        return "请" + (input ? "发送" : "选择") + " 答案 ~";

    }

    @Override
    public VerifyCode fork() {

        return new MathCode(input, img);

    }

    String typeCode() {

        switch (type) {

            case 0:
                return "+";

            default:
                return "-";

        }

    }

    @Override
    public String code() {

        return formatNumber(left + " " + typeCode() + " " + right);

    }

    @Override
    public String validCode() {

        return formatNumber((type == 0 ? left + right : left - right) + "");

    }

    @Override
    public String[] invalidCode() {

        return new String[]{

                formatNumber(RandomUtil.randomInt(-100, 101) + ""),
                formatNumber(RandomUtil.randomInt(-100, 101) + ""),
                formatNumber(RandomUtil.randomInt(-100, 101) + ""),
                formatNumber(RandomUtil.randomInt(-100, 101) + "")

        };

    }

    String formatNumber(String text) {

        if (img) return text;

        return text
                .replace("0", "0⃣")
                .replace("1", "1⃣")
                .replace("2", "2⃣")
                .replace("3", "3⃣")
                .replace("4", "4⃣")
                .replace("5", "5⃣")
                .replace("6", "6⃣")
                .replace("7", "7⃣")
                .replace("8", "8⃣")
                .replace("9", "9⃣")
                .replace("+", "➕")
                .replace("-", "➖");

    }

    String unFormatNumber(String text) {

        return text
                .replace("0⃣", "0")
                .replace("1⃣", "1")
                .replace("2⃣", "2")
                .replace("3⃣", "3")
                .replace("4⃣", "4")
                .replace("5⃣", "5")
                .replace("6⃣", "6")
                .replace("7⃣", "7")
                .replace("8⃣", "8")
                .replace("9⃣", "9")
                .replace("➕", "+")
                .replace("➖", "-");

    }


    @Override
    public boolean verify(String input) {

        try {

            return (type == 0 ? left + right : left - right) == NumberUtil.parseInt(unFormatNumber(input.trim()));

        } catch (Exception ex) {

            return false;

        }

    }

}

