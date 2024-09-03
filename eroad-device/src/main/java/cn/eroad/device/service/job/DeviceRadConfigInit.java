package cn.eroad.device.service.job;

import cn.eroad.device.client.invoker.DeviceRadConfigInvoker;
import cn.eroad.device.mapper.DeviceMaintainMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
* @Description: 定时任务主动查询毫米波雷达配置
* @Param:
* @return:
* @Author: nbr
* @Date: 2022/7/28
*/
@Component
@Slf4j
public class DeviceRadConfigInit {
    @Autowired
    private DeviceRadConfigInvoker radConfigInvoker;

    @Autowired
    private DeviceMaintainMapper maintainMapper;

    /**
     * 每天凌晨1点执行一次
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void radConfigInit(){
        log.info("调用毫米波雷达配置接口");
        List<String> list = maintainMapper.getSnListByType("rad");
        radConfigInvoker.radApi(list);
    }
}
