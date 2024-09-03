package cn.eroad.videocast.service;
import cn.eroad.videocast.model.data.*;
import cn.eroad.videocast.model.topic.TopicEnum;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

@Service
public class VideoService {
    public final String sn = null;

    //交通区域数据
    public void roadFlow(RoadFlowVo roadFlowVo) {
        JSONObject dataObject = new JSONObject();
        dataObject.put("deviceId", sn);
        dataObject.put("reference", roadFlowVo.getReference());
        dataObject.put("cameraId", roadFlowVo.getCameraID());
        dataObject.put("tollgateId", roadFlowVo.getTollgateID());
        dataObject.put("id", roadFlowVo.getID());
        dataObject.put("laneNum", roadFlowVo.getLaneNum());
        dataObject.put("roadStatusInfoList", roadFlowVo.getList());
        dataObject.put("currentTime", roadFlowVo.getCurrentTime());
        String s = dataObject.toJSONString();
        EroadKafkaProducer.send(s, TopicEnum.getTopic(sn, TopicEnum.ServiceDataEnum.ROAD_FLOW));
    }

    //交通统计信息
    public void trafficFlow(TrafficFlowVo trafficFlowVo) {
        JSONObject dataObject = new JSONObject();
        dataObject.put("deviceId", sn);
        dataObject.put("reference", trafficFlowVo.getReference());
        dataObject.put("cameraId", trafficFlowVo.getCameraID());
        dataObject.put("tollgateId", trafficFlowVo.getTollgateID());
        dataObject.put("currentTime", trafficFlowVo.getCurrentTime());
        dataObject.put("period", trafficFlowVo.getPeriod());
        dataObject.put("id", trafficFlowVo.getID());
        dataObject.put("laneNum", trafficFlowVo.getLaneNum());
        dataObject.put("laneFlowInfoList", trafficFlowVo.getList());
        String s = dataObject.toJSONString();
        EroadKafkaProducer.send(s, TopicEnum.getTopic(sn, TopicEnum.ServiceDataEnum.TRAFFIC_FLOW));
    }

    //车辆排队信息
    public void vehicleQueue(VehicleQueueLenVo vehicleQueueLenVo) {
        JSONObject dataObject = new JSONObject();
        dataObject.put("deviceId", sn);
        dataObject.put("reference", vehicleQueueLenVo.getReference());
        dataObject.put("cameraId", vehicleQueueLenVo.getCameraID());
        dataObject.put("tollgateId", vehicleQueueLenVo.getTollgateID());
        dataObject.put("tollgateName", vehicleQueueLenVo.getTollgateName());
        dataObject.put("currentTime", vehicleQueueLenVo.getCurrentTime());
        dataObject.put("vehQueueLenInfo", vehicleQueueLenVo.getVehQueueLenInfo());
        String s = dataObject.toJSONString();
        EroadKafkaProducer.send(s, TopicEnum.getTopic(sn, TopicEnum.ServiceDataEnum.VEHICLE_QUEUE));
    }

    //过车数据
    public void passData(PassDataVo passDataVo) {
        JSONObject dataObject = new JSONObject();
        dataObject.put("deviceID", sn);
        dataObject.put("reference", passDataVo.getReference());
        dataObject.put("cameraId", passDataVo.getCameraID());
        dataObject.put("tollgateId", passDataVo.getTollgateID());
        dataObject.put("currentTime", passDataVo.getCurrentTime());
        dataObject.put("laneId", passDataVo.getLanelID());
        dataObject.put("coilId", passDataVo.getCoilID());
        dataObject.put("speed", passDataVo.getSpeed());
        dataObject.put("vehicleLength", passDataVo.getVehicleLength());
        dataObject.put("vehicleType", passDataVo.getVehicleType());
        dataObject.put("driveIntoTime", passDataVo.getDriveIntoTime());
        dataObject.put("presenceTime", passDataVo.getPressenceTime());
        String s = dataObject.toJSONString();
        EroadKafkaProducer.send(s, TopicEnum.getTopic(sn, TopicEnum.ServiceDataEnum.PASS_DATA));
    }

