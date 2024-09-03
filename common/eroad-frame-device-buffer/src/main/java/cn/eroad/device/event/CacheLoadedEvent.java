package cn.eroad.device.event;

import org.springframework.context.ApplicationEvent;

/**
 * @project: eroad-frame
 * @ClassName: CacheLoadedEvent
 * @author: liyongqiang
 * @creat: 2022/5/17 10:09
 * 描述: 缓存加载完成触发事件
 */
public class CacheLoadedEvent extends ApplicationEvent {
    private static final long serialVersionUID = 8905117895058642111L;

    public CacheLoadedEvent(Object source) {
        super(source);
    }
}
