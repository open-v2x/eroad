package cn.eroad.rad.service;

import cn.eroad.core.utils.StringUtil;
import cn.eroad.rad.model.*;
import cn.eroad.rad.model.dmsModel.DeviceStatus;
import cn.eroad.rad.model.dmsModel.DmsRegister;
import cn.eroad.rad.model.response.RespondToCache;
import cn.eroad.device.operate.DeviceCache;
import cn.eroad.rabbitmq.service.impl.RabbitOperator;
import cn.eroad.rad.util.ByteUtil;
import cn.eroad.redis.utils.RedisUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import cn.eroad.rad.config.AppConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author mrChen
 * @date 2022/7/6 16:20
 */
@Slf4j
public abstract class MessageCache {


    private static final String DAY_FORMATE = "yyyyMMdd";
    private static final String DATE_FORMATE = DAY_FORMATE + "HHmmss";
    private static Map<String, String> hostNameMapCurrentRange = new ConcurrentHashMap<>(1);


    protected String FLAME_HEADER = "C0";
    protected String FLAME_END = "C0";

    @Autowired
    RabbitOperator rabbitOperator;

    static Integer seqNum = 111;

    @Autowired
    RedisUtil redisUtil;

    // 网络参数查询应答
    protected JSONObject objectMmwNetworkData(String msg) {
        JSONObject dataObject = new JSONObject();
        // 至少10个字节
        if (msg.length() < 188) {
            log.error("原始报文错误，网络参数格式有误，网络参数原始数据为{}", msg);
            return dataObject;
        }
        Network network = new Network();
        //IPV4网关
        String gateWayIPV4 = getIp(msg.substring(0, 8));
        network.setIpv4Gateway(gateWayIPV4);
        // IPV4子网掩码
        String subnetMaskIPV4 = getIp(msg.substring(8, 16));
        network.setIpv4Mask(subnetMaskIPV4);
        // IPV4地址
        String addressIPV4 = getIp(msg.substring(16, 24));
        network.setIpv4Address(addressIPV4);
        //IPV6网关
        String gateWayIPV6 = getIpV6(msg.substring(24, 56));
        network.setIpv6Gateway(gateWayIPV6);
        // IPV6子网掩码
        String subnetMaskIPV6 = getIpV6(msg.substring(56, 88));
        network.setIpv6Mask(subnetMaskIPV6);
        String addressLLA = getIpV6(msg.substring(88, 120));
        network.setIpv6LlaAddress(addressLLA);
        String addressGUA = getIpV6(msg.substring(120, 152));
        network.setIpv6GuaAddress(addressGUA);
        // 本地端口号
        String localPortStr = msg.substring(152, 156);
        localPortStr = fromBigToSmall(localPortStr);
        Integer localPort = Integer.valueOf(localPortStr, 16);
        network.setLocalPort(localPort);
        // 目标端口号
        String targetPortStr = msg.substring(156, 160);
        targetPortStr = fromBigToSmall(targetPortStr);
        Integer targetPort = Integer.valueOf(targetPortStr, 16);
        network.setTargetPort(targetPort);

        // 目标端口
        String targetIp = getIp(msg.substring(160, 168));
        network.setTargetIp(targetIp);

        // 点云数据端口号
        String cloudDataPortStr = msg.substring(168, 172);
        cloudDataPortStr = fromBigToSmall(cloudDataPortStr);
        Integer cloudDataPort = Integer.valueOf(cloudDataPortStr, 16);
        network.setPcUpPort(cloudDataPort);

        // 心跳周期 心跳间隔，单位秒
        String heartbeatCycleStr = msg.substring(172, 176);
        heartbeatCycleStr = fromBigToSmall(heartbeatCycleStr);
        Integer heartbeatCycle = Integer.valueOf(heartbeatCycleStr, 16);
        network.setHeartbeatCycle(heartbeatCycle);
        // MAC地址
        String mac = getMac(msg.substring(176, 188));
        network.setMac(mac);
        dataObject = JSON.parseObject(JSON.toJSONString(network));
        log.info("网络参数{}", dataObject);
        return dataObject;
    }

    // 网络参数查询应答
    protected JSONObject objectMmwConfData(String msg) {
        JSONObject dataObject = new JSONObject();
        // 至少10个字节
        if (msg.length() < 12) {
            log.error("原始报文错误，配置参数格式有误，配置参数原始数据为{}", msg);
            return dataObject;
        }
        ConfData confData = new ConfData();

        // 交通目标实时轨迹信息
        String targetTrajectorytStr = msg.substring(0, 2);
        Integer targetTrajectory = Integer.valueOf(targetTrajectorytStr, 16);
        confData.setTraceFre(targetTrajectory);
        // 过车信息
        String passingInformationStr = msg.substring(2, 4);
        Integer passingInformation = Integer.valueOf(passingInformationStr, 16);
        confData.setPassFre(passingInformation);
        // 交通状态信息
        String trafficStatusInformationStr = msg.substring(4, 6);
        Integer trafficStatusInformation = Integer.valueOf(trafficStatusInformationStr, 16);
        confData.setTrafficStatusFre(trafficStatusInformation);
        // 交通流信息
        String trafficFlowStr = msg.substring(6, 10);
        trafficFlowStr = fromBigToSmall(trafficFlowStr);
        Integer trafficFlow = Integer.valueOf(trafficFlowStr, 16);
        confData.setFlowFre(trafficFlow);
        // 交通流信息
        String trafficEventStr = msg.substring(10, 12);
        Integer trafficEvent = Integer.valueOf(trafficEventStr, 16);
        confData.setEventUp(trafficEvent);
        dataObject = JSON.parseObject(JSON.toJSONString(confData));
        log.info("配置信息{}", dataObject);
        return dataObject;
    }

