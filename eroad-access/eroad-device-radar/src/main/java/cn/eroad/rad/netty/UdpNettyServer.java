package cn.eroad.rad.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import lombok.extern.slf4j.Slf4j;

/**
 * @author mrChen
 * @date 2022/7/6 11:28
 */
@Slf4j
public class UdpNettyServer {
    private static String delimiter = "C0C0";

    /**
     * @param port
     */
    public static void start(int port, SimpleChannelInboundHandler simpleChannelInboundHandler) {
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup(30);
        try {
            Bootstrap bootstrap = new Bootstrap()
                    .group(eventLoopGroup)
                    .option(ChannelOption.SO_RCVBUF, 1024 * 1024 * 10)
                    .option(ChannelOption.SO_BROADCAST, true)
                    .option(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvByteBufAllocator(65535))
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .handler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            //对服务器返回的消息通过 _$ 进行分隔，并且每次查找的最大大小为65536，
                            pipeline.addLast(new DelimiterBasedFrameDecoder(65536, Unpooled.wrappedBuffer(delimiter.getBytes())));
                            //将分隔后的字节数据转成字符串
                            pipeline.addLast(new StringDecoder());
                            //对客户端发送的数据进行编码，这里主要是在客户端发送的数据在最后添加分隔符
                            pipeline.addLast(new DelimiterBasedFrameEncoder(delimiter));
                            //客户端发送数据给服务器，并且处理从服务器响应的数据
                            pipeline.addLast(simpleChannelInboundHandler);
                        }
                    });
            bootstrap.channel(NioDatagramChannel.class);
            log.info("UdpServer正在监听，端口为{}", port);
            bootstrap.bind(port).sync().channel().closeFuture().await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            log.error(e.getMessage());
        } finally {
            log.error("Udp关闭");
            eventLoopGroup.shutdownGracefully();
        }
    }
}
