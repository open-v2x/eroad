/**********************************************************************
 * 日 期： 2012-10-23
 * 作 者:  梁洪杰
 * 版 本： v2.0
 * 描 述:  IAsyncResultHandler.java
 * 历 史： 2012-10-23 创建
 *********************************************************************/
package cn.eroad.core.async;

import java.util.Map;

/**
 * 异步结果处理接口
 */
public interface IAsyncResultHandler {
    /**
     * 成功处理
     *
     * @param resultMap 结果
     */
    public void handleSuccess(Map<String, Object> resultMap);

    /**
     * 失败处理
     *
     * @param msg 失败信息
     */
    public void handleFailed(String msg);

    /**
     * 成功处理
     *
     * @param result 结果
     */
    public void handleSuccess(Object result);

    /**
     * 失败处理
     *
     * @param e 失败信息
     */
    public void handleFailed(Exception e);

    /**
     * 完成处理
     */
    public void handleFinished();

    /**
     * 超时处理
     */
    public void handleTimeOut();
}
