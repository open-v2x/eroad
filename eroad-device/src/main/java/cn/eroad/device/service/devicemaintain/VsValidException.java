package cn.eroad.device.service.devicemaintain;

import cn.eroad.core.domain.CommonContent;
import cn.eroad.core.domain.Head;

public class VsValidException extends RuntimeException {

    public VsValidException(String msg) {
        super(msg);
        Head head = resHeadResolve(msg);
    }

    private Head resHeadResolve(String msg) {
        Head resHead = new Head();
        resHead.setMsg(msg);
        resHead.setCode(CommonContent.FAIL_CODE);
        return resHead;
    }

}
