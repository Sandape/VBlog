-- prompt_db.article definition

CREATE TABLE `article` (
                           `id` int NOT NULL AUTO_INCREMENT,
                           `title` varchar(255) DEFAULT NULL,
                           `mdContent` text COMMENT 'md文件源码',
                           `htmlContent` text COMMENT 'html源码',
                           `summary` text,
                           `cid` int DEFAULT NULL,
                           `uid` int DEFAULT NULL,
                           `publishDate` datetime DEFAULT NULL,
                           `editTime` datetime DEFAULT NULL,
                           `state` int DEFAULT NULL COMMENT '0表示草稿箱，1表示已发表，2表示已删除',
                           `pageView` int DEFAULT '0',
                           PRIMARY KEY (`id`),
                           KEY `cid` (`cid`),
                           KEY `uid` (`uid`),
                           CONSTRAINT `article_ibfk_1` FOREIGN KEY (`cid`) REFERENCES `category` (`id`),
                           CONSTRAINT `article_ibfk_2` FOREIGN KEY (`uid`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=126 DEFAULT CHARSET=utf8mb3;

-- prompt_db.article_tags definition

CREATE TABLE `article_tags` (
                                `id` int NOT NULL AUTO_INCREMENT,
                                `aid` int DEFAULT NULL,
                                `tid` int DEFAULT NULL,
                                PRIMARY KEY (`id`),
                                KEY `tid` (`tid`),
                                KEY `article_tags_ibfk_1` (`aid`),
                                CONSTRAINT `article_tags_ibfk_1` FOREIGN KEY (`aid`) REFERENCES `article` (`id`) ON DELETE CASCADE,
                                CONSTRAINT `article_tags_ibfk_2` FOREIGN KEY (`tid`) REFERENCES `tags` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=53 DEFAULT CHARSET=utf8mb3;

-- prompt_db.category definition

CREATE TABLE `category` (
                            `id` int NOT NULL AUTO_INCREMENT,
                            `cateName` varchar(64) DEFAULT NULL,
                            `date` date DEFAULT NULL,
                            PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=66 DEFAULT CHARSET=utf8mb3;

-- prompt_db.comments definition

CREATE TABLE `comments` (
                            `id` int NOT NULL AUTO_INCREMENT,
                            `aid` int DEFAULT NULL,
                            `content` text,
                            `publishDate` datetime DEFAULT NULL,
                            `parentId` int DEFAULT NULL COMMENT '-1表示正常回复，其他值表示是评论的回复',
                            `uid` int DEFAULT NULL,
                            PRIMARY KEY (`id`),
                            KEY `aid` (`aid`),
                            KEY `uid` (`uid`),
                            KEY `parentId` (`parentId`),
                            CONSTRAINT `comments_ibfk_1` FOREIGN KEY (`aid`) REFERENCES `article` (`id`),
                            CONSTRAINT `comments_ibfk_2` FOREIGN KEY (`uid`) REFERENCES `user` (`id`),
                            CONSTRAINT `comments_ibfk_3` FOREIGN KEY (`parentId`) REFERENCES `comments` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

-- prompt_db.login_log definition

CREATE TABLE `login_log` (
                             `id` bigint NOT NULL AUTO_INCREMENT,
                             `user_id` bigint DEFAULT NULL COMMENT '用户ID',
                             `username` varchar(64) DEFAULT NULL COMMENT '用户名',
                             `nickname` varchar(64) DEFAULT NULL COMMENT '昵称',
                             `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP地址',
                             `user_agent` text COMMENT '用户代理',
                             `login_status` varchar(20) NOT NULL DEFAULT 'SUCCESS' COMMENT '登录状态：SUCCESS-成功，FAILED-失败',
                             `failure_reason` varchar(255) DEFAULT NULL COMMENT '失败原因',
                             `login_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
                             `logout_time` datetime DEFAULT NULL COMMENT '登出时间',
                             `session_duration` bigint DEFAULT NULL COMMENT '会话持续时间（秒）',
                             PRIMARY KEY (`id`),
                             KEY `idx_user_id` (`user_id`),
                             KEY `idx_login_time` (`login_time`),
                             KEY `idx_login_status` (`login_status`)
) ENGINE=InnoDB AUTO_INCREMENT=82 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='登录日志表';

-- prompt_db.notification definition

CREATE TABLE `notification` (
                                `id` bigint NOT NULL AUTO_INCREMENT,
                                `user_id` bigint NOT NULL COMMENT '用户ID',
                                `type` varchar(50) NOT NULL COMMENT '通知类型：PARSE_COMPLETED-解析完成，PARSE_FAILED-解析失败',
                                `title` varchar(200) NOT NULL COMMENT '通知标题',
                                `content` text NOT NULL COMMENT '通知内容',
                                `is_read` tinyint NOT NULL DEFAULT '0' COMMENT '是否已读：0-未读，1-已读',
                                `project_id` bigint DEFAULT NULL COMMENT '关联项目ID',
                                `table_name` varchar(100) DEFAULT NULL COMMENT '关联表名',
                                `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                PRIMARY KEY (`id`),
                                KEY `idx_user_id` (`user_id`),
                                KEY `idx_is_read` (`is_read`),
                                KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='通知表';

-- prompt_db.project definition

CREATE TABLE `project` (
                           `id` bigint NOT NULL AUTO_INCREMENT COMMENT '项目ID',
                           `project_name` varchar(100) NOT NULL COMMENT '项目名称',
                           `project_code` varchar(8) NOT NULL COMMENT '项目编码（8位随机数字）',
                           `development_spec` text COMMENT '项目开发规范',
                           `api_key` varchar(255) DEFAULT NULL COMMENT 'API Key',
                           `api_url` varchar(500) DEFAULT NULL COMMENT 'API URL',
                           `model_name` varchar(100) DEFAULT NULL COMMENT 'Model Name',
                           `example_mapper_path` varchar(500) DEFAULT NULL COMMENT '示例Mapper类路径',
                           `example_entity_path` varchar(500) DEFAULT NULL COMMENT '示例实体类路径',
                           `example_interface_path` varchar(500) DEFAULT NULL COMMENT '示例接口路径',
                           `owner_id` bigint NOT NULL COMMENT '项目拥有者ID',
                           `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                           `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态：1-正常，0-禁用',
                           `sql_list` json DEFAULT NULL,
                           PRIMARY KEY (`id`),
                           UNIQUE KEY `project_code` (`project_code`),
                           UNIQUE KEY `uk_project_code` (`project_code`),
                           KEY `idx_owner_id` (`owner_id`),
                           KEY `idx_project_name` (`project_name`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='项目表';

-- prompt_db.project_member definition

CREATE TABLE `project_member` (
                                  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                  `project_id` bigint NOT NULL COMMENT '项目ID',
                                  `user_id` bigint NOT NULL COMMENT '用户ID',
                                  `role` tinyint(1) NOT NULL DEFAULT '2' COMMENT '角色：1-拥有者，2-成员',
                                  `join_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
                                  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态：1-正常，0-已退出',
                                  PRIMARY KEY (`id`),
                                  UNIQUE KEY `uk_project_user` (`project_id`,`user_id`),
                                  KEY `idx_user_id` (`user_id`),
                                  KEY `idx_project_id` (`project_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='项目成员表';

-- prompt_db.prompt_log definition

CREATE TABLE `prompt_log` (
                              `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                              `project_id` bigint NOT NULL COMMENT '项目ID',
                              `user_id` bigint NOT NULL COMMENT '用户ID',
                              `api_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '接口名',
                              `api_path` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '接口路径',
                              `api_desc` text COLLATE utf8mb4_unicode_ci COMMENT '接口描述',
                              `api_request` text COLLATE utf8mb4_unicode_ci COMMENT '接口请求体',
                              `api_response` text COLLATE utf8mb4_unicode_ci COMMENT '接口响应体',
                              `api_sql` text COLLATE utf8mb4_unicode_ci COMMENT '相关数据库表SQL（JSON格式）',
                              `ai_response` text COLLATE utf8mb4_unicode_ci COMMENT 'AI解读结果',
                              `final_prompt` text COLLATE utf8mb4_unicode_ci COMMENT '最终生成的Prompt',
                              `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                              `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                              PRIMARY KEY (`id`),
                              KEY `idx_project_id` (`project_id`),
                              KEY `idx_user_id` (`user_id`),
                              KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI解读请求日志表';

-- prompt_db.pv definition

CREATE TABLE `pv` (
                      `id` int NOT NULL AUTO_INCREMENT,
                      `countDate` date DEFAULT NULL,
                      `pv` int DEFAULT NULL,
                      `uid` int DEFAULT NULL,
                      PRIMARY KEY (`id`),
                      KEY `pv_ibfk_1` (`uid`),
                      CONSTRAINT `pv_ibfk_1` FOREIGN KEY (`uid`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=utf8mb3;

-- prompt_db.roles definition

CREATE TABLE `roles` (
                         `id` int NOT NULL AUTO_INCREMENT,
                         `name` varchar(32) DEFAULT NULL,
                         PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb3;

-- prompt_db.roles_user definition

CREATE TABLE `roles_user` (
                              `id` int NOT NULL AUTO_INCREMENT,
                              `rid` int DEFAULT '2',
                              `uid` int DEFAULT NULL,
                              PRIMARY KEY (`id`),
                              KEY `rid` (`rid`),
                              KEY `roles_user_ibfk_2` (`uid`),
                              CONSTRAINT `roles_user_ibfk_1` FOREIGN KEY (`rid`) REFERENCES `roles` (`id`),
                              CONSTRAINT `roles_user_ibfk_2` FOREIGN KEY (`uid`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=133 DEFAULT CHARSET=utf8mb3;

-- prompt_db.tags definition

CREATE TABLE `tags` (
                        `id` int NOT NULL AUTO_INCREMENT,
                        `tagName` varchar(32) DEFAULT NULL,
                        PRIMARY KEY (`id`),
                        UNIQUE KEY `tagName` (`tagName`)
) ENGINE=InnoDB AUTO_INCREMENT=68 DEFAULT CHARSET=utf8mb3;

-- prompt_db.`user` definition

CREATE TABLE `user` (
                        `id` int NOT NULL AUTO_INCREMENT,
                        `username` varchar(64) DEFAULT NULL,
                        `nickname` varchar(64) DEFAULT NULL,
                        `password` varchar(255) DEFAULT NULL,
                        `enabled` tinyint(1) DEFAULT '1',
                        `email` varchar(64) DEFAULT NULL,
                        `userface` varchar(255) DEFAULT NULL,
                        `regTime` datetime DEFAULT NULL,
                        PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb3;