package cn.eroad.rad.model;

import cn.eroad.rad.model.response.ResultType;
import cn.eroad.rad.vo.Net;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class Prop {
    @ApiModelProperty(value = "id")
    private String id;
    @ApiModelProperty(value = "设备编号")
    private String sn;
    @ApiModelProperty(value = "查询结果")
    private ResultType result;
    @ApiModelProperty(value = "查询结果编码")
    private Integer resultCode;
    @ApiModelProperty(value = "查询结果信息")
    private String resultMsg;
    @ApiModelProperty(value = "业务信息")
    private ConfData confData;
    @ApiModelProperty(value = "网络信息")
    private Net net;
    @ApiModelProperty(value = "工作信息")
    private WorkingStatus workingStatus;


}
