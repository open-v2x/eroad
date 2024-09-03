package cn.eroad.configure;

import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import springfox.documentation.RequestHandler;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
@EnableKnife4j
public class Swagger2Configure {


    @Value("${spring.application.name:eroad-frame}")
    private String appName;

    @Value("${server.servlet.contextPath:/}")
    private String contextPath;

    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(basePackage("cn.eroad.*.controller"))
                .paths(PathSelectors.any())
                .build()
                .pathMapping(contextPath);
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title(appName + "-swagger2")
                .description(appName + " 接口文档")
                .termsOfServiceUrl("http://127.0.0.1:8666")
                .version("1.0.0-SNAPSHOT")
                .build();
    }

    private static PathMatcher pathMatcher = new AntPathMatcher();

    public static Predicate<RequestHandler> basePackage(final String basePackage) {
        return input -> declaringClass(input).transform(handlerPackage(basePackage)).or(true);
    }

    private static Function<Class<?>, Boolean> handlerPackage(final String basePackage) {
        return input -> {
            String[] bases = basePackage.split(",");
            boolean match = false;
            for (String base : bases) {
                if (pathMatcher.match(base, input.getPackage().getName())) {
                    match = true;
                    break;
                }
            }
            return match;
        };
    }

    private static Optional<? extends Class<?>> declaringClass(RequestHandler input) {
        return Optional.fromNullable(input.declaringClass());
    }
}
