package cn.eroad.videocast.model.data;

import cn.eroad.videocast.model.RoadStatusInfo;
import lombok.Data;

import java.util.List;

@Data
public class RoadFlowVo {
    private String Reference;
    private String CameraID;
    private String TollgateID;
    private String CurrentTime;
    private Long ID;
    private Long LaneNum;
    private List<RoadStatusInfo> list;
}
