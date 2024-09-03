package cn.eroad.videocast;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@MapperScan("cn.eroad.videocast.dao.mapper")
@SpringBootApplication(scanBasePackages = {"cn.eroad.*"})
@EnableSwagger2
@EnableRabbit
public class RestfulVideoApplication {
    public static void main(String[] args) {
        SpringApplication.run(RestfulVideoApplication.class, args);
    }
}
