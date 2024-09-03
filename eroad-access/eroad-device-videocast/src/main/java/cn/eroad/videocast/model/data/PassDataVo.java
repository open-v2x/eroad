package cn.eroad.videocast.model.data;

import lombok.Data;

@Data
public class PassDataVo {
    private String Reference;
    private String CameraID;
    private String TollgateID;
    //private String TollgateName;
    private String CurrentTime;
    private Long LanelID;
    private Long CoilID;
    private Float Speed;
    private Long VehicleLength;
    private Long VehicleType;
    private String DriveIntoTime;
    private String PressenceTime;

}
