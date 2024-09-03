package cn.eroad.device.controller;

import cn.eroad.core.domain.Head;
import cn.eroad.device.util.ExcelUtil;
import cn.eroad.device.entity.devicemaintain.DeviceState;
import cn.eroad.device.entity.devicemaintain.DeviceVO;
import cn.eroad.device.entity.query.DeviceInfoQuery;
import cn.eroad.device.service.devicemaintain.impl.ExcelListener;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import cn.eroad.core.domain.CommonContent;
import cn.eroad.core.domain.PageDomain;
import cn.eroad.device.entity.devicemaintain.DeviceMaintain;
import cn.eroad.device.entity.form.DeviceForm;
import cn.eroad.device.entity.query.DeviceMaintainQuery;
import cn.eroad.device.service.devicemaintain.DeviceMaintainService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import cn.eroad.trail.annotation.OperLog;
import cn.eroad.trail.enums.BusinessType;
import org.springframework.web.multipart.MultipartFile;

@Api(tags = "设备维护")
@RestController
@Slf4j
@RequestMapping("/device")
public class DeviceMaintenanceController {


    @Autowired
    private DeviceMaintainService deviceMaintainService;


    @PostMapping(value = "/add")
    @ApiOperation(value = "新增平台(设备)", notes = "新增平台(设备)")
    @OperLog(title = "平台(设备)管理", businessType = BusinessType.INSERT)
    public CommonContent save(@RequestBody @Validated DeviceForm.AddForm addForm) {
        DeviceMaintain deviceMaintain = new DeviceMaintain();
        BeanUtils.copyProperties(addForm, deviceMaintain);
        deviceMaintainService.saves(deviceMaintain);
        return CommonContent.ok();
    }


    @PostMapping(value = "/edit")
    @ApiOperation(value = "更新平台(设备)", notes = "通过ID更新单条数据")

    public CommonContent edit(@RequestBody @Validated DeviceForm.EditForm editForm) {
        deviceMaintainService.update(editForm);
        return CommonContent.ok();
    }


    @ApiOperation(value = "根据单个id查看设备详情")
    @PostMapping("/selectById")
    public CommonContent<DeviceMaintain> SelectAllByID(@RequestBody @Validated DeviceForm.SelectByIdForm selectByIdForm) {
        if (selectByIdForm.getDeviceId().isEmpty()) {
            return CommonContent.error("DeviceId不可为空");
        }
        log.info("DeviceMaintenanceController|SelectAllByID|selectByIdForm.getDeviceId()={}"
                , selectByIdForm.getDeviceId());
        DeviceMaintain deviceMaintain = deviceMaintainService.selectById(selectByIdForm.getDeviceId());
        return CommonContent.ok(deviceMaintain);
    }


    @ApiOperation(value = "根据id集合查看设备详情")
    @PostMapping("/selectByIds")
    public CommonContent<List<DeviceMaintain>> SelectAllById(@RequestBody @Validated DeviceForm.SelectByIdsForm selectByIdsForm) {
        if (selectByIdsForm.getDeviceId().isEmpty()) {
            return CommonContent.error("DeviceId不可为空");
        }
        List<String> deviceIdList = new ArrayList<>(selectByIdsForm.getDeviceId());
        log.info("DeviceMaintenanceController|SelectAllByID|selectByIdForm.getDeviceId()={}|deviceIdList={}"
                , selectByIdsForm.getDeviceId(), deviceIdList);
        List<DeviceMaintain> deviceMaintainList = deviceMaintainService.selectByIds(deviceIdList);
        return CommonContent.ok(deviceMaintainList);
    }


    @PostMapping(value = "/del")
    @ApiOperation(value = "删除平台(设备)", notes = "通过ID删除数据")
    @OperLog(title = "平台(设备)管理", businessType = BusinessType.DELETE)
    public CommonContent delete(@RequestBody @Validated DeviceForm.DeleteForm deleteForm) {
        log.info("DeviceMaintenanceController|delete|开始删除设备|参数deleteForm.getDeviceId={}", deleteForm.getDeviceId());
        List<String> deviceIdList = new ArrayList<>(deleteForm.getDeviceId());
        deviceMaintainService.delete(deviceIdList);
        return CommonContent.ok();
    }


    @PostMapping(value = "/list")
    @ApiOperation(value = "分页查询", notes = "通过条件分页查询数据列表")
    public CommonContent<PageDomain<DeviceVO>> list(@RequestBody @Validated DeviceMaintainQuery deviceMaintainQuery) {
        PageDomain<DeviceVO> pageInfo = deviceMaintainService.selectPageList(deviceMaintainQuery);
        return CommonContent.ok(pageInfo);
    }


    @ApiOperation(value = "设备状态查询")
    @PostMapping("/searchState")
    public CommonContent<DeviceState> deviceState() {
        DeviceState deviceState = deviceMaintainService.selectDeviceState();
        return CommonContent.ok(deviceState);
    }


    @ApiOperation(value = "设备导入")
    @PostMapping("/excelImport")
    public CommonContent upload(@RequestParam @Validated MultipartFile file) throws Exception {
        String fileName = file.getOriginalFilename();
        String fileType = fileName.substring(fileName.lastIndexOf(".") + 1);
        if (!fileType.endsWith("xlsx") && !fileType.endsWith("xls")) {
            return CommonContent.error("文件类型错误");
        }

        log.info("开始文件上传");
        InputStream fileInputStream;
        fileInputStream = file.getInputStream();
        log.info("调用easyExcel");
        //调用EasyExcel.read然后去调用你写的监听器，随后去执行你写的Service
        EasyExcel.read(fileInputStream, DeviceMaintain.class, new ExcelListener(deviceMaintainService)).sheet().doRead();
        log.info("上传文件成功");
        return CommonContent.ok();

    }


    @ApiOperation(value = "设备Excel模板下载", notes = "设备Excel模板下载")
    @GetMapping(value = "excelDownload")  //
    public void excelDownload(HttpServletResponse response) {
        log.info("开始调用模板下载接口");
        try {
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("设备模板", "UTF-8");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
            log.info("response的值为={}", response);
            EasyExcel.write(response.getOutputStream(), DeviceInfoQuery.class).autoCloseStream(Boolean.FALSE).sheet("设备模板").doWrite(new ArrayList());
        } catch (Exception e) {
            log.error("设备Excel模板下载异常", e);
            response.reset();
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
        }
    }

    /**
     * 下载Excel模板
     */
    @ApiOperation(value = "设备模板下载", notes = "设备模板下载")
    @GetMapping("/downloadDemo")
    public void downloadDemo(HttpServletResponse response) {
        log.info("开始调用设备模板下载接口2");
        try {
            String fileName = "设备模板";
            String sheetName = "设备页";
            List<DeviceMaintain> deviceMaintainList = new ArrayList<>();
            log.info("模板下载controller中filename原始值={}", fileName);
            ExcelUtil.writeExcel(response, deviceMaintainList, fileName, sheetName, DeviceMaintain.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @ApiOperation(value = "设备导出")
    @PostMapping("/excelExport")
    public void excelExport(@RequestBody List<DeviceMaintain> excelUploadEntityList, HttpServletResponse response) {
        try {
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("设备模板", "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
            EasyExcel.write(response.getOutputStream(), DeviceInfoQuery.class).autoCloseStream(Boolean.FALSE).head(DeviceMaintain.class)
                    .excelType(ExcelTypeEnum.XLSX)
                    .sheet("设备表")
                    .doWrite(excelUploadEntityList);
        } catch (Exception e) {
            log.error("设备Excel模板下载异常", e);
            response.reset();
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");

        }
    }

}





