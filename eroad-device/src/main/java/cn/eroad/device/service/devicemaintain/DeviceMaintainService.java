package cn.eroad.device.service.devicemaintain;

import cn.eroad.core.domain.PageDomain;
import cn.eroad.device.entity.devicemaintain.DeviceMaintain;
import cn.eroad.device.entity.devicemaintain.DeviceState;
import cn.eroad.device.entity.devicemaintain.DeviceVO;
import cn.eroad.device.entity.form.DeviceForm;
import cn.eroad.device.entity.query.DeviceMaintainQuery;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface DeviceMaintainService extends IService<DeviceMaintain> {

    PageDomain<DeviceVO> selectPageList(DeviceMaintainQuery deviceMaintainQuery);

    String saves(DeviceMaintain deviceMaintain);

    String delete(List<String> deviceIdList);

    String update(DeviceForm.EditForm editForm);

    DeviceMaintain selectById(String deviceId);

    List<DeviceMaintain> selectByIds(List<String> deviceIdList);

    String excelImport(List<DeviceMaintain> deviceMaintainList);

    DeviceState selectDeviceState();

    void deviceSave(DeviceMaintain device);

}
