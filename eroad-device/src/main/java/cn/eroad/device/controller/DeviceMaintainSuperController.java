package cn.eroad.device.controller;

import cn.eroad.core.domain.CommonContent;
import cn.eroad.core.domain.PageDomain;
import cn.eroad.device.entity.devicemaintain.DeviceMaintain;
import cn.eroad.device.entity.devicemaintain.DeviceState;
import cn.eroad.device.entity.devicemaintain.DeviceVO;
import cn.eroad.device.entity.form.DeviceForm;
import cn.eroad.device.entity.query.DeviceMaintainQuery;
import cn.eroad.device.service.devicemaintain.DeviceMaintainService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@Api(tags = "设备维护(北向接口)")
@RestController
@RequestMapping("/device/api")
@Slf4j
@Validated
public class DeviceMaintainSuperController {

    @Autowired
    private DeviceMaintainService deviceMaintainService;

    @ApiOperation(value = "设备状态查询")
    @PostMapping("/searchState")
    public CommonContent<DeviceState> deviceState() {
        DeviceState deviceState = deviceMaintainService.selectDeviceState();
        return CommonContent.ok(deviceState);
    }

    @ApiOperation(value = "根据id集合查看设备详情")
    @PostMapping("/selectByIds")
    public CommonContent<List<DeviceMaintain>> equipmentSelectAllByID(@RequestBody @Valid DeviceForm.SuperSelectByIdForm SuperSelectByIdForm) {
        if (SuperSelectByIdForm.getSn().isEmpty()) {
            return CommonContent.error("SN不可为空");
        }

        List<String> deviceIdList = new ArrayList<>(SuperSelectByIdForm.getSn());
        List<DeviceMaintain> deviceMaintainList = deviceMaintainService.selectByIds(deviceIdList);
        return CommonContent.ok(deviceMaintainList);

    }

    @PostMapping(value = "/list")
    @ApiOperation(value = "分页查询", notes = "通过条件分页查询数据列表")
    public CommonContent<PageDomain<DeviceVO>> list(@RequestBody @Validated DeviceMaintainQuery deviceMaintainQuery) {
        PageDomain<DeviceVO> pageInfo = deviceMaintainService.selectPageList(deviceMaintainQuery);
        return CommonContent.ok(pageInfo);
    }
}
