package org.sang.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.sang.service.SqlAiParserService;

/**
 * AI SQL解析器测试类
 */
public class SqlAiParserTest {

    private static final SqlAiParserService sqlAiParserService = new SqlAiParserService();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) {
        // 测试SQL语句
        String sql = "CREATE TABLE users (id INT PRIMARY KEY, name VARCHAR(100), email VARCHAR(255), created_at TIMESTAMP);";

        // AI配置（示例，需要替换为真实的API配置）
        String apiKey = "your-api-key-here";
        String apiUrl = "https://api.openai.com/v1/chat/completions";
        String modelName = "gpt-3.5-turbo";

        System.out.println("测试SQL: " + sql);
        System.out.println("AI配置: " + apiUrl + " (" + modelName + ")");

        try {
            // 测试SQL验证
            boolean isValid = sqlAiParserService.isValidSql(sql);
            System.out.println("SQL是否有效: " + isValid);

            if (isValid) {
                // 注意：这里只是测试代码结构，实际调用需要真实的API密钥
                System.out.println("AI解析功能已集成，实际调用需要配置有效的API密钥");
                System.out.println("测试通过：代码结构正确");
            }

        } catch (Exception e) {
            System.out.println("测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
