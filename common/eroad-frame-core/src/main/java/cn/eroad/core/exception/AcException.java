package cn.eroad.core.exception;

import cn.eroad.core.domain.Head;

public class AcException extends RuntimeException {

    private Head head;

    public AcException(Head head) {
        super(head.getMsg());
        this.head = head;
    }

    public AcException(String code, String msg) {
        super(msg);
        this.head = resHeadResolve(code, msg);
    }

    private Head resHeadResolve(String code, String msg) {
        Head resHead = new Head();
        resHead.setStatus("F");
        resHead.setMsg(msg);
        resHead.setCode(code);
        return resHead;
    }

    public Head getHead() {
        return head;
    }

    public void setHead(Head head) {
        this.head = head;
    }
}
