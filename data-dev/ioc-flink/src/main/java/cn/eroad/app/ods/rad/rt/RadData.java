package cn.eroad.app.ods.rad.rt;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public abstract class RadData implements Serializable {
    public String timeStamp;

    public int number;

    public String deviceId;

    @JSONField(name = "device_id")
    public String deviceId2;

    @JSONField(name = "lamp_id")
    public String lampId;

    @JSONField(name = "lamp_longitude")
    public Double lampLongitude;

    @JSONField(name = "lamp_latitude")
    public Double lampLatitude;

    @JSONField(name = "cross_id")
    public String crossId;

    @JSONField(name = "cross_name")
    public String crossName;

    @JSONField(name = "cross_longitude")
    public Double crossLongitude;

    @JSONField(name = "cross_latitude")
    public Double crossLatitude;

    public String supplier;

    @JSONField(name = "report_time")
    public String reportTime;

    public Long epoch;


    @JSONField(serialize = false,deserialize = false)
    public boolean isErrFormat(){
        return timeStamp == null || deviceId == null;
    }

    @JSONField(serialize = false,deserialize = false)
    public List<String> getNullFiledNameList(){
        ArrayList<String> nullFiledNameList = new ArrayList<>();
        if (timeStamp == null)
            nullFiledNameList.add("timeStamp");
        if (deviceId == null)
            nullFiledNameList.add("device_id");
        return nullFiledNameList;
    }


    public void setCrossInfo(DeviceCrossInfo deviceCrossInfo) {
        this.lampId = deviceCrossInfo.lampId;
        this.lampLatitude = deviceCrossInfo.lampLatitude;
        this.lampLongitude = deviceCrossInfo.lampLongitude;
        this.crossLatitude = deviceCrossInfo.crossLatitude;
        this.crossLongitude = deviceCrossInfo.crossLongitude;
        this.crossId = deviceCrossInfo.crossId;
        this.crossName = deviceCrossInfo.crossName;
        this.supplier = deviceCrossInfo.supplier;
        this.deviceId2 = this.deviceId;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
