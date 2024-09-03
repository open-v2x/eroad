package cn.eroad.device.service.impl;

import cn.eroad.core.domain.PageDomain;
import cn.eroad.device.service.OperationLogService;
import cn.eroad.device.mapper.OperationLogMapper;
import cn.eroad.device.entity.form.query.OperationLogQuery;
import cn.eroad.device.entity.po.OperationLog;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
* @author 91953
* @description 针对表【operation_log】的数据库操作Service实现
* @createDate 2022-05-17 15:42:47
*/
@Slf4j
@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog>
    implements OperationLogService {

    @Override
    public PageDomain<OperationLog> selectPageList(OperationLogQuery operationLogQuery) {
        QueryWrapper<OperationLog> wrapper = operationLogQuery.buildWrapper();
        PageInfo<OperationLog> page = PageHelper.startPage(operationLogQuery.getPageNum(),operationLogQuery.getPageSize())
                .doSelectPageInfo(()->baseMapper.selectList(wrapper));
        return PageDomain.from(page);
    }

    @Override

    public int expire() {
        Date date = DateUtils.addDays(new Date(), -7);
        QueryWrapper<OperationLog> wrapper = new QueryWrapper<>();
        wrapper.lt("gmt_created",date);
        int effectRows = baseMapper.delete(wrapper);
        if (effectRows>0){
            log.info("已清除超时日志{}条",effectRows);
        }
        return effectRows;
    }

}




