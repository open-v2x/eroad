package cn.eroad.videocast.model.topic;

/**
 * @Author: zhaohanqing
 * @Date: 2022/8/8 10:13
 * @Description:
 */
public class TopicEnum {
    // caikong-设备类型-设备编码-业务数据类型
    private static final String topicTemplate = "caikong-%s-%s";

    private static final String bizTemplate = "kafka_radar_%s";

    public enum ServiceDataEnum {


        ROAD_FLOW("交通区域数据", "roadFlow"),
        TRAFFIC_FLOW("交通统计信息", "trafficFlow"),
        VEHICLE_QUEUE("车辆排队信息", "vehicleQueue"),
        PASS_DATA("过车数据", "passData"),
        OVER_FLOW("溢出事件上报", "overFlow"),
        PEDESTRIAN("行人事件上报", "pedestrian"),
        NONMOTOR_LIST("非机动车事件上报", "nonmotorList"),
        PARKING_LIST("停车事件上报", "parkingList"),
        OVERSPEEDS_LIST("超速事件上报", "overspeedsList"),
        LOWSPEEDS_LIST("低速事件上报", "lowspeedsList"),
        RETROGRADE_LIST("逆行事件上报", "retrogradeList"),
        CONGESTION_LIST("拥堵事件上报", "congestionList"),
        LANE_CHANGE_LIST("变道事件上报", "laneChangeList"),
        OCCUPANCY_EMERGENCT_LIST("占用紧急车道事件上报", "occupancyEmergenctList"),
        RESTRICTED_AREA_LIST("区域入侵事件上报", "restrictedAreaList");


        private String name;
        private String code;

        ServiceDataEnum(String name, String code) {
            this.name = name;
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }
    }

    public static String getTopic(String sn, ServiceDataEnum serviceData) {
        return String.format(topicTemplate, sn, serviceData.code);
    }

    public static String getBizTopic(ServiceDataEnum serviceData) {
        return String.format(bizTemplate, serviceData.code);
    }

}
