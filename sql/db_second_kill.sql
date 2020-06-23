/*
 Navicat MySQL Data Transfer

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 80017
 Source Host           : localhost:3306
 Source Schema         : db_second_kill

 Target Server Type    : MySQL
 Target Server Version : 80017
 File Encoding         : 65001

 Date: 23/06/2020 12:41:29
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for item
-- ----------------------------
DROP TABLE IF EXISTS `item`;
CREATE TABLE `item` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL COMMENT '商品名',
  `code` varchar(255) DEFAULT NULL COMMENT '商品编号',
  `stock` bigint(20) DEFAULT NULL COMMENT '库存',
  `purchase_time` date DEFAULT NULL COMMENT '采购时间',
  `is_active` int(11) DEFAULT '1' COMMENT '是否有效（1=是；0=否）',
  `create_time` datetime DEFAULT NULL,
  `update_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8 COMMENT='商品表';

-- ----------------------------
-- Records of item
-- ----------------------------
BEGIN;
INSERT INTO `item` VALUES (6, 'Java编程思想', 'book10010', 1000, '2019-05-18', 1, '2019-05-18 21:11:23', NULL);
INSERT INTO `item` VALUES (7, 'Spring实战第四版', 'book10011', 2000, '2019-05-18', 1, '2019-05-18 21:11:23', NULL);
INSERT INTO `item` VALUES (8, '深入分析JavaWeb', 'book10012', 2000, '2019-05-18', 1, '2019-05-18 21:11:23', NULL);
INSERT INTO `item` VALUES (9, '精通mysql', 'book10013', 1000, '2020-01-26', 1, '2020-01-26 12:45:30', NULL);
COMMIT;

-- ----------------------------
-- Table structure for item_kill
-- ----------------------------
DROP TABLE IF EXISTS `item_kill`;
CREATE TABLE `item_kill` (
  `id` int(20) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `item_id` int(11) DEFAULT NULL COMMENT '商品id',
  `total` int(11) DEFAULT NULL COMMENT '可被秒杀的总数',
  `start_time` datetime DEFAULT NULL COMMENT '秒杀开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '秒杀结束时间',
  `is_active` tinyint(11) DEFAULT '1' COMMENT '是否有效（1=是；0=否）',
  `create_time` timestamp NULL DEFAULT NULL COMMENT '创建的时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8 COMMENT='待秒杀商品表';

-- ----------------------------
-- Records of item_kill
-- ----------------------------
BEGIN;
INSERT INTO `item_kill` VALUES (1, 6, 10, '2019-06-08 21:59:07', '2019-06-15 21:59:11', 1, '2019-05-20 21:59:41');
INSERT INTO `item_kill` VALUES (2, 7, 0, '2019-06-01 21:59:19', '2019-06-30 21:59:11', 1, '2019-05-20 21:59:41');
INSERT INTO `item_kill` VALUES (3, 8, 5, '2019-07-01 18:58:26', '2020-01-31 21:59:11', 1, '2019-05-20 21:59:41');
COMMIT;

-- ----------------------------
-- Table structure for item_kill_success
-- ----------------------------
DROP TABLE IF EXISTS `item_kill_success`;
CREATE TABLE `item_kill_success` (
  `code` varchar(50) NOT NULL COMMENT '秒杀成功生成的订单编号',
  `item_id` int(11) DEFAULT NULL COMMENT '商品id',
  `kill_id` int(11) DEFAULT NULL COMMENT '秒杀id',
  `user_id` varchar(20) DEFAULT NULL COMMENT '用户id',
  `status` tinyint(4) DEFAULT '-1' COMMENT '秒杀结果: -1无效  0成功(未付款)  1已付款  2已取消',
  `create_time` timestamp NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='秒杀成功订单表';

-- ----------------------------
-- Records of item_kill_success
-- ----------------------------
BEGIN;
INSERT INTO `item_kill_success` VALUES ('343507312969003008', 8, 3, '10', -1, '2020-01-03 16:58:31');
INSERT INTO `item_kill_success` VALUES ('410862499865571328', 8, 3, '10012', -1, '2020-01-04 15:41:59');
INSERT INTO `item_kill_success` VALUES ('410862500872204288', 8, 3, '10019', -1, '2020-01-04 15:41:59');
INSERT INTO `item_kill_success` VALUES ('410862501161611264', 8, 3, '10013', -1, '2020-01-04 15:41:59');
INSERT INTO `item_kill_success` VALUES ('410862501161611265', 8, 3, '10018', -1, '2020-01-04 15:41:59');
INSERT INTO `item_kill_success` VALUES ('410862502294073344', 8, 3, '10011', -1, '2020-01-04 15:42:00');
COMMIT;

-- ----------------------------
-- Table structure for random_code
-- ----------------------------
DROP TABLE IF EXISTS `random_code`;
CREATE TABLE `random_code` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `code` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_code` (`code`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

SET FOREIGN_KEY_CHECKS = 1;
