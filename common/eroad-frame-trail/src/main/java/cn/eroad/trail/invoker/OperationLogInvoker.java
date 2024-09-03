package cn.eroad.trail.invoker;

import cn.eroad.core.domain.CommonContent;
import cn.eroad.core.vo.CommonRequest;
import cn.eroad.trail.entity.UserTrailQuery;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(contextId = "eroad.log", name = "OperationLogInvoker", url = "http://" + "${feign.system.url}")
public interface OperationLogInvoker {

    /**
     * 日志存储接口
     *
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/rpc/operationLog/saveLog")
    CommonContent saveLog(CommonRequest<UserTrailQuery> commonRequest);

}
