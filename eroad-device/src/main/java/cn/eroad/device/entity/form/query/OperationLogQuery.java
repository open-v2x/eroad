package cn.eroad.device.entity.form.query;

import cn.eroad.core.domain.QueryDomain;
import cn.eroad.core.domain.Sorter;
import cn.eroad.core.enums.OrderEnum;
import cn.eroad.core.utils.StringUtil;
import cn.eroad.device.entity.po.OperationLog;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;


/**
 *
 * @author fanjiangqi
 * @create 2021-10-19 20:13
 */
@Data
@ApiModel(value="OperationLogQuery", description="操作日志表单查询")
public class OperationLogQuery extends QueryDomain<OperationLog>  {

    /**
     * 查询时间
     */
    @ApiModelProperty(value = "时间选择器开始时间")
    private Date beginTime;

    @ApiModelProperty(value = "时间选择器结束时间")
    private Date endTime;

    /**
     * 创建者
     */
    @ApiModelProperty(value = "操作者")
    private String creator;

    /**
     * 操作对象
     */
    @ApiModelProperty(value = "操作对象")
    private String operationObject;

    /**
     * 操作
     */
    @ApiModelProperty(value = "操作内容")
    private String operationAction;

    @Override
    public QueryWrapper<OperationLog> buildWrapper() {
        Sorter sorter = this.getSorter();
        QueryWrapper<OperationLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.like(StringUtil.isNotBlank(creator),"creator",this.getCreator());
        queryWrapper.gt(StringUtil.isNotNull(beginTime),"gmt_created",this.getBeginTime());
        queryWrapper.lt(StringUtil.isNotNull(endTime),"gmt_created",this.getEndTime());
        queryWrapper.like(StringUtil.isNotNull(operationObject),"operation_object",this.getOperationObject());
        queryWrapper.like(StringUtil.isNotNull(operationAction),"operation_action",this.getOperationAction());

        boolean haveSortField = sorter != null && StringUtil.isNotBlank(sorter.getField());
        if (haveSortField) {
            queryWrapper.orderBy(true, OrderEnum.ASC.getValue().equalsIgnoreCase(sorter.getOrder()), StringUtil.underscoreName(sorter.getField()));
        }
        return queryWrapper;
    }
}
