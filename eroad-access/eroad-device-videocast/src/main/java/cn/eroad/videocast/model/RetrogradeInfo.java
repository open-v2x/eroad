package cn.eroad.videocast.model;

import lombok.Data;

@Data
public class RetrogradeInfo {
    //车道编号
    private Long LaneID;

    //位置结构体
    private Position position;
}
