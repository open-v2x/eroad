package cn.eroad.device.fegin;


import feign.*;
import feign.querymap.BeanQueryMapEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;


@Configuration
public class FeignConfiguration {
    /** feign-http 链接超時時間*/
    public static int connectTimeOutMillis = 3000;
    /** feign-http 等待超时时间*/
    public static int readTimeOutMillis = 6000;

    /**
     * @description feign-http 超时设置 bean 注册
     * @param
     * @return feign.Request.Options
     * @Author elvin
     * @Date 2019/8/4
     */
    @Bean
    @Primary
    public Request.Options options() {
        return new Request.Options(connectTimeOutMillis, readTimeOutMillis);
    }

    /**
     * 自定义重试机制
     *
     * @return {@link Retryer}
     */
    @Bean
    @Primary
    public Retryer feignRetryer() {
        return Retryer.NEVER_RETRY;
    }

    /**
     * 替换默认的queryMap的类, 默认FieldQueryMapEncoder, 已不推荐使用
     */
    @Bean
    @Primary
    public Feign.Builder feignBuilder(Retryer retryer, Request.Options options) {
        return Feign.builder()
                .options(options)
                .queryMapEncoder(new BeanQueryMapEncoder())
                .retryer(retryer);
    }

    @Bean
    @Primary
    Logger.Level feignLoggerLevel() {
        //这里记录所有，根据实际情况选择合适的日志level
        return Logger.Level.FULL;
    }

    /**
     * fegin 拦截器, 织入认证信息
     *
     * @return {@link RequestInterceptor}
     */
    @Bean
    @Primary
    public RequestInterceptor authorizationSign() {
        return template -> {};
    }
}
