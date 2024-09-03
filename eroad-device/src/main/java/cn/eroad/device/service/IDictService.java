package cn.eroad.device.service;

import cn.eroad.core.domain.PageDomain;
import cn.eroad.device.entity.form.query.DictQuery;
import cn.eroad.device.entity.po.Dict;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author wanglc4
 */
public interface IDictService extends IService<Dict> {
    /**
     * 进入字典表管理时 查询
     * @return
     */
    List<Dict> find();

    /**
     * 根据父级别id分页查询
     * @param dictQuery
     *
     * @return
     */
    PageDomain<Dict> selectPageList(DictQuery dictQuery);

    /**
     * 修改字典
     * @param dict
     */
    Integer update(Dict dict);

    /**
     * 新增字典
     *
     * @param body
     * @return
     */
    void saveDict(Dict body);

    Integer delete(Dict body);

    /**
     * 查询全部字典
     * @return
     */
    List<Dict> findAll();

    /**
     * 提供给其它模块的查询接口
     * @param dictEncoding
     * @return
     */
    List<Dict> findByDictEncoding(String dictEncoding);
}
