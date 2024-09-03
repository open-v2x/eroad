package cn.eroad.rad.service.impl;

import cn.eroad.rad.cache.RedisCache;
import cn.eroad.rad.config.AppConfig;
import cn.eroad.rad.config.DataConfig;
import cn.eroad.rad.model.ParsedData;
import cn.eroad.rad.model.dmsModel.DmsRegister;
import cn.eroad.rad.netty.UdpNettyClient;
import cn.eroad.rad.schedule.HeartbeatSchedule;
import cn.eroad.rad.service.EroadKafkaProducer;
import cn.eroad.rad.service.MessageCache;
import cn.eroad.rad.service.OperationTypeHandler;
import cn.eroad.rad.util.ByteUtil;
import cn.eroad.rad.util.RabbitMqUtil;
import cn.eroad.rad.util.SendMessageEncode;
import cn.eroad.rad.util.TopicUtil;
import cn.eroad.core.dto.AutoRegisterDTO;
import cn.eroad.core.dto.DeviceBaseInfo;
import cn.eroad.core.dto.DeviceOnlineDTO;
import cn.eroad.core.dto.DeviceRegisterDTO;
import cn.eroad.core.utils.StringUtil;
import cn.eroad.device.operate.DeviceCache;
import cn.eroad.device.vo.Device;
import cn.eroad.rabbitmq.service.impl.RabbitOperator;
import cn.eroad.util.OnlineUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 主动上报的接收（接收转发存储）
 *
 * @author mrChen
 * @date 2022/7/6 14:12
 */
@Component
@Slf4j
public class ActiveReportHandler extends MessageCache implements OperationTypeHandler<Void, ParsedData> {

    public String OperationType = DataConfig.operation_initiative_report;

    @Autowired
    private DeviceCache deviceCache;

    @Autowired
    private RabbitOperator rabbitOperator;