    // 工作状态查询应答和主动上报
    protected JSONObject objectMmwWorkingStatusData(String msg, String sn) {
        JSONObject dataObject = new JSONObject();
        // 至少10个字节
        if (msg.length() < 8) {
            log.error("原始报文错误，工作状态格式有误，工作状态原始数据为{}", msg);
            return dataObject;
        }
        WorkingStatus workingStatus = new WorkingStatus();

        // 电压状态
        String voltageStatusStr = msg.substring(0, 2);
        Integer voltageStatus = Integer.valueOf(voltageStatusStr, 16);
        workingStatus.setVoltage(voltageStatus + "");
        // 温度状态
        String temperatureStatusStr = msg.substring(2, 4);
        Integer temperatureStatus = Integer.valueOf(temperatureStatusStr, 16);
        workingStatus.setTemperature(temperatureStatus + "");
        // 湿度状态
        String humidityStatusStr = msg.substring(4, 6);
        Integer humidityStatus = Integer.valueOf(humidityStatusStr, 16);
        workingStatus.setHumidity(humidityStatus + "");
        // 设备状态
        String deviceStatusStr = msg.substring(6, 8);
        Integer deviceStatus = Integer.valueOf(deviceStatusStr, 16);
        workingStatus.setStatus(deviceStatus);

        DeviceStatus deviceStatus1 = new DeviceStatus();
        deviceStatus1.setWorkStatus(workingStatus);
        if (StringUtil.isNotEmpty(sn)) {
            deviceStatus1.setSn(sn.trim());
            deviceStatus1.setId("");
            SimpleDateFormat noticeDateFormate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            deviceStatus1.setUpdateTime(noticeDateFormate.format(new Date()));
        }
        dataObject = JSON.parseObject(JSON.toJSONString(deviceStatus1));
        return dataObject;
    }


    // 点云数据

    protected JSONObject objectPointCloudData(String msg) {

        JSONObject dataObject = new JSONObject();
        // 至少10个字节
        if (msg.length() < 20) {
            log.error("点云数据格式有误，点云数据原始数据为{}", msg);
            return dataObject;
        }

        String timeStamp = getTime(msg);
        String numberStr = msg.substring(16, 20);
        numberStr = fromBigToSmall(numberStr);
        Integer number = Integer.valueOf(numberStr, 16);
        msg = msg.substring(20, msg.length());
        List<CloudData> cloudDatas = new ArrayList<>();
        if (number == 0) {
            log.error("此次点云数据个数  = 0");
            dataObject.put("timeStamp", timeStamp);
            dataObject.put("number", number);
            dataObject.put("cloudDatas", cloudDatas);
            return dataObject;
        }
        if (msg.length() < (number * 26)) {
            log.error("点云数据格式有误，点云个数 = {}，点云数据原始数据为{}", number, msg);
            return dataObject;
        }


        for (int i = 0; i < number; i++) {

            CloudData cloudData = new CloudData();
            String targetIdStr = msg.substring(0, 4);
            targetIdStr = fromBigToSmall(targetIdStr);
            Integer targetId = Integer.valueOf(targetIdStr, 16);
            cloudData.setTargetId(targetId);
            String lateralDistanceStr = msg.substring(4, 8);
            lateralDistanceStr = fromBigToSmall(lateralDistanceStr);
            short lateralDistance = fromHexToShortWithMarkBy2(lateralDistanceStr);
            cloudData.setLateralDistance(lateralDistance);
            String verticalDistanceStr = msg.substring(8, 12);
            verticalDistanceStr = fromBigToSmall(verticalDistanceStr);
            short verticalDistance = fromHexToShortWithMarkBy2(verticalDistanceStr);
            cloudData.setVerticalDistance(verticalDistance);

            String lateralSpeedStr = msg.substring(12, 16);
            lateralSpeedStr = fromBigToSmall(lateralSpeedStr);
            Double lateralSpeed = fromHexToShortWithMarkBy2(lateralSpeedStr) * 0.1;
            cloudData.setLateralSpeed(lateralSpeed);

            String LongitudinalSpeedStr = msg.substring(16, 20);
            LongitudinalSpeedStr = fromBigToSmall(LongitudinalSpeedStr);
            Double LongitudinalSpeed = fromHexToShortWithMarkBy2(LongitudinalSpeedStr) * 0.1;
            cloudData.setLongitudinalSpeed(LongitudinalSpeed);

            String angleStr = msg.substring(20, 24);
            angleStr = fromBigToSmall(angleStr);
            Double angle = fromHexToShortWithMarkBy2(angleStr) * 0.01;
            cloudData.setAngle(angle);

            String SNRStr = msg.substring(24, 26);
            Integer snr = Integer.valueOf(SNRStr, 16);
            cloudData.setSnr(snr);
            msg = msg.substring(26, msg.length());
            cloudDatas.add(cloudData);
        }
        dataObject.put("timeStamp", timeStamp);
        dataObject.put("number", number);
        dataObject.put("cloudDatas", cloudDatas);


        return dataObject;
    }

