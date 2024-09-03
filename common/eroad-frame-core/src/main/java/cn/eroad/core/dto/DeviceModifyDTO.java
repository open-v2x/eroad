package cn.eroad.core.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @project: eroad-frame
 * @ClassName: DeviceModifyDTO
 * @author: liyongqiang
 * @creat: 2022/9/13 9:24
 * 描述: 设备更新操作消息类，新增、删除、修改
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("设备更新操作消息类")
public class DeviceModifyDTO {

    @ApiModelProperty("设备编码")
    private String sn;

    @ApiModelProperty("设备类型")
    private String deviceType;

    @ApiModelProperty("修改类型: add 新增，del 删除，update 修改")
    private String modifyType;

    @ApiModelProperty("设备变更时间")
    private Date updateTime;

    @ApiModelProperty("设备编码-旧")
    private String oldSn;
}
