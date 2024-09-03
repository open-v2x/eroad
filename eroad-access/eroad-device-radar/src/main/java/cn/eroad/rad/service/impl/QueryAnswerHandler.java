package cn.eroad.rad.service.impl;

import cn.eroad.core.utils.StringUtil;
import cn.eroad.rad.util.RabbitMqUtil;
import cn.eroad.device.operate.DeviceCache;
import cn.eroad.rad.service.MessageCache;
import cn.eroad.rad.service.OperationTypeHandler;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import cn.eroad.rad.config.AppConfig;
import cn.eroad.rad.config.DataConfig;
import cn.eroad.rad.model.ParsedData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 查询应答(需要)
 *
 * @author mrChen
 * @date 2022/7/6 15:53
 */
@Component
@Slf4j
public class QueryAnswerHandler extends MessageCache implements OperationTypeHandler<Void, ParsedData> {

    public String OperationType = DataConfig.operation_query_answer;

    @Override
    public Void handler(ParsedData o) {
        JSONObject dataObject;
        switch (o.getObjectIDStr()) {
            case DataConfig.object_mmw_network_data:
                //雷达网络参数查询应答
                dataObject = objectMmwNetworkData(o.getMsg());
                String sn = DeviceCache.getSnByIpFromMap(o.getIp()).trim();
                String key = sn + "-" + AppConfig.SEARCH_NET;
                if (StringUtil.isNotEmpty(sn)) {
                    storeMessageToCaches(sn, JSON.toJSONString(dataObject), true, null, null, key, AppConfig.SEARCH_NET);
                }

                //网络配置上报
                log.info("网络参数配置信息上报sn：{} 数据{}", sn, dataObject);
                RabbitMqUtil.configInfo(sn, "physical", dataObject.toJSONString());
                break;
            case DataConfig.object_mmw_conf_data:
                //配置参数查询应答
                dataObject = objectMmwConfData(o.getMsg());
                String sn1 = DeviceCache.getSnByIpFromMap(o.getIp()).trim();
                String key1 = sn1 + "-" + AppConfig.SEARCH_CONFIG;
                if (StringUtil.isNotEmpty(sn1)) {
                    storeMessageToCaches(sn1, JSON.toJSONString(dataObject), true, null, null, key1, AppConfig.SEARCH_CONFIG);
                }

                //业务配置上报
                log.info("业务参数配置信息上报sn：{} 数据{}", sn1, dataObject);
                RabbitMqUtil.configInfo(sn1, "biz", dataObject.toJSONString());
                break;
            case DataConfig.object_mmw_Working_status_data:
                //毫米波雷达工作状态查询应答
                String sn2 = DeviceCache.getSnByIpFromMap(o.getIp()).trim();
                dataObject = objectMmwWorkingStatusData(o.getMsg(), sn2);
                log.info("工作状态配置信息上报sn：{} 数据{}", sn2, dataObject);
                String key2 = sn2 + "-" + AppConfig.SEARCH_WORKSTATUS;
                if (StringUtil.isNotEmpty(sn2)) {
                    storeMessageToCaches(sn2, JSON.toJSONString(dataObject), true, null, null, key2, AppConfig.SEARCH_WORKSTATUS);
                }


                break;
        }
        return null;
    }

}
