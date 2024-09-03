package cn.eroad.device.service.mq;
import cn.eroad.core.dto.OnlineStateChangeDTO;
import cn.eroad.device.entity.devicemaintain.DeviceMaintain;
import cn.eroad.device.service.devicemaintain.impl.DeviceMaintainServiceImpl;
import cn.eroad.rabbitmq.service.MessageOperator;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Date;

@Component
@Slf4j
public class DeviceRegisterOperator {

    @Autowired
    private DeviceMaintainServiceImpl deviceMaintainService;

    @Autowired
    private MessageOperator messageOperator;

    @RabbitListener(queues = "device_ac_autoRegisterInfo", containerFactory = "rabbitListenerContainerFactory")
    public void getMsg(Message msg) {
        String strMsg = new String(msg.getBody());
        log.info("自动注册消息监听到的内容为：={}", strMsg);
        try {
            DeviceMaintain device = JSONObject.parseObject(strMsg, DeviceMaintain.class);
            String deviceId = device.getDeviceId();
            String deviceType = device.getDeviceType();
            String manufacturer = device.getManufacturer();
            if ((StringUtils.isEmpty(deviceId)||"".equals(deviceId.trim())) ||
                    (StringUtils.isEmpty(deviceType)||"".equals(deviceType.trim())) ||
                            (StringUtils.isEmpty(manufacturer)||"".equals(manufacturer.trim()))) {
                log.error("消息不完整|deviceId={}|deviceType={}|manufacturer={}", deviceId, deviceType, manufacturer);
                return;
            }


            device.setDeviceName(device.getDeviceId() + "/" + device.getDeviceType());
            device.setDeviceSign(0);
            device.setOnlineState(1);
            device.setAlarmStateUpdateTime(new Date());
            device.setOnlineStateUpdateTime(new Date());
            deviceMaintainService.deviceSave(device);

            // 状态变化时给运维平台发送设备状态
            OnlineStateChangeDTO onlineStateChange = new OnlineStateChangeDTO();
            onlineStateChange.setSn(device.getDeviceId());
            onlineStateChange.setDeviceType(device.getDeviceType());
            onlineStateChange.setOldState(0);
            onlineStateChange.setNewState(1);
            onlineStateChange.setUpdateTime(new Date());
            log.info("新增设备向运维管理平台上报设备在线状态:{}", onlineStateChange);
            messageOperator.sendTopic(OnlineStateChangeDTO.EXCHANGE_DEVICE_DATA_REPORT, OnlineStateChangeDTO.ROUTING_KEY_DEVICE_SINGLE_ONLINE_OFFLINE, onlineStateChange);

        } catch (JSONException e) {
            log.error("数据上报格式不正确,无法转换成JSONObject:{}|e={}", strMsg, e);
        }
    }
}
