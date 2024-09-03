package cn.eroad.videocast.model.data;

import cn.eroad.videocast.model.OverFlowInfo;
import lombok.Data;

import java.util.List;

@Data
public class OverFlowVo {
    private String Reference;
    private String AlarmType;
    private Long TimeStamp;
    private Long Seq;
    private Long SourceID;
    private String SourceName;
    private String DeviceID;
    private String DeviceCode;
    private String RelatedID;
    private List<OverFlowInfo> list;
}
