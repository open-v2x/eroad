package cn.eroad.rad;


import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ApplicationContext;

import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Arrays;

@SpringBootApplication(scanBasePackages = {"cn.eroad"}, exclude = DataSourceAutoConfiguration.class)
@EnableScheduling
@EnableRabbit
@EnableAsync
@EnableSwagger2
@EnableFeignClients(basePackages = {"cn.eroad.*"})
@Slf4j
public class RadarApplication {

    public static void main(String[] args) {
        ApplicationContext run = SpringApplication.run(RadarApplication.class, args);
        System.out.println(Arrays.stream(run.getBeanDefinitionNames()));
    }
}
