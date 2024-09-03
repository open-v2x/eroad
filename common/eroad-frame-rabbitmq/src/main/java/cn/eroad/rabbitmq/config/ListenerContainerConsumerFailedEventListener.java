package cn.eroad.rabbitmq.config;

import java.util.Arrays;

import org.springframework.amqp.rabbit.listener.ListenerContainerConsumerFailedEvent;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ListenerContainerConsumerFailedEventListener implements ApplicationListener<ListenerContainerConsumerFailedEvent> {

    @Override
    public void onApplicationEvent(ListenerContainerConsumerFailedEvent event) {
        log.error("消费者失败事件发生：{}", event);
        if (event.isFatal()) {
            log.error("Stopping container from aborted consumer. Reason::{}", event.getReason(), event.getThrowable());
            SimpleMessageListenerContainer container = (SimpleMessageListenerContainer) event.getSource();
            String queueNames = Arrays.toString(container.getQueueNames());
            try {
                try {
                    Thread.sleep(30000);
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
                //判断此时消费者容器是否正在运行
                Assert.state(!container.isRunning(), String.format("监听容器%s正在运行！", container));
                //消费者容器没有在运行时，进行启动
                container.start();
                log.info("重启队列{}的监听成功", queueNames);
            } catch (Exception e) {
                log.error("重启队列{}的监听失败", queueNames, e);
            }

        }
    }
}
