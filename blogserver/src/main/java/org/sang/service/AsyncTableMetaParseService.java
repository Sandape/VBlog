package org.sang.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.sang.bean.TableMetaData;
import org.sang.bean.TableMetaParseTask;
import org.sang.mapper.ProjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 异步表元数据解析服务
 * 用于在后台异步执行AI解析SQL任务
 *
 * @author sang
 * @date 2024
 */
@Service
public class AsyncTableMetaParseService {

    @Autowired
    private SqlAiParserService sqlAiParserService;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private NotificationService notificationService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 异步解析表元数据
     *
     * @param task 解析任务信息
     */
    @Async
    public void parseTableMetaAsync(TableMetaParseTask task) {
        try {
            // 获取项目表元数据Map
            Map<String, TableMetaData> tableMetaMap = getTableMetaMapFromProject(task.getProjectId());

            if (!tableMetaMap.containsKey(task.getTableName())) {
                return; // 表元不存在
            }

            TableMetaData tableMetaData = tableMetaMap.get(task.getTableName());

            // 设置状态为正在解析
            tableMetaData.setParseStatus("PARSING");
            updateProjectTableMeta(task.getProjectId(), tableMetaMap);

            // 执行AI解析
            Map<String, Object> parsedData = sqlAiParserService.parseSql(
                task.getSql(),
                task.getApiKey(),
                task.getApiUrl(),
                task.getModelName()
            );

            // 解析成功，更新表元数据
            tableMetaData.setName((String) parsedData.get("name"));
            tableMetaData.setColum((java.util.List<TableMetaData.ColumnMetaData>) parsedData.get("colum"));

            // 注意：entityPath只保留用户手动设置的值，不使用AI解析的结果进行自动填充

            tableMetaData.setParseStatus("COMPLETED");
            tableMetaData.setParseError(null);

            updateProjectTableMeta(task.getProjectId(), tableMetaMap);

            // 发送解析完成通知
            notificationService.createParseCompletedNotification(task.getUserId(), task.getProjectId(), task.getTableName());

        } catch (Exception e) {
            // 解析失败，更新状态和错误信息
            try {
                Map<String, TableMetaData> tableMetaMap = getTableMetaMapFromProject(task.getProjectId());
                if (tableMetaMap.containsKey(task.getTableName())) {
                    TableMetaData tableMetaData = tableMetaMap.get(task.getTableName());
                    tableMetaData.setParseStatus("FAILED");
                    tableMetaData.setParseError(e.getMessage());
                    updateProjectTableMeta(task.getProjectId(), tableMetaMap);

                    // 发送解析失败通知
                    notificationService.createParseFailedNotification(task.getUserId(), task.getProjectId(), task.getTableName(), e.getMessage());
                }
            } catch (Exception updateEx) {
                // 记录更新失败的日志
                System.err.println("更新解析失败状态时出错: " + updateEx.getMessage());
            }

            System.err.println("异步解析表元数据失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 从项目对象中获取SQL Map
     */
    private Map<String, TableMetaData> getTableMetaMapFromProject(Long projectId) {
        try {
            String sqlListJson = projectMapper.getProjectSqlListOnly(projectId);
            if (sqlListJson == null || sqlListJson.trim().isEmpty()) {
                return new java.util.HashMap<>();
            }
            return objectMapper.readValue(sqlListJson, new com.fasterxml.jackson.core.type.TypeReference<Map<String, TableMetaData>>() {});
        } catch (Exception e) {
            e.printStackTrace();
            return new java.util.HashMap<>();
        }
    }

    /**
     * 更新项目的表元数据
     */
    private void updateProjectTableMeta(Long projectId, Map<String, TableMetaData> tableMetaMap) {
        try {
            String sqlListJson = objectMapper.writeValueAsString(tableMetaMap);
            projectMapper.updateProjectSqlListOnly(projectId, sqlListJson);
        } catch (Exception e) {
            System.err.println("更新项目表元数据失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
