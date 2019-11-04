package com.lxxself.magnetw.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * 爬虫过滤器
 * 除非你明白屏蔽搜索引擎的用意，否则不要暴露给搜索引擎
 */
public class SpiderFilter extends OncePerRequestFilter {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String requestURL = request.getRequestURL().toString();
        String requestURI = request.getRequestURI();
        if (requestURI.contains("resources")) {
            //忽略resources
        } else if (requestURI.endsWith("robots.txt")) {
            //如果是访问robots.txt 过滤掉这些ua
            String userAgent = request.getHeader(HttpHeaders.USER_AGENT);
            String regex = "Mozilla/5.0 \\(compatible; pycurl\\)|Mozilla/5.0 \\(Windows NT 6.1; rv:20.0\\) Gecko/20100101 Firefox/20.0";
            if (Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(userAgent).find()) {
                logger.info(String.format("访问robots.txt被拒绝--->%s\n%s", requestURL, userAgent));
                response.setStatus(HttpStatus.FORBIDDEN.value());
                return;
            }
        } else {
            String userAgent = request.getHeader(HttpHeaders.USER_AGENT);
            String regex = "googlebot|mediapartners-google|adsbot-google|baiduspider|360spider|haosouspider|sosospider|sogou spider|sogou news spider|sogou web spider|sogou inst spider|sogou spider2|sogou blog|sogou orion spider|yodaobot|youdaobot|bingbot|slurp|teoma|ia_archiver|twiceler|msnbot|scrubby|robozilla|gigabot|yahoo-mmcrawler|yahoo-blogs|yahoo! slurp china|yahoo!-adcrawler|psbot|yisouspider|easouspider|jikespider|etaospider";
            if (Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(userAgent).find()) {
                logger.info(String.format("爬虫引擎被拒绝--->%s\n%s", requestURL, userAgent));
                response.setStatus(HttpStatus.NOT_FOUND.value());
                return;
            }
        }

        chain.doFilter(request, response);
    }
}
