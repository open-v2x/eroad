package cn.eroad.trail.entity;

import lombok.Data;

@Data
public class UserTrailQuery {

    private int status;
    private String creator;
    private String operateAction;
    private String operateObject;
    private String description;
}