    // 车轨迹数据上报
    public JSONObject objectTargetTrajectoryData(String msg) {
        JSONObject dataObject = new JSONObject();
        // 至少10个字节
        if (msg.length() < 20) {
            log.error("车轨迹数据格式有误，车轨迹数据原始数据为{}", msg);
            return dataObject;
        }
        String timeStamp = getTime(msg);
        String numberStr = msg.substring(16, 20);
        numberStr = fromBigToSmall(numberStr);
        Integer number = Integer.valueOf(numberStr, 16);
        msg = msg.substring(20, msg.length());
        List<Target> targets = new ArrayList<>();
        if (number == 0) {
            log.error("此次车轨迹数据交通目标数 = 0");
            dataObject.put("timeStamp", timeStamp);
            dataObject.put("number", number);
            dataObject.put("targets", targets);
            return dataObject;
        }

        if (msg.length() < (number * 88)) {

            log.error("车轨迹数据格式有误，交通目标数 = {}，车轨迹数据原始数据 = {}", number, msg);
            dataObject.put("timeStamp", timeStamp);
            dataObject.put("number", number);
            return dataObject;

        }
        for (int i = 0; i < number; i++) {
            Target target = new Target();
            String targetIdStr = msg.substring(0, 4);
            targetIdStr = fromBigToSmall(targetIdStr);
            Integer targetId = Integer.valueOf(targetIdStr, 16);
            target.setTargetId(targetId);
            // 目标类型
            String targetTypeStr = msg.substring(4, 6);
            Integer targetType = Integer.valueOf(targetTypeStr, 16);
            target.setTargetType(targetType);
            // 目标长度
            String targetLengthStr = msg.substring(6, 8);
            Integer targetLengthInt = Integer.valueOf(targetLengthStr, 16);
            Double targetLength = 0.0;

            if (targetLengthInt != 255) {
                targetLength = targetLengthInt * 0.1;
                //保留1位小数
                targetLength = new BigDecimal(targetLength).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
            }
            target.setTargetLength(targetLength);
            // 目标宽度
            String targetWidthStr = msg.substring(8, 10);
            Integer targetWidthInt = Integer.valueOf(targetWidthStr, 16);
            Double targetWidth = 0.0;
            if (targetWidthInt != 255) {
                targetWidth = targetWidthInt * 0.1;
                targetWidth = new BigDecimal(targetWidth).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
            }
            target.setTargetWidth(targetWidth);

            // 目标高度
            String targetHeightStr = msg.substring(10, 12);
            Integer targetHeightInt = Integer.valueOf(targetHeightStr, 16);
            Double targetHeight = 0.0;
            if (targetHeightInt != 255) {
                targetHeight = targetHeightInt * 0.1;
                targetHeight = new BigDecimal(targetHeight).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
            }
            target.setTargetHeight(targetHeight);
            // 经度
            String longitudeStr = msg.substring(12, 28);
            longitudeStr = fromBigToSmall(longitudeStr);
            Double longitude = hexStrToDouble(longitudeStr, msg);
            target.setLongitude(longitude);
            // 纬度
            String latitudeStr = msg.substring(28, 44);
            latitudeStr = fromBigToSmall(latitudeStr);
            Double latitude = hexStrToDouble(latitudeStr, msg);
            target.setLatitude(latitude);
            // 高度
            String heightStr = msg.substring(44, 52);
            heightStr = fromBigToSmall(heightStr);
            Float height = hexStrToFloat(heightStr);
            target.setHeight(height);
            // 车道编号
            String laneNoStr = msg.substring(52, 54);
            Integer laneNo = Integer.valueOf(laneNoStr, 16);
            target.setLaneNo(laneNo);
            // 航向角
            String headingAngleStr = msg.substring(54, 62);
            headingAngleStr = fromBigToSmall(headingAngleStr);
            Float headingAngle = hexStrToFloat(headingAngleStr);
            target.setHeadingAngle(headingAngle);
            // 速度
            String speedStr = msg.substring(62, 70);
            speedStr = fromBigToSmall(speedStr);
            Float speed = hexStrToFloatWithMark(speedStr);
            target.setSpeed(speed);
            // 加速度
            String accelerationStr = msg.substring(70, 78);
            accelerationStr = fromBigToSmall(accelerationStr);
            Float acceleration = hexStrToFloatWithMark(accelerationStr);
            target.setAcceleration(acceleration);
            // RCS
            String rcsStr = msg.substring(78, 86);
            rcsStr = fromBigToSmall(rcsStr);
            Long rcs = Long.valueOf(rcsStr, 16);

            target.setRcs(rcs);
            // 置信度,目标存在概率，单位：%
            String confidenceLevelStr = msg.substring(86, 88);
            Integer confidenceLevel = Integer.valueOf(confidenceLevelStr, 16);
            target.setConfidenceLevel(confidenceLevel);
            targets.add(target);
            msg = msg.substring(88, msg.length());
        }

        dataObject.put("timeStamp", timeStamp);
        dataObject.put("number", number);
        dataObject.put("targets", targets);
        return dataObject;
    }