    private static ThreadPoolExecutor pool = new ThreadPoolExecutor(
            AppConfig.MULTI_THREAD_COUNT,
            AppConfig.MULTI_THREAD_COUNT * 2,
            1000L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(50000),
            new ThreadFactory() {
                AtomicLong i = new AtomicLong();
                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "push-kafka-thread-" + i.getAndIncrement());
                }
            },
            new ThreadPoolExecutor.AbortPolicy());


    @Override
    public Void handler(ParsedData o) {

        JSONObject dataObject = new JSONObject();
        String type = "";
        String sn = DeviceCache.getSnByIpFromMap(o.getIp());
        TopicUtil.DeviceEnum deviceEnum = TopicUtil.DeviceEnum.RAD;
        TopicUtil.ServiceDataEnum serviceDataEnum = null;
        try {
            switch (o.getObjectIDStr()) {
                case DataConfig.object_target_Trajectory_data:
                    //交通目标轨迹信息
                    dataObject = objectTargetTrajectoryData(o.getMsg());
                    serviceDataEnum = TopicUtil.ServiceDataEnum.RAD_TRACK;
                    break;
                case DataConfig.object_Detection_section_passing_information:
                    //检测断面过车信息
                    dataObject = objectDetectionSectionPassingInformation(o.getMsg());
                    serviceDataEnum = TopicUtil.ServiceDataEnum.RAD_CROSS;
                    break;
                case DataConfig.object_Traffic_status_information:
                    //交通状态信息
                    dataObject = objectTrafficStatusInformation(o.getMsg());
                    serviceDataEnum = TopicUtil.ServiceDataEnum.RAD_STATUS;
                    break;
                case DataConfig.object_Traffic_flow_information:
                    //交通流信息
                    dataObject = objectTrafficFlowInformation(o.getMsg());
                    serviceDataEnum = TopicUtil.ServiceDataEnum.RAD_FLOW;
                    break;
                case DataConfig.object_Abnormal_event_information:
                    //异常事件信息
                    dataObject = objectAbnormalEventInformation(o.getMsg());
                    serviceDataEnum = TopicUtil.ServiceDataEnum.RAD_EVENT;
                    break;
                case DataConfig.object_Point_cloud_data:
                    //点云数据
                    dataObject = objectPointCloudData(o.getMsg());
                    log.info("{}点云数据: {}", o.getIp(), dataObject);
                    serviceDataEnum = TopicUtil.ServiceDataEnum.RAD_POINT;
                    break;
                case DataConfig.object_register_data:
                    DmsRegister dmsRegister = objectRegisterData(o.getMsg(), o.getIp());
                    dataObject = JSON.parseObject(JSON.toJSONString(dmsRegister));
                    log.info("{}注册信息解析结果: {}", o.getIp(), dmsRegister);
                    int i = o.getPort();
                    if (DeviceCache.getSnByIpFromMap(o.getIp()) == null) {
                        //当前数据库无设备信息时 发送自动注册消息
                        AutoRegisterDTO dto = AutoRegisterDTO.buildRadAutoRegister(ByteUtil.hexStringToString(o.getMsg().substring(0, 20)), dmsRegister.getVender(), o.getIp(), o.getPort().toString());
                        RabbitMqUtil.reportAutoRegister(dto);
                        Device device = new Device();
                        BeanUtils.copyProperties(dto, device);
                        DeviceCache.setLocalDeviceCache(device);
                    }

                    if (dmsRegister != null) {
                        String parm = SendMessageEncode.encodeRegisterRespond(o.getRealSn());
                        log.info("注册信息回复：{}", o.getIp() + ":" + i);
                        UdpNettyClient.sendMessage(parm, o.getIp(), i);
                        DeviceRegisterDTO dgd = DeviceRegisterDTO.buildRadRegisterReport(sn, dmsRegister);
                        RabbitMqUtil.reportRegister(sn, 1, dgd);
                    }
                    DeviceCache.setDeviceCache(setPortToMap(sn, String.valueOf(o.getPort()), o.getIp()));
                    break;
                case DataConfig.object_heart:
                    RedisCache.snToRealsn.put(sn, o.getRealSn());
                    log.info("心跳上报sn {}", sn);
                    OnlineUtil.online(sn, o.getIp(), "rad");
                    //增加心跳回复
                    String param = SendMessageEncode.encodeHeartRespond(o.getRealSn());

                    UdpNettyClient.sendMessage(param, o.getIp(), o.getPort());
                    //心跳上报
                    sendDeviceOnlineStatus(sn, 1);
                    HeartbeatSchedule.hbSn.add(sn);
                    if (StringUtil.isEmpty(DeviceCache.getPortBySnFromMap(sn))) {//如果缓存中不存在port，那么上报一下
                        DeviceBaseInfo baseInfo = DeviceBaseInfo.builder().deviceIp(o.getIp()).port(String.valueOf(o.getPort()))
                                .deviceId(sn).deviceType("rad").build();
                        RabbitMqUtil.baseInfo(baseInfo);
                        deviceCache.setDeviceCache(setPortToMap(sn, String.valueOf(o.getPort()), o.getIp()));
                    }
                    break;
                case DataConfig.object_mmw_Working_status_data:
                    dataObject = objectMmwWorkingStatusData(o.getMsg(), DeviceCache.getSnByIpFromMap(o.getIp()));
                    log.info("{}工作状态{}", o.getIp(), dataObject);
                    type = "physical";
                    //上报状态
                    RabbitMqUtil.workStatus(sn, type, dataObject.toJSONString());
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            log.error("处理上报信息出现异常，异常信息是:{}:{}:{}", o.getIp(), o.getObjectIDStr(), o.getMsg());
            e.printStackTrace();
        }




        if (serviceDataEnum != null) {
            String topic = TopicUtil.getTopic(deviceEnum, sn, serviceDataEnum);
            dataObject.put("deviceId", sn);
            String json = dataObject.toJSONString();
            pool.execute(new Runnable() {
                @Override
                @Async
                public void run() {
                    EroadKafkaProducer.send(json, topic);
                }
            });
        }
        return null;
    }

    public void sendDeviceOnlineStatus(String sn, Integer online) {
        DeviceOnlineDTO onlineDevice = new DeviceOnlineDTO();
        if (DeviceCache.existDevice(sn)) {
            onlineDevice.setDeviceType("rad");
            onlineDevice.setSn(sn);
            onlineDevice.setUpdateTime(new Date());
            onlineDevice.setOnline(online);
        } else {
            log.info("该设备没有在采集源里边：{}", sn);
        }
        rabbitOperator.sendTopic(DeviceOnlineDTO.DEVICE_COMMON_DATA_REPORT_EXCHANGE, DeviceOnlineDTO.ROUTING_KEY_DEVICE_ONLINE_STATUS, onlineDevice);
    }

    private static Device setPortToMap(String sn, String port, String ip) {
        Device device = new Device();
        device.setDeviceId(sn);
        device.setDeviceName(sn + "\\/" + "rad");
        device.setPort(port);
        device.setDeviceIp(ip);
        device.setDeviceType("rad");
        return device;
    }
}
