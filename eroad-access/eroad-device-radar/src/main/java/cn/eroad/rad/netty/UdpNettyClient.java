package cn.eroad.rad.netty;

import cn.eroad.rad.netty.handler.UdpClientHandler;
import cn.eroad.rad.util.ByteUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.DatagramSocket;
import java.net.InetSocketAddress;



/**
 * @author mrChen
 * @date 2022/7/6 12:21
 */
@Slf4j
@Component
public class UdpNettyClient {

    private static Channel channel;

    @Value("${rad.nettySendServer.port:5678}")
    private Integer port;

    public UdpNettyClient() {
        new Thread() {
            @Override
            public void run() {
                EventLoopGroup group = new NioEventLoopGroup();
                try {
                    Bootstrap bootstrap = new Bootstrap();
                    bootstrap.group(group)
                            .channel(NioDatagramChannel.class)
                            .handler(new ChannelInitializer<NioDatagramChannel>() {
                                @Override
                                protected void initChannel(NioDatagramChannel ch) {
                                    ChannelPipeline pipeline = ch.pipeline();
                                    pipeline.addLast(new UdpClientHandler());
                                }
                            });
                    channel = bootstrap.bind(port).sync().channel();
                    log.info("绑定UDP客户端端口 {}", port);
                    channel.closeFuture().await();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    group.shutdownGracefully();
                }
            }
        }.start();
    }

    public static void sendMessage(String msg, String hostName, Integer port) throws Exception {
        InetSocketAddress address = new InetSocketAddress(hostName, port);
        ByteBuf buff = Unpooled.buffer();
        buff.writeBytes(ByteUtil.hexString2Bytes(msg));
        channel.writeAndFlush(new DatagramPacket(buff, address)).sync();
        log.info("设备通过查询网元信息然后发送到{}端口为{}，内容为{}", hostName, port, msg);
    }

    public static void sendUdpMessage(String message, String targetAddr, Integer targetPort, Integer localPort) {
        InetSocketAddress inetSocketAddress = new InetSocketAddress(targetAddr, targetPort);
        byte[] udpMessage = getHexBytes(message);
        java.net.DatagramPacket datagramPacket;
        try (DatagramSocket datagramSocket = new DatagramSocket()) {
            datagramPacket = new java.net.DatagramPacket(udpMessage, udpMessage.length, inetSocketAddress);
            datagramSocket.send(datagramPacket);
            log.info("udp 发送消息成功,目标地址：{}: {},数据：{}", targetAddr, targetPort, message);
        } catch (Exception e) {
            log.error("udp 发送消息失败,目标地址：{}: {}", targetAddr, targetPort, e);
        }
    }

    public static byte[] getHexBytes(String str) {
        byte[] bytes = new byte[str.length() / 2];
        for (int i = 0; i < str.length() / 2; i++) {
            String subStr = str.substring(i * 2, i * 2 + 2);
            bytes[i] = (byte) Integer.parseInt(subStr, 16);
        }
        return bytes;
    }
}