    // 3.2.2过车信息主动上传
    protected JSONObject objectDetectionSectionPassingInformation(String msg) {
        log.info("过车原始信息：" + msg);
        JSONObject dataObject = new JSONObject();
        // 至少10个字节
        if (msg.length() < 20) {

            return dataObject;
        }
        String timeStamp = getTime(msg);
        String numberStr = msg.substring(16, 18);
        int number = Integer.valueOf(numberStr, 16);
        msg = msg.substring(18);
        List<PassingInformation> passingImformations = new ArrayList<>();
        if (number == 0) {

            dataObject.put("timeStamp", timeStamp);
            dataObject.put("number", number);
            dataObject.put("passingImformations", passingImformations);
            return dataObject;
        }
        if (msg.length() < (number * 28)) {

            dataObject.put("timeStamp", timeStamp);
            dataObject.put("number", number);
            return dataObject;

        }

        for (int i = 0; i < number; i++) {
            PassingInformation passingImformation = new PassingInformation();
            // 标识哪一路检测通道
            String aisleIdStr = msg.substring(0, 2);
            Integer aisleId = Integer.valueOf(aisleIdStr, 16);
            passingImformation.setAisleId(aisleId);
            // 距停止线距离 测量线距停止线的距离，单位：米
            String distanceToStopLineStr = msg.substring(2, 4);
            Integer distanceToStopLine = Integer.valueOf(distanceToStopLineStr, 16);
            passingImformation.setDistanceToStopLine(distanceToStopLine);
            // 车辆类型 1：行人，2：非机动车，3：小型车，4：中型车，5：大型车
            String vehicleTypeStr = msg.substring(4, 6);
            Integer vehicleType = Integer.valueOf(vehicleTypeStr, 16);
            passingImformation.setVehicleType(vehicleType);
            // 行驶方向 0：来向，1：去向
            String directionStr = msg.substring(6, 8);
            Integer direction = Integer.valueOf(directionStr, 16);
            passingImformation.setDirection(direction);
            // 存在状态 0：驶入，1：驶离
            String presenceStateStr = msg.substring(8, 10);
            Integer existenceState = Integer.valueOf(presenceStateStr, 16);
            passingImformation.setExistenceState(existenceState);

            // 车辆速度
            String speedStr = msg.substring(10, 12);
            Integer speed = Integer.valueOf(speedStr, 16);
            passingImformation.setSpeed(speed);
            // 车辆在断面上存在的时间，单位：毫秒
            String residenceTimeStr = msg.substring(12, 20);
            residenceTimeStr = fromBigToSmall(residenceTimeStr);
            Integer existenceTime = Integer.valueOf(residenceTimeStr, 16);
            passingImformation.setExistenceTime(existenceTime);
            // 目标ID
            String targetIdStr = msg.substring(20, 24);
            targetIdStr = fromBigToSmall(targetIdStr);
            Integer targetId = Integer.valueOf(targetIdStr, 16);
            passingImformation.setTargetId(targetId);
            // 是否开启双向检测
            String ifBothWayDetectionStr = msg.substring(24, 26);
            Integer ifBothWayDetection = Integer.valueOf(ifBothWayDetectionStr, 16);
            passingImformation.setIfBothWayDetection(ifBothWayDetection);
            // 车道方向
            String directionLaneStr = msg.substring(26, 28);
            Integer directionLane = Integer.valueOf(directionLaneStr, 16);
            passingImformation.setDirectionLane(directionLane);
            passingImformations.add(passingImformation);
            msg = msg.substring(28);
        }

        dataObject.put("timeStamp", timeStamp);
        dataObject.put("number", number);
        dataObject.put("passingImformations", passingImformations);

        return dataObject;
    }


    // 交通状态信息
    protected JSONObject objectTrafficStatusInformation(String msg) {
        JSONObject dataObject = new JSONObject();
        // 至少10个字节
        if (msg.length() < 20) {
            log.error("交通状态信息格式有误，交通状态信息原始数据为{}", msg);
            return dataObject;
        }

        String timeStamp = getTime(msg);
        String numberStr = msg.substring(16, 18);
        int number = Integer.valueOf(numberStr, 16);
        msg = msg.substring(18);
        List<TrafficStatusInformation> trafficStatusInformations = new ArrayList<>();
        if (number == 0) {
            log.error("此次交通状态信息检测车道数 = 0");
            dataObject.put("timeStamp", timeStamp);
            dataObject.put("number", number);
            dataObject.put("trafficStatusInformations", trafficStatusInformations);
            return dataObject;
        }
        if (msg.length() < (number * 32)) {
            log.error("交通状态信息格式有误，检测车道数 = {}, 交通状态信息原始数据为{}", number, msg);
            dataObject.put("timeStamp", timeStamp);
            dataObject.put("number", number);
            return dataObject;

        }

        for (int i = 0; i < number; i++) {
            TrafficStatusInformation trafficStatusInformation = new TrafficStatusInformation();
            // 标识哪一路检测通道
            String aisleIdStr = msg.substring(0, 2);
            Integer aisleId = Integer.valueOf(aisleIdStr, 16);
            trafficStatusInformation.setAisleId(aisleId);

            // 排队长度
            String queueLengthStr = msg.substring(2, 4);
            Integer queueLength = Integer.valueOf(queueLengthStr, 16);
            trafficStatusInformation.setQueueLength(queueLength);
            // 排队数量
            String queueVehicleNumberStr = msg.substring(4, 6);
            Integer queueVehicleNumber = Integer.valueOf(queueVehicleNumberStr, 16);
            trafficStatusInformation.setQueueVehicleNumber(queueVehicleNumber);
            msg = msg.substring(32);
            trafficStatusInformations.add(trafficStatusInformation);
        }
        dataObject.put("timeStamp", timeStamp);
        dataObject.put("number", number);
        dataObject.put("trafficStatusInformations", trafficStatusInformations);
        return dataObject;
    }

