package org.sang.mapper;

import org.sang.bean.PromptLog;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

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
}
