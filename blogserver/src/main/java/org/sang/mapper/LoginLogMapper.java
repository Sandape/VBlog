package org.sang.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.sang.bean.LoginLog;

import java.util.List;
import java.util.Map;

/**
 * 登录日志Mapper
 * Created by sang on 2017/12/25.
 */
@Mapper
public interface LoginLogMapper {
    
    /**
     * 添加登录日志
     */
    int addLoginLog(LoginLog loginLog);
    
    /**
     * 更新登出时间
     */
    int updateLogoutTime(@Param("id") Long id, @Param("logoutTime") java.util.Date logoutTime);
    
    /**
     * 获取最近7天的登录统计
     */
    List<Map<String, Object>> getLoginStatistics(@Param("uid") Long uid);
    
    /**
     * 获取最近7天的登录次数
     */
    List<Integer> getLoginCounts(@Param("uid") Long uid);
    
    /**
     * 获取最近7天的日期
     */
    List<String> getLoginDates(@Param("uid") Long uid);
    
    /**
     * 获取用户登录日志列表
     */
    List<LoginLog> getLoginLogs(@Param("uid") Long uid, @Param("start") Integer start, @Param("count") Integer count);
    
    /**
     * 获取用户登录日志总数
     */
    int getLoginLogCount(@Param("uid") Long uid);

    /**
     * 获取所有登录日志列表
     */
    List<LoginLog> getAllLoginLogs(@Param("start") Integer start, @Param("count") Integer count);

    /**
     * 获取所有登录日志总数
     */
    int getAllLoginLogCount();
    
    /**
     * 获取所有用户名
     */
    List<String> getUsernames();
    
    /**
     * 按用户统计登录次数
     */
    List<Integer> getLoginCountsByUser();
    
    /**
     * 根据用户名搜索登录记录
     */
    List<LoginLog> searchLoginLogsByUsername(@Param("username") String username, @Param("start") Integer start, @Param("count") Integer count);
    
    /**
     * 根据用户名获取登录记录总数
     */
    int getLoginLogCountByUsername(@Param("username") String username);

    /**
     * 获取登录统计数据（按用户分组）
     */
    List<Map<String, Object>> getLoginStatsByUser();
}