package cn.eroad.device.entity.po;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

/**
 * @author wanglc4
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@ApiModel(value="字典表对象", description="dict")
@TableName("sys_dict")
public class Dict implements Serializable {
    private static final long serialVersionUID = 5704251064867239079L;

    @ApiModelProperty(value = "id")
    @TableId(type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "字典名称")
    @TableField("dict_name")
    private String dictName;

    @ApiModelProperty(value = "字典编码")
    @TableField("dict_encoding")
    private String dictEncoding;

    @ApiModelProperty(value = "字典类型")
    @TableField("dict_type")
    private Integer dictType;

    @ApiModelProperty(value = "备注")
    @TableField("remarks")
    private String remarks;

    @ApiModelProperty(value = "父级别id")
    @TableField("pid")
    private Integer pid;

    @ApiModelProperty(value = "创建时间")
    @TableField(value = "create_time",fill = FieldFill.INSERT)
    private Date createTime;

    @ApiModelProperty(value = "修改时间")
    @TableField(value = "update_time",fill = FieldFill.UPDATE)
    private Date updateTime;

    @ApiModelProperty(value = "逻辑删除标志")
    @TableField(value = "deleted", select = false, fill = FieldFill.INSERT)
    @TableLogic
    private Integer deleted;
}
