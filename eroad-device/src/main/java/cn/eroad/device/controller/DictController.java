package cn.eroad.device.controller;

import cn.eroad.core.domain.CommonContent;
import cn.eroad.core.domain.PageDomain;
import cn.eroad.device.entity.form.query.DictQuery;
import cn.eroad.device.entity.po.Dict;
import cn.eroad.device.service.IDictService;
import cn.eroad.trail.annotation.OperLog;
import cn.eroad.trail.enums.BusinessType;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author wanglc4
 */
@RestController
@RequestMapping("/system/dict")
@Slf4j
@Api(value = "字典表管理", tags = "字典表管理")
public class DictController {
    @Autowired
    private IDictService dictService;

    /**
     * 进入字典表管理时 查询
     */
    @GetMapping("/find")
    @ApiOperation(value = "进入字典表管理时查询", notes = "进入字典表管理时 查询")
    public CommonContent<List<Dict>> find(){
        List<Dict> dictList = dictService.find();
        return CommonContent.ok(dictList);
    }

    /**
     * 根据父级别id分页查询
     * @param dictQuery
     *
     * @return
     */
    @PostMapping("/list")
    @ApiOperation(value = "根据父级别id分页查询", notes = "根据父级别id分页查询")
    public CommonContent<PageDomain<Dict>> list(@RequestBody DictQuery dictQuery){
        PageDomain<Dict> pageInfo = dictService.selectPageList(dictQuery);
        return CommonContent.ok(pageInfo);
    }

    /**
     * 新增字典
     * @param dict
     * @return
     */
    @PostMapping("/add")
    @ApiOperation(value = "新增字典", notes = "新增字典")
    @OperLog(title = "新增字典", businessType = BusinessType.INSERT)
    public CommonContent<Dict> add(@RequestBody Dict dict) {
        dictService.saveDict(dict);
        return CommonContent.ok();
    }

    /**
     * 修改字典
     * @param dict
     * @return
     */
    @PostMapping("/update")
    @ApiOperation(value = "修改字典", notes = "修改字典")
    @OperLog(title = "修改字典", businessType = BusinessType.UPDATE)
    public CommonContent<Dict> update(@RequestBody Dict dict){
        dictService.update(dict);
        return CommonContent.ok();
    }

    /**
     * 删除字典
     * @param dict
     * @return
     */
    @PostMapping("/delete")
    @ApiOperation(value = "删除字典", notes = "删除字典")
    @OperLog(title = "删除字典", businessType = BusinessType.DELETE)
    public CommonContent<Dict> delete(@RequestBody Dict dict){
        dictService.delete(dict);
        return CommonContent.ok();
    }

    /**
     * 查询全部字典
     * @return
     */
    @PostMapping("/findAll")
    @ApiOperation(value = "查询全部字典", notes = "查询全部字典")
    public CommonContent<List<Dict>> findAll(){
        List<Dict> dictList = dictService.findAll();
        return CommonContent.ok(dictList);
    }

    /**
     * 提供给其它模块的查询接口
     * @param dictEncoding
     * @return
     */
    @GetMapping("/rpc/find")
    @ApiOperation(value = "提供给其它模块的查询接口", notes = "提供给其它模块的查询接口")
    public CommonContent<List<Dict>> findByDictEncoding(@RequestParam String dictEncoding){
        List<Dict> dictList = dictService.findByDictEncoding(dictEncoding);
        return CommonContent.ok(dictList);
    }
}
