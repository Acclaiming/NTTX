package io.kurumi.ntt.fragment.sorry;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import java.io.File;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpException;
import io.kurumi.ntt.Env;
import cn.hutool.core.lang.UUID;
import java.util.LinkedList;
import java.util.HashMap;
import cn.hutool.core.util.StrUtil;
import java.util.LinkedHashMap;
import cn.hutool.extra.template.TemplateEngine;
import io.kurumi.ntt.utils.FFMpeg;
import cn.hutool.core.io.FileUtil;

public class SorryApi {

	public static String origin = "https://sorry.xuty.tk";

	private String template;
	public String[] hint;

	public static LinkedHashMap<String,SorryApi> templates; static {

		templates = new LinkedHashMap<>();

		templates.put("盘他",new SorryApi("panta","一看这龙就来气","干干巴巴的","麻麻赖赖的","一点都不圆润","盘他"));
		templates.put("工作细胞",new SorryApi("hataraku","那个呢 那个呢","因为出了些状况","施工进度推迟了"));
		templates.put("元首",new SorryApi("fuhrer","我们装备再好 总会死在外挂手里","没关系 我们可以苦练枪法","这外挂...","这外挂有锁血的 根本打不死","。。。"));
		
		templates.put("为所欲为",new SorryApi("sorry","就算你是一流工程师","就算你出报告再完美","我叫你改报告你就要改","毕竟我是客户","客户了不起啊","sorry 客户真的了不起","以后叫他天天改报告","天天改 天天改"));
		templates.put("王境泽",new SorryApi("wangjingze","我就是饿死","死外边 从这跳下去","也不会吃你们一点东西","真香"));
		templates.put("学习防身术",new SorryApi("fangshen","我将教你如何应对歹徒","集中注意","希望你有所收获"));
		templates.put("星际还是魔兽",new SorryApi("dati","平时你打电子游戏吗","偶尔","星际还是魔兽","赛尔号"));
		templates.put("在座的各位都是垃圾",new SorryApi("dashixiong","问得好，如果各位有兴趣的话", "可以加入我们空手道部门", "不过要经过选拔", "因为我只会训练精英", "绝对不会接收垃圾", "看我干嘛？你把我当垃圾？", "不是...不要误会，我不是针对你", "我是说在座的各位都是垃圾"));
		templates.put("pop子和pipi美的日常",new SorryApi("popteamepic","嘿嘿！", "丢雷楼谋", "大力D", "嘿嘿！！", "丢埋雷楼豆", "再大力D", "唔好丢我啊"));
		templates.put("今天星期五",new SorryApi("friday","今天星期五了","我的天","明天不上班","熬夜到天亮","再睡上一整天","周五万岁"));
		templates.put("谁反对",new SorryApi("mini-disagree","我话说完了","谁赞成","谁反对"));
		templates.put("报告梁非凡",new SorryApi("liangfeifan","报告非凡哥，不必去操场","我在这里说也行，也是六个字","吃屎吧，梁非凡"));
		templates.put("黑人问号",new SorryApi("nick","如果这小子肯认真练球 他以后肯定不得了","但他当时是个中二屁孩"));
		templates.put("耶稣也保不住他",new SorryApi("jesus","段坤我吃定了","耶稣也留不住他","我说的"));
		templates.put("御坂美琴",new SorryApi("meiqin","节操这东西要来干吗","丢了就丢了吧","反正大家都喜欢没节操的帖子"));
		templates.put("伊莉雅",new SorryApi("iriya","肥宅快乐水","您配吗","您~不~配~","我可去你的吧"));
		templates.put("老罗",new SorryApi("laoluo","我王境泽，就算是饿死","死外边 从这里跳下去","也不会吃你们一点东西","对不起 用错模板了"));
		templates.put("记仇",new SorryApi("jichou","2018年六月七号","今天是全国高考","希望所有考生都能发挥出色","金榜题名，鱼跃龙门"));
		templates.put("年轻气盛",new SorryApi("huaqiang","大哥我送你一句话","年轻人不要太气盛","不气盛叫年轻人吗"));
		templates.put("假面骑士",new SorryApi("yanji","一直想看你这幅表情","这幅嫉妒我的表情"));
		templates.put("乌鸦",new SorryApi("wuya","哟 王境泽","今天吃饭没","看看这就是王境泽"));
		templates.put("成为你讨厌的人",new SorryApi("yibaiwan","有钱了不起呀","我出一百万"));
		templates.put("面筋哥",new SorryApi("mianjinge","你知道这五年我怎么过的吗","天天在推特黑屁","爽到.....","窒息"));
		templates.put("天打五雷轰",new SorryApi("diaosi","谁看你洗澡","谁就不得好死","天打五雷轰"));
		templates.put("金坷垃",new SorryApi("jinkela","金坷垃好处都有啥","谁说对了就给他","肥料掺了金坷垃","不流失 不蒸发 零浪费","肥料掺了金坷垃","能吸收两米下的氮磷钾"));
		templates.put("土拨鼠",new SorryApi("marmot","喵","喵喵喵喵喵"));
		templates.put("窃格瓦拉",new SorryApi("dagong","没有钱啊 肯定要做的啊","不做的话没有钱用","那你不会去援交","有手有脚的","援交是不可能的","这辈子不可能援交的"));
		templates.put("偷电动车",new SorryApi("diandongche","戴帽子的首先进里边去","开始拿剪刀出来 拿那个手机","手机上有电筒 用手机照射","寻找那个比较新的电动车","六月六号 两名男子再次出现","民警立即将两人抓获"));
		templates.put("如此粗鄙之语",new SorryApi("kongming","没想到","你竟说出如此粗鄙之语"));
		templates.put("愤怒的老板",new SorryApi("hyundai","[BMW]","[Lexus]","[Lexus]","[BMW]","[BMW]","[Lexus]","Hyundai！","Hyundai！","Hyundai！！","Hyundai！！！","Hyundai！！！！","Hyundai！！！！！"));
		templates.put("压力大爷",new SorryApi("daye","现在这法律啊，尽扯淡","孩子他不经常回来看我违什么法","不经常女装那才是违法"));
		templates.put("郑伊健",new SorryApi("zhengyijian","昨天听到有人说我女装漂亮","吓得我立马去照镜子","嘿，你还别说","真有点漂亮"));
		templates.put("小男孩",new SorryApi("xiaonanhai","找到男朋友了吗","快了","真的吗"));
		templates.put("白眼姐",new SorryApi("baiyanjie","我想问下在做的记者","你们拿着好几万的机器","扛着几千块的灯","又是找角度 又是挑构图","结果到了现场","发现没我跑得快"));
		templates.put("霸王",new SorryApi("bawang","很多领导人问我","怎么才可以连任","Duang","我推荐这款洗发水","质量很好，秒改宪法","以上我全是瞎扯"));

	}

