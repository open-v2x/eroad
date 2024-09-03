package cn.eroad.videocast.model;

import lombok.Data;

/**
 * @Description: 车辆排队信息
 * @Param:
 * @return:
 * @Author: nbr
 * @Date: 2022/4/28
 */
@Data
public class VehQueueLenInfo {
    //车道编号
    private Long LaneID;

    //车道类型
    private Long LaneType;

    //车道行驶方向
    private Long LaneDirection;

    //车道描述
    private Long LaneDescription;

    //排队长度
    private Long QueueLength;

    //排队数量
    private Long QueueNum;

    //排队队首
    private Long QueueHead;

    //排队队尾
    private Long QueueTail;
}
