package cn.eroad.device.service.runner;

import cn.eroad.core.dto.DeviceConfigDTO;
import cn.eroad.device.mapper.DeviceConfigInfoMapper;
import cn.eroad.device.operate.DeviceCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@Slf4j
public class DeviceConfigDataInit implements CommandLineRunner {
    @Autowired
    private DeviceConfigInfoMapper configInfoMapper;

    @Autowired
    private DeviceCache deviceCache;

    /**
    * @Description: 数据库同步redis初始化
    * @Param:
    * @return:
    * @Author: nbr
    * @Date: 2022/8/26
    */
    @Override
    public void run(String... args) {
        log.info("设备配置数据同步到缓存");
        List<DeviceConfigDTO> list = configInfoMapper.getDeviceConfigDTO();
        if(list != null && list.size() > 0){
            deviceCache.saveDeviceConfigList(list);
        }
    }
}
