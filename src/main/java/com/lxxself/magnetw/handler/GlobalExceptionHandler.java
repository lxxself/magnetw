package com.lxxself.magnetw.handler;

import com.lxxself.magnetw.exception.IgnoreCauseLogger;
import com.lxxself.magnetw.exception.MagnetParserException;
import com.lxxself.magnetw.response.BaseResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jsoup.HttpStatusException;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * 全局异常处理
 * created 2019/5/5 13:35
 */
@ControllerAdvice
@ResponseBody
public class GlobalExceptionHandler {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @ExceptionHandler(Throwable.class)
    public BaseResponse handleException(Throwable e) {
        logger.error(e.getMessage(), e instanceof IgnoreCauseLogger ? null : e);

        if (e instanceof MagnetParserException) {
            e = e.getCause();
        }
        if (e instanceof HttpStatusException) {
            return BaseResponse.error(String.format("%s,%d", e.getMessage(), ((HttpStatusException) e).getStatusCode()));
        } else if (e instanceof SocketTimeoutException || e instanceof UnknownHostException) {
            return BaseResponse.error("请求源站超时");
        } else if (e instanceof ConnectException) {
            return BaseResponse.error("访问源站失败");
        } else if (e instanceof MissingServletRequestParameterException) {
            return BaseResponse.error("缺少参数");
        }
        if (StringUtils.isEmpty(e.getMessage())) {
            return BaseResponse.error("未知异常");
        } else {
            return BaseResponse.error(e.getMessage());
        }
    }
}
