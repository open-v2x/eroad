package cn.eroad.rad.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author WuHang
 * @version 1.0
 * @description:
 * @date 2022/6/13 15:44
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Net {
    @ApiModelProperty(value = "IPV4网关")
    private String ipv4Gateway;
    @ApiModelProperty(value = "IPV4子网掩码")
    private String ipv4Mask;
    @ApiModelProperty(value = "IPV4地址")
    private String ipv4Address;
    @ApiModelProperty(value = "IPV6-网关")
    private String ipv6Gateway;
    @ApiModelProperty(value = "IPV6子网掩码")
    private String ipv6Mask;
    @ApiModelProperty(value = "IPV6-LLA地址")
    private String ipv6LlaAddress;
    @ApiModelProperty(value = "IPV6-GUA地址")
    private String ipv6GuaAddress;
    @ApiModelProperty(value = "路侧毫米波雷达本地监听的UDP端口号")
    private int localPort;
    @ApiModelProperty(value = "上层系统监听的UDP端口号")
    private int targetPort;
    @ApiModelProperty(value = "目标IP")
    private String targetIp;
    @ApiModelProperty(value = "点云上报端口上层系统监听的UDP端口,如果为空，则点云数据都向目标端口上报")
    private int pcUpPort;
    @ApiModelProperty(value = "心跳间隔，单位秒")
    private int heartInterval;
    @ApiModelProperty(value = "路侧毫米波雷达设备物理地址 ")
    private String mac;


}