	public SorryApi(String template,String... hint) {

		this.template = template;
		this.hint = hint;

	}

	public File make(String[] lines) {

		return make(template,lines);

	}

	private static File make(String template,String[] lines) {

		File templateDir = FileUtil.file("/usr/local/ntt/res/templates",template);
		File mp4File = FileUtil.file(templateDir,"template.mp4");
		File cache = FileUtil.file(Env.CACHE_DIR,"sorry_make/" + UUID.fastUUID().toString(true) + ".gif");
		
		System.out.print(templateDir.getPath());
		System.out.print(" : ");
		System.out.println(templateDir.isDirectory());
		System.out.println(FileUtil.file("../res/test.json").isDirectory());
		
		if (mp4File.isFile()) {

			File assFile = FileUtil.file(Env.CACHE_DIR,"ass_cache/" + UUID.fastUUID().toString(true) + ".ass");
			
			String assSource = FileUtil.readUtf8String(new File(templateDir,"template.ass"));
			
			for (int index = 0;;index ++) {
				
				if (!assSource.contains("<?=[" + index + "]=?>")) {
					
					break;
					
				}
				
				assSource = assSource.replace("<?=[" + index + "]=?>",lines.length > index ? lines[index] : "");
				
			}
			
			FileUtil.writeUtf8String(assSource,assFile);
			
			File pic = FFMpeg.getGifPalettePic(mp4File);

			FFMpeg.makeGif(pic,mp4File,assFile,cache);
			
			FileUtil.del(assFile);
			
			return cache;
			
		} else {

			JSONObject array = new JSONObject();

			for (Integer index = 0;index < lines.length;index ++) {

				array.put(index.toString(),lines[index]);

			}

			HttpResponse result;

			try {

				result = HttpUtil.createPost(origin + "/api/" + template + "/make").body(array).execute();

			} catch (HttpException exc) {

				return null;

			}

			if (!result.isOk()) return null;

			HttpUtil.downloadFile(origin + result.body(),cache);

			return cache;

		}

	}

}
