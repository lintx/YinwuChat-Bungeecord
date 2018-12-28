/*
Navicat MySQL Data Transfer

Source Server         : local
Source Server Version : 50639
Source Host           : 127.0.0.1:3306
Source Database       : chat

Target Server Type    : MYSQL
Target Server Version : 50639
File Encoding         : 65001

Date: 2018-12-28 10:05:54
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for chat_token
-- ----------------------------
DROP TABLE IF EXISTS `chat_token`;
CREATE TABLE `chat_token` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user` int(11) NOT NULL DEFAULT '0',
  `token` varchar(255) COLLATE utf8_unicode_ci NOT NULL DEFAULT '',
  `title` varchar(100) COLLATE utf8_unicode_ci NOT NULL DEFAULT '',
  `time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `token_index` (`user`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- ----------------------------
-- Table structure for chat_users
-- ----------------------------
DROP TABLE IF EXISTS `chat_users`;
CREATE TABLE `chat_users` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `uuid` varchar(64) COLLATE utf8_unicode_ci NOT NULL DEFAULT '',
  `display_name` varchar(255) COLLATE utf8_unicode_ci NOT NULL DEFAULT '',
  `time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
