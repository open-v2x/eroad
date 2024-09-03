package cn.eroad.device.entity.deviceConfig;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
@ApiModel(description = "设备配置列表参数")
public class ListParamVo {
    @ApiModelProperty(value = "当前页数",required = true)
    @NotBlank
    private Integer pageNum;

    @ApiModelProperty(value = "每页条数",required = true)
    @NotBlank
    private Integer pageSize;

    @ApiModelProperty(value = "设备类型")
    private String deviceType;

    @ApiModelProperty(value = "配置类型")
    private String configType;

    @ApiModelProperty(value = "设备id列表")
    private List<String> snList;

    public Integer getPageNum() {
        return pageNum = "".equals(pageNum) || pageNum ==null ? 0 : pageNum;
    }

    public Integer getPageSize() {
        return pageSize = "".equals(pageSize) || pageSize ==null ? 10 : pageSize;
    }
}
