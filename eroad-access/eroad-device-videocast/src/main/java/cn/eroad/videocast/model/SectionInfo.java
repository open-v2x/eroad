package cn.eroad.videocast.model;

import lombok.Data;

@Data
public class SectionInfo {
    //截面编号
    private Long SectionID;

    //截面左下角 X 坐标
    private Float XPos;

    //截面左下角 Y 坐标
    private Float YPos;

    //截面宽度
    private Float Width;

    //截面长度
    private Float Length;

    //截面描述
    private String LaneDirection;
}
