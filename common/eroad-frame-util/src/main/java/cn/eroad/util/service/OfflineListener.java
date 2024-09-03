package cn.eroad.util.service;

import cn.eroad.util.model.DeviceOnline;

import java.util.EventListener;

public interface OfflineListener extends EventListener {

    void offline(DeviceOnline deviceOnline);
}
