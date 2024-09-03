package cn.eroad.videocast.model.connect;

import lombok.Data;

/**
 * @Description: 设备注册参数类
 * @Param:
 * @return:
 * @Author: nbr
 * @Date: 2022/4/28
 */
@Data
public class RegistParam {
    //设备厂家名
    private String Vendor;

    //设备类型
    private String DeviceType;

    //设备序列号
    private String DeviceCode;

    //签名算法，不带则默认采用如下算法
    private String Algorithm;

    //服务端产生的 16 个随机字符，每次发起必须随机不同
    private String Nonce;

    //客户端计算的签名
    private String Sign;

    //客户端产生的 16 个随机字符，每次发起必须随机不同
    private String Cnonce;

    //WebSocket 会话 ID，当由服务器发起通过
    private String SessionID;


}