    // 交通流信息
    protected JSONObject objectTrafficFlowInformation(String msg) {
        JSONObject dataObject = new JSONObject();
        // 至少10个字节
        if (msg.length() < 20) {
            log.error("交通流信息格式有误，交通流原始数据为{}", msg);
            return dataObject;
        }

        String timeStamp = getTime(msg);
        String numberStr = msg.substring(16, 18);
        int number = Integer.valueOf(numberStr, 16);
        msg = msg.substring(18);
        if (number == 0) {
            log.error("此次交通流检测通道数 = 0");
            dataObject.put("timeStamp", timeStamp);
            dataObject.put("number", number);
            dataObject.put("trafficFlows", null);
            dataObject.put("steeringRatio", null);
            return dataObject;
        }

        if (msg.length() < (number * 30 + 2)) {
            log.error("交通流格式有误，检测通道数 = {}， 交通流原始数据为{}", number, msg);
            dataObject.put("timeStamp", timeStamp);
            dataObject.put("number", number);
            return dataObject;

        }

        List<TrafficFlow> trafficFlows = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            TrafficFlow trafficFlow = new TrafficFlow();
            // 标识哪一路检测通道
            String aisleIdStr = msg.substring(0, 2);
            Integer aisleId = Integer.valueOf(aisleIdStr, 16);
            trafficFlow.setAisleId(aisleId);

            // A类车总流量
            String totalFlowAStr = msg.substring(2, 6);
            totalFlowAStr = fromBigToSmall(totalFlowAStr);
            Integer totalFlowA = Integer.valueOf(totalFlowAStr, 16);
            trafficFlow.setTotalFlowA(totalFlowA);
            // B类车总流量
            String totalFlowBStr = msg.substring(6, 10);
            totalFlowBStr = fromBigToSmall(totalFlowBStr);
            Integer totalFlowB = Integer.valueOf(totalFlowBStr, 16);
            trafficFlow.setTotalFlowB(totalFlowB);
            // C类车总流量
            String totalFlowCStr = msg.substring(10, 14);
            totalFlowCStr = fromBigToSmall(totalFlowCStr);
            Integer totalFlowC = Integer.valueOf(totalFlowCStr, 16);
            trafficFlow.setTotalFlowC(totalFlowC);
            // 平均时间占有率
            String averageTimeShareStr = msg.substring(14, 16);
            Integer averageTimeShare = Integer.valueOf(averageTimeShareStr, 16);
            trafficFlow.setAverageTimeShare(averageTimeShare);
            // 平均车辆速度
            String averageSpeedStr = msg.substring(16, 18);
            Integer averageSpeed = Integer.valueOf(averageSpeedStr, 16);
            trafficFlow.setAverageSpeed(averageSpeed);
            // 平均车辆长度
            String averageLengthStr = msg.substring(18, 20);
            Integer averageLength = Integer.valueOf(averageLengthStr, 16);
            trafficFlow.setAverageLength(averageLength);
            // 平均车辆长度
            String averageHeadwayDistanceStr = msg.substring(20, 22);
            Integer averageHeadwayDistance = Integer.valueOf(averageHeadwayDistanceStr, 16);
            trafficFlow.setAverageHeadwayDistance(averageHeadwayDistance);
            trafficFlows.add(trafficFlow);
            msg = msg.substring(30);
        }

        // 转向比
        String steeringRatioStr = msg.substring(0, 2);
        Integer steeringRatio = Integer.valueOf(steeringRatioStr, 16);
        // 转向比为0时，没有后续数据
        if (steeringRatio == 1) {
            if (msg.length() < 14) {
                log.error("交通流格式有误，转向比不为零时，没有转向数据");
                return dataObject;
            }
            // 右转流量
            String flowRightStr = msg.substring(2, 6);
            flowRightStr = fromBigToSmall(flowRightStr);
            Integer flowRight = Integer.valueOf(flowRightStr, 16);
            // 直行流量
            String flowStraightStr = msg.substring(6, 10);
            flowStraightStr = fromBigToSmall(flowStraightStr);
            Integer flowStraight = Integer.valueOf(flowStraightStr, 16);

            // 左转流量
            String flowLeftStr = msg.substring(10, 14);
            flowLeftStr = fromBigToSmall(flowLeftStr);
            Integer flowLeft = Integer.valueOf(flowLeftStr, 16);
            dataObject.put("flowRight", flowRight);
            dataObject.put("flowStraight", flowStraight);
            dataObject.put("flowLeft", flowLeft);
        }
        dataObject.put("timeStamp", timeStamp);
        dataObject.put("number", number);
        dataObject.put("trafficFlows", trafficFlows);
        dataObject.put("steeringRatio", steeringRatio);

