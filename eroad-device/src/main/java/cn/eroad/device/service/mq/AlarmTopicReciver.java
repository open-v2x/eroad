package cn.eroad.device.service.mq;

import cn.eroad.core.dto.DeviceAlarmDTO;
import cn.eroad.device.entity.deviceAlarm.DeviceAlarmInfo;
import cn.eroad.device.entity.devicemaintain.DeviceMaintain;
import cn.eroad.device.operate.DeviceCache;
import cn.eroad.device.service.alarmService.DeviceAlarmInfoService;
import cn.eroad.device.service.config.DeviceAdminRabbitConfig;
import cn.eroad.device.service.devicemaintain.DeviceMaintainService;
import cn.eroad.device.vo.Device;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * @author yujinfu
 * @version 1.0
 * @create 2021/10/21
 * @description
 */
@Slf4j
@Component
public class AlarmTopicReciver {

    @Autowired
    private DeviceMaintainService deviceMaintainService;

    @Autowired
    private DeviceAlarmInfoService deviceAlarmInfoService;

    @Autowired
    private DeviceCache deviceCache;

    @RabbitHandler
    @RabbitListener(queues = {
            DeviceAdminRabbitConfig.QUEUE_REPORT_DEVICE_ALARM},
            containerFactory = "rabbitListenerContainerFactory"
    )
    @Transactional(rollbackFor = Exception.class)
    public void handleMessage(Message message) {
        MessageProperties messageProperties = message.getMessageProperties();
        String receivedExchange = messageProperties.getReceivedExchange(); //amq.topic
        //也是mqtt消息的topic  mqtt: /drivers/xxxx/abcd  amqp: .drivers.#.abc
        String receivedRoutingKey = messageProperties.getReceivedRoutingKey();
        Byte qos = messageProperties.getHeader("x-mqtt-publish-qos");
        String consumerQueue = messageProperties.getConsumerQueue();
        long deliveryTag = messageProperties.getDeliveryTag();//表示当前服务节点收到的消息计数

        log.info("receivedExchange : " + receivedExchange +
                "  receivedRoutingKey : " + receivedRoutingKey +
                "  qos : " + qos +
                "  consumerQueue : " + consumerQueue +
                "  deliveryTag : " + deliveryTag);

        String content = new String(message.getBody());

        try {
            this.handleAlarmNotify(content);
        } catch (Exception e) {
            log.error("收到MQ消息时出现异常，{}", e.toString());
        }
    }

    public void handleAlarmNotify(String content) {
        log.info("收到设备告警通知：{}", content);
        DeviceAlarmDTO deviceAlarmDTO = JSON.parseObject(content, DeviceAlarmDTO.class);
        if (deviceAlarmDTO == null || deviceAlarmDTO.getSn() == null) {
            log.info("设备关键信息不全");
            return;
        }
        // 判断缓存中有无此设备
        Device deviceCacheInfo = deviceCache.getDeviceBySn(deviceAlarmDTO.getSn());
        if (deviceCacheInfo == null) {
            log.info("redis 无此数据:{}", deviceAlarmDTO.getSn());
            return;
        }
        //告警日志写入数据库
        DeviceAlarmInfo deviceAlarmInfo = new DeviceAlarmInfo();
        deviceAlarmInfo.setSn(deviceAlarmDTO.getSn());
        deviceAlarmInfo.setDeviceType(deviceAlarmDTO.getDeviceType());
        deviceAlarmInfo.setAlarmStatus(deviceAlarmDTO.getAlarmStatus());
        deviceAlarmInfo.setAlarmType(deviceAlarmDTO.getAlarmType());
        //创建sdf对象，指定日期格式类型
        deviceAlarmInfo.setGmtAlarm(deviceAlarmDTO.getAlarmTime());
        deviceAlarmInfo.setGmtReport(deviceAlarmDTO.getUpdateTime());
        deviceAlarmInfo.setGmtCreated(new Date());
        deviceAlarmInfo.setAlarmMessage(deviceAlarmDTO.getAlarmMessage());
        deviceAlarmInfo.setAlarmLevel(deviceAlarmDTO.getAlarmLevel());
        deviceAlarmInfo.setInfo(deviceAlarmDTO.getDataJson());
        deviceAlarmInfoService.save(deviceAlarmInfo);

        // 更新表中告警状态
        DeviceMaintain deviceMaintain = new DeviceMaintain();
        deviceMaintain.setDeviceId(deviceAlarmDTO.getSn());
        // 产生告警信息，告警标识为1,告警消失为0
        deviceMaintain.setAlarmState(deviceAlarmDTO.getAlarmStatus());
        deviceMaintain.setAlarmStateUpdateTime(deviceAlarmDTO.getUpdateTime());

        log.info("更新告警状态:{}", JSON.toJSONString(deviceMaintain));
        deviceMaintainService.updateById(deviceMaintain);

    }


    @RabbitHandler
    @RabbitListener(queues = {
            DeviceAdminRabbitConfig.QUEUE_REPORT_DEVICE_ALARM_SHORTLWT},
            containerFactory = "rabbitListenerContainerFactory"
    )
    @Transactional(rollbackFor = Exception.class)
    public void lwtHandler(Message message) {
        String content = new String(message.getBody());
        try {
            log.info("收到设备离线告警通知：{}", content);
            DeviceAlarmDTO deviceAlarmDTO = JSON.parseObject(content, DeviceAlarmDTO.class);
            if (deviceAlarmDTO == null || deviceAlarmDTO.getSn() == null) {
                log.info("设备关键信息不全");
                return;
            }
            // 判断缓存中有无此设备
            Device deviceCacheInfo = deviceCache.getDeviceBySn(deviceAlarmDTO.getSn());
            if (deviceCacheInfo == null) {
                log.info("redis 无此数据:{}", deviceAlarmDTO.getSn());
                return;
            }
            //告警日志写入数据库
            DeviceAlarmInfo deviceAlarmInfo = new DeviceAlarmInfo();
            deviceAlarmInfo.setSn(deviceAlarmDTO.getSn());
            deviceAlarmInfo.setDeviceType(deviceAlarmDTO.getDeviceType());
            deviceAlarmInfo.setAlarmStatus(deviceAlarmDTO.getAlarmStatus());
            deviceAlarmInfo.setAlarmType(deviceAlarmDTO.getAlarmType());
            //创建sdf对象，指定日期格式类型
            deviceAlarmInfo.setGmtAlarm(deviceAlarmDTO.getAlarmTime());
            deviceAlarmInfo.setGmtReport(deviceAlarmDTO.getUpdateTime());
            deviceAlarmInfo.setGmtCreated(new Date());
            deviceAlarmInfo.setAlarmMessage(deviceAlarmDTO.getAlarmMessage());
            deviceAlarmInfo.setAlarmLevel(deviceAlarmDTO.getAlarmLevel());
            deviceAlarmInfo.setInfo(deviceAlarmDTO.getDataJson());
            deviceAlarmInfoService.save(deviceAlarmInfo);

        } catch (Exception e) {
            log.error("收到离线MQ消息时出现异常", e);
        }
    }
}
