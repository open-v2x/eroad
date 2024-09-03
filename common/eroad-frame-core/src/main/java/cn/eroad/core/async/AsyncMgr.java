package cn.eroad.core.async;

import org.dom4j.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;

/**
 * 异步处理管理类
 */
public class AsyncMgr {
    private static final Logger logger = LoggerFactory.getLogger(AsyncMgr.class);
    private static final AsyncMgr instance = new AsyncMgr();
    private Map<String, List<IEventListener>> listenerMap;
    private Map<Object, AsyncEntity> asyncMap;
    private ScheduledExecutorService scheduledExecutorService;

    private AsyncMgr() {
        listenerMap = new ConcurrentHashMap<String, List<IEventListener>>();
        asyncMap = new ConcurrentHashMap<Object, AsyncEntity>();
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(() -> checkTimeout(), 500, 500, TimeUnit.MILLISECONDS);
    }

    /**
     * 单例
     *
     * @return 单例
     */
    public static AsyncMgr getInstance() {
        return instance;
    }

    /**
     * 系统退出时调用
     */
    public void dispose() {
        scheduledExecutorService.shutdownNow();
        asyncMap.clear();
        listenerMap.clear();
    }

    /**
     * 注册异步消息，用于需要对消息和结果均进行异步处理的情况，在下发协议前调用
     *
     * @param key     关键字，如msgid，由于多个前端可能有相同的msgid，因此最好加上前端id，格式如grpid.msgid
     * @param parser  结果解析
     * @param handler 结果处理
     * @param timeOut 超时时间，单位：毫秒
     */
    public void register(Object key, IAsyncResultParser parser, IAsyncResultHandler handler, int timeOut) {
        asyncMap.put(key, new AsyncEntity(parser, handler, timeOut));
    }

    /**
     * 注册异步消息，用于需要对消息进行异步处理、结果进行同步等待的情况，下发代码封装到run里面
     *
     * @param key     关键字，如msgid，由于多个前端可能有相同的msgid，因此最好加上前端id，格式如grpid.msgid
     * @param run     下发代码
     * @param timeOut 超时时间，单位：毫秒
     * @return 消息返回结果
     * @throws Exception 超时、操作错误异常
     */
    public Object register(Object key, Runnable run, int timeOut) throws Exception {
        AsyncEntity value = new AsyncEntity(null, null, timeOut);
        asyncMap.put(key, value);
        synchronized (value) {
            // 等待
            run.run();
            if (value.iResult == AsyncEntity.OPER_STATUS_UNKONWN && !value.isSignalled) {
                value.wait();
            }
        }
        if (value.iResult != AsyncEntity.OPER_STATUS_SUCCESS) {
            if (value.iResult == AsyncEntity.OPER_STATUS_TIMEOUT) {
                throw new TimeoutException("操作超时");
            } else {
                throw new Exception("操作失败");
            }
        }
        return value.result;
    }

    /**
     * 注册异步消息，用于需要对消息进行异步处理、结果进行同步等待的情况，下发代码封装到run里面
     *
     * @param key     关键字，如msgid，由于多个前端可能有相同的msgid，因此最好加上前端id，格式如grpid.msgid
     * @param call    下发代码
     * @param timeOut 超时时间，单位：毫秒
     * @return 消息返回结果
     * @throws Exception 超时、操作错误异常
     */
    public Object register(Object key, Callable call, int timeOut) throws Exception {
        AsyncEntity value = new AsyncEntity(null, null, timeOut);
        asyncMap.put(key, value);
        logger.debug("注册异步消息，Key：" + key + ",timeOut:" + timeOut);
        synchronized (value) {
            // 等待
            call.call();
            if (value.iResult == AsyncEntity.OPER_STATUS_UNKONWN && !value.isSignalled) {
                value.wait();//同步调用不需要等待
            }
        }
        if (value.iResult != AsyncEntity.OPER_STATUS_SUCCESS) {
            if (value.iResult == AsyncEntity.OPER_STATUS_TIMEOUT) {
                throw new TimeoutException("操作超时");
            } else {
                throw new Exception("操作失败");
            }
        }
        return value.result;
    }

