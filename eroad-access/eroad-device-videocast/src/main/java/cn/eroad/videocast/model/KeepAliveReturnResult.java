package cn.eroad.videocast.model;

import lombok.Data;

/**
 * @Description: 设备向服务器请求保活接口返回参数
 * @Param:
 * @return:
 * @Author: nbr
 * @Date: 2022/4/28
 */
@Data
public class KeepAliveReturnResult {
    //当前服务器时间
    private Long TimeStamp;

    //下次心跳间隔
    private Long Timeout;
}
