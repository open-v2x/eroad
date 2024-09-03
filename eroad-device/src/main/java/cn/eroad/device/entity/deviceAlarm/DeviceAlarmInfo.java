package cn.eroad.device.entity.deviceAlarm;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 *
 * </p>
 *
 * @author jingkang
 * @since 2022-05-18
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DeviceAlarmInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "主键id")
    private Integer id;

    /**
     * 设备sn码
     */
    @ApiModelProperty(value = "设备sn码")
    private String sn;

    /**
     * 设备类型
     */
    @ApiModelProperty(value = "设备sn码")
    private String deviceType;

    /**
     * 告警状态，0:报警消失，1:告警开始
     */
    @ApiModelProperty(value = "设备sn码")
    private Integer alarmStatus;

    /**
     * 告警类型，0:设备报警；1:业务报警
     */
    @ApiModelProperty(value = "设备sn码")
    private Integer alarmType;

    /**
     * 告警信息的中文简短描述
     */
    @ApiModelProperty(value = "告警信息的中文简短描述")
    private String alarmMessage;

    /**
     * 告警级别
一级、二级、三级、四级
取值范围：[1,4]
四级： 严重告警:使业务中断并需要立即
进行故障检修的告警;
三级： 主要告警:影响业务并需要立即进
行故障检修的告警;
二级： 次要告警:不影响现有业务，但需
进行检修以阻止恶化的告警;
一
级： 警告告警:不影响现有业务，但发
展下去有可能影响业务，可视需要采取措施
的告警
     */
    @ApiModelProperty(value = "告警级别")
    private Integer alarmLevel;

    /**
     * 告警时间
     */
    @ApiModelProperty(value = "告警时间")
    private Date gmtAlarm;

    /**
     * 上报时间
     */
    @ApiModelProperty(value = "上报时间")
    private Date gmtReport;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    private Date gmtCreated;

    /**
     * 告警信息结构数据
     */
    @ApiModelProperty(value = "告警信息结构数据")
    private String info;


}
