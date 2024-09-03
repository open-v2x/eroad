package cn.eroad.app.ods.rad.rt;

import com.alibaba.fastjson2.annotation.JSONField;

import java.util.List;


public class RtFlow extends RadData {
    public Integer number;
    public Integer flowRight;
    public Integer flowLeft;
    public Integer flowStraight;
    public List<TrafficFlow> trafficFlows;
    public Integer steeringRatio;

    @Override
    @JSONField(serialize = false,deserialize = false)
    public boolean isErrFormat() {
        if (super.isErrFormat())
            return true;
        return trafficFlows == null;
    }

    @Override
    @JSONField(serialize = false,deserialize = false)
    public List<String> getNullFiledNameList() {
        List<String> nullFiledNameList = super.getNullFiledNameList();
        if (trafficFlows == null)
            nullFiledNameList.add("trafficFlows");
        return nullFiledNameList;
    }
}
