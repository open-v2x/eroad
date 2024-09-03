package cn.eroad.core.domain;

import com.github.pagehelper.PageInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 分页数据
 *
 * @author liyq
 */
@Data
@ApiModel(value = "通用查询封装类", description = "通用查询封装类")
public class PageDomain<T> {
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

    @ApiModelProperty(value = "当前页记录数", example = "0")
    private Integer count;

    @ApiModelProperty(value = "当前条件记录数", example = "0")
    private Integer totalCount;

    private List<T> list;

    public static <T> PageDomain<T> from(List<T> list) {
        PageInfo<?> pageInfo = new PageInfo(list);
        return from(pageInfo);
    }

    public static PageDomain from(@NotNull PageInfo pageInfo) {
        return new PageDomain() {{
            setCount(pageInfo.getSize());
            setList(pageInfo.getList());
            setPageNum(pageInfo.getPageNum());
            setPageSize(pageInfo.getPageSize());
            setTotalCount((int) pageInfo.getTotal());
        }};
    }

}
