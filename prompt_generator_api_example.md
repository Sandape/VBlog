# Prompt生成器API使用示例

## 接口概述

该接口用于接收API相关信息并调用AI进行解读，最终生成一份开发Prompt返回。

## 核心接口

### 1. 生成AI解读Prompt

**接口地址：** `POST /prompt/generate`

**请求体示例：**
```json
{
  "projectId": 1,
  "apiName": "用户登录接口",
  "apiPath": "POST /api/user/login",
  "apiDesc": "用户通过用户名和密码进行登录验证",
  "apiRequest": "{\n  \"username\": \"string\",\n  \"password\": \"string\"\n}",
  "apiResponse": "{\n  \"code\": 200,\n  \"message\": \"登录成功\",\n  \"data\": {\n    \"userId\": 1,\n    \"username\": \"admin\",\n    \"nickname\": \"管理员\",\n    \"token\": \"eyJhbGciOiJIUzI1NiJ9...\"\n  }\n}",
  "apiSqlList": [
    "CREATE TABLE `user` (\n  `id` bigint NOT NULL AUTO_INCREMENT,\n  `username` varchar(50) NOT NULL,\n  `password` varchar(255) NOT NULL,\n  `nickname` varchar(50) DEFAULT NULL,\n  `email` varchar(100) DEFAULT NULL,\n  `phone` varchar(20) DEFAULT NULL,\n  `avatar` varchar(255) DEFAULT NULL,\n  `status` int DEFAULT '1',\n  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,\n  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,\n  PRIMARY KEY (`id`),\n  UNIQUE KEY `uk_username` (`username`)\n) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;",
    "INSERT INTO `user` (`username`, `password`, `nickname`, `email`) VALUES ('admin', '$2a$10$7JB720yubVSQLVOO2X0oHeY5L1KxjqVn5f5OiWTzvUp2TiMcBLe.a', '管理员', 'admin@example.com');"
  ]
}
```

**响应示例：**
```json
{
  "code": 200,
  "message": "Prompt生成成功",
  "data": "- since: 2025/09/28\n- author: 管理员\n\n# 核心任务\n\n请你按要求完成用户登录接口的开发\n\n# 开发规范\n[项目开发规范内容]\n\n# 开发示例\n[项目示例代码路径]\n\n# 接口API\n\n**接口路径**\n```http\nPOST /api/user/login\n```\n\n**请求体：**\n| 参数 | 类型 | 必填 | 说明 |\n| ---- | ---- | ---- | ---- |\n| username | string | 是 | 用户名 |\n| password | string | 是 | 密码 |\n\n**响应结构：**\n| 响应报文字段 | 主数据库源 | 关联数据源 | 逻辑描述 |\n| ------------ | ---------- | ---------- | -------- |\n| userId | user.id | - | 用户主键ID |\n| username | user.username | - | 用户名 |\n| nickname | user.nickname | - | 用户昵称 |\n| token | - | JWT生成 | 登录令牌 |\n\n**关联数据库表：**\n表1：user表\n[具体的表结构信息]\n\n**接口数据源关系**\n[AI分析的数据源关系]"
}
```

### 2. 获取用户Prompt日志

**接口地址：** `GET /prompt/logs/my`

**响应示例：**
```json
{
  "code": 200,
  "message": "获取成功",
  "data": [
    {
      "id": 1,
      "projectId": 1,
      "projectName": "博客系统",
      "apiName": "用户登录接口",
      "apiPath": "POST /api/user/login",
      "createTime": "2025-09-28 10:30:00",
      "updateTime": "2025-09-28 10:30:15"
    }
  ]
}
```

### 3. 获取项目Prompt日志

**接口地址：** `GET /prompt/logs/project/{projectId}`

### 4. 获取Prompt日志详情

**接口地址：** `GET /prompt/logs/{id}`

### 5. 删除Prompt日志

**接口地址：** `DELETE /prompt/logs/{id}`

## 业务流程

1. **接收信息**：接口接收API相关信息（接口名、路径、描述、请求体、响应体、SQL列表）
2. **记录日志**：将请求信息存入`prompt_log`表
3. **解析SQL**：解析SQL语句获取表名，更新项目的`sql_list`字段
4. **AI调用**：使用模板A调用AI进行解析
5. **生成Prompt**：将AI结果填入模板B生成最终Prompt
6. **返回结果**：返回生成的Prompt内容

## 数据库变更

### 新增表：prompt_log

```sql
CREATE TABLE `prompt_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `project_id` bigint NOT NULL COMMENT '项目ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `api_name` varchar(255) NOT NULL COMMENT '接口名',
  `api_path` varchar(500) NOT NULL COMMENT '接口路径',
  `api_desc` text COMMENT '接口描述',
  `api_request` text COMMENT '接口请求体',
  `api_response` text COMMENT '接口响应体',
  `api_sql` text COMMENT '相关数据库表SQL（JSON格式）',
  `ai_response` text COMMENT 'AI解读结果',
  `final_prompt` text COMMENT '最终生成的Prompt',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_project_id` (`project_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI解读请求日志表';
```

### 项目表已有sql_list字段

项目表(`project`)已包含`sql_list`字段用于存储JSON格式的SQL映射。

## 注意事项

1. 确保项目已配置AI相关参数（apiKey、apiUrl、modelName）
2. SQL列表中的SQL语句应为有效的CREATE TABLE或INSERT语句
3. 接口会自动解析SQL中的表名并存储到项目的sql_list字段
4. AI调用失败时会抛出异常，需要检查AI配置和网络连接
5. 生成的Prompt会包含当前日期和登录用户信息
