package org.sang.utils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SQL解析工具类
 * 用于解析SQL语句并提取表名
 */
public class SqlParserUtil {

    // SQL关键字模式（大小写不敏感）
    private static final Pattern CREATE_TABLE_PATTERN = Pattern.compile("(?i)\\bCREATE\\s+TABLE\\s+(?:IF\\s+NOT\\s+EXISTS\\s+)?([`\"]?\\w+[`\"]?\\s*(?:\\.[`\"]?\\w+[`\"]?)?.*?)\\s*\\(");
    private static final Pattern ALTER_TABLE_PATTERN = Pattern.compile("(?i)\\bALTER\\s+TABLE\\s+([`\"]?\\w+[`\"]?\\s*(?:\\.[`\"]?\\w+[`\"]?)?.*?)\\s+");
    private static final Pattern DROP_TABLE_PATTERN = Pattern.compile("(?i)\\bDROP\\s+TABLE\\s+(?:IF\\s+EXISTS\\s+)?([`\"]?\\w+[`\"]?\\s*(?:\\.[`\"]?\\w+[`\"]?)?.*?)\\s*");
    private static final Pattern TRUNCATE_TABLE_PATTERN = Pattern.compile("(?i)\\bTRUNCATE\\s+(?:TABLE\\s+)?([`\"]?\\w+[`\"]?\\s*(?:\\.[`\"]?\\w+[`\"]?)?.*?)\\s*");
    private static final Pattern INSERT_PATTERN = Pattern.compile("(?i)\\bINSERT\\s+(?:INTO\\s+)?([`\"]?\\w+[`\"]?\\s*(?:\\.[`\"]?\\w+[`\"]?)?.*?)\\s*");
    private static final Pattern UPDATE_PATTERN = Pattern.compile("(?i)\\bUPDATE\\s+([`\"]?\\w+[`\"]?\\s*(?:\\.[`\"]?\\w+[`\"]?)?.*?)\\s+");
    private static final Pattern DELETE_PATTERN = Pattern.compile("(?i)\\bDELETE\\s+FROM\\s+([`\"]?\\w+[`\"]?\\s*(?:\\.[`\"]?\\w+[`\"]?)?.*?)\\s+");
    private static final Pattern SELECT_PATTERN = Pattern.compile("(?i)\\bFROM\\s+([`\"]?\\w+[`\"]?\\s*(?:\\.[`\"]?\\w+[`\"]?)?.*?)\\s+");

    // JOIN表名提取模式
    private static final Pattern JOIN_PATTERN = Pattern.compile("(?i)\\b(?:INNER\\s+|LEFT\\s+|RIGHT\\s+|FULL\\s+|CROSS\\s+)?JOIN\\s+([`\"]?\\w+[`\"]?\\s*(?:\\.[`\"]?\\w+[`\"]?)?.*?)\\s+(?:AS\\s+)?(?:\\w+\\s+)?(?:ON\\s+|USING\\s+)");

    /**
     * 解析SQL语句并提取表名
     * @param sql SQL语句
     * @return 表名集合
     */
    public static Set<String> parseTableNames(String sql) {
        Set<String> tableNames = new HashSet<>();

        if (sql == null || sql.trim().isEmpty()) {
            return tableNames;
        }

        // 预处理SQL：移除注释
        sql = removeComments(sql);

        // 按分号分割多个SQL语句
        String[] sqlStatements = sql.split(";");

        for (String statement : sqlStatements) {
            statement = statement.trim();
            if (statement.isEmpty()) {
                continue;
            }

            // 提取各种SQL语句中的表名
            extractTableNamesFromStatement(statement, tableNames);
        }

        return tableNames;
    }

    /**
     * 从单个SQL语句中提取表名
     */
    private static void extractTableNamesFromStatement(String sql, Set<String> tableNames) {
        // 提取CREATE TABLE语句的表名
        extractTableNames(sql, CREATE_TABLE_PATTERN, tableNames);

        // 提取ALTER TABLE语句的表名
        extractTableNames(sql, ALTER_TABLE_PATTERN, tableNames);

        // 提取DROP TABLE语句的表名
        extractTableNames(sql, DROP_TABLE_PATTERN, tableNames);

        // 提取TRUNCATE TABLE语句的表名
        extractTableNames(sql, TRUNCATE_TABLE_PATTERN, tableNames);

        // 提取INSERT语句的表名
        extractTableNames(sql, INSERT_PATTERN, tableNames);

        // 提取UPDATE语句的表名
        extractTableNames(sql, UPDATE_PATTERN, tableNames);

        // 提取DELETE语句的表名
        extractTableNames(sql, DELETE_PATTERN, tableNames);

        // 提取SELECT语句中的FROM表名
        extractTableNames(sql, SELECT_PATTERN, tableNames);

        // 提取JOIN中的表名
        extractTableNames(sql, JOIN_PATTERN, tableNames);
    }

    /**
     * 使用正则表达式提取表名
     */
    private static void extractTableNames(String sql, Pattern pattern, Set<String> tableNames) {
        Matcher matcher = pattern.matcher(sql);
        while (matcher.find()) {
            String tableName = matcher.group(1).trim();
            // 清理表名：移除引号和反引号
            tableName = cleanTableName(tableName);
            if (!tableName.isEmpty()) {
                tableNames.add(tableName);
            }
        }
    }

    /**
     * 清理表名：移除引号、反引号，提取实际表名
     */
    private static String cleanTableName(String tableName) {
        if (tableName == null) {
            return "";
        }

        // 移除引号和反引号
        tableName = tableName.replaceAll("[`\"]", "");

        // 如果包含点号，取最后一部分（表名）
        if (tableName.contains(".")) {
            String[] parts = tableName.split("\\.");
            tableName = parts[parts.length - 1];
        }

        // 移除空格和制表符
        tableName = tableName.trim();

        // 如果包含空格，取第一个单词（表名别名的情况）
        if (tableName.contains(" ")) {
            tableName = tableName.split("\\s+")[0];
        }

        return tableName.toLowerCase();
    }

    /**
     * 移除SQL注释
     */
    private static String removeComments(String sql) {
        // 移除单行注释
        sql = sql.replaceAll("--.*$", "");
        // 移除多行注释
        sql = sql.replaceAll("/\\*.*?\\*/", "");
        return sql;
    }

    /**
     * 验证SQL语句是否有效（简单检查）
     */
    public static boolean isValidSql(String sql) {
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

    /**
     * 获取SQL的主要操作类型
     */
    public static String getSqlOperationType(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return "UNKNOWN";
        }

        String trimmed = sql.trim().toUpperCase();

        if (trimmed.startsWith("CREATE")) {
            return "CREATE";
        } else if (trimmed.startsWith("ALTER")) {
            return "ALTER";
        } else if (trimmed.startsWith("DROP")) {
            return "DROP";
        } else if (trimmed.startsWith("INSERT")) {
            return "INSERT";
        } else if (trimmed.startsWith("UPDATE")) {
            return "UPDATE";
        } else if (trimmed.startsWith("DELETE")) {
            return "DELETE";
        } else if (trimmed.startsWith("SELECT")) {
            return "SELECT";
        } else if (trimmed.startsWith("TRUNCATE")) {
            return "TRUNCATE";
        }

        return "UNKNOWN";
    }
}