    //溢出事件上报
    public void overFlow(OverFlowVo overFlowVo) {
        JSONObject dataObject = new JSONObject();
        dataObject.put("deviceId", sn);
        dataObject.put("refernece", overFlowVo.getReference());
        dataObject.put("alarmType", overFlowVo.getAlarmType());
        dataObject.put("timeStamp", overFlowVo.getTimeStamp());
        dataObject.put("seq", overFlowVo.getSeq());
        dataObject.put("sourceId", overFlowVo.getSourceID());
        dataObject.put("sourceName", overFlowVo.getSourceName());
        dataObject.put("deviceId", overFlowVo.getDeviceID());
        dataObject.put("deviceCode", overFlowVo.getDeviceCode());
        dataObject.put("relatedId", overFlowVo.getRelatedID());
        dataObject.put("overFlowList", overFlowVo.getList());
        String s = dataObject.toJSONString();
        EroadKafkaProducer.send(s, TopicEnum.getTopic(sn, TopicEnum.ServiceDataEnum.OVER_FLOW));

    }

    //行人事件上报
    public void pedestrian(PedestrianVo pedestrianVo) {
        JSONObject object = new JSONObject();
        object.put("deviceId", sn);
        object.put("reference", pedestrianVo.getReference());
        object.put("alarmType", pedestrianVo.getAlarmType());
        object.put("timeStamp", pedestrianVo.getTimeStamp());
        object.put("seq", pedestrianVo.getSeq());
        object.put("sourceId", pedestrianVo.getSourceID());
        object.put("sourceName", pedestrianVo.getSourceName());
        object.put("deviceId", pedestrianVo.getDeviceID());
        object.put("deviceCode", pedestrianVo.getDeviceCode());
        object.put("relatedId", pedestrianVo.getRelatedID());
        object.put("pedestrainList", pedestrianVo.getList());
        String s = object.toJSONString();
        EroadKafkaProducer.send(s, TopicEnum.getTopic(sn, TopicEnum.ServiceDataEnum.PEDESTRIAN));
    }

    //非机动车事件上报
    public void nonmotorList(NonmotorListVo nonmotorListVo) {
        JSONObject object = new JSONObject();
        object.put("deviceId", sn);
        object.put("reference", nonmotorListVo.getReference());
        object.put("alarmType", nonmotorListVo.getAlarmType());
        object.put("timeStamp", nonmotorListVo.getTimeStamp());
        object.put("seq", nonmotorListVo.getSeq());
        object.put("sourceId", nonmotorListVo.getSourceID());
        object.put("sourceName", nonmotorListVo.getSourceName());
        object.put("deviceId", nonmotorListVo.getDeviceID());
        object.put("deviceCode", nonmotorListVo.getDeviceCode());
        object.put("relatedId", nonmotorListVo.getRelatedID());
        object.put("nonmotorList", nonmotorListVo.getList());
        String s = object.toJSONString();
        EroadKafkaProducer.send(s, TopicEnum.getTopic(sn, TopicEnum.ServiceDataEnum.NONMOTOR_LIST));
    }

    //停车事件上报
    public void parkingList(ParkingListVo parkingListVo) {
        JSONObject object = new JSONObject();
        object.put("deviceId", sn);
        object.put("reference", parkingListVo.getReference());
        object.put("alarmType", parkingListVo.getAlarmType());
        object.put("timeStamp", parkingListVo.getTimeStamp());
        object.put("seq", parkingListVo.getSeq());
        object.put("sourceId", parkingListVo.getSourceID());
        object.put("sourceName", parkingListVo.getSourceName());
        object.put("deviceId", parkingListVo.getDeviceID());
        object.put("deviceCode", parkingListVo.getDeviceCode());
        object.put("relatedId", parkingListVo.getRelatedID());
        object.put("parkingList", parkingListVo.getList());
        String s = object.toJSONString();
        EroadKafkaProducer.send(s, TopicEnum.getTopic(sn, TopicEnum.ServiceDataEnum.PARKING_LIST));
    }

