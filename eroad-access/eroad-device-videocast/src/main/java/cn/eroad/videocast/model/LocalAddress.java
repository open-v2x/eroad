package cn.eroad.videocast.model;

import lombok.Data;

/**
 * @Description: 本地地址信息
 * @Param:
 * @return:
 * @Author: nbr
 * @Date: 2022/4/28
 */
@Data
public class LocalAddress {
    //地址类型 0：IPv4 1：IPv6 2：域名
    private Integer AddressType;

    //服务器地址
    private String Address;
}
