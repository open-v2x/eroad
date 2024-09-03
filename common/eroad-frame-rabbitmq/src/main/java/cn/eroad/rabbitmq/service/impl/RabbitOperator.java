package cn.eroad.rabbitmq.service.impl;

import cn.eroad.rabbitmq.bean.ClientConnection;
import cn.eroad.rabbitmq.config.RabbitConfig;
import cn.eroad.rabbitmq.service.MessageOperator;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;




import org.springframework.util.Base64Utils;
import org.springframework.util.StringUtils;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author yujinfu
 * @version 1.0
 * @create 2021/10/26
 * @description RabbitMQ消息发送类
 */
@Slf4j
public class RabbitOperator implements MessageOperator {

    //    @Value("${spring.rabbitmq.host}")













    private RabbitTemplate rabbitTemplate;

    private RabbitProperties rabbitProperties;

    //    @Resource(name = "rabbitAdmin")
    private RabbitAdmin rabbitAdmin;





    public RabbitOperator(RabbitTemplate rabbitTemplate, RabbitProperties rabbitProperties) {//},RestTemplate restTemplate){
        this.rabbitTemplate = rabbitTemplate;
        this.rabbitProperties = rabbitProperties;

    }

    /**
     * 发送topic消息
     *
     * @param exchange   交换机
     * @param routingKey 路由key
     * @param payload    消息内容
     * @param qos
     */
    @Override
    public void sendTopic(String exchange, String routingKey, Object payload, final int qos) {

        log.info("统一出口发送数据；{}，{}，{}", exchange, routingKey, JSON.toJSONString(payload, SerializerFeature.DisableCircularReferenceDetect));
        rabbitTemplate.convertAndSend(exchange, routingKey, payload, new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {



                return message;
            }
        });
    }

    @Override
    public void sendTopic(String exchange, String routingKey, Object payload) {
        log.info("统一出口发送数据；{}，{}，{}", exchange, routingKey, JSON.toJSONString(payload, SerializerFeature.DisableCircularReferenceDetect));
        rabbitTemplate.convertAndSend(exchange, routingKey, payload, new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {



                return message;
            }
        });
    }

    @Override
    public void sendExchange(String exchange, Object payload) {
        log.info("统一出口发送数据；{}，{}", exchange, JSON.toJSONString(payload, SerializerFeature.DisableCircularReferenceDetect));
        rabbitTemplate.convertAndSend(exchange, "", payload, new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {



                return message;
            }
        });
    }


    /**
     * 发送topic消息 默认过期时间为60秒，主要用于mqtt协议通信
     *
     * @param exchange   交换机
     * @param routingKey 路由key
     * @param payload    消息内容
     * @param qos
     */
    @Override
    public void sendTopicWithExpir(String exchange, String routingKey, Object payload, final int qos) {

        log.info("统一出口发送数据；{}，{}，{}", exchange, routingKey, JSON.toJSONString(payload, SerializerFeature.DisableCircularReferenceDetect));
        rabbitTemplate.convertAndSend(exchange, routingKey, payload, new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                message.getMessageProperties().setExpiration("60000"); //暂时按照60s过期处理
                //此处qos必须是byte类型，否则会导致mqtt客户端读取异常马上断开
                message.getMessageProperties().setHeader("x-mqtt-publish-qos", (byte) qos);
                return message;
            }
        });
    }

    /**
     * 发送topic消息 默认过期时间为60秒，主要用于mqtt协议通信
     *
     * @param exchange
     * @param routingKey
     * @param payload
     */
    @Override
    public void sendTopicWithExpir(String exchange, String routingKey, Object payload) {
        log.info("统一出口发送数据；{}，{}，{}", exchange, routingKey, JSON.toJSONString(payload, SerializerFeature.DisableCircularReferenceDetect));
        rabbitTemplate.convertAndSend(exchange, routingKey, payload, new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                message.getMessageProperties().setExpiration("60000"); //暂时按照60s过期处理
                //此处qos必须是byte类型，否则会导致mqtt客户端读取异常马上断开
                message.getMessageProperties().setHeader("x-mqtt-publish-qos", (byte) RabbitConfig.MESSAGE_QOS);
                return message;
            }
        });
    }


    /**
     * 需要连接rabbitmq的user具有management的权限
     * 根据连接的客户端类型，从rabbitmq broker获取连接的客户端数据
     *
     * @param clientType 传空表示获取所有的客户端类型
     * @return
     */
    @Override
    public List<ClientConnection> getConnectionList(ClientConnection.ClientType clientType) throws IOException {
        List<ClientConnection> connectionList = new ArrayList<>();
        int page = 1;
        int size = 100;
        ClientConnection.Connections connections = null;
        while (true) {
            Request request = new Request.Builder()

                    .url("http://" + rabbitProperties.getHost() + ":" + rabbitProperties.getPort() + "/controller/connections?page=" + page + "&page_size=" + size + "&name=&use_regex=false")
                    .addHeader("Authorization", "Basic " + Base64Utils.encodeToString((rabbitProperties.getUsername() + ":" + rabbitProperties.getPassword()).getBytes()))
                    .build();
            OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                    .build();
            //获取响应并把响应体返回
            Response response = okHttpClient.newCall(request).execute();
            if (!response.isSuccessful() || response.code() != 200) {
                break;
            }
            page++;

            connections = JSON.parseObject(response.body().string(), ClientConnection.Connections.class);

            if (connections == null || connections.getItem_count() == 0) {
                break;
            }

            connections.getItems().stream().forEach(item -> {
                if (item.getClient_properties() != null &&
                        !StringUtils.isEmpty(item.getClient_properties().getProduct()) &&
                        clientType != null ? item.getClient_properties().getProduct().startsWith(clientType.name()) : true) {
                    ClientConnection connection = new ClientConnection();
                    connection.setClientAddress(item.getClientAddress());
                    connection.setClientId(item.getClientId());
                    connection.setClientType(clientType == null ? item.getClientType() : clientType);
                    connection.setConnectedTime(new Date(item.getConnected_at()));
                    connection.setProtocol(item.getProtocol());
                    connection.setRunning(item.isRunning());
                    connectionList.add(connection);
                }
            });
        }

        return connectionList;
    }


}
