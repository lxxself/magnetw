package com.lxxself.magnetw.handler;

import org.springframework.http.HttpMethod;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;

/**
 * created 2019/9/30 11:59
 */
public class RequestLoggerHandler {
    public static String buildRequestString(HttpServletRequest request) {
        try {
            StringBuffer sb = new StringBuffer();
            sb.append(request.getRequestURL());
            if (HttpMethod.GET.name().equals(request.getMethod())) {
                if (!StringUtils.isEmpty(request.getQueryString())) {
                    sb.append("?" + URLDecoder.decode(request.getQueryString()));
                }
            } else {
                sb.append("\n[Request Params]\n");
                Set<Map.Entry<String, String[]>> entrySet = request.getParameterMap().entrySet();
                for (Map.Entry<String, String[]> entry : entrySet) {
                    String[] values = entry.getValue();
                    for (String value : values) {
                        sb.append(entry.getKey());
                        sb.append(":");
                        try {
                            sb.append(URLDecoder.decode(value));
                        } catch (IllegalArgumentException e) {
                            sb.append(value);
                        }
                        sb.append("\n");
                    }
                }
            }
            sb.append("\n[Request Headers]\n");
            Enumeration<String> names = request.getHeaderNames();
            while (names.hasMoreElements()) {
                String header = names.nextElement();
                sb.append(header);
                sb.append(":");
                sb.append(request.getHeader(header));
                sb.append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            return e.getMessage();
        }
    }
}
