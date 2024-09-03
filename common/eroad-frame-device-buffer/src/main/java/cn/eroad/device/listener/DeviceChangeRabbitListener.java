package cn.eroad.device.listener;

import cn.eroad.core.utils.StringUtil;
import cn.eroad.device.constants.DeviceConstant;
import cn.eroad.device.operate.DeviceCache;
import cn.eroad.device.vo.Device;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * @project: eroad-frame
 * @ClassName: DeviceChangeListener
 * @author: liyongqiang
 * @creat: 2022/7/8 9:55
 * 描述:
 */
@Component
@Slf4j
@ConditionalOnProperty(name = "dxsc.device.cache.listener", havingValue = "open")
public class DeviceChangeRabbitListener {
    @Autowired
    private DeviceCache deviceCache;

    @RabbitHandler

    @RabbitListener(bindings = @QueueBinding(
            // value = @Queue(value = "${dxsc.device.cache.queue}"),
            value = @Queue(), //切记： 此处无需设置队列名称，否在得话，多个消费者只有一个消费者能消费数据。其它消费者无法消费数据。
            exchange = @Exchange(value = DeviceConstant.DEVICE_CHANGE_FANOUT_EXCHANGE, type = ExchangeTypes.FANOUT)
    ), containerFactory = "rabbitListenerContainerFactory")
    public void handleMessage(Message message) {
        String strMsg = new String(message.getBody());

        if (StringUtil.isEmpty(strMsg)) {
            log.error("设备变化监听到的内容为空");
            return;
        }
        log.info("设备变化监听到的内容为：={}", strMsg);
        try {
            Device device = JSONObject.parseObject(strMsg, Device.class);
            deviceCache.updateLocalCache(device);
        } catch (JSONException e) {
            log.info("数据上报格式不正确,无法转换成JSONObject:{}|e={}", strMsg, e);
        }
    }
}
