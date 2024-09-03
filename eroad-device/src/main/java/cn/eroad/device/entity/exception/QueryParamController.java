package cn.eroad.device.entity.exception;

import cn.eroad.core.domain.CommonContent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author WuHang
 * @description:
 * @date 2022/7/8 9:09
 */
@ControllerAdvice
@Slf4j
@Order(value = Ordered.HIGHEST_PRECEDENCE)
public class QueryParamController {

    @ResponseBody
    @ExceptionHandler(value = HttpMessageNotReadableException.class)
    public CommonContent queryVoException(HttpMessageNotReadableException ex) {
        log.error("Json格式转换异常==>{}" , ex.getMessage());
        return CommonContent.error("入参格式有误，Json格式转换异常");
    }

}
