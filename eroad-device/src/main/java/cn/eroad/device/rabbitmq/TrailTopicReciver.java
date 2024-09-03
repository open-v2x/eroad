package cn.eroad.device.rabbitmq;

import cn.eroad.core.vo.CommonRequest;
import cn.eroad.device.entity.po.OperationLog;
import cn.eroad.device.service.OperationLogService;
import cn.eroad.trail.entity.UserTrailQuery;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Date;

/**
 * @author yujinfu
 * @version 1.0
 * @create 2021/10/21
 * @description
 */
@Slf4j
@Component
public class TrailTopicReciver {

    public static final String QUEUE_REPORT_DEVICE_TRAIL = "device_ac_reportDeviceTrail";

    @Autowired
    private OperationLogService operationLogService;

    @RabbitHandler
    @RabbitListener(queues = {
            QUEUE_REPORT_DEVICE_TRAIL,
    },
            containerFactory= "rabbitListenerContainerFactory"
    )
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
        log.info("收到内容： " + content);
        content = content.replace("\\", "");
        try {



            this.handleLog(content);
        }catch (Exception e){
            log.error("收到MQ消息时出现异常，{}",e);
        }
    }

    public void handleLog(String content){
        try{
            CommonRequest<UserTrailQuery> commonRequest = JSON.parseObject(content,CommonRequest.class);
            UserTrailQuery userTrailQuery = JSONObject.parseObject(JSON.toJSONString(commonRequest.getBody()),UserTrailQuery.class);
            OperationLog operationLog = new OperationLog();
            operationLog.setOperationObject(userTrailQuery.getOperateObject());
            operationLog.setOperationAction(userTrailQuery.getOperateAction());
            operationLog.setStatus(userTrailQuery.getStatus());
            operationLog.setCreator(userTrailQuery.getCreator());
            operationLog.setDescription(userTrailQuery.getDescription());
            operationLog.setApplicationCode(commonRequest.getHead().getApplicationCode());
            operationLog.setCreatorId(commonRequest.getHead().getOperatorId());
            operationLog.setParams(userTrailQuery.getDescription());
            operationLog.setGmtCreated(new Date());
            try {
                log.info("system 开始记录操作日志{}",userTrailQuery);
                operationLogService.save(operationLog);
                log.info("system 记录操作日志成功!");
            }catch (Exception e){
                throw e;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
