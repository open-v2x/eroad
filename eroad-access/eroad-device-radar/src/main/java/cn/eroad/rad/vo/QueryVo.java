package cn.eroad.rad.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Size;
import java.util.List;

@Data

public class QueryVo {
    /**
     * 编号
     */
    @ApiModelProperty("设备编号")
    private String sn;

    /**
     * 类型
     */
    @ApiModelProperty("查询类型 网络配置net，业务配置config，工作状态配置workStatus")
    private List<String> queryTypes;

}
