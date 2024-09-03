package cn.eroad.device.controller;

import cn.eroad.core.domain.CommonContent;
import cn.eroad.core.domain.PageDomain;
import cn.eroad.device.entity.deviceAlarm.DeviceAlarmInfo;
import cn.eroad.device.entity.query.DeviceAlarmInfoQuery;
import cn.eroad.device.service.alarmService.DeviceAlarmInfoService;
import cn.eroad.trail.annotation.OperLog;
import cn.eroad.trail.enums.BusinessType;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author jingkang
 * @since 2022-05-10
 */
@RestController
@RequestMapping("/device/alarm")
@Slf4j
@Api(value = "设备告警管理", tags = "设备告警管理")
public class DeviceAlarmInfoController {

    @Autowired
    private DeviceAlarmInfoService deviceAlarmInfoService;

    @PostMapping("/page")
    @ApiOperation(value = "分页查询设备告警信息", notes = "通过条件分页查询设备告警信息")
    @OperLog(title = "告警日志分页查询", businessType = BusinessType.SELECT,isSaveRequestData = false)
    public CommonContent<PageDomain<DeviceAlarmInfo>> getDeviceAlarmInfoPage(@RequestBody DeviceAlarmInfoQuery deviceAlarmInfoQuery) {
        PageDomain<DeviceAlarmInfo> pageInfo = deviceAlarmInfoService.getPage(deviceAlarmInfoQuery);
        return CommonContent.ok(pageInfo);
    }

    @PostMapping("/expire")
    @ApiOperation(value = "清除超时设备告警信息", notes = "清除超时设备告警信息")
    @OperLog(title = "平台(设备)管理", businessType = BusinessType.DELETE)
    public CommonContent expire(@RequestParam(defaultValue = "60") Integer days){
        int effectRows = deviceAlarmInfoService.expire(days.equals(null)?60:days);
        return CommonContent.ok("已清除超时设备告警信息"+effectRows+"条");
    }

}

