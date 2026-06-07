-- ----------------------------
-- 投喂计划 + 投喂日志 完整字段对齐
-- 适用数据库: MySQL 8.0+
-- ----------------------------

-- ----------------------------
-- 1. 新建投喂计划表 feeding_plans
-- ----------------------------
DROP TABLE IF EXISTS `feeding_plans`;
CREATE TABLE `feeding_plans` (
  `plan_id` int NOT NULL AUTO_INCREMENT COMMENT '计划ID',
  `pond_id` int DEFAULT NULL COMMENT '塘口ID',
  `pond_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '塘口编号（快照）',
  `pond_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '塘口名称（快照）',
  `batch_id` int DEFAULT NULL COMMENT '批次ID，关联 farming_batches',
  `material_id` int DEFAULT NULL COMMENT '饲料ID，关联 materials',
  `material_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '饲料名称（快照）',
  `stock_weight` decimal(10,2) DEFAULT NULL COMMENT '存塘重量(kg)',
  `feed_rate` decimal(6,3) DEFAULT NULL COMMENT '投喂率(%)',
  `suggested_amount` decimal(10,2) DEFAULT NULL COMMENT '建议投喂量(kg)',
  `actual_amount` decimal(10,2) DEFAULT NULL COMMENT '实际投喂量(kg)',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT 'pending' COMMENT '状态: pending待执行/executed已执行/cancelled已作废/no_batch无批次/data_missing数据缺失/no_feed无饲料',
  `generated_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '生成时间',
  `executed_at` datetime DEFAULT NULL COMMENT '执行时间',
  `operator` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '操作人',
  `calc_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '计算说明',
  `factors_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '计算因子快照(JSON)',
  PRIMARY KEY (`plan_id`) USING BTREE,
  INDEX `idx_pond_id`(`pond_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=Dynamic;


-- ----------------------------
-- 2. 重建投喂日志表 feeding_logs（完整字段）
-- ----------------------------
DROP TABLE IF EXISTS `feeding_logs`;
CREATE TABLE `feeding_logs` (
  `log_id` int NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `pond_id` int DEFAULT NULL COMMENT '塘口ID',
  `pond_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '塘口编号（快照）',
  `pond_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '塘口名称（快照）',
  `batch_id` int DEFAULT NULL COMMENT '批次ID，关联 farming_batches',
  `plan_id` int DEFAULT NULL COMMENT '关联计划ID',
  `material_id` int DEFAULT NULL COMMENT '饲料ID，关联 materials',
  `material_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '饲料名称（快照）',
  `planned_amount` decimal(10,2) DEFAULT NULL COMMENT '计划投喂量(kg)',
  `actual_amount` decimal(10,2) DEFAULT NULL COMMENT '实际投喂量(kg)',
  `feed_rate` decimal(6,3) DEFAULT NULL COMMENT '投喂率(%)',
  `stock_weight` decimal(10,2) DEFAULT NULL COMMENT '存塘重量(kg)',
  `operator` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '操作人',
  `execute_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '执行状态: success/fail',
  `feed_time` datetime DEFAULT NULL COMMENT '投喂时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`log_id`) USING BTREE,
  INDEX `idx_log_pond_id`(`pond_id` ASC) USING BTREE,
  INDEX `idx_log_plan_id`(`plan_id` ASC) USING BTREE,
  INDEX `idx_log_execute_status`(`execute_status` ASC) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=Dynamic;

-- ----------------------------
-- 3. 报警阈值表 alarm_thresholds
-- ----------------------------
DROP TABLE IF EXISTS `alarm_thresholds`;
CREATE TABLE `alarm_thresholds` (
  `threshold_id` int NOT NULL AUTO_INCREMENT COMMENT '阈值ID',
  `pond_id` int NOT NULL COMMENT '塘口ID',
  `target_param` varchar(50) NOT NULL COMMENT '指标名称: temperature/ph/dissolvedOxygen/ammoniaNitrogen/nitrite/transparency',
  `min_value` decimal(10,2) DEFAULT NULL COMMENT '下限值(可空,表示不设下限)',
  `max_value` decimal(10,2) DEFAULT NULL COMMENT '上限值(可空,表示不设上限)',
  `severity` varchar(20) DEFAULT 'warning' COMMENT '超阈值严重级别: warning/critical',
  `enabled` tinyint(1) DEFAULT '1' COMMENT '是否启用: 1-启用, 0-禁用',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`threshold_id`) USING BTREE,
  UNIQUE KEY `uk_pond_param` (`pond_id`,`target_param`) USING BTREE,
  INDEX `idx_pond_id`(`pond_id` ASC) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=Dynamic;

-- ----------------------------
-- 4. 报警日志表 alarm_logs
-- ----------------------------
DROP TABLE IF EXISTS `alarm_logs`;
CREATE TABLE `alarm_logs` (
  `alarm_id` int NOT NULL AUTO_INCREMENT COMMENT '报警ID',
  `pond_id` int NOT NULL COMMENT '塘口ID',
  `alarm_item` varchar(50) NOT NULL COMMENT '报警项: temperature/ph/dissolvedOxygen/ammoniaNitrogen/nitrite/transparency',
  `alarm_value` varchar(50) DEFAULT NULL COMMENT '报警描述文字',
  `current_value` decimal(10,2) DEFAULT NULL COMMENT '触发时当前值',
  `threshold_min` decimal(10,2) DEFAULT NULL COMMENT '触发时阈值下限',
  `threshold_max` decimal(10,2) DEFAULT NULL COMMENT '触发时阈值上限',
  `severity` varchar(20) DEFAULT 'warning' COMMENT '严重级别: warning/critical',
  `status` varchar(30) DEFAULT 'unhandled' COMMENT '状态: unhandled/processing/handled/auto_recovered',
  `handle_method` varchar(30) DEFAULT NULL COMMENT '处理方式: manual/popup_ack/auto_recovered',
  `handled_by` varchar(50) DEFAULT NULL COMMENT '处理人',
  `handled_at` datetime DEFAULT NULL COMMENT '处理时间',
  `trigger_count` int DEFAULT '1' COMMENT '触发次数(同指标连续超标累加)',
  `last_triggered_at` datetime DEFAULT NULL COMMENT '最近触发时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '首次创建时间',
  PRIMARY KEY (`alarm_id`) USING BTREE,
  INDEX `idx_pond_id`(`pond_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_severity`(`severity` ASC) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=Dynamic;

-- ----------------------------
-- 5. 报警方式设置表 alarm_settings
-- ----------------------------
DROP TABLE IF EXISTS `alarm_settings`;
CREATE TABLE `alarm_settings` (
  `setting_id` int NOT NULL AUTO_INCREMENT COMMENT '设置ID',
  `pond_id` int NOT NULL COMMENT '塘口ID',
  `popup_enabled` tinyint(1) DEFAULT '1' COMMENT '页面弹窗: 1-启用, 0-禁用',
  `sound_enabled` tinyint(1) DEFAULT '1' COMMENT '声音提醒: 1-启用, 0-禁用',
  `badge_enabled` tinyint(1) DEFAULT '1' COMMENT '角标提醒: 1-启用, 0-禁用',
  `quiet_start` varchar(5) DEFAULT NULL COMMENT '静默时段开始 HH:mm',
  `quiet_end` varchar(5) DEFAULT NULL COMMENT '静默时段结束 HH:mm',
  `repeat_interval_minutes` int DEFAULT '10' COMMENT '重复提醒间隔(分钟), 0表示不重复',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`setting_id`) USING BTREE,
  UNIQUE KEY `uk_setting_pond` (`pond_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=Dynamic;
