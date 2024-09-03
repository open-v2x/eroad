package cn.eroad.videocast.service;


import cn.eroad.videocast.model.data.Response;
import org.springframework.stereotype.Service;

/**
 * @Author: zhaohanqing
 * @Date: 2022/8/11 11:08
 * @Description:
 */
@Service
public class FormatService {
    public Response formatReturn(String url, String seq) {
        //返回格式统一
        Response responseData = new Response();
        responseData.setResponseURL("/LAPI/V1.0/System/Event/Notification" + url);
        responseData.setResponseString("succeed");
        responseData.setCseq(Long.valueOf(seq));
        responseData.setData(null);
        return responseData;
    }
}
