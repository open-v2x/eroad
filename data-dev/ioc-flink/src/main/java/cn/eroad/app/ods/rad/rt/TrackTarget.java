package cn.eroad.app.ods.rad.rt;

import com.alibaba.fastjson2.annotation.JSONField;

import java.io.Serializable;


public class TrackTarget implements Serializable {
    // 使用@JSONField注解来指定字段名和序列化特性
    @JSONField(name = "acceleration")
    public Double acceleration;

    @JSONField(name = "confidenceLevel")
    public Integer confidenceLevel;

    @JSONField(name = "headingAngle")
    public Double headingAngle;

    @JSONField(name = "height")
    public Double height;

    @JSONField(name = "laneNo")
    public Integer laneNo;

    @JSONField(name = "latitude")
    public Double latitude;

    @JSONField(name = "longitude")
    public Double longitude;

    @JSONField(name = "rcs")
    public Long rcs;

    @JSONField(name = "speed")
    public Double speed;

    @JSONField(name = "targetHeight")
    public Double targetHeight;

    @JSONField(name = "targetId")
    public Integer targetId;

    @JSONField(name = "targetLength")
    public Double targetLength;

    @JSONField(name = "targetType")
    public Integer targetType;

    @JSONField(name = "targetWidth")
    public Double targetWidth;
}
