/*

 Source Server         : 临时环境
 Source Server Type    : MySQL
 Source Server Version : 50744 (5.7.44)
 Source Host           : 10.0.31.215:3306
 Source Schema         : national_project

 Target Server Type    : MySQL
 Target Server Version : 50744 (5.7.44)
 File Encoding         : 65001

 Date: 30/07/2024 14:31:42
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for ads_sc_rad_cross_thematic_four_index_h
-- ----------------------------
DROP TABLE IF EXISTS `ads_sc_rad_cross_thematic_four_index_h`;
CREATE TABLE `ads_sc_rad_cross_thematic_four_index_h`  (
  `data_time` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `cross_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `cross_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `saturation` double NULL DEFAULT NULL,
  `delay_time_prop` double NULL DEFAULT NULL,
  `tra_time_index` double NULL DEFAULT NULL,
  `tra_efficiency` double NULL DEFAULT NULL
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ads_sc_rad_predict_total_flow_h
-- ----------------------------
DROP TABLE IF EXISTS `ads_sc_rad_predict_total_flow_h`;
CREATE TABLE `ads_sc_rad_predict_total_flow_h`  (
  `date_h` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '小时',
  `pred_flow` double NULL DEFAULT NULL COMMENT '预测小时车流量'
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ads_sc_rad_road_control_optimizing_index_10min
-- ----------------------------
DROP TABLE IF EXISTS `ads_sc_rad_road_control_optimizing_index_10min`;
CREATE TABLE `ads_sc_rad_road_control_optimizing_index_10min`  (
  `data_time` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '时间戳',
  `crossroad_s_e` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '路段起止点',
  `road_name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '路段名称',
  `road_level` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '道路类型',
  `sum_flow` double(10, 3) NOT NULL COMMENT '车流量',
  `saturation` double(10, 3) NOT NULL COMMENT '饱和度',
  `traffic_density` double(10, 3) NOT NULL COMMENT '车流密度',
  `avg_headwaydistance` double(10, 3) NULL DEFAULT NULL COMMENT '平均车头时距',
  `space_rate` double(10, 3) NOT NULL COMMENT '空间占有率',
  `one_pass_ratio` double(10, 3) NULL DEFAULT NULL COMMENT '一次通过率',
  `two_pass_ratio` double(10, 3) NULL DEFAULT NULL COMMENT '二次通过率',
  `delay_time_prop` double(10, 3) NULL DEFAULT NULL COMMENT '延误时间比',
  `tra_time_index` double(10, 3) NULL DEFAULT NULL COMMENT '行程时间比',
  `tra_efficiency` double(10, 3) NOT NULL COMMENT '通行效率',
  `max_queuelength` double(10, 3) NOT NULL COMMENT '最大排队长度',
  `avg_queuelength` double(10, 3) NOT NULL COMMENT '平均排队长度',
  `avg_wait_time` double(10, 3) NOT NULL COMMENT '平均等待时间',
  `avg_speed` double(10, 3) NOT NULL COMMENT '平均速度',
  `avg_stop_num` double(10, 3) NOT NULL COMMENT '平均停车次数',
  `tci` double(10, 3) NOT NULL COMMENT '拥堵指数',
  `avg_delay_time` double(10, 3) NULL DEFAULT NULL COMMENT '平均延误时间',
  INDEX `idx_road_name`(`road_name`) USING BTREE,
  INDEX `idx_data_time`(`data_time`) USING BTREE,
  INDEX `idx_tra_efficiency`(`tra_efficiency`) USING BTREE,
  INDEX `idx_saturation`(`saturation`) USING BTREE,
  INDEX `idx_delay_time_prop`(`delay_time_prop`) USING BTREE,
  INDEX `idx_tra_time_index`(`tra_time_index`) USING BTREE,
  INDEX `idx_road_level`(`road_level`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ads_sc_rad_road_overflow_rate_10min
-- ----------------------------
DROP TABLE IF EXISTS `ads_sc_rad_road_overflow_rate_10min`;
CREATE TABLE `ads_sc_rad_road_overflow_rate_10min`  (
  `data_time` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '时间戳',
  `crossroad_name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '路口名称',
  `crossroad_id` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '路口ID',
  `overflow_rate` decimal(10, 3) NULL DEFAULT NULL COMMENT '路口溢出率',
  UNIQUE INDEX `data_time`(`data_time`, `crossroad_name`, `crossroad_id`) USING BTREE,
  INDEX `idx_data_time`(`data_time`) USING BTREE,
  INDEX `idx_crossroad_name`(`crossroad_name`) USING BTREE,
  INDEX `idx_overflow_rate`(`overflow_rate`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ads_sc_rad_road_section_cross_flow_1h
-- ----------------------------
DROP TABLE IF EXISTS `ads_sc_rad_road_section_cross_flow_1h`;
CREATE TABLE `ads_sc_rad_road_section_cross_flow_1h`  (
  `crossroad_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `road_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `data_date` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `data_time` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `traffic_flow` int(11) NULL DEFAULT NULL
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ads_sc_rad_road_tci_10min
-- ----------------------------
DROP TABLE IF EXISTS `ads_sc_rad_road_tci_10min`;
CREATE TABLE `ads_sc_rad_road_tci_10min`  (
  `data_time` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '时间戳',
  `crossroad_name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '路口名称',
  `crossroad_id` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '路口ID',
  `tci` decimal(10, 3) NULL DEFAULT NULL COMMENT '路口拥堵指数',
  `h_ratio` decimal(10, 3) NULL DEFAULT NULL COMMENT '环比',
  `t_ratio` decimal(10, 3) NULL DEFAULT NULL COMMENT '同比',
  `congestion_state` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '路口拥堵状态',
  `crossroad_rank` int(10) NULL DEFAULT NULL COMMENT '排序',
  `tci_cnt` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '车辆数',
  `avg_queuelength` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '平均排队长度',
  UNIQUE INDEX `data_time`(`data_time`, `crossroad_name`, `crossroad_id`) USING BTREE,
  INDEX `idx_data_time`(`data_time`) USING BTREE,
  INDEX `idx_crossroad_name`(`crossroad_name`) USING BTREE,
  INDEX `idx_crossroad_rank`(`crossroad_rank`) USING BTREE,
  INDEX `idx_tci`(`tci`) USING BTREE,
  INDEX `idx_tci_cnt`(`tci_cnt`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ads_sc_rad_road_thematic_four_index_h
-- ----------------------------
DROP TABLE IF EXISTS `ads_sc_rad_road_thematic_four_index_h`;
CREATE TABLE `ads_sc_rad_road_thematic_four_index_h`  (
  `data_time` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `crossroad_s_e` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `road_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `road_level` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `saturation` double NULL DEFAULT NULL,
  `delay_time_prop` double NULL DEFAULT NULL,
  `tra_time_index` double NULL DEFAULT NULL,
  `tra_efficiency` double NULL DEFAULT NULL
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ads_sc_rad_road_tra_flow_peak_d
-- ----------------------------
DROP TABLE IF EXISTS `ads_sc_rad_road_tra_flow_peak_d`;
CREATE TABLE `ads_sc_rad_road_tra_flow_peak_d`  (
  `car_cnt` int(20) NULL DEFAULT NULL COMMENT '高峰时段机动车流量',
  `crossroad_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '路口名称',
  `crossroad_id` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '路口编号',
  `crossroad_longitude` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '路口经度',
  `crossroad_latitude` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '路口纬度',
  `d` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '日期',
  `h` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '高峰时段-小时',
  UNIQUE INDEX `name_day`(`crossroad_name`, `d`) USING BTREE COMMENT '哈希索引，不支持范围查询'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ads_sc_rad_road_wait_num_cnt_16h
-- ----------------------------
DROP TABLE IF EXISTS `ads_sc_rad_road_wait_num_cnt_16h`;
CREATE TABLE `ads_sc_rad_road_wait_num_cnt_16h`  (
  `data_time` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '时间戳',
  `crossroad_name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '路口名称',
  `crossroad_id` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '路口id',
  `wait_num` bigint(20) NULL DEFAULT NULL COMMENT '等待次数',
  `car_cnt` bigint(20) NULL DEFAULT NULL COMMENT '车辆数',
  `cnt_ratio` double(10, 3) NULL DEFAULT NULL COMMENT '车辆数占比',
  INDEX `idx_crossroad_name`(`crossroad_name`) USING BTREE,
  INDEX `idx_wait_num`(`wait_num`) USING BTREE,
  INDEX `idx_data_time`(`data_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ads_sc_rad_tci_10min
-- ----------------------------
DROP TABLE IF EXISTS `ads_sc_rad_tci_10min`;
CREATE TABLE `ads_sc_rad_tci_10min`  (
  `data_time` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '时间戳',
  `tci` decimal(10, 3) NULL DEFAULT NULL COMMENT '拥堵指数',
  `congestion_state` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '拥堵状态',
  `tci_rate` decimal(10, 3) NULL DEFAULT NULL COMMENT '拥堵率',
  `h_ratio` decimal(10, 3) NULL DEFAULT NULL COMMENT '环比',
  `t_ratio` decimal(10, 3) NULL DEFAULT NULL COMMENT '同比',
  `tci_cnt` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '车辆数',
  `avg_queuelength` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '平均排队长度',
  UNIQUE INDEX `data_time`(`data_time`) USING BTREE,
  INDEX `idx_data_time`(`data_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ads_sc_rad_traffic_event_count_h
-- ----------------------------
DROP TABLE IF EXISTS `ads_sc_rad_traffic_event_count_h`;
CREATE TABLE `ads_sc_rad_traffic_event_count_h`  (
  `data_time` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '时间戳',
  `cross_name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '路口名称',
  `road_name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '道路名称',
  `crossroad_s_e` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '起止点',
  `road_level` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '道路等级',
  `event_type` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '违规类型',
  `event_num` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '违规数量',
  UNIQUE INDEX `uniq_cons`(`data_time`, `cross_name`, `road_name`, `crossroad_s_e`, `event_type`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ads_traffic_signal_control_optimizing_index_10min
-- ----------------------------
DROP TABLE IF EXISTS `ads_traffic_signal_control_optimizing_index_10min`;
CREATE TABLE `ads_traffic_signal_control_optimizing_index_10min`  (
  `data_time` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '时间戳',
  `cross_name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '路口名称',
  `cross_id` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '路口id',
  `sum_flow` double(10, 3) NULL DEFAULT NULL COMMENT '车流量',
  `saturation` double(10, 3) NULL DEFAULT NULL COMMENT '饱和度',
  `traffic_density` double(10, 3) NOT NULL COMMENT '车流密度',
  `avg_headwaydistance` double(10, 3) NULL DEFAULT NULL COMMENT '平均车头时距',
  `space_rate` double(10, 3) NOT NULL COMMENT '空间占有率',
  `one_pass_ratio` double(10, 3) NULL DEFAULT NULL COMMENT '一次通过率',
  `two_pass_ratio` double(10, 3) NULL DEFAULT NULL COMMENT '二次通过率',
  `delay_time_prop` double(10, 3) NULL DEFAULT NULL COMMENT '延误时间比',
  `tra_time_index` double(10, 3) NULL DEFAULT NULL COMMENT '行程时间比',
  `tra_efficiency` double(10, 3) NULL DEFAULT NULL COMMENT '通行效率',
  `max_queuelength` double(10, 3) NULL DEFAULT NULL COMMENT '最大排队长度',
  `avg_queuelength` double(10, 3) NULL DEFAULT NULL COMMENT '平均排队长度',
  `avg_wait_time` double(10, 3) NOT NULL COMMENT '平均等待时间',
  `avg_speed` double(10, 3) NULL DEFAULT NULL COMMENT '平均速度',
  `avg_stop_num` double(10, 3) NOT NULL COMMENT '平均停车次数',
  `tci` decimal(10, 3) NOT NULL COMMENT '拥堵指数',
  `avg_delay_time` double(10, 3) NULL DEFAULT NULL COMMENT '平均延误时间',
  INDEX `idx_data_time`(`data_time`) USING BTREE,
  INDEX `idx_cross_name`(`cross_name`) USING BTREE,
  INDEX `idx_tra_efficiency`(`tra_efficiency`) USING BTREE,
  INDEX `idx_saturation`(`saturation`) USING BTREE,
  INDEX `idx_tra_time_index`(`tra_time_index`) USING BTREE,
  INDEX `idx_delay_time_prop`(`delay_time_prop`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ads_traffic_signal_control_optimizing_laneno_index_10min
-- ----------------------------
DROP TABLE IF EXISTS `ads_traffic_signal_control_optimizing_laneno_index_10min`;
CREATE TABLE `ads_traffic_signal_control_optimizing_laneno_index_10min`  (
  `data_time` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '时间戳',
  `cross_name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '路口名称',
  `cross_id` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '路口id',
  `device_id` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '设备id',
  `enter_direction` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '方向',
  `laneno` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '车道号',
  `sum_flow` double(10, 3) NOT NULL COMMENT '总流量',
  `delay_time_prop` double(10, 3) NULL DEFAULT NULL COMMENT '延误时间比',
  `tra_efficiency` double(10, 3) NOT NULL COMMENT '通行效率',
  `avg_queuelength` double(10, 3) NOT NULL COMMENT '平均排队长度',
  `avg_wait_time` double(10, 3) NOT NULL COMMENT '平均等待时间',
  `avg_stop_num` double(10, 3) NOT NULL COMMENT '平均停车次数',
  `length` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '长度',
  `max_queuelength` double(10, 3) NULL DEFAULT NULL COMMENT '最大排队长度',
  `space_rate` double(10, 3) NULL DEFAULT NULL COMMENT '空间占有率',
  `avg_speed` double(10, 3) NOT NULL COMMENT '平均车速',
  `saturation` double(10, 3) NULL DEFAULT NULL COMMENT '饱和度',
  `overflow_rate` double(10, 3) NULL DEFAULT NULL COMMENT '溢出',
  `avg_delay_time` double(10, 3) NULL DEFAULT NULL COMMENT '平均延误时间',
  INDEX `idx_data_time`(`data_time`) USING BTREE,
  INDEX `idx_cross_name`(`cross_name`) USING BTREE,
  INDEX `idx_laneno`(`laneno`) USING BTREE,
  INDEX `idx_enter_direction`(`enter_direction`) USING BTREE,
  INDEX `indexs`(`sum_flow`, `avg_stop_num`, `avg_speed`, `saturation`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for device_alarm_info
-- ----------------------------
DROP TABLE IF EXISTS `device_alarm_info`;
CREATE TABLE `device_alarm_info`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `sn` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '设备sn码',
  `device_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '设备类型',
  `alarm_status` int(1) NULL DEFAULT NULL COMMENT '告警状态，0:报警消失，1:告警开始',
  `alarm_type` int(1) NULL DEFAULT NULL COMMENT '告警类型，0:设备报警；1:业务报警',
  `alarm_message` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '告警信息的中文简短描述',
  `alarm_level` int(1) NULL DEFAULT NULL COMMENT '告警级别\r\n一级、二级、三级、四级\r\n取值范围：[1,4]\r\n四级： 严重告警:使业务中断并需要立即\r\n进行故障检修的告警;\r\n三级： 主要告警:影响业务并需要立即进\r\n行故障检修的告警;\r\n二级： 次要告警:不影响现有业务，但需\r\n进行检修以阻止恶化的告警;\r\n一\r\n级： 警告告警:不影响现有业务，但发\r\n展下去有可能影响业务，可视需要采取措施\r\n的告警',
  `gmt_alarm` datetime NULL DEFAULT NULL COMMENT '告警时间',
  `gmt_report` datetime NULL DEFAULT NULL COMMENT '上报时间',
  `gmt_created` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `info` json NULL COMMENT '告警信息结构数据',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `device_alarm_info_id_uindex`(`id`) USING BTREE,
  INDEX `device_alarm_info_sn_index`(`sn`) USING BTREE,
  INDEX `device_alarm_info_device_type_index`(`device_type`) USING BTREE,
  INDEX `device_alarm_info_alarm_level_index`(`alarm_level`) USING BTREE,
  INDEX `device_alarm_info_alarm_status_index`(`alarm_status`) USING BTREE,
  INDEX `device_alarm_info_alarm_type_index`(`alarm_type`) USING BTREE,
  INDEX `device_alarm_info_gmt_created_index`(`gmt_created`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 17829921 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for device_config_info
-- ----------------------------
DROP TABLE IF EXISTS `device_config_info`;
CREATE TABLE `device_config_info`  (
  `device_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '设备id',
  `device_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '设备名称',
  `device_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '设备类型',
  `config_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '配置类型',
  `config_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '配置信息',
  `status` int(11) NOT NULL DEFAULT 1 COMMENT '状态',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `test_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '配置信息',
  PRIMARY KEY (`device_id`, `config_type`) USING BTREE,
  INDEX `device_id`(`device_id`) USING BTREE,
  INDEX `config_type`(`config_type`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '设备配置列表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for device_info_list
-- ----------------------------
DROP TABLE IF EXISTS `device_info_list`;
CREATE TABLE `device_info_list`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `sn` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `data_json` varchar(3000) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `device_type` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '设备类型',
  `status_type` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '状态类型',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `sn_dt_st_index`(`sn`, `device_type`, `status_type`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for device_maintain
-- ----------------------------
DROP TABLE IF EXISTS `device_maintain`;
CREATE TABLE `device_maintain`  (
  `device_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `device_name` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `device_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `manufacturer` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `longitude` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `latitude` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `altitude` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `device_account` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `device_passWord` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `network_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `parent_device_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `device_ip` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `dependency` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `version` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `online_state` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0',
  `alarm_state` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0',
  `comment` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `domain_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `device_sign` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `port` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `device_area` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `online_state_update_time` datetime NULL DEFAULT NULL,
  `alarm_state_update_time` datetime NULL DEFAULT NULL,
  `network_link` int(11) NULL DEFAULT 0,
  `mac` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '设备mac地址',
  `platForm` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '平台',
  `tenant_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '租户名',
  PRIMARY KEY (`device_id`) USING BTREE,
  UNIQUE INDEX `index_name`(`device_name`) USING BTREE,
  UNIQUE INDEX `device_name`(`device_name`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for device_type
-- ----------------------------
DROP TABLE IF EXISTS `device_type`;
CREATE TABLE `device_type`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '唯一id',
  `identifier` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '设备类型唯一标识',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '设备类型名称',
  `enable` bit(1) NOT NULL DEFAULT b'0' COMMENT '启用停用标志',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `udx_idtf`(`identifier`) USING BTREE,
  UNIQUE INDEX `udx_nm`(`name`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '设备类型表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for dim-rad-rd-device-crossroad
-- ----------------------------
DROP TABLE IF EXISTS `dim-rad-rd-device-crossroad`;
CREATE TABLE `dim-rad-rd-device-crossroad`  (
  `device_id` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `lamp_id` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `lamp_longitude` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `lamp_latitude` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `cross_id` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `cross_name` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `cross_longitude` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `cross_latitude` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `is_lonAndLat_filledBy_lamp` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '路口经纬度是否由灯杆填充\r\n',
  `is_cross` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `road_name` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `road_s_e` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `daoLu_name` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '道路名称  对应excelM列  ',
  `starting_ending_point` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '起止点',
  `start_lon` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `start_lat` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `end_cross` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `end_lon` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `end_lat` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `supplier` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '厂商',
  PRIMARY KEY (`device_id`) USING BTREE,
  UNIQUE INDEX `video_id`(`lamp_latitude`, `device_id`, `cross_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '雷达与路口对应维表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for dim_own_szdl_section_relation
-- ----------------------------
DROP TABLE IF EXISTS `dim_own_szdl_section_relation`;
CREATE TABLE `dim_own_szdl_section_relation`  (
  `start_crossroad_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `start_longitude_gaode` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `start_latitude_gaode` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `end_crossroad_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `end_longitude_gaode` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `end_latitude_gaode` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `section_direction` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `device_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `start_longitude_wgs84` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `start_latitude_wgs84` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `end_longitude_wgs84` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `end_latitude_wgs84` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `device_id2` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `road_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `start_end_dot` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `section_location` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `is_section` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ods_sc_rad_devicetocrossroad
-- ----------------------------
DROP TABLE IF EXISTS `ods_sc_rad_devicetocrossroad`;
CREATE TABLE `ods_sc_rad_devicetocrossroad`  (
  `cross_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `cross_longitude` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `cross_latitude` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for operation_log
-- ----------------------------
DROP TABLE IF EXISTS `operation_log`;
CREATE TABLE `operation_log`  (
  `id` int(11) NULL DEFAULT NULL,
  `application_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `operation_object` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `operation_action` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `gmt_created` datetime NULL DEFAULT NULL,
  `description` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `status` int(11) NULL DEFAULT NULL,
  `creator_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `params` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for road_cross_relation_list
-- ----------------------------
DROP TABLE IF EXISTS `road_cross_relation_list`;
CREATE TABLE `road_cross_relation_list`  (
  `road_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `crossroad_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_dict
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict`;
CREATE TABLE `sys_dict`  (
  `id` int(10) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `dict_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `dict_encoding` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `dict_type` int(20) NULL DEFAULT NULL COMMENT '1-目录，2-子项',
  `remarks` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `pid` int(10) NULL DEFAULT NULL COMMENT '父级别id,没有父级别为0',
  `create_time` datetime NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL,
  `deleted` int(10) NULL DEFAULT NULL COMMENT '0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 45 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for videocast_sn_to_ip
-- ----------------------------
DROP TABLE IF EXISTS `videocast_sn_to_ip`;
CREATE TABLE `videocast_sn_to_ip`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `sn` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT '设备编码',
  `ip` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT '设备ip',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `id`(`id`, `sn`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_bin COMMENT = '一体机sn-ip对应关系表' ROW_FORMAT = DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;
