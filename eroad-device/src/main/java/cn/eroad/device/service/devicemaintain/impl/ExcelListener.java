package cn.eroad.device.service.devicemaintain.impl;

import cn.eroad.core.exception.AcException;
import cn.eroad.device.entity.enums.DeviceMainEnum;
import cn.eroad.device.entity.devicemaintain.DeviceMaintain;
import cn.eroad.device.entity.form.DeviceForm;
import cn.eroad.device.service.devicemaintain.DeviceMaintainService;
import cn.eroad.device.service.devicemaintain.ValidatorUtil;
import cn.eroad.device.service.devicemaintain.VsValidException;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ExcelListener extends AnalysisEventListener<DeviceMaintain> {


    /**
     * 每隔5000条存储数据库，然后清理list ，方便内存回收
     */
    private static final int BATCH_COUNT = 5000;
    /**
     * 实现业务逻辑的service
     */
    @Autowired
    private DeviceMaintainService deviceMaintainService;


    /**
     * 保存数据的集合
     */
    List<DeviceMaintain> deviceMaintainList = new ArrayList<DeviceMaintain>();

    /**
     * 如果使用了spring,请使用这个构造方法。每次创建Listener的时候需要把spring管理的类传进来
     *
     * @param
     */
    public ExcelListener(DeviceMaintainService deviceMaintainService) {
        this.deviceMaintainService = deviceMaintainService;
    }

    @Override
    public void invoke(DeviceMaintain deviceMaintain, AnalysisContext context) {

        Integer ttId = context.readRowHolder().getRowIndex();
        String deviceId = deviceMaintain.getDeviceId();
        String deviceName = deviceMaintain.getDeviceName();



        DeviceForm.AddForm subDeviceInfoAddForm = DeviceForm.AddForm.builder()
                .deviceId(deviceId)
                .deviceName(deviceName)
                .deviceType(StringUtils.isEmpty(deviceMaintain.getDeviceType()) ? null : deviceMaintain.getDeviceType())
                .manufacturer(StringUtils.isEmpty(deviceMaintain.getManufacturer()) ? null : deviceMaintain.getManufacturer())
                .altitude(StringUtils.isEmpty(deviceMaintain.getAltitude()) ? null : deviceMaintain.getAltitude())
                .longitude(StringUtils.isEmpty(deviceMaintain.getLongitude()) ? null : deviceMaintain.getLongitude())
                .latitude(StringUtils.isEmpty(deviceMaintain.getLatitude()) ? null : deviceMaintain.getLatitude())
                .build();


        try {
            ValidatorUtil.validateEntity(subDeviceInfoAddForm);
        } catch (VsValidException e) {
            //throw new VsValidException(String.format("导入文档中第%d行数据有误: %s", ttId, e.getMessage()));
            throw new AcException(DeviceMainEnum.EXCEL_FAIL.getCode(), String.format("导入文档中第%d行数据有误: %s", ttId, e.getMessage()));
        }
        // 数据格式校验通过 开始规则校验
        if (StringUtils.isEmpty(subDeviceInfoAddForm.getDeviceId())||StringUtils.isEmpty(subDeviceInfoAddForm.getDeviceName())
                ||StringUtils.isEmpty(subDeviceInfoAddForm.getDeviceType())||StringUtils.isEmpty(subDeviceInfoAddForm.getManufacturer())) {
            throw new AcException(DeviceMainEnum.EXCEL_FAIL.getCode(), String.format("导入文档中第%d行数据有误: %s", ttId, "设备编码/名称/类型/厂商不能为空！"));
        }

        if (!StringUtils.isEmpty(subDeviceInfoAddForm.getDeviceName())) {

            BigDecimal deviceIds = new BigDecimal(subDeviceInfoAddForm.getDeviceId().length());
            if (deviceIds.compareTo(BigDecimal.valueOf(40L)) > 0) {
                throw new AcException(DeviceMainEnum.EXCEL_FAIL.getCode(), String.format("导入文档中第%d行设备名称数值有误,正确区间[0-40]", ttId));
            }
        }

        if (!StringUtils.isEmpty(subDeviceInfoAddForm.getDeviceId())) {
            BigDecimal deviceNames = new BigDecimal(subDeviceInfoAddForm.getDeviceId().length());
            if (deviceNames.compareTo(BigDecimal.valueOf(40L)) > 0) {
                throw new AcException(DeviceMainEnum.EXCEL_FAIL.getCode(), String.format("导入文档中第%d行设备编码数值有误,正确区间[0-40]", ttId));
            }
        }

        if (!StringUtils.isEmpty(subDeviceInfoAddForm.getParentDeviceId())) {
            BigDecimal parentDeviceIds = new BigDecimal(subDeviceInfoAddForm.getParentDeviceId().length());
            if (parentDeviceIds.compareTo(BigDecimal.valueOf(40L)) > 0) {
                throw new AcException(DeviceMainEnum.EXCEL_FAIL.getCode(), String.format("导入文档中第%d行父设备编码数值有误,正确区间[0-40]", ttId));
            }
        }


        deviceMaintainList.add(deviceMaintain);
        // 达到BATCH_COUNT了，需要去存储一次数据库，防止数据几万条数据在内存，容易OOM
        if (deviceMaintainList.size() >= BATCH_COUNT) {


            saveData();
            // 存储完成清理 list
            deviceMaintainList.clear();
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        // 保存数据，确保最后遗留的数据也存储到数据库
        saveData();
        log.info("所有数据解析完成！");
    }

    /**
     * 加上存储数据库
     */
    private void saveData() {
        //数据库交互
        log.info("监听类开始操作数据库");
        deviceMaintainService.excelImport(deviceMaintainList);


    }

}
