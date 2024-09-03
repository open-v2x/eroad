package cn.eroad.device.entity.query;

import cn.eroad.core.domain.QueryDomain;
import cn.eroad.core.domain.Sorter;
import cn.eroad.core.enums.OrderEnum;
import cn.eroad.core.utils.StringUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import cn.eroad.device.entity.deviceAlarm.DeviceAlarmInfo;


import java.util.Date;

/**
 * 设备告警查询表单
 * @create
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel(value="设备告警查询表单", description="设备告警查询表单")
public class DeviceAlarmInfoQuery extends QueryDomain<DeviceAlarmInfo> {

    /**
     * 设备sn码
     */
    @ApiModelProperty(value = "设备sn码")
    private String sn;

    /**
     * 设备类型
     */
    @ApiModelProperty(value = "设备类型")
    private String deviceType;

    /**
     * 告警状态，0:报警消失，1:告警开始
     */
    @ApiModelProperty(value = "告警状态")
    private Integer alarmStatus;

    /**
     * 告警类型，0:设备报警；1:业务报警
     */
    @ApiModelProperty(value = "告警类型")
    private Integer alarmType;

    /**
     * 告警级别
     */
    @ApiModelProperty(value = "告警级别")
    private Integer alarmLevel;

    /**
     * 查询时间
     */
    @ApiModelProperty(value = "时间选择器开始时间")
    private Date beginTime;

    @ApiModelProperty(value = "时间选择器结束时间")
    private Date endTime;


    @Override
    public QueryWrapper<DeviceAlarmInfo> buildWrapper() {
        Sorter sorter = this.getSorter();
        QueryWrapper<DeviceAlarmInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(StringUtil.isNotBlank(sn),"sn",this.getSn());
        queryWrapper.gt(StringUtil.isNotNull(beginTime),"gmt_created",this.getBeginTime());
        queryWrapper.lt(StringUtil.isNotNull(endTime),"gmt_created",this.getEndTime());
        queryWrapper.eq(StringUtil.isNotBlank(deviceType),"device_type",this.getDeviceType());
        queryWrapper.eq(StringUtil.isNotNull(alarmStatus),"alarm_status",this.getAlarmStatus());
        queryWrapper.eq(StringUtil.isNotNull(alarmType),"alarm_type",this.getAlarmType());
        queryWrapper.eq(StringUtil.isNotNull(alarmStatus),"alarm_status",this.getAlarmStatus());
        queryWrapper.eq(StringUtil.isNotNull(alarmLevel),"alarm_level",this.getAlarmLevel());
        boolean haveSortField = sorter != null && StringUtil.isNotBlank(sorter.getField());
        if (haveSortField) {
            queryWrapper.orderBy(true, OrderEnum.ASC.getValue().equalsIgnoreCase(sorter.getOrder()), StringUtil.underscoreName(sorter.getField()));
        }else{
            queryWrapper.orderBy(true, false, "gmt_created");
        }
        return queryWrapper;
    }

}
