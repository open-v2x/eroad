package cn.eroad.device.listener;

import cn.eroad.device.constants.DeviceConstant;
import cn.eroad.device.event.DeviceChangeEvent;
import cn.eroad.device.vo.Device;
import cn.eroad.rabbitmq.service.MessageOperator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * 异步监听日志事件
 */
@Slf4j
@Component
public class DeviceChangeEventListener {
    @Autowired
    @Lazy
    private MessageOperator messageOperator;


    @Async
    @Order
    @EventListener(DeviceChangeEvent.class)
    public void listenOperLog(DeviceChangeEvent event) {
        if (event.getSource() instanceof Device) {
            log.info("发送单个设备变化消息到rabbitmq!");
            Device device = (Device) event.getSource();
            messageOperator.sendExchange(DeviceConstant.DEVICE_CHANGE_FANOUT_EXCHANGE, device);
        } else if (event.getSource() instanceof Collection) {
            log.info("发送集合设备变化消息到rabbitmq!");
            ((Collection<Device>) event.getSource()).stream().forEach(device -> {
                messageOperator.sendExchange(DeviceConstant.DEVICE_CHANGE_FANOUT_EXCHANGE, device);
            });
        }
    }

}
