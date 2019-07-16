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

public class Idcard extends Fragment {

		@Override
		public void init(BotFragment origin) {

				super.init(origin);

				registerFunction("ic_check","ic_18","ic_gen","ic_rand");

		}

		final static Map<Integer, String> zoneNum = new HashMap<Integer, String>();

    static {

        zoneNum.put(11,"北京");
        zoneNum.put(12,"天津");
        zoneNum.put(13,"河北");
        zoneNum.put(14,"山西");
        zoneNum.put(15,"内蒙古");
        zoneNum.put(21,"辽宁");
        zoneNum.put(22,"吉林");
        zoneNum.put(23,"黑龙江");
        zoneNum.put(31,"上海");
        zoneNum.put(32,"江苏");
        zoneNum.put(33,"浙江");
        zoneNum.put(34,"安徽");
        zoneNum.put(35,"福建");
        zoneNum.put(36,"江西");
        zoneNum.put(37,"山东");
        zoneNum.put(41,"河南");
        zoneNum.put(42,"湖北");
        zoneNum.put(43,"湖南");
        zoneNum.put(44,"广东");
        zoneNum.put(45,"广西");
        zoneNum.put(46,"海南");
        zoneNum.put(50,"重庆");
        zoneNum.put(51,"四川");
        zoneNum.put(52,"贵州");
        zoneNum.put(53,"云南");
        zoneNum.put(54,"西藏");
        zoneNum.put(61,"陕西");
        zoneNum.put(62,"甘肃");
        zoneNum.put(63,"青海");
        zoneNum.put(64,"新疆");
        zoneNum.put(71,"台湾");
        zoneNum.put(81,"香港");
        zoneNum.put(82,"澳门");
        zoneNum.put(91,"外国");

    }

    final static char[] PARITYBIT = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};
    final static int[] POWER_LIST = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};

    private static int getIdcardCalendar() {

        GregorianCalendar curDay = new GregorianCalendar();

        int curYear = curDay.get(Calendar.YEAR);

        int year2bit = Integer.parseInt(String.valueOf(curYear).substring(2));

        return year2bit;

    }

		public Map<String,AreaCode> codeMap = new HashMap<>();
		public Map<String,String> provinceMap = new HashMap<>();
		public Map<String,HashMap<String,String>> cityMap = new HashMap<>();
		public Map<String,HashMap<String,String>> detailMap = new HashMap<>();



		void loadData() {

				String json = FileUtil.readUtf8String("./res/areacode.json");

				AreaCode[] areaCodeArr = new Gson().fromJson(json,AreaCode[].class);

				codeMap = new HashMap<>();

				for (AreaCode code : areaCodeArr) {

						codeMap.put(code.getAreaCode(),code);

						String province = code.getAreaCode().substring(0,2);

						provinceMap.put(province,code.getProvince());

						HashMap<String,String> cMap = cityMap.get(province);

						if (cMap == null) {

								cMap = new HashMap<>();

								cityMap.put(province,cMap);

						}

						String city = code.getAreaCode().substring(2,4);

						cMap.put(city,code.getCity());

						HashMap<String,String> dMap = detailMap.get(city);

						if (dMap == null) {

								dMap = new HashMap<>();

								detailMap.put(city,dMap);

						}

						String detail = code.getAreaCode().substring(4,6);

						dMap.put(detail,code.getDetail());

				}

		}

		@Override
		public void onFunction(UserData user,Msg msg,String function,String[] params) {

				if ("ic_check".equals(function)) {

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

						if (!codeMap.containsKey(certNo.substring(0,6))) {

								String province = certNo.substring(0,2);

								if (!provinceMap.containsKey(province)) {

										msg.send("不存在的省/直辖市码 : " + province + " 对照 : \n",MapUtil.join(provinceStrMap,"\n"," : ")).exec();

										return;

								}
								
								String pStr = provinceMap.get(province);

								String city = certNo.substring(2,4);

								HashMap<String, String> cityList = cityMap.get(province);

								if (!cityList.containsKey(city)) {

										msg.send(pStr + " 不存在的市 : " + city + " 对照 : \n",MapUtil.join(cityList,"\n"," : ")).exec();

										return;

								}
								
								String cStr = cityList.get(province);

								String detail = certNo.substring(4,6);

								HashMap<String, String> detailList = detailMap.get(city);

								if (!detailList.containsKey(detail)) {

										msg.send(pStr + " " + cStr + " 不存在的地址 : " + detail + " 对照 : \n",MapUtil.join(detailList,"\n"," : ")).exec();

										return;

								}
								

						}

						//校验年份

						String year = certNo.length() == 15 ? getIdcardCalendar() + certNo.substring(6,8) : certNo.substring(6,10);

						final int iyear = Integer.parseInt(year);

						if (iyear < 1900 || iyear > Calendar.getInstance().get(Calendar.YEAR)) {

								msg.send("无效的年份 : " + iyear).exec();

								return;

						}

						//校验月份
						String month = certNo.length() == 15 ? certNo.substring(8,10) : certNo.substring(10,12);

						final int imonth = Integer.parseInt(month);

						if (imonth < 1 || imonth > 12) {

								msg.send("无效的月份 : " + imonth).exec();

								return;

						}

						//校验天数

						String day = certNo.length() == 15 ? certNo.substring(10,12) : certNo.substring(12,14);

						final int iday = Integer.parseInt(day);

						if (iday < 1 || iday > 31) {

								msg.send("无效的天数 : " + iday).exec();

						}

						//校验"校验码"

						char vCode = cs[cs.length - 1];

						char valid = PARITYBIT[power % 11];

						if (vCode != valid) {

								msg.send("无效的检验位 : " +  vCode + " 应为 : " + Html.code(valid)).html().exec();

								return;

						}

						msg.send("检验完成 身份证有效 :)").exec();

				}

		}

}
