package cn.eroad.device;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@MapperScan("cn.eroad.device.mapper")
@EnableSwagger2
@EnableFeignClients(basePackages = {"cn.eroad.*"})
@EnableRabbit
@EnableScheduling
@SpringBootApplication(scanBasePackages = "cn.eroad")
public class DeviceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeviceApplication.class, args);
    }
}
