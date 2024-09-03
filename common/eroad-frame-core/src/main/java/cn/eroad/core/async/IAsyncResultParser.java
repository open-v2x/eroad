/**********************************************************************
 * 日 期： 2012-10-23
 * 作 者:  梁洪杰
 * 版 本： v2.0
 * 描 述:  IAsyncResultParser.java
 * 历 史： 2012-10-23 创建
 *********************************************************************/
package cn.eroad.core.async;

import java.util.Map;

/**
 * 异步消息解析接口
 */
public interface IAsyncResultParser {
    /**
     * 解析消息
     *
     * @param obj 消息
     * @return 结果
     * @throws Exception 异常
     */
    public Map<String, Object> parse(Object obj) throws Exception;
}
