package cn.eroad.core.domain;

import cn.eroad.core.enums.OrderEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 排序
 *
 * @create 2021-03-24 16:09
 */
@Data
public class Sorter {
    @ApiModelProperty(value = "排序字段名", example = "createTime")
    private String field;

    /**
     * 升序/降序
     * 使用{@link OrderEnum}
     */
    @ApiModelProperty(value = "排序方式")
    private String order;
}
