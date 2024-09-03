package cn.eroad.device.entity.nacos;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value="采控平台服务列表对象", description="采控平台服务列表对象")
public class AcServiceList {

    @ApiModelProperty(value = "服务数量")
    private Integer count;

    @ApiModelProperty(value = "当前页数")
    private Integer pageNo;

    @ApiModelProperty(value = "每页数量")
    private Integer pageSize;

    @ApiModelProperty(value = "采控服务集合")
    private List<AcService> serviceList;


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel(value="采控平台服务对象", description="采控平台服务对象")
    public static class AcService {

        @ApiModelProperty(value = "采控服务名称")
        private String name;

        @ApiModelProperty(value = "采控服务分组")
        private String groupName;

        @ApiModelProperty(value = "集群数目")
        private Integer clusterCount;

        @ApiModelProperty(value = "实例数")
        private Integer ipCount;

        @ApiModelProperty(value = "健康实例数")
        private Integer healthyInstanceCount;

        @ApiModelProperty(value = "触发保护阈值")
        private Boolean triggerFlag;
    }

}
