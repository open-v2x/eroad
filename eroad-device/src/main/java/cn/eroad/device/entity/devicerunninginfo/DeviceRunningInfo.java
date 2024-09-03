package cn.eroad.device.entity.devicerunninginfo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @author WuHang
 * @version 1.0
 * @description:
 * @date 2022/5/17 9:51
 */
@Data
@ApiModel
@NoArgsConstructor
@AllArgsConstructor
@TableName("device_info_list")
public class DeviceRunningInfo implements Serializable {
    @TableId(value = "id",type = IdType.AUTO)
    @ApiModelProperty(value = "主键自增Id")
    private Integer id;
    @TableField(value = "sn")
    @ApiModelProperty(value = "sn号，设备唯一标识")
    private String sn;
    @TableField(value = "data_json")
    @ApiModelProperty(value = "设备运行时信息JSON串")
    private String dataJson;

    @TableField(value = "update_time")
    @ApiModelProperty(value = "修改时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
    @TableField(value = "device_type")
    @ApiModelProperty(value = "设备类型,灯控 lamp，RSU rsu，毫米波雷达 rad，激光雷达 lid，ONU onu，摄像头 cam")
    private String deviceType;
    @TableField(value = "status_type")
    @ApiModelProperty(value = "状态类型,周期状态 cycle，变化状态 change，设备状态 physical，业务状态 biz")
    private String statusType;

    private static final long serialVersionUID=111L;
}
