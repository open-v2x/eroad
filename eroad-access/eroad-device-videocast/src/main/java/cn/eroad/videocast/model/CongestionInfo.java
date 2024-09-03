package cn.eroad.videocast.model;

import lombok.Data;

@Data
public class CongestionInfo {
    //车道编号
    private Long LaneID;

    //拥堵等级 4级为畅通 3级为轻度拥堵 2级为中度拥堵 1级为严重拥堵
    private Long CongestionGrade;

    //路段车车辆数目
    private Long VehicleNum;

    //平均速度
    private Float AverageSpeed;
}
