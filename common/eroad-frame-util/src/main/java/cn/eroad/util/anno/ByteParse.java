package cn.eroad.util.anno;

import cn.eroad.util.converter.AutoConverter;
import cn.eroad.util.converter.Converter;
import cn.eroad.util.enums.Endian;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ByteParse {

    // 字段名称
    String value() default "";

    // 起始位置
    int start();

    // 字节长度
    int length();

    // 列表长度
    int size() default 1;

    // 大小端模式
    Endian endian() default Endian.BIG;

    // 解析类型
    Class<?> parseType() default Object.class;

    // 数据解析器
    Class<? extends Converter> converter() default AutoConverter.class;
}
