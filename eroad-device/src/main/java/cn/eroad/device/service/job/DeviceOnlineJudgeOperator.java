package cn.eroad.device.service.job;

import cn.eroad.core.dto.DeviceOnlineDTO;
import cn.eroad.core.dto.OnlineStateChangeDTO;
import cn.eroad.device.entity.devicemaintain.DeviceMaintain;
import cn.eroad.device.mapper.DeviceMaintainMapper;
import cn.eroad.device.operate.DeviceCache;
import cn.eroad.device.service.devicemaintain.DeviceMaintainService;
import cn.eroad.device.vo.Device;
import cn.eroad.rabbitmq.service.MessageOperator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static cn.eroad.core.context.SpringContextHolder.getApplicationContext;

/**
 * @Author:suncheng
 * @Data:2022/8/19
 * @code:
 */
@Component
@Slf4j
public class DeviceOnlineJudgeOperator implements ApplicationContextAware {

    private static ApplicationContext context;


    @Autowired
    private DeviceMaintainMapper deviceMaintainMapper;

    @Autowired
    private DeviceCache deviceCache;

    @Autowired
    private DeviceMaintainService deviceMaintainService;

    @Autowired
    private MessageOperator messageOperator;

    public static Object getBean(String name) {
        return getApplicationContext().getBean(name);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

    @Value("${heartbeat.type}")
    private String heartbeatType;


    /**
     * 30分钟查询一次
     */
    @Scheduled(cron = "0 */30 * * * ?")
    public void deviceStatesJudge() {
        log.info("定时任务|校验设备心跳|time:={}", new Date());
        List<String> noHeartSnList = new ArrayList<>();
        String[] typeArr = heartbeatType.split(",");
        List<Device> deviceList = new ArrayList<>();

        //遍历type类型,获得该类型的设备list
        for (String str : typeArr) {
            List<Device> deviceLists = deviceCache.getDeviceListByType(str);
            deviceList.addAll(deviceLists);
        }


        //对数据库中获得的总设备进行遍历，将设备sn依次与设备心跳缓存进行查询   查询有结果，说明设备正常发送心跳；查询无结果，说明设备异常未发送心跳，将异常设备放入一个设备List
        for (Device device : deviceList) {
            DeviceOnlineDTO deviceOnlineDTO = deviceCache.getDeviceHeartCacheBySn(device.getDeviceId());
            if (StringUtils.isEmpty(deviceOnlineDTO)) {
                noHeartSnList.add(device.getDeviceId());
            }
        }
        log.info("无心跳设备列表||设备总数count={}", noHeartSnList.size());

        //再遍历无心跳设备List,如果该设备在线状态为1，则变更为0;
        for (String s : noHeartSnList) {
            Device device = deviceCache.getDeviceBySn(s);
            if (StringUtils.isEmpty(device.getOnlineState()) || device.getOnlineState() == 1) {
                log.info("定时任务校验||该设备无心跳且在线状态异常||deviceId={}", device.getDeviceId());

                //更新数据库
                DeviceMaintain deviceMaintain = new DeviceMaintain();
                deviceMaintain.setDeviceId(device.getDeviceId());
                deviceMaintain.setOnlineState(0);
                deviceMaintain.setOnlineStateUpdateTime(new Date());
                try{
                    deviceMaintainMapper.updateById(deviceMaintain);
                }catch (Exception e){
                    e.printStackTrace();
                    log.error("设备定时校准修改状态失败 ",e);
                }

                device.setOnlineState(0);
                //更新设备缓存到redis
                deviceCache.setDevice2Redis(device);

                //更新运维平台，状态变化时给运维平台发送设备新旧状态
                OnlineStateChangeDTO onlineStateChange = new OnlineStateChangeDTO();
                onlineStateChange.setSn(deviceMaintain.getDeviceId());
                onlineStateChange.setDeviceType(deviceMaintain.getDeviceType());
                onlineStateChange.setOldState(1);
                onlineStateChange.setNewState(deviceMaintain.getOnlineState());
                onlineStateChange.setUpdateTime(deviceMaintain.getOnlineStateUpdateTime());
                log.info("定时任务校验||向运维管理平台上报设备在线状态:{}", onlineStateChange);
                messageOperator.sendTopic(OnlineStateChangeDTO.EXCHANGE_DEVICE_DATA_REPORT, OnlineStateChangeDTO.ROUTING_KEY_DEVICE_SINGLE_ONLINE_OFFLINE, onlineStateChange);
            }
        }
        deviceCache.delDeviceHeartCache();
        log.info("定时任务执行完毕");
    }
}
