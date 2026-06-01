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
