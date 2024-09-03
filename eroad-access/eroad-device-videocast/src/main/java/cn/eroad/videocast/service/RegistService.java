package cn.eroad.videocast.service;

import cn.eroad.rabbitmq.service.MessageOperator;
import cn.eroad.videocast.dao.mapper.VideoCastMapper;
import cn.eroad.videocast.model.connect.RegistParam;
import cn.eroad.videocast.model.data.AlarmDataVo;
import cn.eroad.videocast.model.dto.*;
import cn.eroad.videocast.model.dto.DeviceStatusDTO;
import cn.eroad.videocast.util.RandomNumber;
import cn.eroad.videocast.model.config.SubscribeVehicleCondition;
import cn.eroad.videocast.model.regist.RegistResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Base64;
import java.util.Date;

/**
 * @Auther zhaohanqing
 * @Date 2022/7/29
 */
@Slf4j
@Service
public class RegistService {
    @Resource(name = "rabbitOperator")
    private MessageOperator messageOperator;

    @Autowired
    VideoCastMapper videoCastMapper;

    /**
     * 设备注册
     *
     * @param registParam
     * @return
     */
    public RegistResult registOnce(RegistParam registParam) {
        RegistResult result = new RegistResult();
        //首次注册
        if (StringUtils.isEmpty(registParam.getDeviceCode())) {
            String guid = RandomNumber.getGUID();
            result.setNonce(guid);
            result.setData(401);
            result.setResponseString("Not Authorized");
        }
        //第二次注册判断
        if (registParam.getDeviceCode() != null && registParam.getDeviceType() != null) {
            result.setData(101);
            result.setCnonce(registParam.getCnonce());
            result.setResign(genNsign(registParam));
            result.setResponseString("Succeed");
        }
        log.info("注册返回result值为：" + result);
        return result;
    }


    /**
     * nsign生成工具类
     *
     * @param registParam
     * @return
     */
    public String genNsign(RegistParam registParam) {
        String secret = "123456";
        String pstr = registParam.getVendor() +
                "/" + registParam.getDeviceType() +
                "/" + registParam.getDeviceCode() +
                "/" + registParam.getAlgorithm() +
                "/" + registParam.getNonce();
        String algo = DigestUtils.sha256Hex(pstr + secret);
        String Resign = Base64.getEncoder().encodeToString(algo.getBytes());
        log.info("Resign值：" + Resign);
        return Resign;
    }


    /**
     * 设备状态上报
     *
     * @param code
     * @param type
     * @param time
     */
    public void deviceStatus(String code, String type, int time) {
        DeviceStatusDTO deviceStatusDTO = new DeviceStatusDTO();
        deviceStatusDTO.setDeviceType(type);
        deviceStatusDTO.setSn(code);
        //通过业务逻辑60s判断一次来判断是否正常
        if (time == 1) {
            deviceStatusDTO.setStatusType("正常");
        } else {
            deviceStatusDTO.setStatusType("异常");
        }
        deviceStatusDTO.setUpdateTime(new Date(System.currentTimeMillis()));
        messageOperator.sendTopic(cn.eroad.core.dto.DeviceStatusDTO.DEVICE_COMMON_DATA_REPORT_EXCHANGE, cn.eroad.core.dto.DeviceStatusDTO.ROUTING_KEY_DEVICE_SINGLE_STATUSINFO, deviceStatusDTO);
    }


    /**
     * 设备配置上报
     *
     * @param subscribeVehicleCondition
     */
    public void deviceConfig(SubscribeVehicleCondition subscribeVehicleCondition) {
        DeviceConfigDTO deviceConfigDTO = new DeviceConfigDTO();
        deviceConfigDTO.setSn(subscribeVehicleCondition.getDeviceID());
        deviceConfigDTO.setDeviceType(String.valueOf(subscribeVehicleCondition.getType()));
        deviceConfigDTO.setUpdateTime(new Date(subscribeVehicleCondition.getDuration()));
        deviceConfigDTO.setDataJson(String.valueOf(subscribeVehicleCondition));
        messageOperator.sendTopic(cn.eroad.core.dto.DeviceConfigDTO.DEVICE_COMMON_DATA_REPORT_EXCHANGE, cn.eroad.core.dto.DeviceConfigDTO.ROUTING_KEY_DEVICE_SINGLE_CONFIGINFO, deviceConfigDTO);
    }


    /**
     * 设备告警上报
     *
     * @param alarmDataVo
     */
    public void deviceAlarm(AlarmDataVo alarmDataVo) {
        DeviceAlarmDTO deviceAlarmDTO = new DeviceAlarmDTO();
        deviceAlarmDTO.setAlarmMessage(String.valueOf(alarmDataVo.getAlarmInfo()));
        deviceAlarmDTO.setDataJson(String.valueOf(alarmDataVo));
        messageOperator.sendTopic(cn.eroad.core.dto.DeviceAlarmDTO.DEVICE_COMMON_DATA_REPORT_EXCHANGE, cn.eroad.core.dto.DeviceAlarmDTO.ROUTING_KEY_DEVICE_SINGLE_ALARMINFO, deviceAlarmDTO);
    }


}
