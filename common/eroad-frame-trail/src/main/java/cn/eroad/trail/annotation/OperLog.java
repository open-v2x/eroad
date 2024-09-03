package cn.eroad.trail.annotation;

import cn.eroad.trail.enums.BusinessType;

import java.lang.annotation.*;

/**
 * 自定义操作日志记录注解
 *
 * @author ruoyi
 */
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperLog {
    /**
     * 模块
     */
    public String title() default "";

    /**
     * 功能
     */
    public BusinessType businessType() default BusinessType.OTHER;


    /**
     * 是否保存请求的参数
     */
    public boolean isSaveRequestData() default true;
}
