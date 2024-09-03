package cn.eroad.videocast.model;

import lombok.Data;

@Data
public class LaneChangeInfo {
    //变道前车道编号
    private Long Original;

    //变道后车道编号
    private Long Current;

    //位置结构体
    private Position position;
}
