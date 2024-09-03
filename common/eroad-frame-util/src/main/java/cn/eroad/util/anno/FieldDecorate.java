package cn.eroad.util.anno;

import cn.eroad.util.decorator.AutoDecorator;
import cn.eroad.util.decorator.Decorator;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface FieldDecorate {

    // 数量级
    double magnitude() default 1;

    // 前缀
    String prefix() default "";

    // 后缀
    String suffix() default "";

    // 装饰器
    Class<? extends Decorator>[] decorator() default AutoDecorator.class;
}
