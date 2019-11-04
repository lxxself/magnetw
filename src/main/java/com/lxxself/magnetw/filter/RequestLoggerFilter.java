package com.lxxself.magnetw.filter;

import com.lxxself.magnetw.handler.RequestLoggerHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * created 2019/5/5 15:21
 */
public class RequestLoggerFilter extends OncePerRequestFilter {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        if (!request.getRequestURI().endsWith("feedback")) {
            logger.info(RequestLoggerHandler.buildRequestString(request));
        }

        chain.doFilter(request, response);
    }


}
