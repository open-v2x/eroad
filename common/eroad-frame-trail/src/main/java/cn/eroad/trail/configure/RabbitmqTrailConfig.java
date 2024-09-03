package cn.eroad.trail.configure;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import javax.annotation.Resource;

/**
 * @author: luwei
 * @Date: 2022/2/19
 * @description:
 */
@Slf4j
@Configuration
public class RabbitmqTrailConfig {

    /**
     * RabbitMQ 主题
     */
    public static final String ROUTING_KEY_TRAIL = "report.device.trail";

    /**
     * RabbitMQ 队列
     */
    public static final String QUEUE_TRAIL = "report_device_trail";

    /**
     * RabbitMQ 交换机名
     */
    public static final String EXCHANGE_TRAIL = "deviceDataReportExchange";




























}
