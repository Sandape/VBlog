package org.sang.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * SQL AI解析服务类
 * 使用AI对SQL进行解析，获取表结构信息
 *
 * @author sang
 * @date 2024
 */
@Service
public class SqlAiParserService {

    @Autowired
    private LLMService llmService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * AI解析SQL模板
     */
    private static final String SQL_PARSE_TEMPLATE =
        "#请你解析我发送给你的SQL,获取SQL针对的表名并解析此表具有的字段、字段类型及字段含义，以返回格式要求的格式返回。\n\n" +
        "#返回格式要求\n" +
        "以json报文的格式返回，json报文包含 \"name\",\"colum\",\"entityPath\"三个字段，其中\"colum\"又包含\"columName\",\"columType\",\"columdesc\"三个字段\n\n" +
        "SQL：\n" +
        "[这里填充传入的SQL]";

    /**
     * 解析SQL语句
     *
     * @param sql SQL语句
     * @param apiKey AI API密钥
     * @param apiUrl AI API地址
     * @param modelName AI模型名称
     * @return 解析结果Map，包含表名和字段信息
     * @throws Exception 解析异常
     */
    public Map<String, Object> parseSql(String sql, String apiKey, String apiUrl, String modelName) throws Exception {
        if (sql == null || sql.trim().isEmpty()) {
            throw new IllegalArgumentException("SQL语句不能为空");
        }

        // 构建AI提示消息
        String prompt = buildPrompt(sql);

        try {
            // 调用AI服务
            String aiResponse = llmService.getAIResponse(apiKey, apiUrl, modelName, prompt);

            // 解析AI响应为JSON
            return parseAiResponse(aiResponse);

        } catch (Exception e) {
            throw new Exception("AI解析SQL失败: " + e.getMessage(), e);
        }
    }

    /**
     * 构建AI提示消息
     *
     * @param sql SQL语句
     * @return 完整的提示消息
     */
    private String buildPrompt(String sql) {
        return SQL_PARSE_TEMPLATE.replace("[这里填充传入的SQL]", sql);
    }

    /**
     * 解析AI响应内容
     *
     * @param aiResponse AI返回的JSON字符串
     * @return 解析后的数据Map
     * @throws Exception 解析异常
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseAiResponse(String aiResponse) throws Exception {
        if (aiResponse == null || aiResponse.trim().isEmpty()) {
            throw new Exception("AI响应为空");
        }

        try {
            // 清理响应内容，可能包含多余的文本
            String jsonContent = extractJsonFromResponse(aiResponse);

            // 解析JSON
            Map<String, Object> result = objectMapper.readValue(jsonContent, Map.class);

            // 验证必需字段
            validateParsedData(result);

            return result;

        } catch (Exception e) {
            throw new Exception("解析AI响应失败: " + e.getMessage() + ", 原始响应: " + aiResponse, e);
        }
    }

    /**
     * 从AI响应中提取JSON内容
     *
     * @param aiResponse AI响应
     * @return 提取的JSON字符串
     */
    private String extractJsonFromResponse(String aiResponse) {
        // 查找JSON开始和结束位置
        int jsonStart = aiResponse.indexOf('{');
        int jsonEnd = aiResponse.lastIndexOf('}');

        if (jsonStart == -1 || jsonEnd == -1 || jsonStart >= jsonEnd) {
            return aiResponse.trim();
        }

        return aiResponse.substring(jsonStart, jsonEnd + 1);
    }

    /**
     * 验证解析后的数据结构
     *
     * @param data 解析后的数据
     * @throws Exception 验证失败异常
     */
    @SuppressWarnings("unchecked")
    private void validateParsedData(Map<String, Object> data) throws Exception {
        // 检查必需字段
        if (!data.containsKey("name")) {
            throw new Exception("AI响应缺少必需字段: name");
        }
        if (!data.containsKey("colum")) {
            throw new Exception("AI响应缺少必需字段: colum");
        }
        if (!data.containsKey("entityPath")) {
            throw new Exception("AI响应缺少必需字段: entityPath");
        }

        // 检查colum字段结构
        Object columObj = data.get("colum");
        if (!(columObj instanceof java.util.List)) {
            throw new Exception("colum字段必须是数组类型");
        }

        // 可以添加更详细的验证逻辑
    }

    /**
     * 简单的SQL验证（检查是否包含有效关键字）
     *
     * @param sql SQL语句
     * @return 是否有效
     */
    public boolean isValidSql(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return false;
        }

        String trimmed = sql.trim().toUpperCase();

        // 检查是否以有效的SQL关键字开头
        return trimmed.startsWith("CREATE") ||
               trimmed.startsWith("ALTER") ||
               trimmed.startsWith("DROP") ||
               trimmed.startsWith("INSERT") ||
               trimmed.startsWith("UPDATE") ||
               trimmed.startsWith("DELETE") ||
               trimmed.startsWith("SELECT") ||
               trimmed.startsWith("TRUNCATE") ||
               trimmed.startsWith("WITH");
    }
}
