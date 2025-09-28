-- 项目表
CREATE TABLE `project` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '项目ID',
  `project_name` varchar(100) NOT NULL COMMENT '项目名称',
  `project_code` varchar(8) NOT NULL UNIQUE COMMENT '项目编码（8位随机数字）',
  `development_spec` text COMMENT '项目开发规范',
  `api_key` varchar(255) COMMENT 'API Key',
  `api_url` varchar(500) COMMENT 'API URL',
  `model_name` varchar(100) COMMENT 'Model Name',
  `example_mapper_path` varchar(500) COMMENT '示例Mapper类路径',
  `example_entity_path` varchar(500) COMMENT '示例实体类路径',
  `example_interface_path` varchar(500) COMMENT '示例接口路径',
  `owner_id` bigint(20) NOT NULL COMMENT '项目拥有者ID',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '状态：1-正常，0-禁用',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_project_code` (`project_code`),
  KEY `idx_owner_id` (`owner_id`),
  KEY `idx_project_name` (`project_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目表';

-- 项目成员表
CREATE TABLE `project_member` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `project_id` bigint(20) NOT NULL COMMENT '项目ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `role` tinyint(1) NOT NULL DEFAULT 2 COMMENT '角色：1-拥有者，2-成员',
  `join_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '状态：1-正常，0-已退出',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_project_user` (`project_id`, `user_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_project_id` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目成员表';
