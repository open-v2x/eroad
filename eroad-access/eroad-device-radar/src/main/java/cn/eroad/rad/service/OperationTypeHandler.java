package cn.eroad.rad.service;

/**
 * 操作类型处理接口
 *
 * @author mrChen
 * @date 2022/7/6 14:06
 */
public interface OperationTypeHandler<T, O> {

    /**
     * 业务处理
     *
     * @param o
     * @return
     */
    T handler(O o);

}
