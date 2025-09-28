package org.sang.utils;

import java.util.Set;

/**
 * SQL解析器测试类
 */
public class SqlParserTest {

    public static void main(String[] args) {
        // 测试各种SQL语句
        testSql("CREATE TABLE users (id INT PRIMARY KEY, name VARCHAR(100));");
        testSql("ALTER TABLE users ADD COLUMN email VARCHAR(255);");
        testSql("DROP TABLE users;");
        testSql("INSERT INTO users (name, email) VALUES ('John', 'john@example.com');");
        testSql("UPDATE users SET name = 'Jane' WHERE id = 1;");
        testSql("DELETE FROM users WHERE id = 1;");
        testSql("SELECT * FROM users WHERE id = 1;");
        testSql("SELECT u.name, p.title FROM users u JOIN posts p ON u.id = p.user_id;");
        testSql("TRUNCATE TABLE logs;");
        testSql("CREATE TABLE IF NOT EXISTS `test_table` (`id` int(11) NOT NULL);");
    }

    private static void testSql(String sql) {
        System.out.println("SQL: " + sql);
        Set<String> tableNames = SqlParserUtil.parseTableNames(sql);
        System.out.println("解析到的表名: " + tableNames);
        System.out.println("SQL类型: " + SqlParserUtil.getSqlOperationType(sql));
        System.out.println("是否有效: " + SqlParserUtil.isValidSql(sql));
        System.out.println("---");
    }
}
