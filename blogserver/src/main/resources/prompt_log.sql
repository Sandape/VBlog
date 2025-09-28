-- 创建prompt_log表用于记录AI解读请求日志
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
