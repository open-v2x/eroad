/**
 * Copyright (C), 2015-2019  http://www.buukle.top
 * FileName: Head
 * Author:   zhanglei1102
 * Date:     2019/7/26 15:09
 * Description: 来源头部规范
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package cn.eroad.core.vo;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @author zhanglei1102
 * @description 〈来源头部规范〉
 * @create 2019/7/26
 * @since 1.0.0
 */
public class RequestHead implements Serializable {

    /**
     * 来源系统code
     */
    private String applicationCode;
    /**
     * 来源操作时间
     */
    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date operationTime;
    /**
     * 来源操作人
     */
    private String operator;
    /**
     * 来源操作人Id
     */
    private String operatorId;
    /**
     * 签名
     */
    private String signature;

    public Date getOperationTime() {
        return operationTime;
    }

    public void setOperationTime(Date operationTime) {
        this.operationTime = operationTime;
    }

    public String getApplicationCode() {
        return applicationCode;
    }

    public void setApplicationCode(String applicationCode) {
        this.applicationCode = applicationCode;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
