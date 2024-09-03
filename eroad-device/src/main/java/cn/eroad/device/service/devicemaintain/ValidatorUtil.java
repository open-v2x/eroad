package cn.eroad.device.service.devicemaintain;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;

public class ValidatorUtil {
    /**
     * 验证器
     */
    private static Validator validator;

    static {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    /**
     *  验证方法
     *  <p>
     *      同一个pojo类，可能会被多个controller使用验证，而每个controller的验证规则有不同，
     *      这是就需要分组验证，其实就是几个要分组的空接口，指定属性A属于哪个组，属性B又属于
     *      哪个组，这样在controller验证时就指定我要验证哪个组
     *  </p>
     * @param object 被校验的对象
     * @param groups 被校验的组
     * @throws RuntimeException 校验不通过抛出自定义异常
     */
    public static void validateEntity(Object object, Class<?>... groups) throws VsValidException{
        // 用验证器执行验证，返回一个违反约束的set集合
        Set<ConstraintViolation<Object>> violationSet = validator.validate(object, groups);
        // 判断是否为空，空：说明验证通过，否则就验证失败
        if(!violationSet.isEmpty()) {
            // 获取第一个验证失败的属性
            ConstraintViolation<Object> violation = violationSet.iterator().next();
            // 抛出自定义异常
            throw new VsValidException(violation.getMessage());
        }
    }
}

