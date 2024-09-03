package cn.eroad.rad.model;

import lombok.*;

/**
 * @author mrChen
 * @date 2022/7/7 14:42
 */
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Data
public
class AtomData<T> {
    /**
     * 原始数据
     */
    String originalData;
    /**
     * 映射数据
     */
    T mappingData;

}
