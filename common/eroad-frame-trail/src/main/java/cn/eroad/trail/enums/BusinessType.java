package cn.eroad.trail.enums;

/**
 * 业务操作类型
 */
public enum BusinessType {
    /**
     * 其它
     */
    OTHER("其它"),

    /**
     * 查询
     */
    SELECT("查询"),

    /**
     * 新增
     */
    INSERT("新增"),

    /**
     * 修改
     */
    UPDATE("编辑"),

    /**
     * 删除
     */
    DELETE("删除"),

    /**
     * 登录
     */
    LOGIN("登录"),

    /**
     * 登出
     */
    LOGOUT("登出"),

    /**
     * 密码修改
     */
    PASSWORDEDIT("密码修改"),

    /**
     * 导出
     */
    EXPORT("导出"),

    /**
     * 导入
     */
    IMPORT("导入");

    BusinessType(String des) {
        this.des = des;
    }

    private String des;

    public String getDes() {
        return des;
    }

}
