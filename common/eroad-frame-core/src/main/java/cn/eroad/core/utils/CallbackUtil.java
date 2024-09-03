package cn.eroad.core.utils;

import cn.eroad.core.domain.CallbackEntity;
import cn.eroad.core.domain.CommonContent;
import cn.eroad.core.domain.CommonResult;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

@Slf4j
public class CallbackUtil {

    public static void callBack(CallbackEntity tc , CommonResult cr) {
        if(tc==null){
            log.error("未输入回调信息，不能执行结果反馈");
            return;
        }
        String callBackUrl = tc.getCallbackUrl();
        String methodName = tc.getMethod();
        String token = tc.getToken();
        String id = tc.getId();
        if (StringUtil.isEmpty(callBackUrl)) {
            log.error("需要异步回调的接口{},事务id{}，入参未找到回调地址", callBackUrl,id);
            return;
        }

        if(cr==null){
            log.error("回调时获取的数据为空", callBackUrl,id);
            return;
        }
        cr.setTimestamp(System.currentTimeMillis());
        CommonContent<CommonResult> cc ;
        if(cr.getFailList().size()==0 && cr.getSuccessList().size()==0){
            cc = CommonContent.error("F","FAC001","业务处理异常，无回调内容");
            cc.setBody(cr);
        }else{
            cc = CommonContent.ok(cr);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, token);
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
        //调用数据
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> formEntity = new HttpEntity<String>(JSON.toJSONString(cc), headers);

        try {
            ResponseEntity<String> responseEntity = null;
            if ("POST".equalsIgnoreCase(methodName)) {
                log.info("重发是POST方式：url:{},请求头和参数为：{}", callBackUrl, JSON.toJSONString(formEntity));
                responseEntity = restTemplate.postForEntity(callBackUrl, formEntity, String.class);
            } else if ("GET".equalsIgnoreCase(methodName)) {
                //问号传参和路径传参的区别?
                log.info("重发是GET方式：url:{},请求头和参数为：{}", callBackUrl, JSON.toJSONString(formEntity));
                responseEntity = restTemplate.exchange(callBackUrl, HttpMethod.GET, formEntity, String.class);
            }

            if (Objects.nonNull(responseEntity)) {
                log.info("发送回调，url为：{},结果为:{}", callBackUrl, responseEntity.getStatusCode().value());
            } else {
                log.info("发送回调，url为：{},结果异常", callBackUrl);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("发送命令实体异常，url为：{},异常信息为:{}", callBackUrl, e.getMessage());
        }
    }

}
