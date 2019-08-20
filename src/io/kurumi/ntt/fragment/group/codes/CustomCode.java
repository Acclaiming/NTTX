package io.kurumi.ntt.fragment.group.codes;

import io.kurumi.ntt.db.GroupData;
import java.util.ArrayList;
import java.util.List;

public class CustomCode extends VerifyCode {

	private String i_question;

	private List<String> validCode = new ArrayList<>();
	private List<String> invalidCode = new ArrayList<>();

	private String a_question;

	private List<String> custom_kw;

	public CustomCode(boolean input,GroupData data) {

		super(input);

		if (!input) {

			for (GroupData.CustomItem item : data.custom_items) {

				if (item.isValid) validCode.add(item.text);
				else invalidCode.add(item.text);

			}

		}

		this.custom_kw = data.custom_kw;

		this.i_question = data.custom_i_question;
		this.a_question = data.custom_a_question;

	}

	@Override
	public String question() {

		return input ? a_question : i_question;

	}

	@Override
	public String code() {

		return null;

	}

	@Override
	public String validCode() {

		return null;

	}

	String[] codes;

	@Override
	public String[] invalidCode() {

		String[] codes = new String[validCode.size() + invalidCode.size()];

		int index = 0;

		for (String code : validCode) {

			codes[index] = code;

			index++;

		}

		for (String code : invalidCode) {

			codes[index] = code;

			index++;

		}

		return codes;

	}

	@Override
	public boolean verify(String text) {

		if (input) {

			if (custom_kw == null) return true;

			for (String kw : custom_kw) {

				if (text.contains(kw)) return true;

			}

			return false;

		} else {

			return validCode.contains(text);

		}

	}

	@Override
	public VerifyCode fork() {

		return this;

	}


}

