package cn.eroad.videocast.model;

import lombok.Data;

@Data
public class ObjectIDInfo {
    //目标类型：0：未知/未识别的目标类型；1：人脸2：人体3：机动车4：非机动车
    private Long ObjectType;

    //目标ID，用于关联目标结构信息
    private Long ObjectID;
}
