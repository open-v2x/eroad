package cn.eroad.rad.service.impl;

import cn.eroad.core.utils.StringUtil;
import cn.eroad.device.operate.DeviceCache;
import cn.eroad.rad.config.AppConfig;
import cn.eroad.rad.config.DataConfig;
import cn.eroad.rad.model.ParsedData;
import cn.eroad.rad.service.OperationTypeHandler;
import cn.eroad.rad.service.MessageCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 设置应答
 *
 * @author mrChen
 * @date 2022/7/6 16:08
 */
@Component
@Slf4j
public class SetAnswerHandler extends MessageCache implements OperationTypeHandler<Void, ParsedData> {

    public String OperationType = DataConfig.operation_set_answer;

    @Override
    public Void handler(ParsedData o) {
        switch (o.getObjectIDStr()) {
            case DataConfig.object_mmw_network_data:
                //雷达网络参数设置应答
                String sn = DeviceCache.getSnByIpFromMap(o.getIp()).trim();
                if (StringUtil.isNotEmpty(sn)) {
                    String key = sn + "-" + AppConfig.REDIS_SET_NET;
                    storeMessageToCacheWhenSet(key, sn);
                }
                break;
            case DataConfig.object_mmw_conf_data:
                //配置参数设置应答
                String sn1 = DeviceCache.getSnByIpFromMap(o.getIp()).trim();
                if (StringUtil.isNotEmpty(sn1)) {
                    String key = sn1 + "-" + AppConfig.REDIS_SET_CONFIG;
                    storeMessageToCacheWhenSet(key, sn1);
                }
                break;
        }
        return null;
    }

}
