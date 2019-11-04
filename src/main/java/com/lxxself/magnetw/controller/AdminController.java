package com.lxxself.magnetw.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.lxxself.magnetw.config.ApplicationConfig;
import com.lxxself.magnetw.handler.PermissionHandler;
import com.lxxself.magnetw.response.BaseResponse;
import com.lxxself.magnetw.response.MagnetPageConfig;
import com.lxxself.magnetw.response.ReportData;
import com.lxxself.magnetw.response.ReportItem;
import com.lxxself.magnetw.service.MagnetRuleService;
import com.lxxself.magnetw.service.MagnetService;
import com.lxxself.magnetw.service.PermissionService;
import com.lxxself.magnetw.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Field;
import java.util.List;

/**
 * created 2019/5/24 17:13
 */
@Controller
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    PermissionService permissionService;

    @Autowired
    MagnetRuleService ruleService;

    @Autowired
    MagnetService magnetService;

    @Autowired
    ReportService reportService;

    @Autowired
    ApplicationConfig config;

    Gson gson = new Gson();

    @GetMapping
    public String index(HttpServletResponse response, Model model, @RequestParam(value = "p") String password) throws Exception {
        BaseResponse permission = permissionService.runAsPermission(password, null, null);
        if (permission.isSuccess()) {
            model.addAttribute("config", gson.toJson(new MagnetPageConfig(config)));
            return "/admin";
        } else {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType("text/html;charset=utf-8");
            response.getWriter().write(permission.getMessage());
            return null;
        }
    }

    @ResponseBody
    @RequestMapping("/config")
    public BaseResponse config(@RequestParam(value = "p") String password) throws Exception {
        return permissionService.runAsPermission(password, null, new PermissionHandler() {
            @Override
            public Object onPermissionGranted() throws Exception {
                JsonObject newConfig = new JsonObject();
                Field[] fields = ApplicationConfig.class.getFields();
                for (Field field : fields) {
                    Object value = ApplicationConfig.class.getField(field.getName()).get(config);
                    newConfig.addProperty(field.getName(), value.toString());
                }
                return newConfig;
            }
        });
    }

    /**
     * 重载配置
     *
     * @return
     * @throws Exception
     */
    @ResponseBody
    @GetMapping("/reload")
    public BaseResponse reload(@RequestParam(value = "p") String password) throws Exception {
        return permissionService.runAsPermission(password, "规则重载成功", new PermissionHandler<Void>() {
            @Override
            public Void onPermissionGranted() {
                ruleService.reload();
                return null;
            }
        });
    }

    /**
     * 清除缓存
     *
     * @return
     * @throws Exception
     */
    @ResponseBody
    @GetMapping("/clear-cache")
    public BaseResponse clearCache(@RequestParam(value = "p") String password) throws Exception {
        return permissionService.runAsPermission(password, "缓存清除成功", new PermissionHandler<Void>() {
            @Override
            public Void onPermissionGranted() {
                magnetService.clearCache();
                return null;
            }
        });
    }

    /**
     * 重载举报列表
     *
     * @return
     * @throws Exception
     */
    @ResponseBody
    @GetMapping("/report-reload")
    public BaseResponse reportReload(@RequestParam(value = "p") String password) throws Exception {
        BaseResponse permission = permissionService.runAsPermission(password, "举报列表重载成功", null);
        if (permission.isSuccess()) {
            reportService.reload();
        }
        return permission;
    }

    /**
     * 删除举报记录
     *
     * @return
     * @throws Exception
     */
    @ResponseBody
    @DeleteMapping("/report-delete")
    public BaseResponse reportDelete(@RequestParam(value = "p") String password, @RequestParam final String value) throws Exception {
        BaseResponse response = permissionService.runAsPermission(password, "删除成功", null);
        if (response.isSuccess()) {
            reportService.deleteReport(value);
        }
        return response;
    }

    /**
     * 添加举报记录
     *
     * @return
     * @throws Exception
     */
    @ResponseBody
    @PostMapping("/report-add")
    public BaseResponse reportAdd(@RequestParam(value = "p") String password, @RequestParam final String value) throws Exception {
        BaseResponse response = permissionService.runAsPermission(password, "添加成功", null);
        if (response.isSuccess()) {
            reportService.put(null, value);
        }
        return response;
    }


    /**
     * 获取举报列表
     *
     * @param password
     * @return
     * @throws Exception
     */
    @ResponseBody
    @GetMapping("/report-list")
    public BaseResponse reportList(@RequestParam(value = "p") String password) throws Exception {
        BaseResponse response = permissionService.runAsPermission(password, null, null);
        if (response.isSuccess()) {
            List<ReportItem> keywords = reportService.getKeywordList();
            List<ReportItem> urls = reportService.getUrlList();
            ReportData data = new ReportData();
            data.setKeywords(keywords);
            data.setUrls(urls);
            int count = keywords.size() + urls.size();
            response.setMessage(String.format("共%d条记录", count));
            response.setData(data);
        }
        return response;
    }

}
