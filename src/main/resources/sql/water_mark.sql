/*
 Navicat Premium Data Transfer

 Source Server         : localhost_3300
 Source Server Type    : MySQL
 Source Server Version : 80026
 Source Host           : 127.0.0.1:3300
 Source Schema         : stu

 Target Server Type    : MySQL
 Target Server Version : 80026
 File Encoding         : 65001

 Date: 22/11/2024 15:03:16
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for water_mark
-- ----------------------------
DROP TABLE IF EXISTS `water_mark`;
CREATE TABLE `water_mark`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `image` longblob NULL COMMENT '源图片',
  `marked_image` longblob NULL COMMENT '加水印后的图片',
  `image_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图片名称',
  `create_time` datetime NULL DEFAULT NULL,
  `seed` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '种子',
  `private_key` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '私钥',
  `public_key` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '公钥',
  `water_text` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '水印文本',
  `encrypted_water_text` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
