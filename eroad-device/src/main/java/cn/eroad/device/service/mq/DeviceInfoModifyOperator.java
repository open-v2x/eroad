package cn.eroad.device.service.mq;

import cn.eroad.device.entity.devicemaintain.DeviceMaintain;
import cn.eroad.device.operate.DeviceCache;
import cn.eroad.device.service.devicemaintain.DeviceMaintainService;
import cn.eroad.device.vo.Device;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DeviceInfoModifyOperator {
    private final DeviceMaintainService deviceMaintainService;
    private final DeviceCache deviceCache;

    @Autowired
    public DeviceInfoModifyOperator(DeviceMaintainService deviceMaintainService, DeviceCache deviceCache) {
        this.deviceMaintainService = deviceMaintainService;
        this.deviceCache = deviceCache;
    }

    @RabbitListener(queues = "device_ac_modifyInfo", containerFactory = "rabbitListenerContainerFactory")
    public void DeviceInfoModify(Message message) {
        String strMsg = new String(message.getBody());
        log.info("设备信息修改队列监听到的内容为:{}", strMsg);
        try {
            DeviceMaintain deviceMaintain = JSONObject.parseObject(strMsg, DeviceMaintain.class);
            if (deviceMaintain.getDeviceId() == null) {
                log.info("要更新的设备未上传sn:{}", deviceMaintain);
                return;
            }
            // 获取缓存中的数据
            Device deviceInCache = deviceCache.getDeviceBySn(deviceMaintain.getDeviceId());
            String deviceName = deviceMaintain.getDeviceName();
            String deviceType = deviceMaintain.getDeviceType();
            String domainName = deviceMaintain.getDomainName();
            String deviceIp = deviceMaintain.getDeviceIp();
            String port = deviceMaintain.getPort();
            String manufacturer = deviceMaintain.getManufacturer();
            String deviceArea = deviceMaintain.getDeviceArea();
            String mac = deviceMaintain.getMac();
            if (deviceInCache != null) {
                if (deviceName != null)
                    deviceInCache.setDeviceName(deviceName);
                if (deviceType != null)
                    deviceInCache.setDeviceType(deviceType);
                if (domainName != null)
                    deviceInCache.setDomainName(domainName);
                if (deviceIp != null)
                    deviceInCache.setDeviceIp(deviceIp);
                if (port != null)
                    deviceInCache.setPort(port);
                if (manufacturer != null)
                    deviceInCache.setManufacturer(manufacturer);
                if (deviceArea != null)
                    deviceInCache.setDeviceArea(deviceArea);
                if (mac != null)
                    deviceInCache.setMac(mac);
                // 更新缓存和map
                deviceCache.updateDeviceCache(deviceInCache);
            } else {
                log.info("缓存中没有此数据，无法修改:{}", deviceMaintain);
            }

            // 更新数据库
            deviceMaintainService.updateById(deviceMaintain);
            log.info("设备信息更新:{}", deviceMaintain);
        } catch (JSONException ex) {
            log.error("数据上报格式不正确,无法转换成JSONObject:{}|e={}", strMsg, ex);
        }

    }
}
