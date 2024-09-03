package cn.eroad.rad.util;

import cn.eroad.util.model.DeviceOnline;
import cn.eroad.util.service.OfflineListener;

public class OfflineReportListener implements OfflineListener {

    @Override
    public void offline(DeviceOnline deviceOnline) {
        RabbitMqUtil.reportOnline(deviceOnline.getSn(), 0);
    }
}
