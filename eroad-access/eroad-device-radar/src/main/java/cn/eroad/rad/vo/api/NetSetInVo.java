package cn.eroad.rad.vo.api;

import cn.eroad.rad.vo.SnNetList;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
 * @author WuHang
 * @version 1.0
 * @description:
 * @date 2022/6/13 15:27
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class NetSetInVo {

    @ApiModelProperty(value = "配置参数结构")
    private List<SnNetList> snNetList;




}

