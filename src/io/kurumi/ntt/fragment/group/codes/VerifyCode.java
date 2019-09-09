package io.kurumi.ntt.fragment.group.codes;

public abstract class VerifyCode {

    public final boolean input;

    public VerifyCode(boolean input) {

        this.input = input;

    }

    public abstract String question();

    public abstract String code();

    public abstract String validCode();

    public abstract String[] invalidCode();

    public abstract boolean verify(String input);

    public abstract VerifyCode fork();

}

