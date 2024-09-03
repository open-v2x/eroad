package cn.eroad.rad.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class AppConfig {
    public static int rangeInMinutes = 60;

    // 其他数据监听端口
    public static Integer NETTY_SERVER_PORT;
    // 点云数据监听端口
    public static Integer CLOUD_POINT_SERVER_PORT;

    @Value("${rad.cloudpoint.port}")
    public void setCloudPointServerPort(Integer cloudPointServerPort) {
        CLOUD_POINT_SERVER_PORT = cloudPointServerPort;
    }

    // 与设备运营管理平台
    // 信息查询统一接口
    public static final String URL_COMMON_QUERY = "/search/propIn";
    // 重启
    public static final String URL_REBOOT = "/cmd/rebootIn";
    public static final String REDIS_REBOOT = "mmw_redis_reboot";
    //恢复出厂设置
    public static final String URL_RESET = "/cmd/restoreFactorySetIn";
    public static final String REDIS_RESET = "mmw_redis_reset";
    //网络参数设置
    public static final String URL_SET_NET = "/cmd/netSetIn";
    public static final String REDIS_SET_NET = "mmw_redis_set_net";
    //配置参数设置
    public static final String URL_SET_CONFIG = "/cmd/configSetIn";
    public static final String REDIS_SET_CONFIG = "rad/cmd/configSetIn";


    // 工作状态查询
    public static final String SEARCH_WORKSTATUS = "workStatus";
    // 网络参数查询
    public static final String SEARCH_NET = "net";
    // 配置参数查询
    public static final String SEARCH_CONFIG = "config";

    // post
    public static final int POST_SUCCESS = 0;
    public static final String OPERATE_SUCCESS = "操作成功";

    // 线程池大小
    public static Integer MULTI_THREAD_COUNT = 128;

    @Value("${rad.nettyserver.port}")
    public void setNettyServerPort(Integer nettyServerPort) {
        NETTY_SERVER_PORT = nettyServerPort;
    }

}
