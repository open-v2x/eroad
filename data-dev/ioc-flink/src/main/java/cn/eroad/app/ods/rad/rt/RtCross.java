package cn.eroad.app.ods.rad.rt;

import com.alibaba.fastjson2.annotation.JSONField;

import java.util.List;


public class RtCross extends RadData {
    @JSONField(name = "passingImformations")
    public List<PassingInformation> passingInformationList;

    @Override
    @JSONField(serialize = false,deserialize = false)
    public boolean isErrFormat() {
        if (super.isErrFormat())
            return true;
        return passingInformationList == null;
    }

    @Override
    @JSONField(serialize = false,deserialize = false)
    public List<String> getNullFiledNameList() {
        List<String> nullFiledNameList =  super.getNullFiledNameList();
        if (passingInformationList == null)
            nullFiledNameList.add("passingInformationList");
        return nullFiledNameList;
    }
}
