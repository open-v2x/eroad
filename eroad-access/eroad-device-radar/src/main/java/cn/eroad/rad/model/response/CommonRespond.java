package cn.eroad.rad.model.response;


public class CommonRespond<T> {

    private Integer resultCode;
    private String resultMsg;
    private T resultData;

    public Integer getResultCode() {
        return resultCode;
    }

    public void setResultCode(Integer resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultMsg() {
        return resultMsg;
    }

    public void setResultMsg(String resultMsg) {
        this.resultMsg = resultMsg;
    }

    public T getResultData() {
        return resultData;
    }

    public void setResultData(T resultData) {
        this.resultData = resultData;
    }

    public void setResult(ResultType resultType) {
        this.resultCode = resultType.getResultCode();
        this.resultMsg = resultType.getResultMsg();
    }
}
