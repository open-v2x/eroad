package cn.eroad.videocast.model;

import lombok.Data;

/**
 * @Description: 车道流量信息
 * @Param:
 * @return:
 * @Author: nbr
 * @Date: 2022/4/28
 */
@Data
public class LaneFlowInfo {
    //车道编号
    private Long LaneID;

    //流量状态
    private Long State;

    //流量值
    private Long Flow;

    //方向编号
    private Long Direction;

    //通过车辆总数
    private Long Vehicles;

    //平均车速
    private Long AverageSpeed;

    //平均车外廓长
    private Long VehicleLength;

    //时间占有率
    private Long TimeOccupyRatio;

    //空间占有率
    private Long SpaceOccupyRatio;

    //车头间距
    private Long SpaceHeadway;

    //车头时距
    private Long TimeHeadway;

    //车辆密度
    private Long Density;

    //超速车辆数
    private Long OverSpeedVehicles;

    //低速车辆数
    private Long UnderSpeedVehicles;

    //大型车交通量
    private Long LargeVehicles;

    //中型车交通量
    private Long MediumVehicles;

    //小型车交通量
    private Long SmallVehicles;

    //摩托车交通量
    private Long MotoVehicles;

    //超长车交通量
    private Long LongVehicles;

    //轿车交通量
    private Long SedanVehicles;

    //SUV 交通量
    private Long SUVVehicles;

    //面包车交通量
    private Long MinibusVehicles;

    //小货车交通量
    private Long MinivanVehicles;

    //中巴车交通量
    private Long MediumBusVehicles;

    //大客车交通量
    private Long LargeBusVehicles;

    //大货车交通量
    private Long LargeVanVehicles;

    //白牌车车流量
    private Long WhitePlateVehicles;

    //黄牌车车流量
    private Long YellowPlateVehicles;

    //蓝牌车车流量
    private Long BluePlateVehicles;

    //黑牌车车流量
    private Long BlackPlateVehicles;

    //其它牌车流量
    private Long OtherPlateVehicles;

    //交通量
    private Long Volume;

    //流率
    private Long FlowRate;

    //排队长度
    private Long BackOfQueue;

    //旅行时间
    private Long TravelTime;

    //延误次数
    private Long DelayNum;

    //延误（停车时间）
    private Long Delay;
}
