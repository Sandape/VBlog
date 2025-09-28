package org.sang.controller;

import org.sang.bean.PromptLog;
import org.sang.bean.RespBean;
import org.sang.bean.dto.*;
import org.sang.service.DataStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 数据统计控制器
 * 处理登录记录、登录统计、使用记录、使用统计相关接口
 */
@RestController
@RequestMapping("/statistics")
public class DataStatisticsController {

    @Autowired
    private DataStatisticsService dataStatisticsService;

    /**
     * 获取登录记录列表
     * @param page 页码
     * @param pageSize 每页大小
     * @param username 用户名（可选，用于搜索）
     * @return 登录记录分页数据
     */
    @GetMapping("/login/records")
    public RespBean getLoginRecords(@RequestParam(defaultValue = "1") Integer page,
                                   @RequestParam(defaultValue = "10") Integer pageSize,
                                   @RequestParam(required = false) String username) {
        try {
            PageResultDTO<LoginRecordDTO> result = dataStatisticsService.getLoginRecords(page, pageSize, username);
            return RespBean.success("获取成功", result);
        } catch (Exception e) {
            e.printStackTrace();
            return RespBean.error("获取登录记录失败: " + e.getMessage());
        }
    }

    /**
     * 获取登录统计数据
     * @return 登录统计数据
     */
    @GetMapping("/login/stats")
    public RespBean getLoginStatistics() {
        try {
            List<LoginStatsDTO> result = dataStatisticsService.getLoginStatistics();
            return RespBean.success("获取成功", result);
        } catch (Exception e) {
            e.printStackTrace();
            return RespBean.error("获取登录统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取使用记录列表
     * @param page 页码
     * @param pageSize 每页大小
     * @param username 用户名（可选，用于搜索）
     * @return 使用记录分页数据
     */
    @GetMapping("/usage/records")
    public RespBean getUsageRecords(@RequestParam(defaultValue = "1") Integer page,
                                   @RequestParam(defaultValue = "10") Integer pageSize,
                                   @RequestParam(required = false) String username) {
        try {
            PageResultDTO<UsageRecordDTO> result = dataStatisticsService.getUsageRecords(page, pageSize, username);
            return RespBean.success("获取成功", result);
        } catch (Exception e) {
            e.printStackTrace();
            return RespBean.error("获取使用记录失败: " + e.getMessage());
        }
    }

    /**
     * 获取使用统计数据
     * @return 使用统计数据
     */
    @GetMapping("/usage/stats")
    public RespBean getUsageStatistics() {
        try {
            List<UsageStatsDTO> result = dataStatisticsService.getUsageStatistics();
            return RespBean.success("获取成功", result);
        } catch (Exception e) {
            e.printStackTrace();
            return RespBean.error("获取使用统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取使用记录详情
     * @param id 记录ID
     * @return 使用记录详情
     */
    @GetMapping("/usage/records/{id}")
    public RespBean getUsageRecordDetail(@PathVariable Long id) {
        try {
            PromptLog result = dataStatisticsService.getUsageRecordDetail(id);
            if (result != null) {
                return RespBean.success("获取成功", result);
            } else {
                return RespBean.error("记录不存在");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return RespBean.error("获取使用记录详情失败: " + e.getMessage());
        }
    }
}
