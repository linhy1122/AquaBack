/*
 Navicat Premium Dump SQL

 Source Server         : myDB
 Source Server Type    : MySQL
 Source Server Version : 80044 (8.0.44)
 Source Host           : localhost:3306
 Source Schema         : aqua

 Target Server Type    : MySQL
 Target Server Version : 80044 (8.0.44)
 File Encoding         : 65001

 Date: 10/05/2026 22:07:28
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for alarm_logs
-- ----------------------------
DROP TABLE IF EXISTS `alarm_logs`;
CREATE TABLE `alarm_logs`  (
  `alarm_id` int NOT NULL AUTO_INCREMENT,
  `pond_id` int NOT NULL,
  `alarm_item` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '报警项名称',
  `alarm_value` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '触发时的数值',
  `status` enum('unhandled','handled') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'unhandled',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`alarm_id`) USING BTREE,
  INDEX `pond_id`(`pond_id` ASC) USING BTREE,
  CONSTRAINT `alarm_logs_ibfk_1` FOREIGN KEY (`pond_id`) REFERENCES `ponds` (`pond_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for alarm_thresholds
-- ----------------------------
DROP TABLE IF EXISTS `alarm_thresholds`;
CREATE TABLE `alarm_thresholds`  (
  `threshold_id` int NOT NULL AUTO_INCREMENT,
  `pond_id` int NOT NULL,
  `target_param` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '如: temp, ph, do',
  `min_value` decimal(10, 2) NULL DEFAULT NULL,
  `max_value` decimal(10, 2) NULL DEFAULT NULL,
  PRIMARY KEY (`threshold_id`) USING BTREE,
  INDEX `pond_id`(`pond_id` ASC) USING BTREE,
  CONSTRAINT `alarm_thresholds_ibfk_1` FOREIGN KEY (`pond_id`) REFERENCES `ponds` (`pond_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for devices
-- ----------------------------
DROP TABLE IF EXISTS `devices`;
CREATE TABLE `devices`  (
  `device_id` int NOT NULL AUTO_INCREMENT,
  `pond_id` int NOT NULL,
  `device_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `device_type` enum('aerator','pump') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '增氧机/水泵',
  `status` enum('on','off','error') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'off',
  `last_update` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`device_id`) USING BTREE,
  INDEX `pond_id`(`pond_id` ASC) USING BTREE,
  CONSTRAINT `devices_ibfk_1` FOREIGN KEY (`pond_id`) REFERENCES `ponds` (`pond_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for extra_costs
-- ----------------------------
DROP TABLE IF EXISTS `extra_costs`;
CREATE TABLE `extra_costs`  (
  `cost_id` int NOT NULL AUTO_INCREMENT,
  `batch_id` int NOT NULL,
  `item_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `amount` decimal(12, 2) NOT NULL,
  `record_date` date NOT NULL,
  PRIMARY KEY (`cost_id`) USING BTREE,
  INDEX `batch_id`(`batch_id` ASC) USING BTREE,
  CONSTRAINT `extra_costs_ibfk_1` FOREIGN KEY (`batch_id`) REFERENCES `farming_batches` (`batch_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for farming_batches
-- ----------------------------
DROP TABLE IF EXISTS `farming_batches`;
CREATE TABLE `farming_batches`  (
  `batch_id` int NOT NULL AUTO_INCREMENT,
  `pond_id` int NOT NULL,
  `species` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '养殖品种',
  `stock_count` int NOT NULL COMMENT '放养数量(尾)',
  `stock_date` date NOT NULL,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`batch_id`) USING BTREE,
  INDEX `pond_id`(`pond_id` ASC) USING BTREE,
  CONSTRAINT `farming_batches_ibfk_1` FOREIGN KEY (`pond_id`) REFERENCES `ponds` (`pond_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for feeding_logs
-- ----------------------------
DROP TABLE IF EXISTS `feeding_logs`;
CREATE TABLE `feeding_logs`  (
  `log_id` int NOT NULL AUTO_INCREMENT,
  `batch_id` int NOT NULL,
  `feed_type_id` int NOT NULL,
  `amount` decimal(10, 2) NOT NULL COMMENT '投喂量(kg)',
  `feed_time` datetime NOT NULL,
  PRIMARY KEY (`log_id`) USING BTREE,
  INDEX `batch_id`(`batch_id` ASC) USING BTREE,
  CONSTRAINT `feeding_logs_ibfk_1` FOREIGN KEY (`batch_id`) REFERENCES `farming_batches` (`batch_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for inventory_records
-- ----------------------------
DROP TABLE IF EXISTS `inventory_records`;
CREATE TABLE `inventory_records`  (
  `record_id` int NOT NULL AUTO_INCREMENT,
  `material_id` int NOT NULL,
  `batch_id` int NULL DEFAULT NULL COMMENT '如果是出库给特定批次，则记录',
  `type` enum('in','out') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '入库/出库',
  `quantity` decimal(10, 2) NOT NULL,
  `total_cost` decimal(12, 2) NULL DEFAULT NULL COMMENT '该次变动涉及的总金额',
  `record_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`record_id`) USING BTREE,
  INDEX `material_id`(`material_id` ASC) USING BTREE,
  INDEX `batch_id`(`batch_id` ASC) USING BTREE,
  CONSTRAINT `inventory_records_ibfk_1` FOREIGN KEY (`material_id`) REFERENCES `materials` (`material_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `inventory_records_ibfk_2` FOREIGN KEY (`batch_id`) REFERENCES `farming_batches` (`batch_id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for materials
-- ----------------------------
DROP TABLE IF EXISTS `materials`;
CREATE TABLE `materials`  (
  `material_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `category` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `unit` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '单位：kg, 尾, 瓶',
  `unit_price` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '当前单价/成本',
  `stock_qty` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '当前库存余量',
  PRIMARY KEY (`material_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ponds
-- ----------------------------
DROP TABLE IF EXISTS `ponds`;
CREATE TABLE `ponds`  (
  `pond_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `area` decimal(10, 2) NULL DEFAULT NULL COMMENT '亩',
  `location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `status` enum('idle','active') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'idle' COMMENT '状态：空闲/养殖中',
  `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '池塘编码',
  `depth` decimal(10, 2) NULL DEFAULT NULL COMMENT '水深',
  `water_source` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '水源',
  `manager` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '负责人',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted` tinyint(1) NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`pond_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for profit_projections
-- ----------------------------
DROP TABLE IF EXISTS `profit_projections`;
CREATE TABLE `profit_projections`  (
  `projection_id` int NOT NULL AUTO_INCREMENT,
  `batch_id` int NOT NULL,
  `target_price` decimal(10, 2) NOT NULL COMMENT '预期单价(元/斤)',
  `exp_survival_rate` decimal(5, 2) NULL DEFAULT 90.00 COMMENT '预期成活率%',
  `exp_avg_weight` decimal(10, 2) NULL DEFAULT NULL COMMENT '预期成品均重(斤)',
  PRIMARY KEY (`projection_id`) USING BTREE,
  UNIQUE INDEX `batch_id`(`batch_id` ASC) USING BTREE,
  CONSTRAINT `profit_projections_ibfk_1` FOREIGN KEY (`batch_id`) REFERENCES `farming_batches` (`batch_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for stocking_record
-- ----------------------------
DROP TABLE IF EXISTS `stocking_record`;
CREATE TABLE `stocking_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `pond_id` int NOT NULL COMMENT '池塘ID（类型与ponds.pond_id一致）',
  `breed_id` bigint NULL DEFAULT NULL COMMENT '品种ID',
  `species` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '物种名称',
  `stock_count` int NOT NULL COMMENT '投放数量',
  `current_num` int NULL DEFAULT 0 COMMENT '当前数量',
  `avg_spec` decimal(10, 2) NULL DEFAULT NULL COMMENT '平均规格',
  `survival_rate` decimal(5, 2) NULL DEFAULT 0.00 COMMENT '存活率',
  `stock_date` date NOT NULL COMMENT '投放日期',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'active' COMMENT '状态',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_pond_id`(`pond_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码（BCrypt加密存储）',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'USER' COMMENT '用户角色（ADMIN, USER, MANAGER）',
  `enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '账户是否启用（1：启用，0：禁用）',
  `account_locked` tinyint(1) NOT NULL DEFAULT 0 COMMENT '账户是否锁定（1：锁定，0：未锁定）',
  `account_expired` tinyint(1) NOT NULL DEFAULT 0 COMMENT '账户是否过期（1：过期，0：未过期）',
  `credentials_expired` tinyint(1) NOT NULL DEFAULT 0 COMMENT '凭据是否过期（1：过期，0：未过期）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记（0：未删除，1：已删除）',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `uk_email`(`email` ASC) USING BTREE,
  INDEX `idx_role`(`role` ASC) USING BTREE,
  INDEX `idx_deleted`(`deleted` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统用户信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for water_quality_data
-- ----------------------------
DROP TABLE IF EXISTS `water_quality_data`;
CREATE TABLE `water_quality_data`  (
  `data_id` bigint NOT NULL AUTO_INCREMENT,
  `pond_id` int NOT NULL,
  `temperature` decimal(5, 2) NULL DEFAULT NULL,
  `ph_value` decimal(4, 2) NULL DEFAULT NULL,
  `dissolved_oxygen` decimal(5, 2) NULL DEFAULT NULL COMMENT '溶解氧 mg/L',
  `ammonia_nitrogen` decimal(5, 2) NULL DEFAULT NULL COMMENT '氨氮 mg/L',
  `nitrite` decimal(5, 2) NULL DEFAULT NULL COMMENT '亚硝酸盐 mg/L',
  `transparency` decimal(5, 2) NULL DEFAULT NULL COMMENT '透明度 cm',
  `recorded_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`data_id`) USING BTREE,
  INDEX `idx_pond_time`(`pond_id` ASC, `recorded_at` ASC) USING BTREE,
  CONSTRAINT `water_quality_data_ibfk_1` FOREIGN KEY (`pond_id`) REFERENCES `ponds` (`pond_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
