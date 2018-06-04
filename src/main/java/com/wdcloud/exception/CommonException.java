package com.wdcloud.exception;

/**
 * Created by bigd on 2017/7/21.
 */
public class CommonException extends  RuntimeException {

    private static final long serialVersionUID = 1L;

    public CommonException() {
        super();
    }

    public CommonException(String message) {
        super(message);
    }

    public CommonException(String message, Throwable cause) {
        super(message,cause);
    }

}
