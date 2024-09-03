package cn.eroad.videocast.service;

import cn.eroad.videocast.dao.mapper.VideoCastMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author: zhaohanqing
 * @Date: 2022/8/2 19:18
 * @Description:
 */
@Service
public class DeviceNumberService {
    @Autowired
    VideoCastMapper videoCastMapper;

    //通过sn删除设备
    public int deleteDevice(String sn) {
        return videoCastMapper.deleteDevice(sn);
    }
}
