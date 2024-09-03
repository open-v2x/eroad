package cn.eroad.rad.util;

import cn.eroad.rad.config.DmsConfig;
import cn.eroad.rabbitmq.service.impl.RabbitOperator;
import cn.eroad.core.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

@Component
public class RabbitMqUtil {

    private static RabbitOperator rabbitOperator;

    @Autowired
    public void setRabbitOperator(RabbitOperator rabbitOperator) {
        RabbitMqUtil.rabbitOperator = rabbitOperator;
    }

    //遗言上报
    public static void reportOnline(String sn, int online) {
        DeviceOnlineDTO deviceOnlineDTO = new DeviceOnlineDTO(sn, "rad", online, new Date(), "周期内未进行心跳上报");
        rabbitOperator.sendTopic(DmsConfig.EXCHANGE_LAST_WORDS, DmsConfig.ROUTING_KEY_LAST_WORDS, deviceOnlineDTO);
        //rabbitOperator.sendTopic(DmsConfig.EXCHANGE_SUPER_LAST_WORDS,DmsConfig.ROUTING_SUPER_KEY_LAST_WORDS,deviceOnlineDTO);
    }

    //设备上电注册上报
    public static void reportRegister(String sn, int online, DeviceRegisterDTO dgd) {
        //DeviceOnlineDTO deviceOnlineDTO = new DeviceOnlineDTO(sn, "rad", online, new Date());
        rabbitOperator.sendTopic(DmsConfig.EXCHANGE_REGISTER, DmsConfig.ROUTING_KEY_REGISTER, dgd);
    }

    //自动注册上报
    public static void reportAutoRegister(AutoRegisterDTO dgd) {
        //DeviceOnlineDTO deviceOnlineDTO = new DeviceOnlineDTO(sn, "rad", online, new Date());
        rabbitOperator.sendTopic(DmsConfig.EXCHANGE_AUTO_REGISTER, DmsConfig.ROUTING_KEY_AUTO_REGISTER, dgd);
    }

    //工作状态上报
    public static void workStatus(String sn, String type, String dataJson) {
        DeviceStatusDTO deviceStatusDTO = new DeviceStatusDTO(sn, "rad", type, new Date(), dataJson);
        rabbitOperator.sendTopic(DmsConfig.DMS_EXCHANGE_WORK_STATUS, DmsConfig.DMS_TOPIC_WORK_STATUS, deviceStatusDTO);
    }

    //业务参数上报
    public static void configInfo(String sn, String type, String dataJson) {
        DeviceConfigDTO deviceConfigDTO = new DeviceConfigDTO(sn, "rad", type, new Date(), dataJson);
        rabbitOperator.sendTopic(DmsConfig.EXCHANGE_CONFIG_INFO, DmsConfig.ROUTING_CONFIG_INFO, deviceConfigDTO);
    }

    //基础信息修改上报
    public static void baseInfo(DeviceBaseInfo dto) {
        rabbitOperator.sendTopic(DmsConfig.EXCHANGE_BASEINFO, DmsConfig.ROUTING_BASEINFO, dto);
    }
}
