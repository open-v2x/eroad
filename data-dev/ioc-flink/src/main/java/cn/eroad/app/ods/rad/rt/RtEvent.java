package cn.eroad.app.ods.rad.rt;

import com.alibaba.fastjson2.annotation.JSONField;

import java.util.List;


public class RtEvent extends RadData {
    public Integer eventId;
    public Integer targetId;
    public Integer eventType;
    public Double longitude;
    public Double latitude;
    public Double height;
    public Integer scope;
    public Integer lane;

    @Override
    @JSONField(serialize = false,deserialize = false)
    public boolean isErrFormat() {
        if (super.isErrFormat())
            return true;
        return eventId == null;
    }

    @Override
    @JSONField(serialize = false,deserialize = false)
    public List<String> getNullFiledNameList() {
        List<String> nullFiledNameList = super.getNullFiledNameList();
        if (eventId == null)
            nullFiledNameList.add("eventId");
        return nullFiledNameList;
    }

}
