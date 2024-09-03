package cn.eroad.videocast.model.data;

import cn.eroad.videocast.model.LaneFlowInfo;
import lombok.Data;

import java.util.List;

@Data
public class TrafficFlowVo {
    private String Reference;
    private String CameraID;
    private String TollgateID;
    private String CurrentTime;
    private Long Period;
    private Long ID;
    private Long LaneNum;
    private List<LaneFlowInfo> list;
}
