package cn.eroad.videocast.dao.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface VideoCastMapper {
    int insertInfor(@Param("sn") String sn, @Param("ip") String ip);//添加设备序列号，ip

    String findSn(String ip);//通过ip查找设备序列号

    int deleteDevice(String sn);//通过设备sn删除

    List<String> findDeviceSn();//查询数据库中的sn列表
}
