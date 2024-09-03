package cn.eroad.rad.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CloudData {
    private Integer targetId;
    private short lateralDistance;
    private short verticalDistance;
    private Double lateralSpeed;
    private Double LongitudinalSpeed;
    private Double angle;
    private Integer snr;
}
