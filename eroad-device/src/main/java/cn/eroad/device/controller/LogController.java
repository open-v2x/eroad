package cn.eroad.device.controller;

import cn.eroad.core.domain.CommonContent;
import cn.eroad.core.domain.PageDomain;
import cn.eroad.device.entity.form.query.OperationLogQuery;
import cn.eroad.device.entity.po.OperationLog;
import cn.eroad.device.service.OperationLogService;
import cn.eroad.trail.annotation.OperLog;
import cn.eroad.trail.enums.BusinessType;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 描述
 *
 *
 * @author 91953
 * @date 2022/1/5 10:41
 */
@RestController
@Slf4j
@Api(value = "操作日志管理", tags = "操作日志管理")
@RequestMapping("/system/log")
public class LogController {
    @Autowired
    private OperationLogService operationLogService;

    /**
     * 删除设备/NVR
     * @return
     */
    @PostMapping(value = "/del")
    @ApiOperation(value = "清除超时操作日志", notes = "清除超时操作日志")
    @OperLog(title = "操作日志清除", businessType = BusinessType.DELETE,isSaveRequestData = false)
    public CommonContent delete() {
        int expire = operationLogService.expire();
        return CommonContent.ok();
    }

    /**
     * 分页日志列表
     * @param operationLogQuery
     * @description 新增
     * @Author elvin
     * @Date 2019/8/5
     * @return
     */
    @PostMapping(value = "/list")
    @ApiOperation(value = "分页查询操作日志", notes = "通过条件分页查询数据列表")
    @OperLog(title = "操作日志分页查询", businessType = BusinessType.SELECT,isSaveRequestData = false)
    public CommonContent<PageDomain<OperationLog>> list(@RequestBody OperationLogQuery operationLogQuery) {
        PageDomain<OperationLog> pageInfo = operationLogService.selectPageList(operationLogQuery);
        return CommonContent.ok(pageInfo);
    }

}
