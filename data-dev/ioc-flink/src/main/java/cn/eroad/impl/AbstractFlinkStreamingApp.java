package cn.eroad.impl;

import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;


public abstract class AbstractFlinkStreamingApp implements Serializable {
    public void flinkBegin(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        restartConfig(env);
        checkPointConfig(env);
        envConfig(env);
        processCode(env, args);
        env.execute(appName());
    }

    /*程序*/
    protected abstract void processCode(StreamExecutionEnvironment env, String[] args) throws Exception;

    /*服务名称*/
    protected abstract String appName();

    /*重启设置*/
    protected void restartConfig(StreamExecutionEnvironment env) {
        env.setRestartStrategy(RestartStrategies.failureRateRestart(
                3,
                Time.of(5, TimeUnit.MINUTES),
                Time.of(1, TimeUnit.MINUTES)
        ));
    }

    /*检查点设置*/
    protected void checkPointConfig(StreamExecutionEnvironment env) {
    }

    /*其他环境设置 保留*/
    protected void envConfig(StreamExecutionEnvironment env) {
    }
}
