-- 为project表添加sql_list字段
-- 执行此脚本前请备份数据库

ALTER TABLE `project` ADD COLUMN `sql_list` json COMMENT 'SQL表元列表，JSON格式：{"table_name": "sql_content"}' AFTER `example_interface_path`;
