package cn.eroad.rad.model.response;

import java.util.List;

public class ResultDataForControl {

    private List<String> successList;

    private List<FailMsg> failList;

    public List<String> getSuccessList() {
        return successList;
    }

    public void setSuccessList(List<String> successList) {
        this.successList = successList;
    }

    public List<FailMsg> getFailList() {
        return failList;
    }

    public void setFailList(List<FailMsg> failList) {
        this.failList = failList;
    }
}
