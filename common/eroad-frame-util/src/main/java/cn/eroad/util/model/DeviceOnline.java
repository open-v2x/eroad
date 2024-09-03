package cn.eroad.util.model;

import cn.eroad.util.enums.OnlineStatus;

import java.util.EventObject;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class DeviceOnline extends EventObject implements Delayed {

    // 设备编号
    private final String sn;
    // 设备ip
    private String ip;
    // 设备类型
    private String type;
    // 设备在线状态
    private OnlineStatus status;
    // 心跳过期毫秒时间戳
    private long expiredMilli;

    // 构造函数初始化在线设备
    public DeviceOnline(Object source, String sn, String ip, String type, long delayMilli) {
        super(source);
        this.sn = sn;
        this.ip = ip;
        this.type = type;
        this.status = OnlineStatus.ONLINE;
        this.expiredMilli = System.currentTimeMillis() + delayMilli;
    }

    // 构造函数，生成快照当前心跳过期时间戳到队列
    public DeviceOnline(Object source, String sn, long expiredMilli) {
        super(source);
        this.sn = sn;
        this.expiredMilli = expiredMilli;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(this.expiredMilli - System.currentTimeMillis(), TimeUnit.MILLISECONDS);

    }

    @Override
    public int compareTo(Delayed o) {
        return Long.compare(getDelay(TimeUnit.MILLISECONDS), o.getDelay(TimeUnit.MILLISECONDS));
    }

    // 已注册设备设备上线
    public void online(long delayMilli) {
        this.status = OnlineStatus.ONLINE;
        this.expiredMilli = System.currentTimeMillis() + delayMilli;
    }

    // 已注册设备设备离线
    public void offline() {
        this.status = OnlineStatus.OFFLINE;
    }

    public String getSn() {
        return sn;
    }

    public long getExpiredMilli() {
        return expiredMilli;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public OnlineStatus getStatus() {
        return status;
    }

    public void setStatus(OnlineStatus status) {
        this.status = status;
    }
}
