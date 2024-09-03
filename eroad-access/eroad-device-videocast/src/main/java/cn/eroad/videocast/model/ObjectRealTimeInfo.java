package cn.eroad.videocast.model;

import lombok.Data;

@Data
public class ObjectRealTimeInfo {
    //目标 ID 号
    private Long ID;

    //x 坐标
    private Float X;

    //y 坐标
    private Float Y;

    //x 方向的速度
    private Float Velocityx;

    //Y 方向的速度
    private Float VelocityY;

    //目标航向速度即目标实际速度
    private Float Velocity;

    //字符枚举类型：0：小型车；1：行人；2：
    //非机动车；3：中型车；4：大型车
    private Long targetType;

    //目标经度，精确到小数点后 8 位
    private Double Longitude;

    //目标纬度，精确到小数点后 8 位
    private Double Latitude;

    //目标海拔
    private Double Altitude;

    //目标角度偏移，默认为雷达相对
    private Float Angle;

    //车道号 1-64
    private Long Lane;

    //车牌号码
    private String PlateNo;

    //车牌颜色
    private Long Color;

    //车牌类型
    private Long Type;
}
