package io.kurumi.ntt.maven;

public class MvnException extends Exception {

    public MvnException() {
    }

    public MvnException(String message) {

        super(message);

    }

    public MvnException(String message, Throwable cause) {

        super(message, cause);

    }

    public MvnException(Throwable cause) {

        super(cause);

    }


}
