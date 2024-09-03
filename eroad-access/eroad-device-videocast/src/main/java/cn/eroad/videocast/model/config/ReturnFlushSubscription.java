package cn.eroad.videocast.model.config;

import lombok.Data;

/**
 * @Auther zhaohanqing
 * @Date 2022/7/28
 */
@Data
public class ReturnFlushSubscription {
    private String Reference;
    private long CurrentTime;
    private long TerminationTime;
}
