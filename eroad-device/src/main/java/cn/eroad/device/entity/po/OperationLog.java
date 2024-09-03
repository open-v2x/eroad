package cn.eroad.device.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @TableName operation_log
 */
@TableName(value ="operation_log")
@Data
public class OperationLog implements Serializable {
    /**
     * 主键
     */
    @TableField(value = "id")
    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "主键")
    private Integer id;

    /**
     * 应用编码
     */
    @TableField(value = "application_code")
    @ApiModelProperty(value = "应用编码")
    private String applicationCode;

    /**
     * 创建者
     */
    @TableField(value = "creator")
    @ApiModelProperty(value = "创建者")
    private String creator;

    /**
     * 操作对象
     */
    @TableField(value = "operation_object")
    @ApiModelProperty(value = "操作对象")
    private String operationObject;

    /**
     * 操作
     */
    @TableField(value = "operation_action")
    @ApiModelProperty(value = "操作")
    private String operationAction;

    /**
     * 创建时间
     */
    @TableField(value = "gmt_created")
    @ApiModelProperty(value = "创建时间")
    private Date gmtCreated;

    /**
     * 描述
     */
    @TableField(value = "description")
    @ApiModelProperty(value = "描述")
    private String description;

    /**
     * 状态
     */
    @TableField(value = "status")
    @ApiModelProperty(value = "状态")
    private Integer status;

    /**
     * 创建者uid
     */
    @TableField(value = "creator_id")
    @ApiModelProperty(value = "创建者id")
    private String creatorId;

    /**
     * 操作参数
     */
    @ApiModelProperty(value = "操作参数")
    @TableField(value = "params")
    private String params;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
