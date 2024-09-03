package cn.eroad.rad.model.response;

public class ResultDataForQuery<T> {

    private String id;
    private String sn;
    private T prop;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public T getProp() {
        return prop;
    }

    public void setProp(T prop) {
        this.prop = prop;
    }
}
