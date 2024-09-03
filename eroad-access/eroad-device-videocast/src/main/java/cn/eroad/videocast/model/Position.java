package cn.eroad.videocast.model;

import lombok.Data;

/**
 * @Description: 车道信息位置结构体
 * @Param:
 * @return:
 * @Author: nbr
 * @Date: 2022/4/29
 */
@Data
public class Position {
    //x坐标
    private Float XPos;

    //y坐标
    private Float YPos;

    //x方向的速度
    private Float XSpeed;

    //y方向的速度
    private Float YSpeed;
}
