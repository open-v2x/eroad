package cn.eroad.rad.listener;

import cn.eroad.rad.util.RabbitMqUtil;
import cn.eroad.util.OnlineUtil;
import cn.eroad.util.service.HeartbeatListener;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HeartbeatReportListener implements HeartbeatListener {

    @Override
    public void heartbeat(String sn) {
        log.info("业务数据达到上限触发心跳,sn: {}", sn);
        RabbitMqUtil.reportOnline(sn, 1);
        OnlineUtil.online(sn, null, "rad");
    }
}
