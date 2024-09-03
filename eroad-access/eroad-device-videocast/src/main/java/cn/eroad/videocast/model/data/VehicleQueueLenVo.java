package cn.eroad.videocast.model.data;

import cn.eroad.videocast.model.VehQueueLenInfo;
import lombok.Data;

@Data
public class VehicleQueueLenVo {
    private String Reference;
    private String CameraID;
    private String TollgateID;
    private String TollgateName;
    private String CurrentTime;
    private VehQueueLenInfo vehQueueLenInfo;
}
