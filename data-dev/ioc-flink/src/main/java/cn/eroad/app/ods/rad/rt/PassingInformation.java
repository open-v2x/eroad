package cn.eroad.app.ods.rad.rt;

import java.io.Serializable;


public class PassingInformation  implements Serializable {
    public Integer aisleId;
    public Integer direction;
    public Integer directionLane;
    public Integer distanceToStopLine;
    public Integer existenceState;
    public Integer existenceTime;
    public Integer ifBothWayDetection;
    public Integer speed;
    public Integer targetId;
    public Integer vehicleType;
}
