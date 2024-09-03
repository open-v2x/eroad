package cn.eroad.device.entity.form.query;

import cn.eroad.core.domain.QueryDomain;
import cn.eroad.core.utils.StringUtil;
import cn.eroad.core.utils.ValueUtil;
import cn.eroad.device.entity.po.Dict;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel(value = "DictQuery查询表单",description = "DictQuery查询表单")
public class DictQuery extends QueryDomain<Dict> {
    @ApiModelProperty(value = "字典名称")
    @TableField("dict_name")
    private String dictName;

    @ApiModelProperty(value = "字典编码")
    @TableField("dict_encoding")
    private String dictEncoding;

    @ApiModelProperty(value = "父级别id")
    @TableField("pid")
    private Integer pid;

    @Override
    public QueryWrapper<Dict> buildWrapper() {
        QueryWrapper<Dict> dictQueryWrapper = new QueryWrapper<>();
        dictQueryWrapper.eq("pid",ValueUtil.replaceAllSpecial(this.getPid().toString()));
        dictQueryWrapper.like(StringUtil.isNotBlank(dictName),"dict_name", ValueUtil.replaceAllSpecial(this.getDictName()));
        dictQueryWrapper.like(StringUtil.isNotBlank(dictEncoding),"dict_encoding",ValueUtil.replaceAllSpecial(this.getDictEncoding()));
        return dictQueryWrapper;
    }
}