    //超速事件上报
    public void overSpeedsList(OverSpeedsListVo overSpeedsListVo) {
        JSONObject object = new JSONObject();
        object.put("deviceId", sn);
        object.put("reference", overSpeedsListVo.getReference());
        object.put("alarmType", overSpeedsListVo.getAlarmType());
        object.put("timeStamp", overSpeedsListVo.getTimeStamp());
        object.put("seq", overSpeedsListVo.getSeq());
        object.put("sourceId", overSpeedsListVo.getSourceID());
        object.put("sourceName", overSpeedsListVo.getSourceName());
        object.put("deviceId", overSpeedsListVo.getDeviceID());
        object.put("deviceCode", overSpeedsListVo.getDeviceCode());
        object.put("relatedId", overSpeedsListVo.getRelatedID());
        object.put("overspeedsList", overSpeedsListVo.getList());
        String s = object.toJSONString();
        EroadKafkaProducer.send(s, TopicEnum.getTopic(sn, TopicEnum.ServiceDataEnum.OVERSPEEDS_LIST));
    }

    //低速事件上报
    public void lowSpeedsList(LowSpeedsListVo lowSpeedsListVo) {
        JSONObject object = new JSONObject();
        object.put("deviceId", sn);
        object.put("reference", lowSpeedsListVo.getReference());
        object.put("alarmType", lowSpeedsListVo.getAlarmType());
        object.put("timeStamp", lowSpeedsListVo.getTimeStamp());
        object.put("seq", lowSpeedsListVo.getSeq());
        object.put("sourceId", lowSpeedsListVo.getSourceID());
        object.put("sourceName", lowSpeedsListVo.getSourceName());
        object.put("deviceId", lowSpeedsListVo.getDeviceID());
        object.put("deviceCode", lowSpeedsListVo.getDeviceCode());
        object.put("relatedId", lowSpeedsListVo.getRelatedID());
        object.put("lowspeedsList", lowSpeedsListVo.getList());
        String s = object.toJSONString();
        EroadKafkaProducer.send(s, TopicEnum.getTopic(sn, TopicEnum.ServiceDataEnum.LOWSPEEDS_LIST));
    }

    //逆行事件上报
    public void retrogradeList(RetrogradeListVo retrogradeListVo) {
        JSONObject object = new JSONObject();
        object.put("deviceId", sn);
        object.put("reference", retrogradeListVo.getReference());
        object.put("alarmType", retrogradeListVo.getAlarmType());
        object.put("timeStamp", retrogradeListVo.getTimeStamp());
        object.put("seq", retrogradeListVo.getSeq());
        object.put("sourceId", retrogradeListVo.getSourceID());
        object.put("sourceName", retrogradeListVo.getSourceName());
        object.put("deviceId", retrogradeListVo.getDeviceID());
        object.put("deviceCode", retrogradeListVo.getDeviceCode());
        object.put("relatedId", retrogradeListVo.getRelatedID());
        object.put("retrogradeList", retrogradeListVo.getList());
        String s = object.toJSONString();
        EroadKafkaProducer.send(s, TopicEnum.getTopic(sn, TopicEnum.ServiceDataEnum.RETROGRADE_LIST));
    }

