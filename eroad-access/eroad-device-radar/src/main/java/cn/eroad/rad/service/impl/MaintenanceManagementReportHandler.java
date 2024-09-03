package cn.eroad.rad.service.impl;

import cn.eroad.device.operate.DeviceCache;
import cn.eroad.rad.config.AppConfig;
import cn.eroad.rad.config.DataConfig;
import cn.eroad.rad.model.ParsedData;
import cn.eroad.rad.service.OperationTypeHandler;
import cn.eroad.rad.service.MessageCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 设备维护的应答
 *
 * @author mrChen
 * @date 2022/7/6 16:09
 */
@Component
@Slf4j
public class MaintenanceManagementReportHandler extends MessageCache implements OperationTypeHandler<Void, ParsedData> {

    public String OperationType = DataConfig.operation_Maintenance_management_report;

    @Override
    public Void handler(ParsedData o) {
        String sn1 = DeviceCache.getSnByIpFromMap(o.getIp());
        String key = "";
        switch (o.getObjectIDStr()) {
            case DataConfig.object_mmw_reboot:
                key = sn1 + "-" + AppConfig.REDIS_REBOOT;
                break;
            case DataConfig.object_mmw_reset:
                key = sn1 + "-" + AppConfig.REDIS_RESET;
                break;
            default:
                break;
        }
        String resultStr = o.getMsg().substring(0, 2);
        Integer result = Integer.valueOf(resultStr, 16);
        if (result == 0) {
            storeMessageToCaches(sn1, null, true, null, null, key, null);
        } else {
            String errorDesc = "";
            switch (o.getObjectIDStr()) {
                case DataConfig.object_mmw_reboot:
                    errorDesc = "重启失败";
                    break;
                case DataConfig.object_mmw_reset:
                    errorDesc = "恢复出厂设置失败";
                    break;
            }
            storeMessageToCaches(sn1, null, false, null, errorDesc, key, null);
        }
        return null;
    }

}
