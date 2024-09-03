package cn.eroad.rad.model.dmsModel;

import cn.eroad.rad.model.WorkingStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeviceStatus {
    private String sn;
    private String id;
    private WorkingStatus workStatus;
    private String updateTime;

}
