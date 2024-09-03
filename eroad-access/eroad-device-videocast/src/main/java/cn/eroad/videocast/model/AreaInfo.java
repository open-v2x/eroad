package cn.eroad.videocast.model;

import lombok.Data;

@Data
public class AreaInfo {
    //区域编号
    private Long AreaID;

    //区域左下角 X 坐标
    private Float XPos;

    //区域左下角 Y 坐标
    private Float YPos;

    //区域宽度
    private Float Width;

    //区域长度
    private Float Length;
}
