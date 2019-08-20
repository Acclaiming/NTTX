package io.kurumi.ntt.fragment.group.codes;

import cn.hutool.captcha.generator.RandomGenerator;

public class StringCode extends VerifyCode {

	public StringCode(boolean input) {
		super(input);
	}

	RandomGenerator gen = new RandomGenerator("喵呜",5);

	String code = gen.generate();

	@Override
	public String question() {

		return "请" + (input ? "发送" : "选择") + " 验证码以通过验证 ~";

	}

	@Override
	public VerifyCode fork() {

		return new StringCode(input);

	}

	@Override
	public String code() {

		return code;

	}

	@Override
	public String validCode() {

		return code;

	}


	@Override
	public String[] invalidCode() {

		return new String[]{

			gen.generate(),
			gen.generate(),
			gen.generate(),
			gen.generate()

		};

	}

	@Override
	public boolean verify(String input) {

		return code.equals(input.trim().replace("國","国"));

	}

}

