package cn.eroad.rad.controller;


import cn.eroad.rad.model.Prop;
import cn.eroad.rad.vo.QueryVo;
import cn.eroad.rad.vo.api.ConfigSetInVo;
import cn.eroad.rad.vo.api.NetSetInVo;
import cn.eroad.core.domain.CommonContent;
import cn.eroad.core.vo.DeviceCommonRequest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("api/v1")
@Slf4j
@Api("rad接口")
public class ApiRemoteOperateController {
    @Autowired
    private AsynServiceForCmd asynServiceForCmd;


    //修改1、返回值responsedata<commonresult>；2、路径；3、实体类入参；4、service层内容
    //设备重启
    @PostMapping("/cmd/rebootIn")
    @ApiOperation("rad重启服务")
    public CommonContent reboot(@RequestBody DeviceCommonRequest<Object> vo) {
        CompletableFuture.runAsync(() -> {
            log.info("rad重启服务API，异步开始");
            asynServiceForCmd.reboot(vo);
        });
        log.info("rad重启服务API，出口");

        return CommonContent.ok();

    }

    //恢复出厂设置
    @PostMapping("/cmd/restoreFactorySetIn")
    @ApiOperation("rad恢复出厂设置")
    public CommonContent restoreFactorySetIn(@RequestBody DeviceCommonRequest<Object> vo) {
        CompletableFuture.runAsync(() -> {
            log.info("rad恢复出厂设置，异步开始");
            asynServiceForCmd.restoreFactorySetIn(vo);
        });
        log.info("rad恢复出厂设置，出口");
        return CommonContent.ok();


    }

    //网络参数设置
    @PostMapping("/cmd/netSetIn")
    @ApiOperation("rad网络参数设置")
    public CommonContent netSetIn(@RequestBody DeviceCommonRequest<NetSetInVo> vo) {
        CompletableFuture.runAsync(() -> {
            log.info("rad网络参数设置，异步开始");
            asynServiceForCmd.netSetIn(vo);
        });
        log.info("rad网络参数设置，出口");
        return CommonContent.ok();

    }

    //配置参数设置
    @PostMapping("/cmd/configSetIn")
    @ApiOperation("rad配置参数设置")
    public CommonContent configSetIn(@RequestBody DeviceCommonRequest<ConfigSetInVo> vo) {
        CompletableFuture.runAsync(() -> {
            log.info("rad配置参数设置，异步开始");
            asynServiceForCmd.configSetIn(vo);
        });
        log.info("rad配置参数设置，出口");
        return CommonContent.ok();

    }

    // 设备信息查询
    @PostMapping("/search/propIn")
    @ApiOperation("查询类接口包括 工作状态查询、网络参数查询、配置参数查询")
    public CommonContent<Prop> queryRad(@RequestBody QueryVo vo) {
        Prop c = asynServiceForCmd.commonInterfaceForQuery(vo);
        return CommonContent.ok(c);
    }
}
