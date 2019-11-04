package com.lxxself.magnetw.controller;

import com.lxxself.magnetw.config.ApplicationConfig;
import com.lxxself.magnetw.handler.RequestLoggerHandler;
import com.lxxself.magnetw.response.*;
import com.lxxself.magnetw.service.MagnetRuleService;
import com.lxxself.magnetw.service.MagnetService;
import com.lxxself.magnetw.service.PermissionService;
import com.lxxself.magnetw.service.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * created 2019/05/05 12:04
 */
@RestController
@RequestMapping("/api")
public class MagnetApiController {
    @Autowired
    ApplicationConfig config;

    @Autowired
    PermissionService permissionService;

    @Autowired
    MagnetRuleService ruleService;

    @Autowired
    MagnetService magnetService;

    @Autowired
    ReportService reportService;

    private Logger logger = LoggerFactory.getLogger(getClass());
    private Logger feedback = LoggerFactory.getLogger("feedback");


    /**
     * 获取源站列表
     *
     * @return
     */
    @ResponseBody
    @GetMapping(value = "/source")
    public BaseResponse<List<MagnetRule>> source() throws Exception {
        List<MagnetRule> sites = ruleService.getSites();
        return BaseResponse.success(sites, String.format("%d个规则加载成功", sites.size()));
    }

    /**
     * 搜索
     *
     * @param request
     * @param source  源站名称
     * @param keyword 关键词
     * @param sort    排序
     * @param page    页码
     * @return
     * @throws Exception
     */
    @ResponseBody
    @GetMapping(value = "/search")
    public BaseResponse<MagnetPageData> search(HttpServletRequest request, @RequestParam(required = false) String source, @RequestParam(required = false) String keyword,
                                               @RequestParam(required = false) String sort, @RequestParam(required = false) Integer page) throws Exception {
        //是否需要屏蔽关键词
        if (config.reportEnabled && reportService.containsKeyword(keyword)) {
            logger.info("搜索结果被屏蔽--->" + keyword);
            return BaseResponse.error("搜索结果被屏蔽");
        }

        String userAgent = request.getHeader(HttpHeaders.USER_AGENT);

        //默认参数
        MagnetPageOption pageOption = magnetService.transformCurrentOption(source, keyword, sort, page);
        MagnetRule rule = ruleService.getRuleBySite(pageOption.getSite());

        List<MagnetItem> infos = new ArrayList<MagnetItem>();
        infos.addAll(magnetService.parser(rule, pageOption.getKeyword(), pageOption.getSort(), pageOption.getPage(), userAgent));
        int dataCount = infos.size();

        //是否需要屏蔽结果
        String supplement = "";
        if (config.reportEnabled) {
            List<MagnetItem> filterItems = new ArrayList<MagnetItem>();
            for (MagnetItem item : infos) {
                if (reportService.containsUrl(item.getMagnet())) {
                    filterItems.add(item);
                }
            }
            infos.removeAll(filterItems);
            if (filterItems.size() > 0) {
                supplement = String.format("，其中%d个被屏蔽", filterItems.size());
            }
        }

        MagnetPageData data = new MagnetPageData();
        data.setRule(rule);
        data.setTrackersString(ruleService.getTrackersString());
        //如果过期了就重新异步缓存Tracker服务器列表
        if (ruleService.isTrackersExpired()) {
            ruleService.reloadTrackers();
        }
        data.setCurrent(pageOption);
        data.setResults(infos);

        if (config.preloadEnabled && dataCount > 0) {
            magnetService.asyncPreloadNextPage(rule, pageOption, userAgent);
        }
        return BaseResponse.success(data, String.format("搜索到%d条结果%s", dataCount, supplement));
    }


    /**
     * 详情
     *
     * @param request
     * @param source    源站名称
     * @param detailUrl 源站详情url
     * @return
     * @throws Exception
     */
    @ResponseBody
    @GetMapping(value = "/detail")
    public BaseResponse<MagnetItemDetail> search(HttpServletRequest request, @RequestParam String source, @RequestParam("url") String detailUrl) throws Exception {
        String userAgent = request.getHeader(HttpHeaders.USER_AGENT);

        MagnetRule rule = ruleService.getRuleBySite(source);
        MagnetItemDetail detail = magnetService.parserDetail(detailUrl, rule, userAgent);
        return BaseResponse.success(detail, null);
    }

    /**
     * 举报
     *
     * @param name  名称
     * @param value 搜索关键词或磁力链
     * @return
     */
    @ResponseBody
    @PostMapping(value = "/report")
    public BaseResponse<MagnetPageData> report(@RequestParam(required = false) String name, @RequestParam String value) {
        if (StringUtils.isEmpty(value)) {
            return BaseResponse.error("请输入关键词或磁力链");
        } else {
            try {
                reportService.put(name, value);
                return BaseResponse.success(null, "举报成功");
            } catch (Exception e) {
                return BaseResponse.error(e.getMessage());
            }

        }
    }

    @PostMapping(value = "/feedback")
    public BaseResponse<String> feedback(HttpServletRequest request, @RequestBody String json) throws Exception {
        StringBuffer sb=new StringBuffer();
        sb.append(RequestLoggerHandler.buildRequestString(request));
        sb.append("\n");
        sb.append(json);
        feedback.info(sb.toString());
        return BaseResponse.success(null, "已记录本次结果");
    }


}
