package cn.eroad.core.vo;


import java.util.Date;


/**
 * @Author elvin
 * @Date Created by elvin on 2018/9/19.
 * @Description : buukle 公共请求规范
 */
public class CommonRequest<T> {

    /**
     * 请求头
     */
    private RequestHead head;
    /**
     * 请求体
     */
    private T body;

    public RequestHead getHead() {
        return head;
    }

    public void setHead(RequestHead head) {
        this.head = head;
    }

    public T getBody() {
        return body;
    }

    public void setBody(T body) {
        this.body = body;
    }

    public static class Builder {

        public static CommonRequest build(String applicationName) {
            CommonRequest commonRequest = new CommonRequest<>();
            RequestHead head = new RequestHead();
            head.setApplicationCode(applicationName);
            head.setOperationTime(new Date());
            head.setOperator(applicationName);
            head.setOperatorId(applicationName);
            commonRequest.setHead(head);
            return commonRequest;
        }
    }
}


