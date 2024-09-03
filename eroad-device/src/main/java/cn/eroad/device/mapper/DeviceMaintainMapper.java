package cn.eroad.device.mapper;

import cn.eroad.device.entity.deviceConfig.DeviceMaintainVo;
import cn.eroad.device.entity.devicemaintain.DeviceMaintain;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import feign.Param;
import java.util.List;

public interface DeviceMaintainMapper extends BaseMapper<DeviceMaintain> {

    int excelImport(@Param("list") List<DeviceMaintain> deviceMaintainList);

    /**
     * 批量更新表格中旧数据
     * @param deviceMaintainList
     * @return
     */
    int excelUpdate(@Param("list") List<DeviceMaintain> deviceMaintainList);

    /**
    * @Description: 根据设备id获取设备名称
    * @Param:
    * @return:
    * @Author: nbr
    * @Date: 2022/7/25
    */
    String getNameByDeviceId(@Param("deviceId") String deviceId);

    /**
    * @Description: 根据设备类型获取设备id列表
    * @Param:
    * @return:
    * @Author: nbr
    * @Date: 2022/7/28
    */
    List<String> getSnListByType(@Param("deviceType") String deviceType);

    /**
    * @Description: 根据id获取设备信息
    * @Param:
    * @return:
    * @Author: nbr
    * @Date: 2022/8/8
    */
    List<DeviceMaintainVo> getMaintainById(@Param("list") List<String> list);

    /**
     * 查询心跳设备信息
     * @return
     */
    List<DeviceMaintain> selectHeartDevice();

    /**
     * 查看无心跳设备类型
     * @return
     */
    List<String> selectNoHeartDeviceType();
}
