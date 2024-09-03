package cn.eroad.rad.service;

import cn.eroad.core.domain.CommonResult;
import cn.eroad.core.utils.StringUtil;
import cn.eroad.core.vo.DeviceCommonRequest;
import cn.eroad.device.operate.DeviceCache;
import cn.eroad.device.vo.Device;
import cn.eroad.rad.config.AppConfig;
import cn.eroad.rad.model.*;
import cn.eroad.rad.model.response.*;
import cn.eroad.rad.netty.UdpNettyClient;
import cn.eroad.rad.util.ByteUtil;
import cn.eroad.rad.util.SendMessageEncode;
import cn.eroad.rad.vo.Net;
import cn.eroad.rad.vo.QueryVo;
import cn.eroad.rad.vo.SnNetList;
import cn.eroad.rad.vo.api.ConfigSetInVo;
import cn.eroad.rad.vo.api.NetSetInVo;
import cn.eroad.redis.utils.RedisUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.socket.DatagramPacket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Service
@Slf4j
public class ApiRemoteOperateService {
    public static final Map<String, Integer> seqNum = new HashMap<>(1);
    //    @Autowired

    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;
    @Autowired
    private RedisUtil redisUtil;


    public static Boolean checkHaveNullKey(ConfData ConfData) {
        Class cls = ConfData.getClass();
        Field[] fields = cls.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field f = fields[i];
            f.setAccessible(true);
            try {

                System.out.println("属性名:" + f.getName() + " 属性值:" + f.get(ConfData));
                if (f.get(ConfData) == null) {
                    return false;
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return true;
    }


    //设备重启
    public CommonResult rebootUnifiedFormat(DeviceCommonRequest<Object> vo) {
        CommonResult commonResult = new CommonResult();
        CommonResult.FailItem failList = new CommonResult.FailItem();
        List<FailMsg> failMsgs = new ArrayList<>();//用于存放通过sn查询不到ip或端口的设备信息
        if (vo != null) {
            List<String> snList = vo.getSnList();
            List<String> remove = new ArrayList<>();
            Integer intervalTime = vo.getIntervalTime();

            if (snList != null && snList.size() > 0) {
                for (int i = 0; i < snList.size(); i++) {
                    String sn = snList.get(i);
                    String content = SendMessageEncode.encodeReboot(sn.trim());
                    String ip = DeviceCache.getIpBySnFromMap(sn);
                    String portStr = DeviceCache.getPortBySnFromMap(sn);
                    if (null == portStr || ip == null) {
                        FailMsg fm = new FailMsg();
                        fm.setSn(sn);
                        fm.setErrMsg("无法通过sn查询网到ip或端口");
                        fm.setErrCode("404");
                        failMsgs.add(fm);
                        log.info("设备通过sn查询网络参数信息然失败,设备为{}，ip{}, ", sn, ip);
                        remove.add(sn);
                        continue;
                    }
                    Integer port = Integer.valueOf(portStr);
                    try {
                        UdpNettyClient.sendMessage(content, ip, port);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (intervalTime > 0) {
                        try {
                            Thread.sleep(intervalTime);
                        } catch (InterruptedException e) {
                        }
                    }
                }
                snList.removeAll(remove);
                //之前传参为JSON类型，修改后为实体类，将其中list转为JSON数组格式
                JSONArray jsonArraySnList = JSONArray.parseArray(JSONObject.toJSONString(snList));
                //准备从redis中获取返回值
                ResultDataForControl resultDataForControl = new ResultDataForControl();
                resultDataForControl = getRespondResultByControl(jsonArraySnList, AppConfig.REDIS_REBOOT);

                List<FailMsg> failMsgList = resultDataForControl.getFailList();
                failMsgList.addAll(failMsgs);
                List<String> successList = resultDataForControl.getSuccessList();
                List<CommonResult.FailItem> failLists = new ArrayList<>();

                if (failMsgList.size() != 0) {
                    for (FailMsg f : failMsgList) {
                        CommonResult.FailItem fail = new CommonResult.FailItem();
                        fail.setErrMsg(f.getErrMsg());
                        fail.setSn(f.getSn());
                        fail.setErrCode(Integer.parseInt(f.getErrCode()));
                        failLists.add(fail);
                    }
                }

                commonResult.setSuccessList(successList);
                commonResult.setFailList(failLists);
                return commonResult;

            } else {
                //snList为空
                failList.setErrMsg("snList为空");
                return commonResult;
            }
        } else {
            //形参为空
            failList.setErrMsg("传入参数为空");
            return commonResult;
        }

    }

    //恢复出厂设置
    public CommonResult restoreFactorySetInUnifiedFormat(DeviceCommonRequest<Object> vo) {
        CommonResult commonResult = new CommonResult();
        CommonResult.FailItem failList = new CommonResult.FailItem();
        List<FailMsg> failMsgs = new ArrayList<>();//用于存放通过sn查询不到ip或端口的设备信息
        if (vo != null) {
            List<String> snList = vo.getSnList();
            Integer intervalTime = vo.getIntervalTime();
            List<String> remove = new ArrayList<>();
            if (snList != null && snList.size() > 0) {
                for (int i = 0; i < snList.size(); i++) {
                    String sn = snList.get(i);

                    String content = SendMessageEncode.encodeReset(sn.trim());

                    String ip = DeviceCache.getIpBySnFromMap(sn);
                    String portStr = DeviceCache.getPortBySnFromMap(sn);
                    if (null == portStr || ip == null) {
                        FailMsg fm = new FailMsg();
                        fm.setSn(sn);
                        fm.setErrMsg("无法通过sn查询网到ip或端口");
                        fm.setErrCode("404");
                        failMsgs.add(fm);
                        log.info("设备通过sn查询网络参数信息然失败,设备为{}，ip{}, ", sn, ip);
                        remove.add(sn);
                        continue;
                    }
                    Integer port = Integer.valueOf(portStr);
                    try {
                        UdpNettyClient.sendMessage(content, ip, port);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (intervalTime > 0) {
                        try {
                            Thread.sleep(intervalTime);
                        } catch (InterruptedException e) {
                            // e.printStackTrace();
                        }
                    }
                }
                snList.removeAll(remove);
                //之前传参为JSON类型，修改后为实体类，将其中list转为JSON数组格式
                JSONArray jsonArraySnList = JSONArray.parseArray(JSONObject.toJSONString(snList));
                //准备从redis中获取返回值
                ResultDataForControl resultDataForControl = new ResultDataForControl();
                resultDataForControl = getRespondResultByControl(jsonArraySnList, AppConfig.REDIS_RESET);

                List<FailMsg> failMsgList = resultDataForControl.getFailList();
                List<String> successList = resultDataForControl.getSuccessList();
                failMsgList.addAll(failMsgs);
                List<CommonResult.FailItem> failLists = new ArrayList<>();
                if (failMsgList.size() != 0) {
                    for (FailMsg f : failMsgList) {
                        CommonResult.FailItem fail = new CommonResult.FailItem();
                        fail.setErrMsg(f.getErrMsg());
                        fail.setSn(f.getSn());
                        fail.setErrCode(Integer.parseInt(f.getErrCode()));
                        failLists.add(fail);
                    }
                }
                commonResult.setSuccessList(successList);
                commonResult.setFailList(failLists);
                return commonResult;
            } else {
                //snList为空
                failList.setErrMsg("snList为空");
                return commonResult;
            }
        } else {
            //形参为空
            failList.setErrMsg("传入参数为空");
            return commonResult;
        }
    }

    //网络参数设置
    public CommonResult netSetInUnifiedFormat(DeviceCommonRequest<NetSetInVo> vo) {
        CommonResult commonResult = new CommonResult();
        List<FailMsg> failMsgs = new ArrayList<>();//用于存放通过sn查询不到ip或端口的设备信息
        CommonResult.FailItem failList = new CommonResult.FailItem();
        if (vo != null && vo.getCmdProps().getSnNetList() != null) {
            List<SnNetList> snNetLists = vo.getCmdProps().getSnNetList();
            if (snNetLists != null && snNetLists.size() > 0) {
                JSONArray allSnList = new JSONArray();
                for (SnNetList snNetList : snNetLists) {
                    String sn = snNetList.getSn();
                    Network net = snNetList.getNet();
                    if (net == null) {
                        break;
                    }

                    String content = null;
                    try {
                        content = SendMessageEncode.encodeNetworkDataSet(net, sn.trim());
                    } catch (Exception e) {
                        failList.setErrMsg("传入参数有误");
                        return commonResult;
                    }
                    String ip = DeviceCache.getIpBySnFromMap(sn);
                    String portStr = DeviceCache.getPortBySnFromMap(sn);
                    if (null == portStr || ip == null) {
                        FailMsg fm = new FailMsg();
                        fm.setSn(sn);
                        fm.setErrMsg("无法通过sn查询网到ip或端口");
                        fm.setErrCode("404");
                        failMsgs.add(fm);
                        log.info("设备通过sn查询网络参数信息然失败,设备为{}，ip{}, ", sn, ip);
                        continue;
                    }
                    allSnList.add(sn);
                    Integer port = Integer.valueOf(portStr);
                    try {
                        UdpNettyClient.sendMessage(content, ip, port);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                ResultDataForControl resultData = new ResultDataForControl();
                List<FailMsg> failMsgList = new ArrayList<>();
                if (allSnList.size() > 0) {
                    resultData = getRespondResultByControl(allSnList, AppConfig.REDIS_SET_NET);
                    failMsgList = resultData.getFailList();
                }
                failMsgList.addAll(failMsgs);
                List<String> successList = resultData.getSuccessList();
                List<CommonResult.FailItem> failLists = new ArrayList<>();
                if (failMsgList.size() != 0) {
                    for (FailMsg f : failMsgList) {
                        CommonResult.FailItem fail = new CommonResult.FailItem();
                        fail.setErrMsg(f.getErrMsg());
                        fail.setSn(f.getSn());
                        fail.setErrCode(Integer.parseInt(f.getErrCode()));
                        failLists.add(fail);
                    }
                }
                commonResult.setSuccessList(successList);
                commonResult.setFailList(failLists);
            } else {
                //snNetLists为空
                failList.setErrMsg("snNetLists为空");
                return commonResult;
            }
        } else {
            //传入参数为空
            failList.setErrMsg("传入参数为空");
            return commonResult;
        }
        return commonResult;
    }

    //配置参数设置
    public CommonResult configSetInUnifiedFormat(DeviceCommonRequest<ConfigSetInVo> vo) {
        CommonResult commonResult = new CommonResult();
        CommonResult.FailItem failList = new CommonResult.FailItem();
        List<FailMsg> failMsgs = new ArrayList<>();//用于存放通过sn查询不到ip或端口的设备信息
        if (vo != null) {
            List<String> snList = vo.getSnList();
            if (snList != null && snList.size() > 0) {
                JSONArray allSnList = new JSONArray();
                ConfData confData = vo.getCmdProps().getConfData();
                Boolean is_null = checkHaveNullKey(confData);
                if (!is_null) {
                    failList.setErrMsg("confData工作状态结构为空");
                    return commonResult;
                }
                String content = null;
                for (int i = 0; i < snList.size(); i++) {
                    String sn = snList.get(i);
                    try {
                        content = SendMessageEncode.encodeConfDataSet(confData, sn.trim());
                    } catch (Exception e) {
                        failList.setErrMsg("参数有误");
                        return commonResult;
                    }

                    Integer intervalTime = vo.getIntervalTime();

                    String ip = DeviceCache.getIpBySnFromMap(sn);
                    String portStr = DeviceCache.getPortBySnFromMap(sn);
                    if (null == portStr || ip == null) {
                        FailMsg fm = new FailMsg();
                        fm.setSn(sn);
                        fm.setErrMsg("无法通过sn查询网到ip或端口");
                        fm.setErrCode("404");
                        failMsgs.add(fm);
                        log.info("设备通过sn查询网络参数信息然失败,设备为{}，ip{}, ", sn, ip);
                        continue;
                    }
                    allSnList.add(sn);
                    Integer port = Integer.valueOf(portStr);
                    try {
                        UdpNettyClient.sendMessage(content, ip, port);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (intervalTime > 0) {
                        try {
                            Thread.sleep(intervalTime);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                ResultDataForControl resultData = new ResultDataForControl();
                List<FailMsg> failMsgList = new ArrayList<>();
                if (allSnList.size() > 0) {
                    resultData = getRespondResultByControl(allSnList, AppConfig.REDIS_SET_CONFIG);
                    failMsgList = resultData.getFailList();
                }
                failMsgList.addAll(failMsgs);
                List<String> successList = resultData.getSuccessList();
                List<CommonResult.FailItem> failLists = new ArrayList<>();
                if (failMsgList.size() != 0) {
                    for (FailMsg f : failMsgList) {
                        CommonResult.FailItem fail = new CommonResult.FailItem();
                        fail.setErrMsg(f.getErrMsg());
                        fail.setSn(f.getSn());
                        fail.setErrCode(Integer.parseInt(f.getErrCode()));
                        failLists.add(fail);
                    }
                }

                commonResult.setSuccessList(successList);
                commonResult.setFailList(failLists);
            } else {
                failList.setErrMsg("传入参数snList为空");
                return commonResult;
            }
        } else {
            failList.setErrMsg("传入参数为空");
            return commonResult;
        }

        return commonResult;
    }

    public void sendMessageByUdp(String parm, UdpModel ctx) {
        log.info("下发给设备ip = {}，端口 = {},param = {}", ctx.getPacket().sender().getAddress().getHostAddress(), ctx.getPacket().sender().getPort(), parm);
        if (!"".equals(parm)) {
            ByteBuf buff = Unpooled.buffer();
            buff.writeBytes(ByteUtil.hexString2Bytes(parm));
            ctx.getCtx().channel().writeAndFlush(new DatagramPacket(PooledByteBufAllocator.DEFAULT.buffer().writeBytes(buff), ctx.getPacket().sender()));
        }
    }


    // 控制接口
    public ResultDataForControl getRespondResultByControl(JSONArray snList, String type) {
        List<String> redisKeys = new ArrayList<>();
        List<String> timeOutList = new ArrayList<>();
        for (int m = 0; m < snList.size(); m++) {
            redisKeys.add(snList.get(m) + "-" + type);
            timeOutList.add(snList.getString(m));
        }
        ResultDataForControl resultData = new ResultDataForControl();
        List<RespondToCache> caches = getCacheFromRedis(redisKeys);
        List<String> successList = new ArrayList<>();
        List<FailMsg> failList = new ArrayList<>();
        if (caches != null && caches.size() > 0) {
            for (int i = 0; i < caches.size(); i++) {
                System.out.println(caches.get(i));
                timeOutList.remove(caches.get(i).getSn());
                if (caches.get(i).getSuccess()) {
                    // 成功的设备
                    successList.add(caches.get(i).getSn());
                } else {
                    // 失败的设备
                    FailMsg failMsg = new FailMsg();
                    failMsg.setSn(caches.get(i).getSn());
                    failMsg.setErrCode(caches.get(i).getErrCode());
                    failMsg.setErrMsg(caches.get(i).getErrMsg());
                    failList.add(failMsg);
                }
            }
            if (timeOutList.size() > 0) {
                timeOutList.stream().forEach(item -> {
                    failList.add(getTimeOutRespond(item));
                });
            }
        } else {
            // 所有的设备都超时
            timeOutList.stream().forEach((item) -> {
                failList.add(getTimeOutRespond(item));
            });
        }
        resultData.setSuccessList(successList);
        resultData.setFailList(failList);
        return resultData;
    }

    private FailMsg getTimeOutRespond(String sn) {
        FailMsg failMsg = new FailMsg();
        failMsg.setSn(sn);
        failMsg.setErrMsg(ResultType.connectTimeout.getResultMsg());
        failMsg.setErrCode(ResultType.connectTimeout.getResultCode() + "");
        return failMsg;
    }

    public List<RespondToCache> getCacheFromRedis(List<String> snList) {
        Future<List<RespondToCache>> future = taskExecutor.submit(() -> {
            List<RespondToCache> result = new ArrayList();
            try {
                snList.forEach(sn -> {
                    String item = String.valueOf(redisUtil.get(sn));
                    result.add(JSON.parseObject(item, RespondToCache.class));
                });
            } catch (Exception e) {
                log.error("{}列表获取缓存信息有误: {}", snList, e.getMessage());
            }
            return result;
        });
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("{}列表获取缓存信息有误: {}", snList, e.getMessage());
        }
        return null;
    }

    public String byteToHexString(byte[] bytes) {
        StringBuilder resultHexString = new StringBuilder();
        String tempStr;
        for (byte b : bytes) {
            //这里需要对b与0xff做位与运算，
            //若b为负数，强制转换将高位位扩展，导致错误，
            //故需要高位清零
            tempStr = Integer.toHexString(b & 0xff);
            //若转换后的十六进制数字只有一位，
            //则在前补"0"
            if (tempStr.length() == 1) {
                resultHexString.append(0).append(tempStr);
            } else {
                resultHexString.append(tempStr);
            }
        }
        return resultHexString.toString();
    }
    public ResultDataForQuery getCacheFromRedis(String sn) {
        ResultDataForQuery resultDataForQuery = new ResultDataForQuery();
        try {
            String result = String.valueOf(redisUtil.get(sn));
            if (StringUtil.isNotEmpty(result)) {
                resultDataForQuery = JSON.parseObject(result, ResultDataForQuery.class);
            }
        } catch (Exception e) {
            log.error("{}获取缓存信息有误{}", sn, e.getMessage());
        }
        return resultDataForQuery;
    }

    private String getSeqNum(String deviceSn) {
        String key = deviceSn;
        Integer num;
        if (seqNum.containsKey(key)) {
            num = seqNum.get(key);
            if (num > 65534) {
                num = 1;
            } else {
                num++;
            }
        } else {
            num = 1;
        }
        seqNum.put(key, num);
        return num + "";
    }


    public Prop commonInterfaceForQuery(QueryVo body) {
        Prop commonRespond = new Prop();
        String sn = body.getSn();
        List<String> snList = new ArrayList<>();
        List<String> queryTypes = body.getQueryTypes();
        if (queryTypes != null && queryTypes.size() > 0) {
            for (int i = 0; i < queryTypes.size(); i++) {
                String type = queryTypes.get(i);
                String content = "";
                String key = "";
                switch (type) {
                    case AppConfig.SEARCH_WORKSTATUS:
                        content = SendMessageEncode.encodeWorkingStatusQuery(sn.trim());
                        key = sn + "-" + AppConfig.SEARCH_WORKSTATUS;
                        break;
                    case AppConfig.SEARCH_NET:
                        content = SendMessageEncode.encodeNetworkDataQuery(sn.trim());
                        key = sn + "-" + AppConfig.SEARCH_NET;
                        break;
                    case AppConfig.SEARCH_CONFIG:
                        content = SendMessageEncode.encodeConfDataQuery(sn.trim());
                        key = sn + "-" + AppConfig.SEARCH_CONFIG;
                        break;
                }
                Device device = DeviceCache.getDeviceFromCache(sn);

                String ip = DeviceCache.getIpBySnFromMap(sn);
                if (device.getPort() == null) {
                    continue;
                }
                Integer port = Integer.parseInt(device.getPort());

                try {
                    UdpNettyClient.sendMessage(content, ip, port);
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("设备{}通过查询网元信息然后发送到{}失败,端口为{}，内容为{}, 错误信息: {}",
                            sn, ip, port, content, e.getMessage());
                }
                snList.add(key);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            commonRespond = getRespondResultByMultiQuery(snList, sn);
            commonRespond.setResultCode(AppConfig.POST_SUCCESS);
            commonRespond.setResultMsg(AppConfig.OPERATE_SUCCESS);
        } else {
            commonRespond.setResult(ResultType.paramsError);
        }
        return commonRespond;
    }

    public Prop getRespondResultByMultiQuery(List<String> keyList, String sn) {

        List<RespondToCache> caches = getCacheFromRedis(keyList);
        Prop prop = new Prop();
        if (caches != null && !caches.isEmpty()) {
            for (RespondToCache respondToCache : caches) {
                if (null != respondToCache) {
                    String type = respondToCache.getType();
                    JSONObject jo = JSON.parseObject(respondToCache.getProp());
                    if (type.equals(AppConfig.SEARCH_WORKSTATUS)) {
                        JSONObject workJson = jo.getJSONObject("workStatus");
                        WorkingStatus workingStatus = new WorkingStatus();
                        workingStatus.setStatus(workJson.getString("status") != null ? Integer.parseInt(workJson.getString("status")) : null);
                        workingStatus.setHumidity(workJson.getString("humidity"));
                        workingStatus.setTemperature(workJson.getString("temperature"));
                        workingStatus.setVoltage(workJson.getString("voltage"));
                        prop.setWorkingStatus(workingStatus);
                    } else if (type.equals(AppConfig.SEARCH_NET)) {
                        Net net = new Net();
                        net.setHeartInterval(jo.getString("heartbeatCycle") != null ? Integer.parseInt(jo.getString("heartbeatCycle")) : null);
                        net.setIpv4Address(jo.getString("ipv4Address"));
                        net.setIpv4Gateway(jo.getString("ipv4Gateway"));
                        net.setIpv4Mask(jo.getString("ipv4Mask"));
                        net.setIpv6Gateway(jo.getString("ipv6Gateway"));
                        net.setIpv6GuaAddress(jo.getString("ipv6GuaAddress"));
                        net.setIpv6LlaAddress(jo.getString("ipv6LlaAddress"));
                        net.setLocalPort(jo.getInteger("localPort"));
                        net.setMac(jo.getString("mac"));
                        net.setPcUpPort(jo.getInteger("pcUpPort"));
                        net.setTargetPort(jo.getInteger("targetPort"));
                        net.setTargetIp(jo.getString("targetIp"));
                        net.setIpv6Mask(jo.getString("ipv6Mask"));
                        prop.setNet(net);
                    } else if (type.equals(AppConfig.SEARCH_CONFIG)) {
                        ConfData confData = new ConfData();
                        confData.setEventUp(jo.getInteger("eventUp"));
                        confData.setFlowFre(jo.getInteger("flowFre"));
                        confData.setPassFre(jo.getInteger("passFre"));
                        confData.setTraceFre(jo.getInteger("traceFre"));
                        confData.setTrafficStatusFre(jo.getInteger("trafficStatusFre"));
                        prop.setConfData(confData);
                    }
                }

            }
        }
        prop.setSn(sn);
        prop.setId(getSeqNum(sn));
        return prop;
    }
}
