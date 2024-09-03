package cn.eroad.device.config;


import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
@Slf4j
public class InterceptorConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String os = System.getProperty("os.name");
        if (os.toLowerCase().startsWith("win")) {
            //如果是Windows系统
            registry.addResourceHandler("/files/**").
                    addResourceLocations("file:D:\\DeviceImages\\");
        }else{//linux和mac系统
            registry.addResourceHandler("/productPic/**").
                    addResourceLocations("file:/uploadBaseDir/productPic/");
        }
    }
}
