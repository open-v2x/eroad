package cn.eroad.device.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import cn.eroad.device.entity.po.Dict;
import org.apache.ibatis.annotations.Param;

/**
 * @author wanglc4
 */
public interface DictMapper extends BaseMapper<Dict> {

    Integer deleteFlag(@Param("dictEncoding") String dictEncoding);
}
