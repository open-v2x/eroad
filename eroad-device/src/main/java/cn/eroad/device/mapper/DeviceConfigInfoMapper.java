package cn.eroad.device.mapper;

import cn.eroad.core.dto.DeviceConfigDTO;
import cn.eroad.device.entity.deviceConfig.*;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * @Description: deviceConfigInfoMapper
 * @Param:
 * @return:
 * @Author: nbr
 * @Date: 2022/5/18
 */
public interface DeviceConfigInfoMapper{
    //获取设备配置列表
    List<DeviceConfigInfo> getDeviceConfigList(ListParamVo listParamVo);

    //获取设备配置列表（增加snList筛选条件）
    List<DeviceConfigInfo> getDeviceConfigListBySnList(@Param("deviceType") String deviceType,@Param("configType") String configType,
                                                       @Param("list") List<String> list);

    //获取配置详情
    DetailResultVo getConfigDetail(DetailParamVo detailParamVo);

    //查询配置详情（超维用）
    List<ConfigSuperResultVo> getConfig(@Param("snList") List<String> snList, @Param("configType") String configType);

    //批量删除设备配置
    int deleteConfig(@Param("list") List<String> list);

    //添加设备配置信息
    int addConfigInfo(AddConfigParamVo paramVo);

    //修改设备配置信息
    int changeConfigInfo(AddConfigParamVo paramVo);

    //查询配置供缓存初始化用
    List<DeviceConfigDTO> getDeviceConfigDTO();
}
