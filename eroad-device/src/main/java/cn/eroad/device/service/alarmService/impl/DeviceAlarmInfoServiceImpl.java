package cn.eroad.device.service.alarmService.impl;

import cn.eroad.core.domain.PageDomain;
import cn.eroad.device.mapper.DeviceAlarmInfoMapper;
import cn.eroad.device.entity.deviceAlarm.DeviceAlarmInfo;
import cn.eroad.device.entity.query.DeviceAlarmInfoQuery;
import cn.eroad.device.service.alarmService.DeviceAlarmInfoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author jingkang
 * @since 2022-05-10
 */
@Service
@Slf4j
@EnableScheduling
public class DeviceAlarmInfoServiceImpl extends ServiceImpl<DeviceAlarmInfoMapper, DeviceAlarmInfo> implements DeviceAlarmInfoService {

    @Override
    public PageDomain<DeviceAlarmInfo> getPage(DeviceAlarmInfoQuery deviceAlarmInfoQuery) {

        PageInfo<DeviceAlarmInfo> page = PageHelper.startPage(deviceAlarmInfoQuery.getPageNum(), deviceAlarmInfoQuery.getPageSize())
                .doSelectPageInfo(() -> baseMapper.selectList(deviceAlarmInfoQuery.buildWrapper()));
        return PageDomain.from(page);
    }

    @Override
    public int expire(Integer days) {
        Date date = DateUtils.addDays(new Date(), -days);
        QueryWrapper<DeviceAlarmInfo> wrapper = new QueryWrapper<>();
        wrapper.lt("gmt_created",date);
        int effectRows = baseMapper.delete(wrapper);
        if (effectRows>0){
            log.info("已清除超时日志{}条",effectRows);
        }
        return effectRows;
    }
}
