package cn.eroad.app.ods.rad.rt;

import java.io.Serializable;


public class TrafficFlow implements Serializable {
    public Integer aisleId;
    public Integer averageHeadwayDistance;
    public Integer averageLength;
    public Integer averageSpeed;
    public Integer averageTimeShare;
    public Integer totalFlowA;
    public Integer totalFlowB;
    public Integer totalFlowC;
}
