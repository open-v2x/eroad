package cn.eroad.device.dto;

import cn.eroad.device.vo.Device;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @project: eroad-frame
 * @ClassName: CacheDTO
 * @author: liyongqiang
 * @creat: 2022/7/11 11:33
 * 描述:
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CacheDTO {
    private List<Device> list;
}
