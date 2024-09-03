package cn.eroad.device.service.mq;

import cn.eroad.redis.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@Slf4j
public class CountBizInfoOperator {

    @Autowired
    private RedisUtil redis;
    @RabbitListener(queues = "device_ac_countInfo", containerFactory = "rabbitListenerContainerFactory")

    public void receiveMqCount(Message msg){
        if (StringUtils.isEmpty(msg)) {
            log.warn("接收统计消息异常!!!!!");
            return;
        }
        byte[] body = msg.getBody();
        String strMsg = new String(body);
        log.info("接收统计消息>>>>>>>>>{}", strMsg);
        redis.set("rad_count",strMsg);
    }
}
