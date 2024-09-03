package cn.eroad.device.service;

import cn.eroad.core.domain.PageDomain;
import cn.eroad.device.entity.form.query.OperationLogQuery;
import cn.eroad.device.entity.po.OperationLog;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author 91953
* @description 针对表【operation_log】的数据库操作Service
* @createDate 2022-05-17 15:42:47
*/
public interface OperationLogService extends IService<OperationLog> {

    /**
     * 分页查询平台(设备)列表
     * @param operationLogQuery
     * @return
     */
    PageDomain<OperationLog> selectPageList(OperationLogQuery operationLogQuery);

    /**
     * 清除超时告警日志
     * @return
     */
    int expire();

}
