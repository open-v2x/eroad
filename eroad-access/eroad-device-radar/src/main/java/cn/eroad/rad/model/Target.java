package cn.eroad.rad.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Target {
    private Integer targetId;
    private Integer targetType;
    private Double targetLength;
    private Double targetWidth;
    private Double targetHeight;
    private Double longitude;
    private Double latitude;
    private Float height;
    private Integer laneNo;
    private Float headingAngle;
    private Float speed;
    private Float acceleration;
    private Long rcs;
    private Integer confidenceLevel;

}
