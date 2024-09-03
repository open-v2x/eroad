package cn.eroad.device.service.mq;

import cn.eroad.core.dto.DeviceAlarmDTO;
import cn.eroad.core.dto.DeviceOnlineDTO;
import cn.eroad.core.dto.OnlineStateChangeDTO;
import cn.eroad.device.entity.devicemaintain.DeviceMaintain;
import cn.eroad.device.operate.DeviceCache;
import cn.eroad.device.service.devicemaintain.DeviceMaintainService;
import cn.eroad.device.vo.Device;
import cn.eroad.rabbitmq.service.MessageOperator;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @author cuizhiming
 */
@Component
@Slf4j

public class DeviceOnlineChangeOperator {

    private final DeviceMaintainService deviceMaintainService;
    private final MessageOperator messageOperator;
    private final DeviceCache deviceCache;

    @Autowired
    public DeviceOnlineChangeOperator(DeviceMaintainService deviceMaintainService, MessageOperator messageOperator, DeviceCache deviceCache) {
        this.deviceMaintainService = deviceMaintainService;
        this.messageOperator = messageOperator;
        this.deviceCache = deviceCache;
    }

    /**
     * 监听队列消息,获取设备在线状态
     * 持久化到数据库
     *
     * @param content
     */
    @RabbitListener(queues = DeviceOnlineDTO.DEVICE_ONLINE_STATUS_REPORT, containerFactory = "rabbitListenerContainerFactory")
    public void reportDeviceOnlineStatus(Message content) {
        String jsonStr = new String(content.getBody());
        log.info("DeviceOnlineChangeOperator||reportDeviceOnlineStatus||监听到的队列内容：{}", jsonStr);
        try {
            DeviceOnlineDTO deviceOnline = JSON.parseObject(jsonStr, DeviceOnlineDTO.class);
            deviceCache.updateDeviceHeartCache(deviceOnline);
            if (deviceOnline == null) {
                return;
            }

            // 设备上报上来的状态和缓存中的一致,不重复更新数据库
            // 获取旧的设备在线状态
            Device device = deviceCache.getDeviceBySn(deviceOnline.getSn());
            if (device == null) {
                log.info("redis 无此数据 不做状态更新处理：{}", deviceOnline.getSn());
                return;
            }
            Integer onlineStateInCache = device.getOnlineState();
            if (onlineStateInCache != null && onlineStateInCache.equals(deviceOnline.getOnline())) {
                log.info("状态无变化 不做状态更新处理：{} {}", onlineStateInCache, deviceOnline.getOnline());
                return;
            }

            // 设备上报上来的状态和缓存中的不一致，更新数据库并且给运维平台发送消息
            if (deviceOnline.getUpdateTime() == null) {
                deviceOnline.setUpdateTime(new Date());
            }

            //更新设备缓存到redis
            device.setOnlineState(deviceOnline.getOnline());
            deviceCache.setDevice2Redis(device);



            // 持久化变更状态数据到数据库
            DeviceMaintain deviceMaintain = new DeviceMaintain();
            deviceMaintain.setDeviceId(deviceOnline.getSn());
            deviceMaintain.setOnlineState(deviceOnline.getOnline());
            deviceMaintain.setOnlineStateUpdateTime(deviceOnline.getUpdateTime());

            deviceMaintainService.updateById(deviceMaintain);
            log.info("数据库更新设备在线状态完成,{}", deviceMaintain);

            // 状态变化时给运维平台发送设备新旧状态
            OnlineStateChangeDTO onlineStateChange = new OnlineStateChangeDTO();
            onlineStateChange.setSn(deviceOnline.getSn());
            onlineStateChange.setDeviceType(deviceOnline.getDeviceType());
            onlineStateChange.setOldState(onlineStateInCache);
            onlineStateChange.setNewState(deviceOnline.getOnline());
            onlineStateChange.setUpdateTime(deviceOnline.getUpdateTime());
            onlineStateChange.setReason(deviceOnline.getReason());
            log.info("向运维管理平台上报设备在线状态:{}", onlineStateChange);
            messageOperator.sendTopic(OnlineStateChangeDTO.EXCHANGE_DEVICE_DATA_REPORT, OnlineStateChangeDTO.ROUTING_KEY_DEVICE_SINGLE_ONLINE_OFFLINE, onlineStateChange);

            //如果是离线。发送离线警告
            if(deviceOnline.getOnline()==0){
                DeviceAlarmDTO deviceAlarmDTO = DeviceAlarmDTO.builder().deviceType(deviceOnline.getDeviceType())
                        .alarmMessage("设备离线警告")
                        .alarmLevel(20000)
                        .sn(deviceOnline.getSn())
                        .alarmTime(deviceOnline.getUpdateTime()).build();
                messageOperator.sendTopicWithExpir(DeviceAlarmDTO.DEVICE_COMMON_DATA_REPORT_EXCHANGE, "device.single.shortlwt.alarmInfo", deviceAlarmDTO);
            }
        } catch (JSONException e) {
            log.info("数据上报格式不正确,无法转换成JSONObject:{}", jsonStr);
        }
    }

}