        return dataObject;
    }

    // 异常事件
    protected JSONObject objectAbnormalEventInformation(String msg) {
        JSONObject dataObject = new JSONObject();
        // 至少10个字节
        if (msg.length() < 70) {
            log.error("原始报文错误，异常事件信息格式有误，异常事件原始数据为{}", msg);
            return dataObject;
        }

        String timeStamp = getTime(msg);
        // 经度
        String longitudeStr = msg.substring(16, 32);
        longitudeStr = fromBigToSmall(longitudeStr);
        Double longitude = hexStrToDouble(longitudeStr, msg);
        // 纬度
        String latitudeStr = msg.substring(32, 48);
        latitudeStr = fromBigToSmall(latitudeStr);
        Double latitude = hexStrToDouble(latitudeStr, msg);
        // 高度
        String heightStr = msg.substring(48, 56);
        heightStr = fromBigToSmall(heightStr);
        Float height = hexStrToFloat(heightStr);

        /* 事件类型
        1.停车事件
        2.变道事件
        3.逆行事件
        4.低速行驶事件
        5.超高速事件
        6.未保持安全车距事件
        7.占用应急车道事件
        8.缓行事件
        9.拥堵事件
        10.排队超限事件
        11.交叉口溢出事件*/
        String eventTypeStr = msg.substring(56, 58);
        Integer eventType = Integer.valueOf(eventTypeStr, 16);
        // 所在车道
        String laneStr = msg.substring(58, 60);
        Integer lane = Integer.valueOf(laneStr, 16);
        // 影响范围
        String scopeStr = msg.substring(60, 62);
        Integer scope = Integer.valueOf(scopeStr, 16);
        //事件ID
        String eventIdStr = msg.substring(62, 66);
        eventIdStr = fromBigToSmall(eventIdStr);
        Integer eventId = Integer.valueOf(eventIdStr, 16);
        //目标ID
        String targetIdStr = msg.substring(66, 70);
        targetIdStr = fromBigToSmall(targetIdStr);
        Integer targetId = Integer.valueOf(targetIdStr, 16);

        dataObject.put("timeStamp", timeStamp);
        dataObject.put("longitude", longitude);
        dataObject.put("latitude", latitude);
        dataObject.put("height", height);
        dataObject.put("eventType", eventType);
        dataObject.put("lane", lane);
        dataObject.put("scope", scope);
        dataObject.put("eventId", eventId);
        dataObject.put("targetId", targetId);
        return dataObject;
    }

    // 注册信息
    protected DmsRegister objectRegisterData(String msg, String ip) {
        log.info("收到注册信息, id = {}", ip);
        if (msg.length() < 348) {
            log.error("注册信息格式有误，注册信息原始数据为{}", msg);
            return null;
        }
        Resigster resigster = new Resigster();
        // 设备序列号，原始数据
        String deviceSnStr = msg.substring(0, 40);
        String deviceSn = ByteUtil.decodeHexStr(deviceSnStr);
        resigster.setSn(DeviceCache.getSnByIpFromMap(ip));
        redisUtil.set("radSn" + deviceSn.trim(), ip);
        // 设备序列号
        String vendorStr = msg.substring(40, 80);
        String vendor = ByteUtil.decodeHexStr(vendorStr);
        resigster.setVender(vendor.trim());
        // 设备型号
        String deviceModelStr = msg.substring(80, 120);
        String deviceModel = ByteUtil.decodeHexStr(deviceModelStr);
        resigster.setCtlVer(deviceModel.trim());
        // 经度
        String longitudeStr = msg.substring(120, 136);
        longitudeStr = fromBigToSmall(longitudeStr);
        Double longitude = hexStrToDouble(longitudeStr, msg);
        resigster.setLongitude(longitude);
        // 纬度
        String latitudeStr = msg.substring(136, 152);
        latitudeStr = fromBigToSmall(latitudeStr);
        Double latitude = hexStrToDouble(latitudeStr, msg);
        resigster.setLatitude(latitude);
        // 高度
        String heightStr = msg.substring(152, 160);
        heightStr = fromBigToSmall(heightStr);
        Float height = hexStrToFloat(heightStr);
        resigster.setAltitude(height);
        //IPV4网关
        String gateWayIPV4 = getIp(msg.substring(160, 168));
        resigster.setIpv4Gateway(gateWayIPV4);
        // IPV4子网掩码
        String subnetMaskIPV4 = getIp(msg.substring(168, 176));
        resigster.setIpv4Mask(subnetMaskIPV4);
        // IPV4地址
        String addressIPV4 = getIp(msg.substring(176, 184));
        resigster.setIpv4Address(addressIPV4);
        // 目标ip地址
        String targetIP = getIp(msg.substring(184, 192));
        resigster.setTargetIp(targetIP);

        //IPV6网关
        String gateWayIPV6 = getIpV6(msg.substring(192, 224));
        resigster.setIpv6Gateway(gateWayIPV6);
        // IPV6子网掩码
        String subnetMaskIPV6 = getIpV6(msg.substring(224, 256));
        resigster.setIpv6Mask(subnetMaskIPV6);
        // IPV6-LLA地址
        String addressLLA = getIpV6(msg.substring(256, 288));
        resigster.setIpv6LlaAddress(addressLLA);
        // IPV6-LLA地址
        String addressGUA = getIpV6(msg.substring(288, 320));
        resigster.setIpv6GuaAddress(addressGUA);
        // 本地端口号
        String localPortStr = msg.substring(320, 324);
        localPortStr = fromBigToSmall(localPortStr);
        Integer localPort = Integer.valueOf(localPortStr, 16);
        resigster.setLocalPort(localPort);
        // 目标端口号
        String targetPortStr = msg.substring(324, 328);
        targetPortStr = fromBigToSmall(targetPortStr);
        Integer targetPort = Integer.valueOf(targetPortStr, 16);
        resigster.setTargetPort(targetPort);
        // 点云数据端口号
        String cloudDataPortStr = msg.substring(328, 332);
        cloudDataPortStr = fromBigToSmall(cloudDataPortStr);
        Integer cloudDataPort = Integer.valueOf(cloudDataPortStr, 16);
        resigster.setPcUpPort(cloudDataPort);
        // 心跳周期 心跳间隔，单位秒
        String heartbeatCycleStr = msg.substring(332, 336);
        heartbeatCycleStr = fromBigToSmall(heartbeatCycleStr);
        Integer heartbeatCycle = Integer.valueOf(heartbeatCycleStr, 16);
        resigster.setHeartbeatCycle(heartbeatCycle);
        // MAC地址
        String mac = getMac(msg.substring(336, 348));
        resigster.setMac(mac);

        DmsRegister dmsRegister = null;
        if (StringUtil.isNotEmpty(deviceSn)) {
            dmsRegister = new DmsRegister();
            dmsRegister.setId(getSeqNum());
            dmsRegister.setAltitude(height);
            dmsRegister.setCtlVer(deviceModel.trim());
            dmsRegister.setLatitude(latitude);
            dmsRegister.setLongitude(longitude);
            dmsRegister.setSn(DeviceCache.getSnByIpFromMap(ip));
            dmsRegister.setVender(vendor.trim());
            Network network = new Network();
            network.setPcUpPort(cloudDataPort);
            network.setTargetIp(targetIP);
            network.setIpv6GuaAddress(addressGUA);
            network.setIpv6Mask(subnetMaskIPV6);
            network.setIpv6LlaAddress(addressLLA);
            network.setIpv6Gateway(gateWayIPV6);
            network.setIpv4Address(addressIPV4);
            network.setIpv4Gateway(gateWayIPV4);
            network.setIpv4Mask(subnetMaskIPV4);
            network.setLocalPort(localPort);
            network.setTargetPort(targetPort);
            network.setMac(mac);
            dmsRegister.setNet(network);
        }
        return dmsRegister;
    }

    protected String getMac(String msg) {
        String mac;
        mac = String.format("%s:%s:%s:%s:%s:%s", msg.substring(0, 2), msg.substring(2, 4), msg.substring(4, 6), msg.substring(6, 8), msg.substring(8, 10), msg.substring(10, 12));
        return mac;
    }

    protected String getIp(String msg) {
        String gatewayIpv4Str1 = msg.substring(0, 2);
        Integer gateWayIPV41 = Integer.valueOf(gatewayIpv4Str1, 16);
        String gatewayIpv4Str2 = msg.substring(2, 4);
        Integer gateWayIPV42 = Integer.valueOf(gatewayIpv4Str2, 16);
        String gatewayIpv4Str3 = msg.substring(4, 6);
        Integer gateWayIPV43 = Integer.valueOf(gatewayIpv4Str3, 16);
        String gatewayIpv4Str4 = msg.substring(6, 8);
        Integer gateWayIPV44 = Integer.valueOf(gatewayIpv4Str4, 16);
        return String.format("%d.%d.%d.%d", gateWayIPV41, gateWayIPV42, gateWayIPV43, gateWayIPV44);
    }


    protected String getIpV6(String msg) {
        return String.format("%s:%s:%s:%s:%s:%s:%s:%s", msg.substring(0, 4), msg.substring(4, 8), msg.substring(8, 12), msg.substring(12, 16), msg.substring(16, 20), msg.substring(20, 24), msg.substring(24, 28), msg.substring(28, 32));
    }

    /**
     * hexStr 转 Double
     *
     * @param hexStr
     * @return
     */
    public static Double hexStrToDouble(String hexStr, String msg) {
        if (StringUtil.isEmpty(hexStr)) {
            return null;
        }
        try {
            Long longBits = Long.valueOf(hexStr, 16);
            return Double.longBitsToDouble(longBits);
        } catch (Exception e) {
            log.info("字符串{} String转Double过程出现异常，异常原因是：{},{}。 原消息is{}", hexStr, e.getCause(), e.getMessage(), msg);
            return 0.0d;
        }

    }

    /**
     * hexStr 转 Float
     *
     * @param hexStr
     * @return
     */
    public static Float hexStrToFloat(String hexStr) {
        if (StringUtil.isEmpty(hexStr)) {
            return null;
        }
        try {
            Integer integerBits = Integer.valueOf(hexStr, 16);
            return Float.intBitsToFloat(integerBits);
        } catch (Exception e) {
            log.info("字符串{} Integer转Float过程出现异常，异常原因是：{},{}", hexStr, e.getCause(), e.getMessage());
            return 0.0f;
        }

    }

    /**
     * Byte[4] 转换为 you符号int
     *
     * @param b
     * @return
     */
    public static int byte4ArrayToInt(byte[] b) {
        return b[3] & 0xFF |
                (b[2] & 0xFF) << 8 |
                (b[1] & 0xFF) << 16 |
                (b[0] & 0xFF) << 24;
    }

    /**
     * Byte[2] 转换为 you符号int
     *
     * @param b
     * @return
     */
    public static int byte2ArrayToInt(byte[] b) {
        return b[1] & 0xFF |
                (b[0] & 0xFF) << 8;
    }

    public static Float hexStrToFloatWithMark(String hexStr) {
        byte[] a = hexStrToByteArray(hexStr);
        int b = byte4ArrayToInt(a);
        return Float.intBitsToFloat(b);
    }

    protected String getTime(String msg) {
        String timeSecondStr = msg.substring(0, 8);
        timeSecondStr = fromBigToSmall(timeSecondStr);
        Long timeSecond = Long.valueOf(timeSecondStr, 16);
        String timeMillStr = msg.substring(8, 16);
        timeMillStr = fromBigToSmall(timeMillStr);
        Long timeMill = Long.valueOf(timeMillStr, 16);
        Long timeLong = timeSecond * 1000 + timeMill;
        return String.valueOf(timeLong);
    }

    protected String fromBigToSmall(String parm) {
        byte[] bytes = parm.getBytes(StandardCharsets.UTF_8);
        byte temporary;
        for (int i = 0; i < bytes.length / 2; i += 2) {
            temporary = bytes[bytes.length - i - 2];
            bytes[bytes.length - i - 2] = bytes[i];
            bytes[i] = temporary;

            temporary = bytes[bytes.length - i - 1];
            bytes[bytes.length - i - 1] = bytes[i + 1];
            bytes[i + 1] = temporary;
        }
        return new String(bytes);

    }

    protected void storeMessageToCaches(String sn, String prop, Boolean success, String errCode, String errMsg, String key, String type) {

        RespondToCache respondToCache = new RespondToCache();
        if (StringUtil.isNotEmpty(sn)) {
            respondToCache.setSn(sn);
        }
        if (StringUtil.isNotEmpty(prop)) {
            respondToCache.setProp(prop);
        }
        respondToCache.setSuccess(success);
        if (StringUtil.isNotEmpty(errCode)) {
            respondToCache.setErrCode(errCode);
        }
        if (StringUtil.isNotEmpty(errMsg)) {
            respondToCache.setErrMsg(errMsg);
        }
        if (StringUtil.isNotEmpty(type)) {
            respondToCache.setType(type);
        }
        log.info("存入redis{}值为{}", key, JSON.toJSONString(respondToCache));
        redisUtil.set(key, JSON.toJSONString(respondToCache));
    }

    // 参数设置时，将答复结果存入redis
    protected void storeMessageToCacheWhenSet(String key, String sn) {
        storeMessageToCaches(sn, null, true, null, null, key, null);
    }

    public static short fromHexToShortWithMarkBy2(String parm) {
        byte[] a = hexStrToByteArray(parm);
        Integer b = byte2ArrayToInt(a);
        return b.shortValue();
    }

    public static byte[] hexStrToByteArray(String str) {
        if (str == null) {
            return null;
        }
        if (str.length() == 0) {
            return new byte[0];
        }
        byte[] byteArray = new byte[str.length() / 2];
        for (int i = 0; i < byteArray.length; i++) {
            String subStr = str.substring(2 * i, 2 * i + 2);
            byteArray[i] = ((byte) Integer.parseInt(subStr, 16));
        }
        return byteArray;
    }

    public static String getSeqNum() {
        if (seqNum < 1000000) {
            seqNum++;
        } else {
            seqNum = 0;
        }
        return seqNum + "";
    }

    private static String rangeStringDropedIn(long timeToReceive) {
        Long start = timeToReceive - timeToReceive % TimeUnit.MINUTES.toMillis(AppConfig.rangeInMinutes);
        java.sql.Date timeOfStart = new java.sql.Date(start);
        java.sql.Date timeOfEnd = new java.sql.Date(start + TimeUnit.MINUTES.toMillis(AppConfig.rangeInMinutes));
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMATE);
        String s = simpleDateFormat.format(timeOfStart);
        String e = simpleDateFormat.format(timeOfEnd);
        return String.format("%s_%s", s, e);
    }
}
