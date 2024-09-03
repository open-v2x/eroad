package cn.eroad.videocast.model.config;

import lombok.Data;

import java.util.List;

@Data
public class SubscribeVehicleCondition {
    //订阅类型
    private Long Type;
    private long AddressType;
    private String IPAddress;
    private long Port;
    private String DeviceID;
    private long Duration;
    private List<SubscribeVehicleCondition> SubscribeVehicleCondition;

}
