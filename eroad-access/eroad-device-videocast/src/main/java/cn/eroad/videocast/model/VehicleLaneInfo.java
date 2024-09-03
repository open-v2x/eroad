package cn.eroad.videocast.model;

import lombok.Data;

@Data
public class VehicleLaneInfo {
    //车道编号
    private Long LaneID;

    //限速值
    private Long LimitSpeed;

    //车道左下角 X 坐标
    private Float XPos;

    //车道左下角 Y 坐标
    private Float YPos;

    //角度偏移，默认为雷达相对
    private Float Angle;

    //车道宽度
    private Float Width;

    //车道长度
    private Float Length;

    //车道类型
    private Long LaneType;

    //车道行驶方向
    private Long LaneDirection;

    //车道描述
    private String LaneDescription;
}
