package org.sang.service;

import org.sang.bean.LoginLog;
import org.sang.bean.PromptLog;
import org.sang.bean.dto.*;
import org.sang.mapper.LoginLogMapper;
import org.sang.mapper.PromptLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 数据统计服务类
 * 处理登录记录、登录统计、使用记录、使用统计相关业务
 */
@Service
public class DataStatisticsService {

    @Autowired
    private LoginLogMapper loginLogMapper;

    @Autowired
    private PromptLogMapper promptLogMapper;

    /**
     * 获取登录记录列表（分页）
     */
    public PageResultDTO<LoginRecordDTO> getLoginRecords(Integer page, Integer pageSize, String username) {
        List<LoginLog> loginLogs;
        int totalCount;

        if (username != null && !username.trim().isEmpty()) {
            loginLogs = loginLogMapper.searchLoginLogsByUsername(username, (page - 1) * pageSize, pageSize);
            totalCount = loginLogMapper.getLoginLogCountByUsername(username);
        } else {
            loginLogs = loginLogMapper.getAllLoginLogs((page - 1) * pageSize, pageSize);
            totalCount = loginLogMapper.getAllLoginLogCount();
        }

        List<LoginRecordDTO> records = new ArrayList<>();
        for (LoginLog log : loginLogs) {
            LoginRecordDTO dto = new LoginRecordDTO(
                log.getId(),
                log.getUserId(),
                log.getUsername(),
                log.getNickname(),
                log.getIpAddress(),
                log.getLoginStatus(),
                log.getLoginTime()
            );
            records.add(dto);
        }

        return new PageResultDTO<>(records, totalCount, page, pageSize);
    }

    /**
     * 获取登录统计数据
     */
    public List<LoginStatsDTO> getLoginStatistics() {
        List<Map<String, Object>> statsData = loginLogMapper.getLoginStatsByUser();
        List<LoginStatsDTO> result = new ArrayList<>();

        for (Map<String, Object> data : statsData) {
            LoginStatsDTO dto = new LoginStatsDTO();
            dto.setUsername((String) data.get("username"));
            dto.setNickname((String) data.get("nickname"));
            dto.setLoginCount((Integer) data.get("loginCount"));
            dto.setLastLoginTime((String) data.get("lastLoginTime"));
            dto.setLastLoginIp((String) data.get("lastLoginIp"));
            result.add(dto);
        }

        return result;
    }

    /**
     * 获取使用记录列表（分页）
     */
    public PageResultDTO<UsageRecordDTO> getUsageRecords(Integer page, Integer pageSize, String username) {
        List<PromptLog> promptLogs;
        int totalCount;

        if (username != null && !username.trim().isEmpty()) {
            promptLogs = promptLogMapper.getPromptLogsByUsername(username, (page - 1) * pageSize, pageSize);
            totalCount = promptLogMapper.getPromptLogCountByUsername(username);
        } else {
            promptLogs = promptLogMapper.getAllPromptLogs((page - 1) * pageSize, pageSize);
            totalCount = promptLogMapper.getAllPromptLogCount();
        }

        List<UsageRecordDTO> records = new ArrayList<>();
        for (PromptLog log : promptLogs) {
            UsageRecordDTO dto = new UsageRecordDTO(
                log.getId(),
                log.getUserId(),
                log.getUsername(),
                log.getUserNickname(),
                log.getProjectId(),
                log.getProjectName(),
                log.getApiName(),
                log.getApiPath(),
                log.getApiDesc(),
                log.getCreateTime()
            );
            records.add(dto);
        }

        return new PageResultDTO<>(records, totalCount, page, pageSize);
    }

    /**
     * 获取使用统计数据
     */
    public List<UsageStatsDTO> getUsageStatistics() {
        List<Map<String, Object>> statsData = promptLogMapper.getUsageStatsByUser();
        List<UsageStatsDTO> result = new ArrayList<>();

        for (Map<String, Object> data : statsData) {
            UsageStatsDTO dto = new UsageStatsDTO();
            dto.setUserId(Long.valueOf((Integer) data.get("userId")));
            dto.setUsername((String) data.get("username"));
            dto.setNickname((String) data.get("nickname"));
            dto.setUsageCount((Integer) data.get("usageCount"));
            dto.setLastUsageTime((String) data.get("lastUsageTime"));
            dto.setMostUsedProject((String) data.get("mostUsedProject"));
            result.add(dto);
        }

        return result;
    }

    /**
     * 获取使用记录详情
     */
    public PromptLog getUsageRecordDetail(Long id) {
        return promptLogMapper.getPromptLogById(id);
    }
}
