package cn.eroad.videocast.model;

import lombok.Data;

import java.util.List;

@Data
public class AlarmTypeList {
    //告警类型:排队溢出“OverFlow”,行人”Pedestrian”,非机动车“Non-motor”,停车”Parking”,超速”OverSpeeds”,低速”LowSpeeds”,逆行”Retrograde”
    //拥堵”Congestion”,变道”LaneChange”,异常占用”OccupancyEmergency”,闯入”RestrictedArea”
    private String AlarmType;

    //告警时间
    private Long TimeStamp;

    //告警事件与告警数据的关联ID,同一个相机内全局唯一
    private String RelatedID;

    //告警列表，根据告警类型决定是什么类型的列表
    private List<Object> list;
}
