package org.sang.mapper;

import org.sang.bean.PromptLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * AI解读请求日志Mapper接口
 */
@Mapper
public interface PromptLogMapper {

    /**
     * 插入日志记录
     * @param promptLog 日志对象
     * @return 影响行数
     */
    int insertPromptLog(PromptLog promptLog);

    /**
     * 根据ID查询日志
     * @param id 日志ID
     * @return 日志对象
     */
    PromptLog getPromptLogById(Long id);

    /**
     * 根据项目ID查询日志列表
     * @param projectId 项目ID
     * @param userId 用户ID（权限控制）
     * @return 日志列表
     */
    List<PromptLog> getPromptLogsByProjectId(Long projectId, Long userId);

    /**
     * 根据用户ID查询日志列表
     * @param userId 用户ID
     * @return 日志列表
     */
    List<PromptLog> getPromptLogsByUserId(Long userId);

    /**
     * 更新AI响应和最终Prompt
     * @param id 日志ID
     * @param aiResponse AI响应
     * @param finalPrompt 最终Prompt
     * @return 影响行数
     */
    int updatePromptLogResult(Long id, String aiResponse, String finalPrompt);

    /**
     * 删除日志记录
     * @param id 日志ID
     * @param userId 用户ID（权限控制）
     * @return 影响行数
     */
    int deletePromptLog(Long id, Long userId);

    /**
     * 获取所有使用记录列表（分页）
     * @param start 起始位置
     * @param count 数量
     * @return 使用记录列表
     */
    List<PromptLog> getAllPromptLogs(@Param("start") Integer start, @Param("count") Integer count);

    /**
     * 获取所有使用记录总数
     * @return 总数
     */
    int getAllPromptLogCount();

    /**
     * 根据用户名搜索使用记录
     * @param username 用户名
     * @param start 起始位置
     * @param count 数量
     * @return 使用记录列表
     */
    List<PromptLog> getPromptLogsByUsername(@Param("username") String username, @Param("start") Integer start, @Param("count") Integer count);

    /**
     * 根据用户名获取使用记录总数
     * @param username 用户名
     * @return 总数
     */
    int getPromptLogCountByUsername(@Param("username") String username);

    /**
     * 获取使用统计数据（按用户分组）
     * @return 统计数据
     */
    List<Map<String, Object>> getUsageStatsByUser();
}
