package org.sang.controller;

import org.sang.bean.PromptLog;
import org.sang.bean.RespBean;
import org.sang.service.PromptGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Prompt生成器控制器
 */
@RestController
@RequestMapping("/prompt")
public class PromptGeneratorController {

    @Autowired
    private PromptGeneratorService promptGeneratorService;

    /**
     * 生成AI解读Prompt
     * 
     * 接口接收以下信息：
     * - apiName: 接口名
     * - apiPath: 接口路径
     * - apiDesc: 接口描述
     * - apiRequest: 接口请求体
     * - apiResponse: 接口响应体
     * - apiSqlList: 相关数据库表SQL列表（建表语句或INSERT语句）
     * - projectId: 项目ID
     */
    @PostMapping("/generate")
    public RespBean generatePrompt(@RequestBody Map<String, Object> request) {
        try {
            // 获取请求参数
            Long projectId = Long.valueOf(request.get("projectId").toString());
            String apiName = (String) request.get("apiName");
            String apiPath = (String) request.get("apiPath");
            String apiDesc = (String) request.get("apiDesc");
            String apiRequest = (String) request.get("apiRequest");
            String apiResponse = (String) request.get("apiResponse");
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> apiSqlList = (List<Map<String, Object>>) request.get("apiSqlList");

            // 参数校验
          if (apiName == null || apiName.trim().isEmpty()) {
                return RespBean.error("接口名不能为空");
            }
            if (apiPath == null || apiPath.trim().isEmpty()) {
                return RespBean.error("接口路径不能为空");
            }
            if (apiSqlList == null || apiSqlList.isEmpty()) {
                return RespBean.error("相关数据库表SQL不能为空");
            }

            // 调用服务生成Prompt
            String prompt = promptGeneratorService.generatePrompt(
                projectId, apiName, apiPath, apiDesc, apiRequest, apiResponse, apiSqlList
            );

            return RespBean.success("Prompt生成成功", prompt);

        } catch (NumberFormatException e) {
            return RespBean.error("项目ID格式错误");
        } catch (ClassCastException e) {
            return RespBean.error("请求参数格式错误");
        } catch (Exception e) {
            return RespBean.error("生成Prompt失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户的Prompt日志列表
     */
    @GetMapping("/logs/my")
    public RespBean getUserPromptLogs() {
        try {
            List<PromptLog> logs = promptGeneratorService.getUserPromptLogs();
            return RespBean.success("获取成功", logs);
        } catch (Exception e) {
            return RespBean.error("获取日志失败: " + e.getMessage());
        }
    }

    /**
     * 获取项目的Prompt日志列表
     */
    @GetMapping("/logs/project/{projectId}")
    public RespBean getProjectPromptLogs(@PathVariable Long projectId) {
        try {
            List<PromptLog> logs = promptGeneratorService.getProjectPromptLogs(projectId);
            return RespBean.success("获取成功", logs);
        } catch (Exception e) {
            return RespBean.error("获取项目日志失败: " + e.getMessage());
        }
    }

    /**
     * 获取Prompt日志详情
     */
    @GetMapping("/logs/{id}")
    public RespBean getPromptLogDetail(@PathVariable Long id) {
        try {
            PromptLog log = promptGeneratorService.getPromptLogById(id);
            if (log != null) {
                return RespBean.success("获取成功", log);
            } else {
                return RespBean.error("日志不存在");
            }
        } catch (Exception e) {
            return RespBean.error("获取日志详情失败: " + e.getMessage());
        }
    }

    /**
     * 删除Prompt日志
     */
    @DeleteMapping("/logs/{id}")
    public RespBean deletePromptLog(@PathVariable Long id) {
        try {
            boolean success = promptGeneratorService.deletePromptLog(id);
            if (success) {
                return RespBean.success("删除成功");
            } else {
                return RespBean.error("删除失败，日志不存在或无权限");
            }
        } catch (Exception e) {
            return RespBean.error("删除日志失败: " + e.getMessage());
        }
    }

    /**
     * 测试接口
     */
    @GetMapping("/test")
    public RespBean test() {
        return RespBean.success("Prompt生成器服务正常运行");
    }
}
