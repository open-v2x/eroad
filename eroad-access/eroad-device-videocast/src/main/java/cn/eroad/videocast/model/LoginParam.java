package cn.eroad.videocast.model;

import lombok.Data;

/**
 * @Description: 登录参数
 * @Param:
 * @return:
 * @Author: nbr
 * @Date: 2022/4/28
 */
@Data
public class LoginParam {
    private LocalAddress localAddress;
    //登录端口
    private Long serverPort;
    private ServerAddress serverAddress;
}
