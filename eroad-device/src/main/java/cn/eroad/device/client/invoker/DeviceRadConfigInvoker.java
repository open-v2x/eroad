package cn.eroad.device.client.invoker;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.List;

/**
* @Description: 毫米波雷达获取配置feign
* @Param:
* @return:
* @Author: nbr
* @Date: 2022/7/28
*/
@FeignClient(contextId = "dxsc.ac.device.rad.config", name = "DeviceRadConfigInvoker", url = "http://" + "${feign.rad.url}")
public interface DeviceRadConfigInvoker {
    @PostMapping("/api/v1/search/propIn/Schedul")
    void radApi(@RequestBody List<String> list);
}
