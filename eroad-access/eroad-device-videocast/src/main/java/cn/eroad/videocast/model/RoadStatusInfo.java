package cn.eroad.videocast.model;

import lombok.Data;

/**
 * @Description: 道路状态信息
 * @Param:
 * @return:
 * @Author: nbr
 * @Date: 2022/4/28
 */
@Data
public class RoadStatusInfo {
    //车道编号
    private Long LaneID;

    //通过车辆总数
    private Long Vehicles;

    //平均车速
    private Long AverageSpeed;

    //时间占有率,单位时间内通过断面的车辆所用时间的总和占单位时间的比例
    private Long TimeOccupyRatio;

    //空间占有率
    private Long SpaceOccupyRatio;

    //车辆分布情况即车间距方差
    private Float Pareto;

    //头车位置
    private Float HeadPos;

    //头车速度
    private Float HeadSpeed;

    //末车位置
    private Float Last_Pos;

    //末车速度
    private Float lastSpeed;
}
