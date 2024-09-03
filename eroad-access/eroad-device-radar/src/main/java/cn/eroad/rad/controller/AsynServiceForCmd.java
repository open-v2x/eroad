package cn.eroad.rad.controller;

import cn.eroad.core.utils.CallbackUtil;
import cn.eroad.rad.model.Prop;
import cn.eroad.rad.service.ApiRemoteOperateService;
import cn.eroad.rad.vo.QueryVo;
import cn.eroad.rad.vo.api.ConfigSetInVo;
import cn.eroad.rad.vo.api.NetSetInVo;
import cn.eroad.core.domain.CommonResult;
import cn.eroad.core.domain.ResponseData;
import cn.eroad.core.vo.DeviceCommonRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AsynServiceForCmd {

    @Autowired
    private ApiRemoteOperateService remoteOperateService;

    public void reboot(DeviceCommonRequest<Object> vo) {
        CommonResult commonResultResponseData =
                remoteOperateService.rebootUnifiedFormat(vo);
        CallbackUtil.callBack(vo.getCallback(), commonResultResponseData);
    }

    public void restoreFactorySetIn(DeviceCommonRequest<Object> vo) {
        CommonResult commonResultResponseData =
                remoteOperateService.restoreFactorySetInUnifiedFormat(vo);
        CallbackUtil.callBack(vo.getCallback(), commonResultResponseData);
    }

    public void netSetIn(DeviceCommonRequest<NetSetInVo> vo) {
        CommonResult commonResultResponseData =
                remoteOperateService.netSetInUnifiedFormat(vo);
        CallbackUtil.callBack(vo.getCallback(), commonResultResponseData);
    }

    public void configSetIn(DeviceCommonRequest<ConfigSetInVo> vo) {
        CommonResult commonResultResponseData =
                remoteOperateService.configSetInUnifiedFormat(vo);
        CallbackUtil.callBack(vo.getCallback(), commonResultResponseData);
    }

    public Prop commonInterfaceForQuery(QueryVo vo) {
        return remoteOperateService.commonInterfaceForQuery(vo);
    }
}
