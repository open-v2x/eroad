package cn.eroad.device.service.alarmService;

import cn.eroad.core.domain.PageDomain;
import cn.eroad.device.entity.deviceAlarm.DeviceAlarmInfo;
import cn.eroad.device.entity.query.DeviceAlarmInfoQuery;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author jingkang
 * @since 2022-05-10
 */
public interface DeviceAlarmInfoService extends IService<DeviceAlarmInfo> {

    PageDomain<DeviceAlarmInfo> getPage(DeviceAlarmInfoQuery deviceAlarmInfoQuery);

    int expire(Integer days);
}
