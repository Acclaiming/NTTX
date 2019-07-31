package io.kurumi.ntt.fragment.idcard;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.map.MapUtil;
import com.google.gson.Gson;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.utils.Html;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

import io.kurumi.ntt.model.request.ButtonMarkup;
import io.kurumi.ntt.model.request.ButtonLine;
import io.kurumi.ntt.model.request.Keyboard;
import com.pengrad.telegrambot.model.request.KeyboardButton;
import io.kurumi.ntt.model.request.KeyboradButtonLine;
import io.kurumi.ntt.db.PointData;

import java.util.Set;

import cn.hutool.core.util.StrUtil;

import java.util.List;
import java.util.ArrayList;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.ArrayUtil;

public class Idcard extends Fragment {

    final String POINT_IC_GEN = "ic_gen";

    @Override
    public void init(BotFragment origin) {

        super.init(origin);

        registerFunction("ic_check", "ic_18", "ic_gen", "ic_rand");

        registerPoint(POINT_IC_GEN);

    }

    final static char[] PARITYBIT = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};
    final static int[] POWER_LIST = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};


    public Map<String, AreaCode> codeMap = new HashMap<>();
    public Map<String, String> provinceMap = new HashMap<>();
    public Map<String, HashMap<String, String>> cityMap = new HashMap<>();
    public Map<String, HashMap<String, String>> detailMap = new HashMap<>();

    void loadData() {

        String json = FileUtil.readUtf8String("../res/areacode.json");

        AreaCode[] areaCodeArr = new Gson().fromJson(json, AreaCode[].class);

        codeMap = new HashMap<>();

        for (AreaCode code : areaCodeArr) {

            codeMap.put(code.getAreaCode(), code);

            String province = code.getAreaCode().substring(0, 2);

            provinceMap.put(province, code.getProvince());

            HashMap<String, String> cMap = cityMap.get(province);

            if (cMap == null) {

                cMap = new HashMap<>();

                cityMap.put(province, cMap);

            }

            String city = code.getAreaCode().substring(2, 4);

            cMap.put(city, code.getCity());

            HashMap<String, String> dMap = detailMap.get(city);

            if (dMap == null) {

                dMap = new HashMap<>();

                detailMap.put(city, dMap);

            }

            String detail = code.getAreaCode().substring(4, 6);

            dMap.put(detail, code.getDetail());

        }

    }

    class ICGen extends PointData {

        int type = 0;

        List<AreaCode> code;
        Map<String, AreaCode> detailMap;


    }

    @Override
    public void onPoint(UserData user, Msg msg, String point, PointData data) {

        ICGen gen = (ICGen) data.with(msg);

        if (gen.type == 0) {

            if (!provinceMap.containsValue(msg.text())) {

                msg.send("没有这个省 / 直辖市").withCancel().exec(data);

                return;

            }

            gen.code = new ArrayList<>();

            Set<String> districtList = new HashSet<>();

            AreaCode code = null;

            for (AreaCode areaCode : codeMap.values()) {

                if (msg.text().contains(areaCode.getProvince()) || areaCode.getProvince().contains(msg.text())) {

                    code = areaCode;

                    if (!StrUtil.isBlank(areaCode.getCity())) {

                        gen.code.add(areaCode);

                        districtList.add(areaCode.getCity());

                    }

                }

            }

            if (districtList.isEmpty()) {

                clearPrivatePoint(user);

                String[] ics = new String[10];

                for (int index = 0; index < ics.length; index++) {

                    ics[index] = Html.code(idCardGen(code));

                }

                msg.send("生成完成 : " + Html.code(code.getFull()) + "\n", ArrayUtil.join(ics, "\n")).html().async();

                return;

            }

            Keyboard buttons = new Keyboard();

            KeyboradButtonLine line = buttons.newButtonLine();

            int size = 0;

            for (String city : districtList) {

                size++;

                line.newButton(city);

                if (size == 3) {

                    line = buttons.newButtonLine();

                    size = 0;

                }

            }

            gen.type = 1;

            msg.send("请选择市").keyboard(buttons).exec(data);

        } else if (gen.type == 1) {


            Map<String, AreaCode> code = new HashMap<>();

            Set<String> districtList = new HashSet<>();

            for (AreaCode areaCode : gen.code) {

                if (msg.text().contains(areaCode.getCity()) || areaCode.getCity().contains(msg.text())) {

                    if (!StrUtil.isBlank(areaCode.getDetail())) {

                        String detail = areaCode.getDetail();

                        if (detail.startsWith(areaCode.getProvince())) {

                            detail = detail.substring(areaCode.getProvince().length());

                        }

                        if (detail.startsWith(areaCode.getCity())) {

                            detail = detail.substring(areaCode.getCity().length());

                        }

                        code.put(detail, areaCode);

                        districtList.add(detail);

                    }

                }

            }

            if (districtList.isEmpty()) {

                msg.send("没有这个市").withCancel().exec();

                return;

            }

            Keyboard buttons = new Keyboard();

            KeyboradButtonLine line = buttons.newButtonLine();

            int size = 0;

            for (String detail : districtList) {

                size++;

                line.newButton(detail);

                if (size == 2) {

                    line = buttons.newButtonLine();

                    size = 0;

                }

            }


            gen.detailMap = code;

            gen.type = 2;

            msg.send("请选择地区").keyboard(buttons).exec(data);


        } else if (gen.type == 2) {

            if (!gen.detailMap.containsKey(msg.text())) {

                msg.send("没有这个地区").withCancel().exec(data);

                return;

            }

            AreaCode code = gen.detailMap.get(msg.text());

            clearPrivatePoint(user);

            String[] ics = new String[10];

            for (int index = 0; index < ics.length; index++) {

                ics[index] = Html.code(idCardGen(code));

            }

            msg.send("生成完成 : " + Html.code(code.getFull()) + "\n", ArrayUtil.join(ics, "\n")).html().exec();

        }


    }

    private String idCardGen(AreaCode code) {

        String ic17 = code.getAreaCode() + RandomUtil.randomInt(1969, Calendar.getInstance().get(Calendar.YEAR) - 3);

        int month = RandomUtil.randomInt(1, 13);

        if (month < 10) ic17 = ic17 + "0";

        ic17 = ic17 + month;

        int day = RandomUtil.randomInt(1, 29);

        if (day < 10) ic17 = ic17 + "0";

        ic17 = ic17 + day + String.valueOf((int) (Math.random() * 900 + 100));

        return ic17 + getParityBit(ic17);

    }

    private char getParityBit(String cardCode17) {

        final char[] cs = cardCode17.toUpperCase().toCharArray();

        int power = 0;

        for (int i = 0; i < cs.length; i++) {

            power += (cs[i] - '0') * POWER_LIST[i];

        }

        char keyChar = PARITYBIT[power % 11];

        return keyChar;
    }


    @Override
    public void onFunction(UserData user, Msg msg, String function, String[] params) {

        if (user.blocked()) {

            msg.send("你不能这么做 (为什么？)").async();

            return;

        }

        if (codeMap.isEmpty()) loadData();

        if ("ic_gen".equals(function)) {

            Keyboard buttons = new Keyboard();

            KeyboradButtonLine line = buttons.newButtonLine();

            int size = 0;

            for (String province : provinceMap.values()) {

                size++;

                line.newButton(province);

                if (size == 3) {

                    line = buttons.newButtonLine();

                    size = 0;

                }

            }

            ICGen data = new ICGen();

            setPrivatePoint(user, POINT_IC_GEN, data);

            msg.send("请选择省/直辖市").keyboard(buttons).exec(data);

        } else if ("ic_18".equals(function)) {

            String certNo = params.length == 0 ? null : params[0];

            if (certNo == null || certNo.length() != 17) {

                msg.send("请输入前 17 位 身份证").exec();

                return;

            }


            char valid = getParityBit(certNo);

            msg.send("第十八位为 : " + valid + " , 身份证为 " + Html.code(certNo + valid)).html().exec();

        } else if ("ic_check".equals(function)) {

            String certNo = params.length == 0 ? null : params[0];

            if (certNo == null || (certNo.length() != 15 && certNo.length() != 18)) {

                msg.invalidParams("十五或十八位身份证号").exec();

                return;

            }

            final char[] cs = certNo.toUpperCase().toCharArray();

            //校验位数

            int power = 0;

            for (int i = 0; i < cs.length; i++) {

                if (i == cs.length - 1 && cs[i] == 'X') {

                    break;//最后一位可以 是X或x

                }

                if (cs[i] < '0' || cs[i] > '9') {

                    msg.send("无效的数字 : 非最后一位不能为数字以外字符").exec();

                    return;

                }

                if (i < cs.length - 1) {

                    power += (cs[i] - '0') * POWER_LIST[i];

                }

            }

            // 校验区位码

            if (!codeMap.containsKey(certNo.substring(0, 6))) {

                String province = certNo.substring(0, 2);

                if (!provinceMap.containsKey(province)) {

                    msg.send("不存在的省/直辖市码 : " + province + " 对照 : \n", MapUtil.join(provinceMap, "\n", " : ")).exec();

                    return;

                }

                String pStr = provinceMap.get(province);

                String city = certNo.substring(2, 4);

                HashMap<String, String> cityList = cityMap.get(province);

                if (!cityList.containsKey(city)) {

                    msg.send(pStr + " 不存在的市 : " + city + " 对照 : \n", MapUtil.join(cityList, "\n", " : ")).exec();

                    return;

                }

                String cStr = cityList.get(province);

                String detail = certNo.substring(4, 6);

                HashMap<String, String> detailList = detailMap.get(city);

                if (!detailList.containsKey(detail)) {

                    msg.send(pStr + " " + cStr + " 不存在的地址 : " + detail + " 对照 : \n", MapUtil.join(detailList, "\n", " : ")).exec();

                    return;

                }


            }

            //校验年份


            GregorianCalendar curDay = new GregorianCalendar();

            int curYear = curDay.get(Calendar.YEAR);

            int year2bit = Integer.parseInt(String.valueOf(curYear).substring(2));

            String year = certNo.length() == 15 ? year2bit + certNo.substring(6, 8) : certNo.substring(6, 10);

            final int iyear = Integer.parseInt(year);

            if (iyear < 1900 || iyear > Calendar.getInstance().get(Calendar.YEAR)) {

                msg.send("无效的年份 : " + iyear).exec();

                return;

            }

            //校验月份
            String month = certNo.length() == 15 ? certNo.substring(8, 10) : certNo.substring(10, 12);

            final int imonth = Integer.parseInt(month);

            if (imonth < 1 || imonth > 12) {

                msg.send("无效的月份 : " + imonth).exec();

                return;

            }

            //校验天数

            String day = certNo.length() == 15 ? certNo.substring(10, 12) : certNo.substring(12, 14);

            final int iday = Integer.parseInt(day);

            if (iday < 1 || iday > 31) {

                msg.send("无效的天数 : " + iday).exec();

            }

            //校验"校验码"

            char vCode = cs[cs.length - 1];

            char valid = PARITYBIT[power % 11];

            if (vCode != valid) {

                msg.send("无效的检验位 : " + vCode + " 应为 : " + Html.code(valid)).html().exec();

                return;

            }

            msg.send("检验完成 身份证有效 :)").exec();

        }

    }

}
