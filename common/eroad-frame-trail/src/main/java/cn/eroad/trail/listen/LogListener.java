package cn.eroad.trail.listen;

import cn.eroad.core.vo.CommonRequest;
import cn.eroad.rabbitmq.service.impl.RabbitOperator;
import cn.eroad.trail.event.SysOperLogEvent;
import cn.eroad.trail.invoker.OperationLogInvoker;
import cn.eroad.trail.configure.RabbitmqTrailConfig;
import cn.eroad.trail.entity.UserTrailQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 异步监听日志事件
 */
@Slf4j

@Component
public class LogListener {
    @Autowired
    private OperationLogInvoker operationLogInvoker;

    @Autowired
    private Environment env;

    @Autowired
    @Lazy
    private RabbitOperator rabbitOperator;

    @Async
    @Order
    @EventListener(SysOperLogEvent.class)
    public void listenOperLog(SysOperLogEvent event) {
        UserTrailQuery sysOperLog = (UserTrailQuery) event.getSource();
        CommonRequest<UserTrailQuery> commonRequest = CommonRequest.Builder.build(env.getProperty("spring.application.name"));
        commonRequest.setBody(sysOperLog);
        log.info("frame 开始记录远程操作日志：{}", sysOperLog);
        rabbitOperator.sendTopic(RabbitmqTrailConfig.EXCHANGE_TRAIL, RabbitmqTrailConfig.ROUTING_KEY_TRAIL, commonRequest);
        log.info("frame 远程操作日志记录成功!");
    }

}
