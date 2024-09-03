package cn.eroad.videocast.model.data;

import cn.eroad.videocast.model.ObjectRealTimeInfo;
import lombok.Data;

import java.util.List;

/**
 * @Description: 实时目标数据上报参数
 * @Param:
 * @return:
 * @Author: nbr
 * @Date: 2022/7/8
 */
@Data
public class ObjectRealTimeDataVo {
    private String Reference;
    private String CameraID;
    private String TollgateID;
    private String CurrentTime;
    private Long ObjectNum;
    private List<ObjectRealTimeInfo> list;
}
