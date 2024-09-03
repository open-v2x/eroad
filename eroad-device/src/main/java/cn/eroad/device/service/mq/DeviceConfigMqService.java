package cn.eroad.device.service.mq;

import cn.eroad.core.dto.DeviceConfigDTO;
import cn.eroad.device.mapper.DeviceConfigInfoMapper;
import cn.eroad.device.entity.deviceConfig.AddConfigParamVo;
import cn.eroad.device.operate.DeviceCache;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * @Description: 设备配置mq处理
 * @Param:
 * @return:
 * @Author: nbr
 * @Date: 2022/7/11
 */
@Component
@Slf4j
public class DeviceConfigMqService {
    @Autowired
    private DeviceCache deviceCache;

    @Autowired
    private DeviceConfigInfoMapper configInfoMapper;




    @RabbitHandler
    @RabbitListener(queues = "device_ac_configInfo", containerFactory = "rabbitListenerContainerFactory")
    @Transactional(rollbackFor = Exception.class)
    public void receiveMsg(Message msg) {
        if (StringUtils.isEmpty(msg)) {
            log.error("接收设备配置消息异常!!!!!");
            return;
        }
        String strMsg = new String(msg.getBody());
        log.info("消息内容为::{}",strMsg);
        try {
            DeviceConfigDTO dto = JSONObject.parseObject(strMsg, DeviceConfigDTO.class);
            log.info("dto内容为::{}",dto);
            String key = dto.getSn();
            String nowConfig = deviceCache.getDeviceConfig(key);
            //key不存在
            if(nowConfig == null || nowConfig.trim().equals("")){
                String newConfig = JSONObject.toJSONString(dto);
                //redis和mysql都新增数据

            //key存在
            }else{
                String newConfig = deviceCache.getDeviceConfig(key);
                if(StringUtil.isNotEmpty(newConfig)){
                    JSONObject jsonObject = JSONObject.parseObject(newConfig);
                    DeviceConfigDTO configInfo = JSON.toJavaObject(jsonObject,DeviceConfigDTO.class);
                    //上报的和已存在的配置不同，更新redis和mysql
                    if(!dto.getDataJson().equals(configInfo.getDataJson())){
                        deviceCache.setDeviceConfig(key,newConfig);
                        configInfoMapper.changeConfigInfo(new AddConfigParamVo()
                                .setDeviceId(dto.getSn())
                                //.setDeviceName("毫米波雷达")
                                .setDeviceName(deviceCache.getDeviceBySn(dto.getSn()).getDeviceName())
                                .setConfigType(dto.getConfigType())
                                .setConfigJson(dto.getDataJson()));
                    }
                }
            }
        }catch (Exception e){
            log.error("设备配置消息内容异常！",strMsg,e);
        }
    }
}
