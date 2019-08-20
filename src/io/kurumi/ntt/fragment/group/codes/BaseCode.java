package io.kurumi.ntt.fragment.group.codes;

import cn.hutool.core.util.RandomUtil;

public class BaseCode extends VerifyCode {

	public BaseCode(boolean input) {
		
		super(input);
		
	}

	boolean code = RandomUtil.randomBoolean();

	@Override
	public VerifyCode fork() {

		return new BaseCode(input);

	}

	@Override
	public String question() {

		return "请" + (input ? "发送" : "选择") + " " + (code ? "喵" : "嘤") + " 以通过验证 ~";

	}

	@Override
	public String code() {

		return null;

	}

	@Override
	public String validCode() {

		return code ? "喵" : "嘤";

	}

	@Override
	public String[] invalidCode() {

		return new String[]{code ? "嘤" : "喵"};

	}

	@Override
	public boolean verify(String input) {

		return code ? input.contains("喵") : (input.contains("嘤") || input.contains("嚶"));

	}

}
