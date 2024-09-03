package cn.eroad.rabbitmq.config;

import cn.eroad.rabbitmq.converter.FastJsonMessageConverter;
import cn.eroad.rabbitmq.service.impl.RabbitOperator;
import com.rabbitmq.client.SslContextFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/*
 * @project: eroad-frame
 * @ClassName: RabbitConfig
 * @author: liyongqiang
 * @creat: 2022/5/30 8:39
 */

/**
 * Broker:它提供一种传输服务,它的角色就是维护一条从生产者到消费者的路线，保证数据能按照指定的方式进行传输,
 * Exchange：消息交换机,它指定消息按什么规则,路由到哪个队列。
 * Queue:消息的载体,每个消息都会被投到一个或多个队列。
 * Binding:绑定，它的作用就是把exchange和queue按照路由规则绑定起来.
 * Routing Key:路由关键字,exchange根据这个关键字进行消息投递。
 * vhost:虚拟主机,一个broker里可以有多个vhost，用作不同用户的权限分离。
 * Producer:消息生产者,就是投递消息的程序.
 * Consumer:消息消费者,就是接受消息的程序.
 * Channel:消息通道,在客户端的每个连接里,可建立多个channel.
 */
@EnableConfigurationProperties(RabbitProperties.class)
@Configuration//(proxyBeanMethods = false)
@Slf4j
public class RabbitConfig {

    /**
     * 所有消息的qos默认都为1
     */
    public static final int MESSAGE_QOS = 1;



    @Autowired
    private RabbitProperties rabbitProperties;




    private static Map<String, RabbitAdmin> adminMap = new ConcurrentHashMap<>();
    private static Map<String, CachingConnectionFactory> connectionMap = new ConcurrentHashMap<>();
    private static Map<String, SimpleRabbitListenerContainerFactory> listenerMap = new ConcurrentHashMap<>();
    private static Map<String, RabbitTemplate> templateMap = new ConcurrentHashMap<>();


    //单机节点
    @Bean("connectionFactory")
    @ConditionalOnProperty(name = "spring.rabbitmq.mode", havingValue = "single")
    public ConnectionFactory connectionFactorySingle(SimpleRabbitListenerContainerFactoryConfigurer configurer) {
        String key = rabbitProperties.getHost() + rabbitProperties.getPort();
        CachingConnectionFactory connectionFactory;
        if (!connectionMap.containsKey(key)) {
            connectionFactory = new CachingConnectionFactory(rabbitProperties.getHost(), rabbitProperties.getPort());
            if (rabbitProperties.getSsl().isEnabled()) {
                connectionFactory.getRabbitConnectionFactory().setSslContextFactory(new SslContextFactory() {
                    @Override
                    public SSLContext create(String s) {
                        return getSSLSocktet(rabbitProperties.getSsl().getKeyStore());
                    }
                });
            }
            connectionFactory.setUsername(rabbitProperties.getUsername());
            connectionFactory.setPassword(rabbitProperties.getPassword());
            if (rabbitProperties.getVirtualHost() != null) {
                connectionFactory.setVirtualHost(rabbitProperties.getVirtualHost());
            }
            connectionFactory.setPublisherConfirms(rabbitProperties.isPublisherConfirms());
            connectionFactory.setPublisherReturns(rabbitProperties.isPublisherReturns());
            if (rabbitProperties.getConnectionTimeout() != null) {
                connectionFactory.setConnectionTimeout((int) rabbitProperties.getConnectionTimeout().getSeconds() * 1000);
            }
            if (rabbitProperties.getCache().getChannel().getSize() != null) {
                connectionFactory.setChannelCacheSize(rabbitProperties.getCache().getChannel().getSize());
            }

            if (rabbitProperties.getCache().getChannel().getCheckoutTimeout() != null) {
                connectionFactory.setChannelCheckoutTimeout(rabbitProperties.getCache().getChannel().getCheckoutTimeout().getSeconds() * 1000);
            }

            connectionFactory.setCacheMode(rabbitProperties.getCache().getConnection().getMode());
            connectionMap.put(key, connectionFactory);
        } else {
            connectionFactory = connectionMap.get(key);
        }
        return connectionFactory;
    }

    //集群节点
    @Bean("connectionFactory")
    @ConditionalOnProperty(name = "spring.rabbitmq.mode", havingValue = "cluster")
    public ConnectionFactory connectionFactoryCluster(SimpleRabbitListenerContainerFactoryConfigurer configurer) {
        String key = rabbitProperties.getHost() + rabbitProperties.getPort();
        CachingConnectionFactory connectionFactory;
        if (!connectionMap.containsKey(key)) {
            connectionFactory = new CachingConnectionFactory();
            if (rabbitProperties.getSsl().isEnabled()) {
                connectionFactory.getRabbitConnectionFactory().setSslContextFactory(new SslContextFactory() {
                    @Override
                    public SSLContext create(String s) {
                        return getSSLSocktet(rabbitProperties.getSsl().getKeyStore());
                    }
                });
            }
            connectionFactory.setAddresses(rabbitProperties.getAddresses());
            connectionFactory.setUsername(rabbitProperties.getUsername());
            connectionFactory.setPassword(rabbitProperties.getPassword());
            if (rabbitProperties.getVirtualHost() != null) {
                connectionFactory.setVirtualHost(rabbitProperties.getVirtualHost());
            }
            connectionFactory.setPublisherConfirms(rabbitProperties.isPublisherConfirms());
            connectionFactory.setPublisherReturns(rabbitProperties.isPublisherReturns());
            if (rabbitProperties.getConnectionTimeout() != null) {
                connectionFactory.setConnectionTimeout((int) rabbitProperties.getConnectionTimeout().getSeconds() * 1000);
            }
            if (rabbitProperties.getCache().getChannel().getSize() != null) {
                connectionFactory.setChannelCacheSize(rabbitProperties.getCache().getChannel().getSize());
            }

            if (rabbitProperties.getCache().getChannel().getCheckoutTimeout() != null) {
                connectionFactory.setChannelCheckoutTimeout(rabbitProperties.getCache().getChannel().getCheckoutTimeout().getSeconds() * 1000);
            }

            connectionFactory.setCacheMode(rabbitProperties.getCache().getConnection().getMode());
            log.info("集群连接工厂设置完成，连接地址{}" + rabbitProperties.getAddresses());
            log.info("集群连接工厂设置完成，连接用户{}" + rabbitProperties.getUsername());
            connectionMap.put(key, connectionFactory);
        } else {
            connectionFactory = connectionMap.get(key);
        }

        return connectionFactory;
    }