    /**
     * 注册异步消息，用于需要等待返回结果的情况
     *
     * @param key     关键字，如msgid，由于多个前端可能有相同的msgid，因此最好加上前端id，格式如grpid.msgid
     * @param timeOut 超时时间，单位：毫秒, 0: 不超时
     * @return 消息返回结果
     * @throws Exception 超时 TimeoutException、操作错误异常
     */
    public Object register(Object key, long timeOut) throws Exception {
        logger.info("注册异步消息：Key：" + key + " timeOut:" + timeOut);
        AsyncEntity value = new AsyncEntity(null, null, (int) timeOut);
        asyncMap.put(key, value);
        synchronized (value) { // 等待
            if (value.iResult == AsyncEntity.OPER_STATUS_UNKONWN && !value.isSignalled) {
                value.wait();//同步调用不需要等待
            }
        }
        if (value.iResult != AsyncEntity.OPER_STATUS_SUCCESS) {
            if (value.iResult == AsyncEntity.OPER_STATUS_TIMEOUT) {
                throw new TimeoutException("操作超时");
            } else {
                throw new Exception("操作失败" + value.result);
            }
        }
        return value.result;
    }


    /**
     * 通知处理异步消息的结果
     *
     * @param key   关键字，如msgid，由于多个前端可能有相同的msgid，因此最好加上前端id，格式如grpid.msgid
     * @param value 结果值
     * @return true:通知成功,false:通知失败
     */
    public boolean notify(Object key, Object value) {
        logger.info("通知处理异步消息的结果：Key：" + key + " value:" + value);
        AsyncEntity async = asyncMap.remove(key);
        if (async == null) {
            // 同步控制
            return false;
        }
        if (async.parser == null && async.handler == null) {
            // 唤醒异步消息处理
            synchronized (async) {
                async.iResult = AsyncEntity.OPER_STATUS_SUCCESS;
                async.result = value;
                async.isSignalled = true;
                async.notify();
            }
            return true;
        }
        try {
            // 处理异步消息和结果
            Map<String, Object> resultMap = null;
            if (async.parser != null) {
                resultMap = async.parser.parse(value);
            } else if (value == null) {
                throw new Exception("操作失败.");
            } else {
                resultMap = new HashMap<String, Object>();
                resultMap.put("RESULT", value);
            }
            async.iResult = AsyncEntity.OPER_STATUS_SUCCESS;
            async.result = resultMap;
            if (async.handler != null) {
                async.handler.handleSuccess(resultMap);
            }
        } catch (Exception e) {
            // 处理异常情况
            async.iResult = AsyncEntity.OPER_STATUS_FAILED;
            async.result = e.getMessage();
            if (async.handler != null) {
                async.handler.handleFailed(e.getMessage());
            }
        }
        if (async.handler != null) {
            // 完成收尾工作
            async.handler.handleFinished();
        }
        logger.info("注销异步消息的结果：Key：" + key + " value:" + value);
        return true;
    }

    /**
     * 通知处理异步消息的结果
     *
     * @param key   关键字，如msgid，由于多个前端可能有相同的msgid，因此最好加上前端id，格式如grpid.msgid
     * @param value 结果值
     * @return true:通知成功,false:通知失败
     */
    public boolean notify(Object key, Object value, Exception e) {
        Document doc = (Document) value;
        logger.info("通知处理异步消息的结果：Key：" + key + " value:" + doc.asXML().replaceAll("\n", ""));
        AsyncEntity async = asyncMap.remove(key);
        if (async == null) {
            // 同步控制
            return false;
        }
        if (async.handler == null) {
            // 唤醒异步消息处理
            synchronized (async) {
                if (e != null) {
                    async.iResult = AsyncEntity.OPER_STATUS_FAILED;
                    async.result = e;
                } else {
                    async.iResult = AsyncEntity.OPER_STATUS_SUCCESS;
                    async.result = value;
                }
                async.isSignalled = true;
                async.notify();
            }
            return true;
        }
        if (e != null) {
            async.iResult = AsyncEntity.OPER_STATUS_FAILED;
            async.result = e.getMessage();
            if (async.handler != null) {
                async.handler.handleFailed(e);
                // 完成收尾工作
                async.handler.handleFinished();
            }
        } else {
            async.iResult = AsyncEntity.OPER_STATUS_SUCCESS;
            async.result = value;
            if (async.handler != null) {
                async.handler.handleSuccess(value);
                // 完成收尾工作
                async.handler.handleFinished();
            }
        }
        return true;
    }

