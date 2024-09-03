package cn.eroad.rad.netty.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;

/**
 * 客户端业务处理
 *
 * @author LionLi
 */
public class UdpClientHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    //接受服务端发送的内容
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) {
        ByteBuf byteBuf = packet.content();
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);
        String content = new String(bytes);
        System.out.println("客户端接收到消息：" + content);
    }


}
