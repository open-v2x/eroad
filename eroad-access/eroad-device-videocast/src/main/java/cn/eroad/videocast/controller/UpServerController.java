package cn.eroad.videocast.controller;

import cn.eroad.videocast.model.connect.RegistParam;
import cn.eroad.videocast.model.connect.UnregisterResultVo;
import cn.eroad.videocast.model.data.Response;
import cn.eroad.videocast.service.DeviceNumberService;
import cn.eroad.videocast.model.config.UpServerConfigure;
import cn.eroad.videocast.model.regist.DataResult;
import cn.eroad.videocast.model.regist.RegistResult;
import cn.eroad.videocast.service.RegistService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Description: 设备连接
 * @Param:
 * @return:
 * @Author: nbr
 * @Date: 2022/6/20
 */
@RestController
@RequestMapping("/LAPI/V1.0/System/UpServer")
@Slf4j
@Api(value = "设备操作")
public class UpServerController {

    /**
     * @Description: 设备注册
     * @Param:
     * @return:
     * @Author: nbr
     * @Date: 2022/6/20
     */
    @Autowired
    RegistService registService;

    @Autowired
    DeviceNumberService deviceNumberService;

    public int time = (int) System.currentTimeMillis();//全局变量

    @GetMapping("Register")
    @ApiOperation(value = "设备注册")
    public Response register(RegistParam registParam) {
        log.info("**收到注册请求**:" + registParam);
        //注册 -- 注册、在线上报
        RegistResult result = registService.registOnce(registParam);
        //返回格式统一
        Response responseData = new Response();
        responseData.setResponseURL("/LAPI/V1.0/System/UpServer/register");
        responseData.setStatusCode(String.valueOf(result.getData()));
        responseData.setResponseString(result.getResponseString());
        if (result.getData() == 401) {
            responseData.setData(new DataResult().setNonce(result.getNonce()));
        } else {
            responseData.setData(new DataResult().setCnonce(result.getCnonce()));
            responseData.setData(new DataResult().setResign(result.getResign()));
        }
        log.info("**返回ResponseData**：" + responseData);
        System.out.println(responseData);
        return responseData;
    }

    /**
     * @Description: 设备注销
     * @Param:
     * @return:
     * @Author: nbr
     * @Date: 2022/6/20
     */
    @PutMapping("Unregister")
    @ApiOperation(value = "设备注销")
    public Response unregister() {
        UnregisterResultVo resultVo = new UnregisterResultVo();
        resultVo.setTimeStamp(System.currentTimeMillis());
        resultVo.setTimeOut(60L);

        String sn = null;
        deviceNumberService.deleteDevice(sn);

        //返回格式统一
        Response responseData = new Response();
        responseData.setResponseURL("/LAPI/V1.0/System/UpServer/unregister");
        responseData.setResponseString("succeed");
        responseData.setCseq(Long.valueOf(""));//消息序号，没有先空着
        return responseData;
    }

    /**
     * @Description: 设备保活
     * @Param:
     * @return:
     * @Author: nbr
     * @Date: 2022/6/20
     */
    @PutMapping("Keepalive")
    @ApiOperation(value = "设备保活")
    public Response keepalive() {
        UnregisterResultVo resultVo = new UnregisterResultVo();
        resultVo.setTimeStamp(System.currentTimeMillis());
        resultVo.setTimeOut(60L);
        String sn = null;
        if (System.currentTimeMillis() - time > 60) {
            registService.deviceStatus(sn, "雷视一体机", 0);
        } else {
            registService.deviceStatus(sn, "雷视一体机", 1);
        }
        time = (int) System.currentTimeMillis();

        //返回格式统一
        Response responseData = new Response();
        responseData.setResponseURL("/LAPI/V1.0/System/UpServer/Keepalive");
        responseData.setResponseString("succeed");
        responseData.setCseq(Long.valueOf(""));
        return responseData;
    }

    /**
     * @Description: 数据服务器
     * @Param:
     * @return:
     * @Author: nbr
     * @Date: 2022/6/20
     */
    @PutMapping("Configure")
    @ApiOperation(value = "数据服务器")
    public Response configure() {
        UpServerConfigure configure = new UpServerConfigure();
        configure.setEnabled(1);
        configure.setAddressType(1);
        configure.setAddress("FD20:D1A9:3C94:2001::0287");
        Response responseData = new Response();
        responseData.setResponseURL("/LAPI/V1.0/System/UpServer/Configure");
        responseData.setResponseString("succeed");
        responseData.setCseq(Long.valueOf(""));
        return responseData;
    }
}
