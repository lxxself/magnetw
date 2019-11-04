package com.lxxself.magnetw.exception;

/**
 * 防止空数据被缓存
 * created 2019/09/30 11:21:47
 */
public class EmptyListException extends Exception implements IgnoreCauseLogger{
    public EmptyListException() {
        this(null);
    }

    public EmptyListException(Throwable cause) {
        super("什么也没搜到", cause);
    }
}