    private void checkTimeout() {
        Collection<Map.Entry<Object, AsyncEntity>> values = new ArrayList<Map.Entry<Object, AsyncEntity>>(asyncMap.entrySet());
        for (Map.Entry<Object, AsyncEntity> entry : values) {
            AsyncEntity async = entry.getValue();
            async.timeOut -= 500;
            if (async.timeOut > 0) {
                continue;
            }
            async = asyncMap.remove(entry.getKey());
            if (async == null) {
                // 同步控制
                continue;
            }

            // 唤醒异步消息处理
            synchronized (async) {
                async.iResult = AsyncEntity.OPER_STATUS_TIMEOUT;
                if (async.handler != null) {
                    // 处理超时情况
                    async.handler.handleTimeOut();
                }
                async.isSignalled = true;
                async.notify();
            }
        }
    }

    /**
     * 注册事件监听器
     *
     * @param eventName 事件名称
     * @param listener  监听器
     */
    public synchronized void addEventListener(String eventName, IEventListener listener) {
        List<IEventListener> listenerLst = listenerMap.get(eventName);
        if (listenerLst == null) {
            listenerLst = new ArrayList<IEventListener>();
            listenerMap.put(eventName, listenerLst);
        }
        listenerLst.add(listener);
    }

    /**
     * 移除事件监听器
     *
     * @param eventName 事件名称
     * @param listener  监听器
     */
    public synchronized void removeEventListener(String eventName, IEventListener listener) {
        List<IEventListener> listenerLst = listenerMap.get(eventName);
        if (listenerLst == null) {
            return;
        }
        listenerLst.remove(listener);
    }

    /**
     * 通知监听器
     *
     * @param eventName 事件名称
     * @param event     事件
     */
    public void notifyEvent(final String eventName, final EventObject event) {
        final List<IEventListener> listenerLst = listenerMap.get(eventName);
        if (listenerLst == null) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (IEventListener listener : listenerLst) {
                    try {
                        listener.notify(eventName, event);
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        }).start();
    }

    public boolean containsKey(Object key) {
        return asyncMap.containsKey(key);
    }


    private class AsyncEntity {
        private IAsyncResultParser parser;
        private IAsyncResultHandler handler;
        private int timeOut;
        private Object result;

        /**
         * 是否被通知过，防止notify在wait之前发送
         */
        private boolean isSignalled = false;

        /**
         * 结果状态
         */
        private int iResult = -1;
        private static final int OPER_STATUS_SUCCESS = 0;
        private static final int OPER_STATUS_TIMEOUT = 1;
        private static final int OPER_STATUS_FAILED = 2;
        private static final int OPER_STATUS_UNKONWN = -1;

        /**
         * 异步消息实体类
         *
         * @param parser  消息解析类
         * @param handler 结果处理类
         * @param timeOut 超时时间，单位：毫秒
         */
        public AsyncEntity(IAsyncResultParser parser, IAsyncResultHandler handler, int timeOut) {
            this.parser = parser;
            this.handler = handler;
            this.timeOut = timeOut;
        }

        public IAsyncResultParser getParser() {
            return parser;
        }

        public void setParser(IAsyncResultParser parser) {
            this.parser = parser;
        }

        public IAsyncResultHandler getHandler() {
            return handler;
        }

        public void setHandler(IAsyncResultHandler handler) {
            this.handler = handler;
        }

        public int getTimeOut() {
            return timeOut;
        }

        public void setTimeOut(int timeOut) {
            this.timeOut = timeOut;
        }

        public Object getResult() {
            return result;
        }

        public void setResult(Object result) {
            this.result = result;
        }
    }
}
