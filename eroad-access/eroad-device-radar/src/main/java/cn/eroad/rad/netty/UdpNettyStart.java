package cn.eroad.rad.netty;

import cn.eroad.rad.netty.handler.PointCloudDataHandler;
import cn.eroad.rad.netty.handler.ServerDataHandler;
import cn.eroad.rad.util.OfflineReportListener;
import cn.eroad.util.HeartbeatUtil;
import cn.eroad.util.OnlineUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * udp服务 启动
 *
 * @author mrChen
 * @date 2022/7/6 12:29
 */
@Component
@Slf4j
public class UdpNettyStart {

    @Resource(name = "serverDataHandler")
    ServerDataHandler serverDataHandler;

    @Resource(name = "pointCloudDataHandler")
    PointCloudDataHandler pointCloudDataHandler;

    @Value("${rad.cloudpoint.port}")
    private Integer cloudPointPort;

    @Value("${rad.nettyserver.port}")
    private Integer bizPort;

    //   @PostConstruct
    public void init() {
        // 注册监听器
        OnlineUtil.addListener(new OfflineReportListener());

        // 设置心跳监听器
        HeartbeatUtil.setListener();
        /**
         * 点云端口
         */
        new Thread(() -> UdpNettyServer.start(cloudPointPort, pointCloudDataHandler)).start();
        /**
         * 业务端口
         */
        new Thread(() -> UdpNettyServer.start(bizPort, serverDataHandler)).start();
    }
}
