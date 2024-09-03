package cn.eroad.app.ods.rad.rt;

import com.alibaba.fastjson2.annotation.JSONField;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class RtTrack extends RadData {
    public Integer number;
    public List<TrackTarget> targets;

    @Override
    @JSONField(serialize = false, deserialize = false)
    public boolean isErrFormat() {
        if (super.isErrFormat())
            return true;
        if (targets == null )
            return true;
        for (TrackTarget target : targets) {
            if (target.targetId == null || target.longitude == null || target.latitude == null || target.targetType == null || target.headingAngle == null || target.speed == null
                    || target.acceleration == null || target.confidenceLevel == null || target.laneNo == null)
                return true;
        }
        return false;
    }

    @Override
    @JSONField(serialize = false,deserialize = false)
    public List<String> getNullFiledNameList() {
        List<String> nullFiledNameList = super.getNullFiledNameList();
        if (targets == null){
            nullFiledNameList.add("targets");
            return nullFiledNameList;
        }
        Set<String> set = new HashSet<>();
        for (TrackTarget target : targets) {
            if (target.targetId == null)
                set.add("targetId");

            if (target.longitude == null ){
                set.add("longitude");
            }
            if (target.latitude == null ){
                set.add("latitude");
            }
            if (target.targetType == null ){
                set.add("targetType");
            }
            if (target.headingAngle == null ){
                set.add("headingAngle");
            }
            if (target.speed == null ){
                set.add("speed");
            }
            if (target.acceleration == null ){
                set.add("acceleration");
            }
            if (target.confidenceLevel == null ){
                set.add("confidenceLevel");
            }
            if (target.laneNo == null ){
                set.add("laneNo");
            }
        }
        nullFiledNameList.addAll(set);
        return nullFiledNameList;
    }
}
