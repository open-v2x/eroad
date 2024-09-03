package cn.eroad.device.service.config;

import org.springframework.context.annotation.Configuration;

/**
 * @author yujinfu
 * @version 1.0
 * @create 2021/11/15
 * @description
 */
@Configuration
public class DeviceAdminRabbitConfig {
    public static final String QUEUE_REPORT_DEVICE_ALARM_SHORTLWT = "device_ac_shortlwt_alarmInfo";
    public static final String QUEUE_REPORT_DEVICE_ALARM = "device_ac_alarmInfo";
}
