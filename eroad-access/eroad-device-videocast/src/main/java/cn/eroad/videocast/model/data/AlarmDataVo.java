package cn.eroad.videocast.model.data;

import cn.eroad.videocast.model.AlarmInfo;
import lombok.Data;

@Data
public class AlarmDataVo {
    private String Reference;
    private AlarmInfo alarmInfo;
    private RelatedObjects relatedObjects;
}
