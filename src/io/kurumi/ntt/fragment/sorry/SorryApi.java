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

public class SorryApi {

	public static String origin = "https://sorry.xuty.tk/api/";

	private String template;
	public String[] hint;

	public static HashMap<String,SorryApi> templates; static {
		
		templates = new HashMap<>();
		
		templates.put("为所欲为",new SorryApi("sorry","就算你是一流工程师","就算你出报告再完美","我叫你改报告你就要改","毕竟我是客户","客户了不起啊","sorry 客户真的了不起","以后叫他天天改报告","天天改 天天改"));
		templates.put("王境泽",new SorryApi("wangjingze","我就是饿死","死外边 从这跳下去","也不会吃你们一点东西","真香"));
		templates.put("学习防身术",new SorryApi("fangshen","我将教你如何应对歹徒","集中注意","希望你有所收获"));
		templates.put("星际还是魔兽",new SorryApi("dati","平时你打电子游戏吗", "偶尔", "星际还是魔兽", "赛尔号"));
		templates.put("今天星期五",new SorryApi("friday","今天星期五了", "我的天", "明天不上班", "熬夜到天亮", "再睡上一整天", "周五万岁"));
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
		
	}
	
	public SorryApi(String template,String... hint) {
	
		this.template = template;
		this.hint = hint;
		
	}
	
	public File make(String[] lines) {
		
		return make(template,lines);
		
	}
	
	private static File make(String template,String[] lines) {

		JSONObject array = new JSONObject();

		for (Integer index = 0;index < lines.length;index ++) {

			array.put(index.toString(),lines[index]);

		}

		HttpResponse result;

		try {

			result = HttpUtil.createPost(origin + template + "/make").body(array).execute();

		} catch (HttpException exc) {

			return null;

		}

		if (!result.isOk()) return null;

		File cache = new File(Env.CACHE_DIR,"sorry_make/" + UUID.fastUUID().toString(true) + ".gif");

		HttpUtil.downloadFile(result.body(),cache);

		return cache;

	}

}
