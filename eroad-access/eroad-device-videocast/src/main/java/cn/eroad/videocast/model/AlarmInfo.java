package cn.eroad.videocast.model;

import lombok.Data;

import java.util.List;

/**
 * @Description: 告警信息
 * @Param:
 * @return:
 * @Author: nbr
 * @Date: 2022/4/29
 */
@Data
public class AlarmInfo {
    //告警列表
    private List<AlarmTypeList> list;
}
