package cn.eroad.rad.model;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import lombok.*;


/**
 * @author mrChen
 * @date 2022/7/6 15:59
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class ParsedData {
    /**
     * 数据
     */
    String msg;
    /**
     * 类型
     */
    String objectIDStr;
    /**
     * 设备IP
     */
    String ip;


    /**
     * 设备真实sn，用于下发和回复使用
     */
    private String realSn;

    /**
     * 设备通信端口
     */
    private Integer port;
    /**
     * operationTypeStr
     */
    String operationTypeStr;

    private ChannelHandlerContext channelHandlerContext;

    private DatagramPacket packet;


}
