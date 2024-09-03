package cn.eroad.core.exception;


import cn.eroad.core.domain.CommonContent;
import cn.eroad.core.domain.Head;

/**
 * @ClassName: AcValidException
 * @Description:
 */
public class AcValidException extends RuntimeException {

    private Head head;

    public AcValidException(String msg) {
        super(msg);
        this.head = resHeadResolve(msg);
    }

    private Head resHeadResolve(String msg) {
        Head resHead = new Head();
        resHead.setStatus("F");
        resHead.setMsg(msg);
        resHead.setCode(CommonContent.FAIL_CODE);
        return resHead;
    }

    public Head getHead() {
        return head;
    }

    public void setHead(Head head) {
        this.head = head;
    }
}
