package cn.eroad.trail.event;

import cn.eroad.trail.entity.UserTrailQuery;
import org.springframework.context.ApplicationEvent;

/**
 * 用戶日志事件
 */
public class SysOperLogEvent extends ApplicationEvent {
    //
    private static final long serialVersionUID = 8905017895058642111L;

    public SysOperLogEvent(UserTrailQuery source) {
        super(source);
    }
}
