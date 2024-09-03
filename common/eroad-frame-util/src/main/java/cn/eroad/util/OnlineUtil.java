package cn.eroad.util;

import cn.eroad.util.model.DeviceOnline;
import cn.eroad.util.service.OfflineListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
public class OnlineUtil {

    // 参数配置离线毫秒时间，默认60秒
    private static long expiredMills;
    // map保存设备在线状态
    private static final Map<String, DeviceOnline> map = new ConcurrentHashMap<>();
    // 延时队列定时离线
    private static final DelayQueue<DeviceOnline> delayQueue = new DelayQueue<>();
    // 单任务线程池，启动消费延时队列任务
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();
    // 监听器列表
    private static final Set<OfflineListener> listeners = new HashSet<>();

    // 项目启动时开启消费延时队列任务
    public OnlineUtil() {
        executorService.execute(() -> {
            // 死循环
            while (true) {
                // 获取心跳过期设备信息
                DeviceOnline offline = null;
                try {
                    // 阻塞查询
                    offline = delayQueue.take();
                } catch (InterruptedException e) {
                    log.error(e.getMessage());
                }
                // 有心跳过期时，判断是否离线
                if (offline != null) {
                    // 获取设备状态
                    DeviceOnline online = map.get(offline.getSn());
                    // 如果心跳过期前存在新的心跳上报，则丢弃不认为离线
                    log.info("sn为：{}，过期时间为{}，当前有效期为：{}", offline.getSn(), offline.getExpiredMilli(), online.getExpiredMilli());
                    if (offline.getExpiredMilli() < online.getExpiredMilli()) {
                        continue;
                    }
                    // 否则设备离线
                    online.offline();
                    // 实现的离线操作
                    offline(online);
                }
            }
        });
    }

    /**
     * 设备上线
     *
     * @param sn   设备编号
     * @param ip   设备ip
     * @param type 设备类型
     */
    public static void online(String sn, String ip, String type) {
        // 查询设备是否已注册
        DeviceOnline deviceOnline = map.get(sn);
        if (deviceOnline == null) {
            // 设备未注册就注册一下
            deviceOnline = new DeviceOnline(expiredMills, sn, ip, type, expiredMills);
            map.put(sn, deviceOnline);
        } else {
            // 设备已注册就更新心跳过期时间戳
            deviceOnline.online(expiredMills);
        }
        // 生成快照将心跳过期信息加入延时队列
        delayQueue.put(new DeviceOnline(expiredMills, deviceOnline.getSn(), deviceOnline.getExpiredMilli()));
    }

    /**
     * 查询设备在线状态
     *
     * @return 设备在线状态
     */
    public static Collection<DeviceOnline> deviceList() {
        return map.values();
    }

    /**
     * 添加监听器
     *
     * @param listener 监听器
     */
    public static void addListener(OfflineListener listener) {
        OnlineUtil.listeners.add(listener);
    }

    /**
     * 剔除监听器
     *
     * @param listener 监听器
     */
    public static void removeListener(OfflineListener listener) {
        OnlineUtil.listeners.remove(listener);
    }

    /**
     * 根据SN从当前延迟队列里MAP取在线信息
     *
     * @param sn
     * @return
     */
    public static DeviceOnline getDeviceOnlineBySn(String sn) {
        return map.get(sn);
    }

    /**
     * 执行所有监听器动作
     *
     * @param deviceOnline 监听对象
     */
    private void offline(DeviceOnline deviceOnline) {
        for (OfflineListener listener : OnlineUtil.listeners) {
            listener.offline(deviceOnline);
        }
    }

    @Value("${device.online.expired-milli:60000}")
    public void setExpiredMills(int expiredMills) {
        OnlineUtil.expiredMills = expiredMills;
    }

    public static void main(String[] args) {
        System.out.println(System.currentTimeMillis());
    }
}
