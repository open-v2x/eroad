package cn.eroad.rad.model;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TrafficStatusInformation {
    private Integer aisleId;
    private Integer queueLength;
    private Integer queueVehicleNumber;
}
