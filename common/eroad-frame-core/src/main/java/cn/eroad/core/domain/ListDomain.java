package cn.eroad.core.domain;

import java.util.List;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * @param <T>
 */
@Data
@ApiModel(value = "通用查询封装类", description = "通用查询封装类")
public class ListDomain<T> {
    private List<T> list;
}
