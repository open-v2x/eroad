package cn.eroad.trail.aspect;

import cn.eroad.core.context.SpringContextHolder;
import cn.eroad.core.domain.CommonContent;
import cn.eroad.trail.annotation.OperLog;
import cn.eroad.trail.entity.UserTrailQuery;
import cn.eroad.trail.event.SysOperLogEvent;
import com.alibaba.fastjson.JSON;
import io.swagger.annotations.ApiModelProperty;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;



import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 操作日志记录处理
 */
@Aspect
@Slf4j
@Component
public class OperLogAspect {
    // 配置织入点
    @Pointcut("@annotation(cn.eroad.trail.annotation.OperLog)")
    public void logPointCut() {
    }

    /**
     * 处理完请求后执行
     *
     * @param joinPoint 切点
     */
    @AfterReturning(returning = "responseData", pointcut = "logPointCut()")
    public void doAfterReturning(JoinPoint joinPoint, Object responseData) {
        // 规范编码，异常情况主动抛出异常，全局捕获，以下逻辑判断处理异常情况直接响应的情景
        if (responseData instanceof CommonContent) {
            String status = ((CommonContent) responseData).getHead().getStatus();
            if ("S".equals(status)) {
                // 请求成功才记录日志
                handleLog(joinPoint, null);
            }
        }
    }

    /**
     * 拦截异常操作
     *
     * @param joinPoint 切点
     * @param e         异常
     */
    @AfterThrowing(value = "logPointCut()", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Exception e) {
        // 抛异常时不记录日志
        // handleLog(joinPoint, e);
    }

    protected void handleLog(final JoinPoint joinPoint, final Exception e) {
        try {

            // 获得注解
            OperLog controllerLog = getAnnotationLog(joinPoint);
            if (controllerLog == null) {
                return;
            }
            // *========数据库日志=========*//
            UserTrailQuery operLog = new UserTrailQuery();
            // 请求的地址



            // 处理设置注解上的参数
            Object[] args = joinPoint.getArgs();
            getControllerMethodDescription(controllerLog, operLog, args);
            // 发布事件
            SpringContextHolder.publishEvent(new SysOperLogEvent(operLog));
        } catch (Exception exp) {
            // 记录本地异常日志
            log.error("==前置通知异常==");
            log.error("异常信息:{}", exp.getMessage());
            exp.printStackTrace();
        }
    }

    /**
     * 获取注解中对方法的描述信息 用于Controller层注解
     *
     * @param log     日志
     * @param operLog 操作日志
     * @throws Exception
     */
    public void getControllerMethodDescription(OperLog log, UserTrailQuery operLog, Object[] args) throws Exception {
        // 设置action动作
        operLog.setOperateAction(log.businessType().getDes());
        // 设置标题
        operLog.setOperateObject(log.title());
        // 是否需要保存request，参数和值
        if (log.isSaveRequestData()) {
            // 获取参数的信息，传入到数据库中。
            setRequestValue(operLog, args);
        }
    }

    /**
     * 获取请求的参数，放到log中
     *
     * @param operLog 操作日志
     * @throws Exception 异常
     */
    private void setRequestValue(UserTrailQuery operLog, Object[] args) throws Exception {
        List<?> params = new ArrayList<>(Arrays.asList(args)).stream().filter(p -> p instanceof CommonContent)
                .collect(Collectors.toList());
        log.debug("args:{}", params);
        String paramStr = "";
        if (params.size() > 0) {
            if (params.get(0) instanceof MultipartFile) { // 导入文件无法做json转换
                String filename = ((MultipartFile) (params.get(0))).getResource().getFilename();
                paramStr = "{文件名:'" + filename + "'}";
            } else {
                Object body = ((CommonContent) params.get(0)).getBody();
                Map<String, Object> paramMap = new LinkedHashMap<>();
                if (body instanceof Map) {
                    paramMap = (LinkedHashMap) body;
                } else {
                    try {
                        Class cls = body.getClass();
                        Field[] fields = cls.getDeclaredFields();

                        for (Field field : fields) {
                            field.setAccessible(true);
                            Object fieldValue = field.get(body);
                            ApiModelProperty api = field.getAnnotation(ApiModelProperty.class);
                            if (null != api && null != api.value()) {
                                if (Objects.nonNull(fieldValue)) {
                                    paramMap.put(api.value(), fieldValue);
                                }
                            } else {
                                if (Objects.nonNull(fieldValue)) {
                                    paramMap.put(field.getName(), fieldValue);
                                }
                            }
                        }
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }

                paramStr = StringUtils.strip(JSON.toJSONString(paramMap), "{}");
            }
        }
        operLog.setDescription(StringUtils.substring(paramStr, 0, 500));
    }

    /**
     * 是否存在注解，如果存在就获取
     */
    private OperLog getAnnotationLog(JoinPoint joinPoint) throws Exception {
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();
        if (method != null) {
            return method.getAnnotation(OperLog.class);
        }
        return null;
    }
}
