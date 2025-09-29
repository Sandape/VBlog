package org.sang.service;

import java.sql.Timestamp;
import org.sang.bean.LoginLog;
import org.sang.bean.User;
import org.sang.mapper.LoginLogMapper;
import org.sang.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 登录日志服务
 * Created by sang on 2017/12/25.
 */
@Service
public class LoginLogService {
    
    @Autowired
    LoginLogMapper loginLogMapper;
    
    @Autowired
    UserMapper userMapper;
    
    /**
     * 记录登录日志
     */
    public void recordLoginLog(Long userId, String username, String nickname, String ipAddress, String loginStatus) {
        try {
            LoginLog loginLog = new LoginLog(userId, username, nickname, ipAddress, loginStatus);
            loginLog.setLoginTime(new Timestamp(new Date().getTime()));
            loginLogMapper.addLoginLog(loginLog);
        } catch (Exception e) {
            System.err.println("记录登录日志失败: " + e.getMessage());
        }
    }
    
    /**
     * 记录登录失败日志
     */
    public void recordLoginFailure(String username, String ipAddress, String failureReason) {
        try {
            LoginLog loginLog = new LoginLog(null, username, null, ipAddress, "FAILED");
            loginLogMapper.addLoginLog(loginLog);
        } catch (Exception e) {
            System.err.println("记录登录失败日志失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取登录统计
     */
    public List<Map<String, Object>> getLoginStatistics(Long uid) {
        return loginLogMapper.getLoginStatistics(uid);
    }
    
    /**
     * 获取登录次数
     */
    public List<Integer> getLoginCounts(Long uid) {
        return loginLogMapper.getLoginCounts(uid);
    }
    
    /**
     * 获取登录日期
     */
    public List<String> getLoginDates(Long uid) {
        return loginLogMapper.getLoginDates(uid);
    }
    
    /**
     * 获取登录日志列表
     */
    public List<LoginLog> getLoginLogs(Long uid, Integer page, Integer count) {
        return loginLogMapper.getLoginLogs(uid, (page - 1) * count, count);
    }
    
    /**
     * 获取登录日志总数
     */
    public int getLoginLogCount(Long uid) {
        return loginLogMapper.getLoginLogCount(uid);
    }
    
    /**
     * 获取所有登录日志列表
     */
    public List<LoginLog> getAllLoginLogs(Integer page, Integer count) {
        return loginLogMapper.getAllLoginLogs((page - 1) * count, count);
    }
    
    /**
     * 获取所有登录日志总数
     */
    public int getAllLoginLogCount() {
        return loginLogMapper.getAllLoginLogCount();
    }
    
    /**
     * 根据用户名获取用户信息
     */
    public User getUserByUsername(String username) {
        return userMapper.loadUserByUsername(username);
    }

    /**
     * 获取所有用户名
     */
    public List<String> getUsernames() {
        return loginLogMapper.getUsernames();
    }

    /**
     * 按用户统计登录次数
     */
    public List<Integer> getLoginCountsByUser() {
        return loginLogMapper.getLoginCountsByUser();
    }

    /**
     * 根据用户名搜索登录记录
     */
    public List<LoginLog> searchLoginLogsByUsername(String username, Integer page, Integer count) {
        return loginLogMapper.searchLoginLogsByUsername(username, (page - 1) * count, count);
    }

    /**
     * 根据用户名获取登录记录总数
     */
    public int getLoginLogCountByUsername(String username) {
        return loginLogMapper.getLoginLogCountByUsername(username);
    }
}