    //拥堵事件上报
    public void congestionList(CongestionListVo congestionListVo) {
        JSONObject object = new JSONObject();
        object.put("deviceId", sn);
        object.put("reference", congestionListVo.getReference());
        object.put("alarmType", congestionListVo.getAlarmType());
        object.put("timeStamp", congestionListVo.getTimeStamp());
        object.put("seq", congestionListVo.getSeq());
        object.put("sourceId", congestionListVo.getSourceID());
        object.put("sourceName", congestionListVo.getSourceName());
        object.put("deviceId", congestionListVo.getDeviceID());
        object.put("deviceCode", congestionListVo.getDeviceCode());
        object.put("relatedId", congestionListVo.getRelatedID());
        object.put("congestionList", congestionListVo.getList());
        String s = object.toJSONString();
        EroadKafkaProducer.send(s, TopicEnum.getTopic(sn, TopicEnum.ServiceDataEnum.CONGESTION_LIST));
    }

    //变道事件上报
    public void laneChangeList(LaneChangeListVo laneChangeListVo) {
        JSONObject object = new JSONObject();
        object.put("deviceId", sn);
        object.put("reference", laneChangeListVo.getReference());
        object.put("alarmType", laneChangeListVo.getAlarmType());
        object.put("timeStamp", laneChangeListVo.getTimeStamp());
        object.put("seq", laneChangeListVo.getSeq());
        object.put("sourceId", laneChangeListVo.getSourceID());
        object.put("sourceName", laneChangeListVo.getSourceName());
        object.put("deviceId", laneChangeListVo.getDeviceID());
        object.put("deviceCode", laneChangeListVo.getDeviceCode());
        object.put("relatedId", laneChangeListVo.getRelatedID());
        object.put("laneChangeList", laneChangeListVo.getList());
        String s = object.toJSONString();
        EroadKafkaProducer.send(s, TopicEnum.getTopic(sn, TopicEnum.ServiceDataEnum.LANE_CHANGE_LIST));
    }

    //占用紧急车道事件上报
    public void occupancyEmergenctList(OccupancyEmergenctListVo occupancyEmergenctListVo) {
        JSONObject object = new JSONObject();
        object.put("deviceId", sn);
        object.put("reference", occupancyEmergenctListVo.getReference());
        object.put("alarmType", occupancyEmergenctListVo.getAlarmType());
        object.put("timeStamp", occupancyEmergenctListVo.getTimeStamp());
        object.put("seq", occupancyEmergenctListVo.getSeq());
        object.put("sourceId", occupancyEmergenctListVo.getSourceID());
        object.put("sourceName", occupancyEmergenctListVo.getSourceName());
        object.put("deviceId", occupancyEmergenctListVo.getDeviceID());
        object.put("deviceCode", occupancyEmergenctListVo.getDeviceCode());
        object.put("relatedId", occupancyEmergenctListVo.getRelatedID());
        object.put("occupyEmergenctList", occupancyEmergenctListVo.getList());
        String s = object.toJSONString();
        EroadKafkaProducer.send(s, TopicEnum.getTopic(sn, TopicEnum.ServiceDataEnum.OCCUPANCY_EMERGENCT_LIST));
    }

    //区域入侵事件上报
    public void restrictedAreaLiat(RestrictedAreaListVo restrictedAreaListVo) {
        JSONObject object = new JSONObject();
        object.put("deviceId", sn);
        object.put("reference", restrictedAreaListVo.getReference());
        object.put("alarmType", restrictedAreaListVo.getAlarmType());
        object.put("timeStamp", restrictedAreaListVo.getTimeStamp());
        object.put("seq", restrictedAreaListVo.getSeq());
        object.put("sourceId", restrictedAreaListVo.getSourceID());
        object.put("sourceName", restrictedAreaListVo.getSourceName());
        object.put("deviceId", restrictedAreaListVo.getDeviceID());
        object.put("deviceCode", restrictedAreaListVo.getDeviceCode());
        object.put("relatedId", restrictedAreaListVo.getRelatedID());
        object.put("restrictedAreaList", restrictedAreaListVo.getList());
        String s = object.toJSONString();
        EroadKafkaProducer.send(s, TopicEnum.getTopic(sn, TopicEnum.ServiceDataEnum.RESTRICTED_AREA_LIST));
    }
}
