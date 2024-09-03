/**********************************************************************
 * 日 期： 2013-4-11
 * 作 者:  梁洪杰
 * 版 本： v1.0
 * 描 述:  IEventListener.java
 * 历 史： 2013-4-11 创建
 *********************************************************************/
package cn.eroad.core.async;

import java.util.EventListener;
import java.util.EventObject;

/**
 * 事件监听器
 */
public interface IEventListener extends EventListener {
    void notify(String eventName, EventObject event);
}
