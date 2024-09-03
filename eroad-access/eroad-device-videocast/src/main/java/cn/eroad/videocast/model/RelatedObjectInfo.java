package cn.eroad.videocast.model;

import lombok.Data;

import java.util.List;

@Data
public class RelatedObjectInfo {
    //目标个数
    private Long ObjectNum;

    //目标列表
    private List<ObjectIDInfo> list;
}
