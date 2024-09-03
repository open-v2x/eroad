package cn.eroad.videocast.controller;

import cn.eroad.videocast.model.data.Response;
import cn.eroad.videocast.util.HttpClientUtil;
import com.alibaba.fastjson.JSONObject;
import cn.eroad.videocast.model.config.ReturnFlushSubscription;
import cn.eroad.videocast.model.config.ReturnSubscribe;
import cn.eroad.videocast.model.config.SubscribeVehicleCondition;
import cn.eroad.videocast.service.RegistService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description: 设备配置
 * @Param:
 * @return:
 * @Author: nbr
 * @Date: 2022/6/20
 */
@RestController
@RequestMapping("/LAPI/V1.0/System/Event")
@Slf4j
public class ConfigureController {
    @Autowired
    RegistService registService;

    @PostMapping("/Subscription")
    @ApiOperation(value = "数据订阅")
    public Response subscription(SubscribeVehicleCondition subscribeVehicleCondition) {
        //设备配置上报
        registService.deviceConfig(subscribeVehicleCondition);

        //调用设备，订阅功能
        HttpClientUtil.getHttp("/LAPI/V1.0/System/Event/Subscription" + subscribeVehicleCondition, null);
        Response responseData = new Response();
        responseData.setResponseURL("/LAPI/V1.0/System/Event/Subscription");
        responseData.setResponseString("succeed");
        responseData.setCseq(Long.valueOf(""));
        return responseData;
    }

    @PostMapping("/Subscription/{ID}")
    @ApiOperation(value = "刷新/取消订阅")
    public Response subscriptionId(@PathVariable Long id, Long duration) {
        //调用设备，取消订阅
        HttpClientUtil.getHttp("/LAPI/V1.0/System/Event/Subscription" + id + duration, null);
        Response responseData = new Response();
        responseData.setResponseURL("/LAPI/V1.0/System/Event//Subscription/{ID}");

        responseData.setResponseString("succeed");
        responseData.setCseq(Long.valueOf(""));
        return responseData;
    }

}