    @Bean("rabbitListenerContainerFactory")
    @ConditionalOnBean(ConnectionFactory.class)
    public RabbitListenerContainerFactory listenerContainerFactory(SimpleRabbitListenerContainerFactoryConfigurer configurer, @Qualifier("connectionFactory") ConnectionFactory connectionFactory) {//},@Qualifier("otherRabbitConfig") Object obj) {
        String key = rabbitProperties.getHost() + rabbitProperties.getPort();
        SimpleRabbitListenerContainerFactory factory;
        if (!listenerMap.containsKey(key)) {
            factory = new SimpleRabbitListenerContainerFactory();
            configurer.configure(factory, connectionFactory);
            //        factory.setConnectionFactory(connectionFactory);
            //        factory.setMessageConverter(new FastJsonMessageConverter());
            //        //最小消费者数量
            //        factory.setConcurrentConsumers(10);
            //        //最大消费者数量
            //        factory.setMaxConcurrentConsumers(10);
            //        //一个请求最大处理的消息数量
            //        factory.setPrefetchCount(10);
            //        //
            //        factory.setChannelTransacted(true);
            //        //默认不排队
            //        factory.setDefaultRequeueRejected(true);
            //        //手动确认接收到了消息
            //        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
            //自动重连 重新消费
            factory.setMissingQueuesFatal(false);
            log.info("监听者设置完成");
            listenerMap.put(key, factory);
        } else {
            factory = listenerMap.get(key);
        }

        return factory;
    }

    public static SSLContext getSSLSocktet(String crtPath) {

        try {
            // CA certificate is used to authenticate server
            CertificateFactory cAf = CertificateFactory.getInstance("X.509");
            ClassPathResource caIn = new ClassPathResource(crtPath);
            X509Certificate ca = (X509Certificate) cAf.generateCertificate(caIn.getInputStream());
            KeyStore caKs = KeyStore.getInstance("JKS");
            caKs.load(null, null);
            caKs.setCertificateEntry("ca-certificate", ca);
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(caKs);
            // finally, create SSL socket factory
            SSLContext context = SSLContext.getInstance("TLSv1.2");
            context.init(null, tmf.getTrustManagers(), new SecureRandom());
            return context;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    @Bean("rabbitTemplate")
    @ConditionalOnBean(ConnectionFactory.class)
    public RabbitTemplate rabbitTemplate(@Qualifier("connectionFactory") ConnectionFactory connectionFactory) {
        String key = rabbitProperties.getHost() + rabbitProperties.getPort();
        RabbitTemplate template;
        if (!templateMap.containsKey(key)) {
            template = new RabbitTemplate(connectionFactory);
            template.setMessageConverter(new FastJsonMessageConverter());
            if (rabbitProperties.getTemplate().getRetry().isEnabled()) {
                RetryTemplate retryTemplate = new RetryTemplate();
                SimpleRetryPolicy policy = new SimpleRetryPolicy();
                policy.setMaxAttempts(rabbitProperties.getTemplate().getRetry().getMaxAttempts());
                retryTemplate.setRetryPolicy(policy);
                template.setRetryTemplate(retryTemplate);
            }
            templateMap.put(key, template);
        } else {
            template = templateMap.get(key);
        }
        return template;
    }

    @Bean("rabbitAdmin")
    @ConditionalOnBean(ConnectionFactory.class)
    RabbitAdmin rabbitAdmin(@Qualifier("rabbitTemplate") RabbitTemplate rabbitTemplate) {
        String key = rabbitProperties.getHost() + rabbitProperties.getPort();
        RabbitAdmin ra;
        if (!adminMap.containsKey(key)) {
            ra = new RabbitAdmin(rabbitTemplate);
            adminMap.put(key, ra);
        } else {
            ra = adminMap.get(key);
        }
        return ra;
    }


    @Bean("rabbitOperator")
    @ConditionalOnBean(ConnectionFactory.class)
    RabbitOperator rabbitOperator(@Qualifier("rabbitTemplate") RabbitTemplate rabbitTemplate) {
        return new RabbitOperator(rabbitTemplate, rabbitProperties);//,restTemplate);
    }


    private static ApplicationContext applicationContext;

    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

}
