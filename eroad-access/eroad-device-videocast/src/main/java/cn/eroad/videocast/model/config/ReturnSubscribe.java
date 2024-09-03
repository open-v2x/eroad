package cn.eroad.videocast.model.config;

import lombok.Data;

/**
 * @Auther zhaohanqing
 * @Date 2022/7/28
 */
@Data
public class ReturnSubscribe {
    private long ID;
    private String Reference;
    private long CurrentTime;
    private long TerminationTime;
    private long SupportType;
}
