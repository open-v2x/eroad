package cn.eroad.rad.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TrafficFlow {
    private Integer aisleId;
    private Integer totalFlowA;
    private Integer totalFlowB;
    private Integer totalFlowC;
    private Integer averageTimeShare;
    private Integer averageSpeed;
    private Integer averageLength;
    private Integer averageHeadwayDistance;

}
