package cn.eroad.core.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Objects;

/**
 * 分页数据
 */
@Data
@ApiModel(value = "通用查询封装类", description = "通用查询封装类")
public class QueryDomain<T> {
    /**
     * 当前记录起始索引
     */
    @ApiModelProperty(value = "当前记录起始索引", example = "1")
    private Integer pageNum;

    /**
     * 每页显示记录数
     */
    @ApiModelProperty(value = "每页显示记录数", example = "10")
    private Integer pageSize;

    @ApiModelProperty(value = "Table页单字段动态排序", example = "{\"field\":\"createTime\", \"order\":\"ascend\"}")
    private Sorter sorter;

    /**
     * 分页模型建模
     *
     * @param defaultPageNum  默认页码
     * @param defaultPageSize 默认分页大小
     */
    public void pageMode(Integer defaultPageNum, Integer defaultPageSize) {
        this.pageMode(defaultPageNum, defaultPageSize, null, null);
    }

    /**
     * 分页模型建模
     *
     * @param defaultPageNum  默认页码
     * @param defaultPageSize 默认分页大小
     * @param maxPageNum      最大页码
     * @param maxPageSize     最大分页大小
     */
    private void pageMode(Integer defaultPageNum, Integer defaultPageSize, Integer maxPageNum, Integer maxPageSize) {
        if (this.getPageNum() == null || this.getPageNum() <= 0) {
            this.setPageNum(Objects.isNull(defaultPageNum) ? 1 : defaultPageNum);
        } else if (Objects.nonNull(maxPageNum) && this.getPageNum().compareTo(maxPageNum) > 0) {
            this.setPageNum(maxPageNum);
        }

        if (this.getPageSize() == null || this.getPageSize() <= 0) {
            this.setPageSize(Objects.isNull(defaultPageSize) ? 10 : defaultPageSize);
        } else if (Objects.nonNull(maxPageSize) && this.getPageSize().compareTo(maxPageSize) > 0) {
            this.setPageSize(maxPageSize);
        }
    }

    /**
     * 简单查询时，通过分页参数进行列表数量限制
     *
     * @return
     */
    public Boolean doPageable() {
        return Objects.nonNull(pageNum) && Objects.nonNull(pageSize);
    }

    /**
     * mybatis-plus Wrapper生成器
     *
     * @return {@link Object }
     */
    public Object buildWrapper() {
        return null;
    }
}
