package cn.eroad.rad.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PassingInformation {
    private Integer aisleId;
    private Integer distanceToStopLine;
    private Integer vehicleType;
    private Integer direction;
    private Integer existenceState;
    private Integer speed;
    private Integer existenceTime;
    private Integer targetId;
    private Integer ifBothWayDetection;
    private Integer directionLane;
}
