package cn.eroad.exception;

import cn.eroad.core.domain.CommonContent;
import cn.eroad.core.exception.AcException;
import cn.eroad.core.exception.AcValidException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description : springboot 应用全局异常解决方案
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 系统异常处理
     *
     * @param exception
     * @return
     */
    @ExceptionHandler({AcException.class, AcValidException.class})
    @ResponseBody
    public CommonContent exceptionHandler(AcException exception) {
        exception.printStackTrace();
        log.error("Ac*Exception:", exception);
        return CommonContent.error(exception.getHead());
    }

    /**
     * 系统异常处理
     *
     * @param exception
     * @return
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public CommonContent exceptionHandler(Exception exception) {
        exception.printStackTrace();
        log.error("Exception:", exception);
        return CommonContent.error(exception.getMessage());
    }

    /**
     * 处理所有校验失败的异常（MethodArgumentNotValidException异常）
     *
     * @param ex
     * @return
     */
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    @ResponseBody
    public CommonContent handleBindGetException(MethodArgumentNotValidException ex) {
        // todo
        // CommonContent commonContent = (CommonContent) ex.getBindingResult().getTarget();
        // Head head = commonContent.getHead();
        System.out.println("MethodArgumentNotValidException");
        // 获取所有异常
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(x -> x.getDefaultMessage())
                .collect(Collectors.toList());
        return CommonContent.error(String.join(",", errors));
    }

    /**
     * 处理所有校验失败的异常（MethodArgumentNotValidException异常）
     */
    @ExceptionHandler(value = BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public CommonContent validationExceptionHandler(BindException ex) {
        System.out.println("BindException");
        Map<String, Object> body = new LinkedHashMap<String, Object>();
        body.put("timestamp", new Date());
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(x -> x.getDefaultMessage())
                .collect(Collectors.toList());
        body.put("errors", errors);
        CommonContent cc = new CommonContent();
        cc.setBody(body);
        return cc;
    }

    /**
     * 处理所有校验失败的异常（MethodArgumentNotValidException异常）
     */
    @ExceptionHandler(value = ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public CommonContent ConstraintViolationExceptionHandler(ConstraintViolationException ex) {
        System.out.println("ConstraintViolationException");
        Map<String, Object> body = new LinkedHashMap<String, Object>();
        body.put("timestamp", new Date());
        Set<ConstraintViolation<?>> constraintViolations = ex.getConstraintViolations();
        Iterator<ConstraintViolation<?>> iterator = constraintViolations.iterator();
        List<String> errors = new ArrayList<>();
        while (iterator.hasNext()) {
            ConstraintViolation<?> cvl = iterator.next();
            errors.add(cvl.getMessageTemplate());
        }
        body.put("errors", errors);
        CommonContent cc = new CommonContent();
        cc.setBody(body);
        return cc;
    }

}
