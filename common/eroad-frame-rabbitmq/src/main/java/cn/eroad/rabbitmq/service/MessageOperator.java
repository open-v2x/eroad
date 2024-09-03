package cn.eroad.rabbitmq.service;

import cn.eroad.rabbitmq.bean.ClientConnection;

import java.io.IOException;
import java.util.List;

/**
 * @author liyongqiang
 * @version 1.0
 * @description
 */
public interface MessageOperator {


    /**
     * 发送topic消息
     *
     * @param exchange   交换机
     * @param routingKey 路由key
     * @param payload    消息内容
     * @param qos
     */
    public void sendTopic(String exchange, String routingKey, Object payload, final int qos);

    /**
     * 发送topic消息
     *
     * @param exchange   交换机
     * @param routingKey 路由key
     * @param payload    消息内容
     */
    public void sendTopic(String exchange, String routingKey, Object payload);


    /**
     * 发送消息到交换机
     *
     * @param exchange 交换机
     * @param payload  消息内容
     */
    public void sendExchange(String exchange, Object payload);

    /**
     * 发送topic消息 默认过期时间为60秒，主要用于mqtt协议通信
     *
     * @param exchange   交换机
     * @param routingKey 路由key
     * @param payload    消息内容
     * @param qos
     */
    public void sendTopicWithExpir(String exchange, String routingKey, Object payload, final int qos);

    /**
     * 发送topic消息 默认过期时间为60秒，主要用于mqtt协议通信
     *
     * @param exchange
     * @param routingKey
     * @param payload
     */
    public void sendTopicWithExpir(String exchange, String routingKey, Object payload);




    /**
     * 需要连接rabbitmq的user具有management的权限
     * 根据连接的客户端类型，从rabbitmq broker获取连接的客户端数据
     *
     * @param clientType 传空表示获取所有的客户端类型
     * @return 返回客户端连接列表
     */
    List<ClientConnection> getConnectionList(ClientConnection.ClientType clientType) throws IOException;
}
