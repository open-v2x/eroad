package cn.eroad.app.ods.rad.rt;

import com.alibaba.fastjson2.annotation.JSONField;

import java.util.List;


public class RtStatus extends RadData {
    public Integer number;
    @JSONField(name = "trafficStatusInformations")
    public List<TrafficStatusInformation> trafficStatusInformationList;

    @Override
    @JSONField(serialize = false,deserialize = false)
    public boolean isErrFormat() {
        if (super.isErrFormat())
            return true;
        return trafficStatusInformationList == null;
    }

    @Override
    @JSONField(serialize = false,deserialize = false)
    public List<String> getNullFiledNameList() {
        List<String> nullFiledNameList = super.getNullFiledNameList();
        if (trafficStatusInformationList == null)
            nullFiledNameList.add("trafficStatusInformationList");
        return nullFiledNameList;
    }
}
