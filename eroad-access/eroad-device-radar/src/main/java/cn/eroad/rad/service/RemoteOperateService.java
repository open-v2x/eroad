package cn.eroad.rad.service;

import cn.eroad.core.utils.StringUtil;
import cn.eroad.device.operate.DeviceCache;
import cn.eroad.rad.model.response.*;
import cn.eroad.rad.netty.UdpNettyClient;
import cn.eroad.rad.util.SendMessageEncode;
import cn.eroad.redis.utils.RedisUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import cn.eroad.rad.config.AppConfig;
import cn.eroad.rad.model.ConfData;
import cn.eroad.rad.model.Network;
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
public class RemoteOperateService {
    public static final Map<String, Integer> seqNum = new HashMap<>(1);
    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    private RedisUtil redisUtil;

    public CommonRespond commonInterfaceForQuery(String body) {
        CommonRespond commonRespond = new CommonRespond();
        JSONObject requestBody = JSON.parseObject(body);
        String sn = requestBody.getString("sn");
        String prop = requestBody.getString("prop");
        JSONArray props = JSON.parseArray(prop);
        List<String> snList = new ArrayList<>();
        if (props != null && props.size() > 0) {
            for (int i = 0; i < props.size(); i++) {
                String type = props.getString(i);
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
                    default:
                        break;
                }
                String ip = DeviceCache.getIpBySnFromMap(sn);
                int port = Integer.parseInt(DeviceCache.getPortBySnFromMap(sn));

                try {
                    UdpNettyClient.sendUdpMessage(content, ip, Integer.valueOf(port), 5678);
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
            ResultDataForQuery resultDataForQuery = getRespondResultByMultiQuery(snList, sn);
            commonRespond.setResultCode(AppConfig.POST_SUCCESS);
            commonRespond.setResultMsg(AppConfig.OPERATE_SUCCESS);
            commonRespond.setResultData(resultDataForQuery);

        } else {
            commonRespond.setResult(ResultType.paramsError);
        }
        return commonRespond;
    }

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


    // 配置参数设置
    public CommonRespond confDataSet(String body) {
        CommonRespond commonRespond = new CommonRespond();
        JSONObject requestBody = JSON.parseObject(body);
        JSONObject cmdData = requestBody.getJSONObject("cmdData");
        if (cmdData != null) {
            JSONArray snList = cmdData.getJSONArray("snList");
            if (snList != null && snList.size() > 0) {
                JSONArray allSnList = new JSONArray();
                Integer intervalTime = 0;
                String configStr = cmdData.getString("config");
                ConfData confData = JSON.parseObject(configStr, ConfData.class);
                Boolean is_null = checkHaveNullKey(confData);
                if (!is_null) {
                    commonRespond.setResult(ResultType.paramsError);
                    return commonRespond;
                }
                String content = null;
                for (int m = 0; m < snList.size(); m++) {
                    String sn = snList.getString(m);
                    try {
                        content = SendMessageEncode.encodeConfDataSet(confData, sn.trim());
                    } catch (Exception e) {
                        commonRespond.setResult(ResultType.paramsError);
                        return commonRespond;
                    }
                    if (cmdData.containsKey("intervalTime")) {
                        intervalTime = cmdData.getInteger("intervalTime");
                    }

                    allSnList.add(sn);
                    String ip = DeviceCache.getIpBySnFromMap(sn);
                    Integer port = Integer.valueOf(DeviceCache.getPortBySnFromMap(sn));
                    try {
                        UdpNettyClient.sendUdpMessage(content, ip, port, 5678);
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
                if (!allSnList.isEmpty()) {
                    ResultDataForControl resultData = getRespondResultByControl(allSnList, AppConfig.REDIS_SET_CONFIG);
                    commonRespond.setResultData(resultData);
                    commonRespond.setResultCode(AppConfig.POST_SUCCESS);
                    commonRespond.setResultMsg(AppConfig.OPERATE_SUCCESS);
                } else {
                    commonRespond.setResult(ResultType.paramsError);
                }

            } else {
                commonRespond.setResult(ResultType.paramsError);
                return commonRespond;
            }
        } else {
            commonRespond.setResult(ResultType.paramsError);
            return commonRespond;
        }

        return commonRespond;
    }


    // 网络参数设置
    public CommonRespond networkConfSet(String body) {
        CommonRespond commonRespond = new CommonRespond();
        JSONObject requestBody = JSON.parseObject(body);
        JSONObject cmdData = requestBody.getJSONObject("cmdData");

        if (cmdData != null) {
            JSONArray snNetList = cmdData.getJSONArray("snNetList");
            if (snNetList != null && snNetList.size() > 0) {
                JSONArray allSnList = new JSONArray();
                for (int m = 0; m < snNetList.size(); m++) {

                    JSONObject snNet = snNetList.getJSONObject(m);
                    String sn = snNet.getString("sn");
                    allSnList.add(sn);
                    String netStr = snNet.getString("net");
                    Network net = JSON.parseObject(netStr, Network.class);
                    if (net == null) {
                        break;
                    }
                    String content;
                    try {
                        content = SendMessageEncode.encodeNetworkDataSet(net, sn.trim());
                    } catch (Exception e) {
                        commonRespond.setResult(ResultType.paramsError);
                        return commonRespond;
                    }
                    String ip = DeviceCache.getIpBySnFromMap(sn);
                    Integer port = Integer.valueOf(DeviceCache.getPortBySnFromMap(sn));
                    try {
                        UdpNettyClient.sendUdpMessage(content, ip, port, 5678);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (!allSnList.isEmpty()) {
                    ResultDataForControl resultData = getRespondResultByControl(allSnList, AppConfig.REDIS_SET_NET);
                    commonRespond.setResultData(resultData);
                    commonRespond.setResultCode(AppConfig.POST_SUCCESS);
                    commonRespond.setResultMsg(AppConfig.OPERATE_SUCCESS);
                } else {
                    commonRespond.setResult(ResultType.paramsError);
                }

            } else {
                commonRespond.setResult(ResultType.paramsError);
                return commonRespond;
            }


        } else {
            commonRespond.setResult(ResultType.paramsError);
            return commonRespond;
        }

        return commonRespond;
    }

    public CommonRespond commonInterfaceForControl(String body, String path) {
        CommonRespond commonRespond = new CommonRespond();
        if (StringUtil.isEmpty(body)) {
            commonRespond.setResult(ResultType.paramsError);
            return commonRespond;
        }
        JSONObject requestBody = JSON.parseObject(body);
        if (requestBody != null) {
            String snListStr = requestBody.getString("snList");
            Integer intervalTime = 0;
            if (requestBody.containsKey("intervalTime")) {
                intervalTime = requestBody.getInteger("intervalTime");
            }
            JSONArray snList = JSON.parseArray(snListStr);
            if (snList != null && snList.size() > 0) {
                for (int i = 0; i < snList.size(); i++) {
                    String sn = snList.getString(i);
                    String content = "";
                    switch (path) {
                        case AppConfig.URL_REBOOT:
                            content = SendMessageEncode.encodeReboot(sn.trim());
                            break;
                        case AppConfig.URL_RESET:
                            content = SendMessageEncode.encodeReset(sn.trim());
                            break;
                    }
                    String ip = DeviceCache.getIpBySnFromMap(sn);
                    Integer port = Integer.valueOf(DeviceCache.getPortBySnFromMap(sn));
                    try {
                        UdpNettyClient.sendUdpMessage(content, ip, port, 5678);
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
                // 准备从redis里面获取返回值
                ResultDataForControl resultDataForControl = new ResultDataForControl();
                switch (path) {
                    case AppConfig.URL_REBOOT:
                        resultDataForControl = getRespondResultByControl(snList, AppConfig.REDIS_REBOOT);
                        break;
                    case AppConfig.URL_RESET:
                        resultDataForControl = getRespondResultByControl(snList, AppConfig.REDIS_RESET);
                        break;
                }
                commonRespond.setResultData(resultDataForControl);
                commonRespond.setResultCode(AppConfig.POST_SUCCESS);
                commonRespond.setResultMsg(AppConfig.OPERATE_SUCCESS);
                return commonRespond;
            } else {
                commonRespond.setResult(ResultType.paramsError);
                return commonRespond;
            }
        } else {
            commonRespond.setResult(ResultType.paramsError);
            return commonRespond;
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
        if (caches != null && !caches.isEmpty()) {
            for (int i = 0; i < caches.size(); i++) {
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


    public ResultDataForQuery getRespondResultByQuery(String sn, int count, int gapTime, String type) {
        String redisKey = "";
        redisKey = sn + "-" + type;

        ResultDataForQuery resultDataForQuery = new ResultDataForQuery();
        resultDataForQuery = getCacheFromRedis(redisKey);
        resultDataForQuery.setId(getSeqNum(sn));
        return resultDataForQuery;
    }

    public ResultDataForQuery getRespondResultByMultiQuery(List<String> keyList, String sn) {

        List<RespondToCache> caches = getCacheFromRedis(keyList);
        ResultDataForQuery resultDataForQuery = new ResultDataForQuery();
        JSONObject jsonObject = new JSONObject();
        if (caches != null && caches.size() > 0) {
            for (RespondToCache respondToCache : caches) {
                String type = respondToCache.getType();
                jsonObject.put(type, JSON.parseObject(respondToCache.getProp()));
            }

        }
        resultDataForQuery.setSn(sn);
        resultDataForQuery.setId(getSeqNum(sn));
        resultDataForQuery.setProp(jsonObject);
        return resultDataForQuery;
    }

    public ResultDataForQuery getCacheFromRedis(String sn) {
        ResultDataForQuery resultDataForQuery = new ResultDataForQuery();
        try {
            String result = String.valueOf(redisUtil.get(sn));
            if (StringUtil.isNotEmpty(result)) {
                resultDataForQuery = JSON.parseObject(result, ResultDataForQuery.class);
            }
        } catch (Exception e) {
            // e.printStackTrace();
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
}
