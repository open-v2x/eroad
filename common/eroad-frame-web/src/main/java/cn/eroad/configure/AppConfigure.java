package cn.eroad.configure;


import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @Description : AppConfigure Mvc系统配置类
 */
@Configuration
public class AppConfigure {

    /**
     * @param
     * @return void
     * @description 时区设置
     * @Author 86180
     * @Date 2020/9/11
     */
    @PostConstruct
    void started() {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+8"));
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setLenient(false);
        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
    }

    /**
     * 异步执行线程池
     *
     * @return
     */
    @Bean
    public TaskExecutor executor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //核心线程数
        executor.setCorePoolSize(2);
        //最大线程数
        executor.setMaxPoolSize(10);
        //队列大小
        executor.setQueueCapacity(20);
        //线程最大空闲时间
        executor.setKeepAliveSeconds(300);
        //指定用于新创建的线程名称的前缀
        executor.setThreadNamePrefix("iot-feign-");
        /**
         * 异步任务被拒绝时处理策略
         * 1. {@link ThreadPoolExecutor.AbortPolicy} 默认，被拒绝时丢弃且抛出异常
         * 2. {@link ThreadPoolExecutor.DiscardPolicy} 被拒绝时丢弃且不抛出异常
         * 3. {@link ThreadPoolExecutor.CallerRunsPolicy} 被拒绝时使用调用execute函数的上层线程去执行被拒绝的任务, 可能会阻塞主线程
         * ……
         * ！！！使用下游服务熔断机制和巡检机制解决阻塞&数据一致性问题
         */
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return executor;
    }

    /**
     * 用于spring session，防止每次创建一个线程
     *
     * @return
     */
    @Bean
    public ThreadPoolTaskExecutor springSessionRedisTaskExecutor() {
        ThreadPoolTaskExecutor springSessionRedisTaskExecutor = new ThreadPoolTaskExecutor();
        springSessionRedisTaskExecutor.setCorePoolSize(8);
        springSessionRedisTaskExecutor.setMaxPoolSize(100);
        springSessionRedisTaskExecutor.setKeepAliveSeconds(10);
        springSessionRedisTaskExecutor.setQueueCapacity(1000);
        springSessionRedisTaskExecutor.setThreadNamePrefix("Spring session redis executor thread: ");
        return springSessionRedisTaskExecutor;
    }

}
