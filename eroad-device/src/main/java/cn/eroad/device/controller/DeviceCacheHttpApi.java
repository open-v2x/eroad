package cn.eroad.device.controller;

import cn.eroad.device.dto.CacheDTO;
import cn.eroad.device.operate.DeviceCache;
import cn.eroad.device.vo.Device;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

/**
 * @project: eroad-frame
 * @ClassName: DeviceCacheHttpApi
 * @author: liyongqiang
 * @creat: 2022/7/11 10:32
 * 描述:
 */
@RestController
@RequestMapping("/device/cache")
@Api(tags = "设备缓存", value = "设备缓存接口")
@Slf4j
public class DeviceCacheHttpApi {

    @Autowired
    private DeviceCache deviceCache;

    private static int count = 200;

    /**
     * 响应式返回存量设备缓存数据
     *
     * @param type
     * @return
     */
    @GetMapping("/list")
    @ApiOperation(value = "设备存量数据查询接口")
    public Flux<CacheDTO> list(@RequestParam() String type) {
        List<Device> list = deviceCache.getDeviceListByType(type);
        if (list == null || list.isEmpty()) {
            return Flux.empty();
        }
        List<CacheDTO> temp = initFluxList(list);
        return Flux.fromIterable(temp);
    }

    /**
     * 根据存量设备list初始化为批量返回的list
     *
     * @param list
     * @return
     */
    private List<CacheDTO> initFluxList(List<Device> list) {
        List<CacheDTO> temp = new ArrayList<>();

        int batch = list.size() / count;
        if (list.size() % count != 0) {
            batch += 1;
        }
        for (int i = 0; i < batch; i++) {
            if (i + 1 == batch) {
                temp.add(CacheDTO.builder().list(list.subList(i * count, list.size())).build());
            } else {
                temp.add(CacheDTO.builder().list(list.subList(i * count, i * count + count)).build());
            }
        }

        return temp;
    }
}
