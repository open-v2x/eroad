package cn.eroad.device.entity.nacos;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value="采控平台服务实例列表对象", description="采控平台服务实例列表对象")
public class AcServiceInstanceList {

    @ApiModelProperty(value = "服务实例数量")
    private Integer count;

    @ApiModelProperty(value = "当前页数")
    private Integer pageNo;

    @ApiModelProperty(value = "每页数量")
    private Integer pageSize;

    @ApiModelProperty(value = "采控服务实例集合")
    private List<AcServiceInstance> list;

    public Integer getTotalCount(){
        return count;
    }

    public Integer getPageCount(){
        return (list==null|| list.size()==0)?0:list.size();
    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel(value="采控平台服务实例对象", description="采控平台服务实例对象")
    public static class AcServiceInstance {

        @ApiModelProperty(value = "采控服务实例ID")
        private String instanceId;
        @ApiModelProperty(value = "采控服务实例IP")
        private String ip;
        @ApiModelProperty(value = "采控服务实例端口")
        private Integer port;
        @ApiModelProperty(value = "采控服务实例权重")
        private Integer weight;
        @ApiModelProperty(value = "是否健康")
        private Boolean healthy;
        @ApiModelProperty(value = "是否上线")
        private Boolean enabled;
        @ApiModelProperty(value = "是否临时实例")
        private Boolean ephemeral;

        @ApiModelProperty(value = "所属集群")
        private String clusterName;

        @ApiModelProperty(value = "服务名")
        private String serviceName;

        @ApiModelProperty(value = "服务心跳周期")
        private Integer instanceHeartBeatInterval;

        @ApiModelProperty(value = "心跳超时时间")
        private Integer instanceHeartBeatTimeOut;

        @ApiModelProperty(value = "超时删除时间")
        private Integer ipDeleteTimeout;

        @ApiModelProperty(value = "服务运行ID")
        private String serviceRunnerId;

        @ApiModelProperty(value = "服务ServiceId")
        private String serviceId;

        @ApiModelProperty(value = "服务版本")
        private String version;

        @ApiModelProperty(value = "其他业务端口")
        private String otherPorts;


        @ApiModelProperty(value = "元数据")
        private Map<String,String> metadata;

        public void setMetadata(Map<String, String> metadata) {
            this.metadata = metadata;

            if(metadata!=null){
                this.serviceRunnerId = metadata.get("serviceRunnerId");

                this.serviceId = metadata.get("serviceId");

                this.serviceName = metadata.get("serviceName");

                this.version = metadata.get("version");

                this.otherPorts = metadata.get("otherPorts");

            }
        }
    }

}
