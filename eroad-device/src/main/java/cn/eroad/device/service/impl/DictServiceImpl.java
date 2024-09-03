package cn.eroad.device.service.impl;

import cn.eroad.core.domain.Head;
import cn.eroad.core.domain.PageDomain;
import cn.eroad.core.exception.AcException;
import cn.eroad.device.mapper.DictMapper;
import cn.eroad.device.entity.form.query.DictQuery;
import cn.eroad.device.entity.po.Dict;
import cn.eroad.device.service.IDictService;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements IDictService {
    @Resource
    private DictMapper dictMapper;
    private static final String basePid = "0";

    @Override
    public PageDomain<Dict> selectPageList(DictQuery dictQuery) {
        List<Dict> dictList = new ArrayList<>();
        if (CollectionUtil.isEmpty(dictList)){
            PageHelper.startPage(dictQuery.getPageNum(),dictQuery.getPageSize());
            QueryWrapper<Dict> dictQueryWrapper = dictQuery.buildWrapper();
            dictQueryWrapper.orderByDesc("create_time");
            dictList = dictMapper.selectList(dictQueryWrapper);
        }

        PageInfo<Dict> dictPageInfo = new PageInfo<>(dictList);
        return PageDomain.from(dictPageInfo);
    }

    @Override
    public Integer delete(Dict dict) {
        int count = dictMapper.selectCount(new QueryWrapper<Dict>().eq("pid", dict.getId()));
        if (count > 0) {
            throw new AcException(new Head("F", "CC000500","抱歉，该字典存在子字典，请先删除全部子字典！"));
        }

        dict = dictMapper.selectById(dict.getId());
        Integer flag = dictMapper.deleteFlag(dict.getDictEncoding());
        if (flag != null && flag == 1){
            throw new AcException(new Head("F", "CC000500","抱歉，该字典正在使用中，暂时无法删除！"));
        }
        return dictMapper.deleteById(dict.getId());
    }

    @Override
    public List<Dict> findByDictEncoding(String dictEncoding) {
        Dict dict = dictMapper.selectOne(new QueryWrapper<Dict>().eq("dict_encoding", dictEncoding));
        if (dict == null) {
            return null;
        }
        List<Dict> dictList = dictMapper.selectList(new QueryWrapper<Dict>().eq("pid", dict.getId()));
        return dictList;
    }

    @Override
    public List<Dict> findAll() {
        QueryWrapper<Dict> dictQueryWrapper = new QueryWrapper<>();
        dictQueryWrapper.select("id","dict_name","dict_encoding");
        dictQueryWrapper.ne("pid",basePid);
        return dictMapper.selectList(dictQueryWrapper);
    }
    @Override
    public Integer update(Dict dict) {
        return dictMapper.updateById(dict);
    }

    @Override
    public void saveDict(Dict dict) {
        Integer count = dictMapper.selectCount(new QueryWrapper<Dict>().eq("dict_encoding", dict.getDictEncoding()));
        if (count > 0) {
            throw new AcException(new Head("F", "CC000500","抱歉，该字典编码已被使用，请尝试其它编码！"));
        }
        dictMapper.insert(dict);
    }

    @Override
    public List<Dict> find() {

        QueryWrapper<Dict> dictQueryWrapper = new QueryWrapper<>();
        dictQueryWrapper.select("id, dict_name, dict_encoding, dict_type, remarks, pid");
        dictQueryWrapper.eq("pid", 0);
        dictQueryWrapper.orderByDesc("create_time");
        List<Dict> dictList = dictMapper.selectList(dictQueryWrapper);

        return dictList;
    }

}
