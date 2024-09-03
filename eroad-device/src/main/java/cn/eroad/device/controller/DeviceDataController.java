package cn.eroad.device.controller;

import cn.eroad.core.domain.CommonContent;
import lombok.Data;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RestController
public class DeviceDataController {

    @GetMapping("/device/data")
    public CommonContent<List<Item>> deviceData(@RequestParam(value = "type", required = false) String type) {
        if (StringUtils.isEmpty(type)) {
            return CommonContent.ok(Arrays.asList(
                    new Item("激光雷达", "lid"),
                    new Item("毫米波雷达", "rad"),
                    new Item("路侧单元", "rsu")
            ));
        }
        switch (type) {
            case "lid":
                return CommonContent.ok(Collections.singletonList(
                        new Item("原始点云数据", "point")
                ));
            case "rad":
                return CommonContent.ok(Arrays.asList(
                        new Item("点云数据", "point"),
                        new Item("车轨迹数据", "track"),
                        new Item("过车信息", "cross"),
                        new Item("交通状态信息", "status"),
                        new Item("交通流信息", "flow"),
                        new Item("异常事件信息", "event")
                ));
            case "rsu":
                return CommonContent.ok(Arrays.asList(
                        new Item("设备注册", "register"),
                        new Item("设备信息上报", "info"),
                        new Item("运维配置信息上报", "operation"),
                        new Item("业务配置信息上报", "service"),
                        new Item("设备运行状态上报", "deviceState"),
                        new Item("业务运行状态上报", "serviceState"),
                        new Item("实时告警信息", "alarm"),
                        new Item("操作日志信息", "log"),
                        new Item("心跳", "heartbeat"),
                        new Item("遗言", "lastWill")
                ));
            default:
                return CommonContent.error();
        }
    }
}


@Data
class Item {

    private String name;

    private String code;

    public Item(String name, String code) {
        this.name = name;
        this.code = code;
    }
}
