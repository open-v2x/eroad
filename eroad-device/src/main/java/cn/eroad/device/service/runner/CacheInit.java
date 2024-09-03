package cn.eroad.device.service.runner;

import cn.eroad.device.mapper.DeviceMaintainMapper;
import cn.eroad.device.entity.devicemaintain.DeviceMaintain;
import cn.eroad.device.operate.DeviceCache;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
@Slf4j
public class CacheInit implements CommandLineRunner {
    @Autowired
    private DeviceMaintainMapper deviceMaintainMapper;
    @Autowired
    private DeviceCache deviceCache;

    @Override
    public void run(String... args) {
        PageInfo<DeviceMaintain> pageInfo;
        // 所有已经注册设备
        ArrayList<DeviceMaintain> deviceMaintains = new ArrayList<>();
        int pageNum = 0;
        int pageSize = 1000;
        do {
            pageNum++;
            pageInfo = PageHelper.startPage(pageNum, pageSize).doSelectPageInfo(() -> {
                deviceMaintainMapper.selectList(new QueryWrapper<>());
            });
            deviceMaintains.addAll(pageInfo.getList());
        } while (pageNum < pageInfo.getPages());
        // 注册设备初始化到缓存
        deviceCache.initDeviceCache(deviceMaintains);

    }
}
