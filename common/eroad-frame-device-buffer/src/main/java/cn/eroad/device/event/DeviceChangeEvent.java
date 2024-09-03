package cn.eroad.device.event;

import cn.eroad.device.vo.Device;
import org.springframework.context.ApplicationEvent;

/**
 * 用戶日志事件
 */
public class DeviceChangeEvent extends ApplicationEvent {
    //
    private static final long serialVersionUID = 8905017895058642111L;

    public DeviceChangeEvent(Object source) {
        super(source);
    }
}
