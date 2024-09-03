package cn.eroad.device.service.devicemaintain.impl;

import cn.eroad.core.domain.PageDomain;
import cn.eroad.core.exception.AcException;
import cn.eroad.device.mapper.DeviceMaintainMapper;
import cn.eroad.device.entity.enums.DeviceMainEnum;
import cn.eroad.device.entity.devicemaintain.DeviceMaintain;
import cn.eroad.device.entity.devicemaintain.DeviceState;
import cn.eroad.device.entity.devicemaintain.DeviceVO;
import cn.eroad.device.entity.form.DeviceForm;
import cn.eroad.device.entity.query.DeviceMaintainQuery;
import cn.eroad.device.operate.DeviceCache;
import cn.eroad.device.service.devicemaintain.DeviceMaintainService;
import cn.eroad.device.vo.Device;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Slf4j
public class DeviceMaintainServiceImpl extends ServiceImpl<DeviceMaintainMapper, DeviceMaintain> implements DeviceMaintainService {

    @Autowired
    private DeviceMaintainMapper deviceMaintainMapper;

    @Autowired
    private DeviceCache deviceCache;

    @Value("#{'${noneIp.type:}'.empty ? 'null' : '${noneIp.type:}'.split(',')}")
    private Set<String> noneIpTypeSet;


    @Override
    public PageDomain<DeviceVO> selectPageList(DeviceMaintainQuery deviceMaintainQuery) {
        deviceMaintainQuery.pageMode(1, 10);
        PageInfo<DeviceMaintain> page = PageHelper.startPage(deviceMaintainQuery.getPageNum(), deviceMaintainQuery.getPageSize())
                .doSelectPageInfo(() -> baseMapper.selectList(deviceMaintainQuery.buildWrapper()));
        if (page != null && page.getList() != null) {
            List<DeviceVO> list = new ArrayList<>();
            page.getList().stream().forEach(d -> {
                DeviceVO vo = new DeviceVO();
                BeanUtils.copyProperties(d, vo);
                if (noneIpTypeSet.contains(vo.getDeviceType())) {
                    vo.setNetworkLink(vo.getOnlineState());
                    vo.setUnreachableTimesToday(0L);
                } else {
                    vo.setUnreachableTimesToday(ipTimeOutCount(d.getDeviceId()));
                }
                list.add(vo);
            });
            return new PageDomain() {
                {
                    this.setCount(page.getSize());
                    this.setList(list);
                    this.setPageNum(page.getPageNum());
                    this.setPageSize(page.getPageSize());
                    this.setTotalCount((int) page.getTotal());
                }
            };
        }
        return PageDomain.from(page);
    }


    @Autowired
    @Qualifier("stringRedisTemplate")
    private RedisTemplate redisTemplate;

    private Long ipTimeOutCount(String deviceId) {
        String timeOutKey = "ac_device_time_out_" + deviceId;
        Object redisTimeOutCount = redisTemplate.opsForValue().get(timeOutKey);
        Long timeOutCount;
        if (redisTimeOutCount == null) {
            timeOutCount = 0L;
        } else {
            timeOutCount = Long.parseLong(String.valueOf(redisTimeOutCount));
        }
        return timeOutCount;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public String saves(DeviceMaintain deviceMaintain) {
        //对编码唯一性进行校验
        Device redisDevice = deviceCache.getDeviceBySn(deviceMaintain.getDeviceId());
        if (redisDevice != null && Objects.equals(redisDevice.getDeviceId(), deviceMaintain.getDeviceId())) {
            throw new AcException(DeviceMainEnum.DEVICE_ID_NOT_ONLY.getCode(), DeviceMainEnum.DEVICE_ID_NOT_ONLY.getMsg());
        }

        //对姓名唯一性进行校验
        DeviceMaintain nameDeviceInfo = this.getOne(new QueryWrapper<DeviceMaintain>().lambda().eq(DeviceMaintain::getDeviceName, deviceMaintain.getDeviceName()));
        if (nameDeviceInfo != null) {
            throw new AcException(DeviceMainEnum.DEVICE_NAME_NOT_ONLY.getCode(), DeviceMainEnum.DEVICE_NAME_NOT_ONLY.getMsg());
        }

        int r = deviceMaintainMapper.insert(deviceMaintain);
        if (r <= 0) {
            throw new AcException(DeviceMainEnum.ORACLE_INTER_FAIL.getCode(), DeviceMainEnum.ORACLE_INTER_FAIL.getMsg());
        }
        //更新redis缓存
        deviceCache.updateDeviceCache(deviceMaintain);
        return "success";
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public String delete(List<String> deviceIdList) {

        int r = deviceMaintainMapper.deleteBatchIds(deviceIdList);
        if (r <= 0) {
            throw new AcException(DeviceMainEnum.ORACLE_INTER_FAIL.getCode(), DeviceMainEnum.ORACLE_INTER_FAIL.getMsg());
        }
        //更新redis缓存
        deviceCache.delDeviceCache(deviceIdList);
        return "success";

    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public String update(DeviceForm.EditForm editForm) {
        DeviceMaintain deviceMaintain = new DeviceMaintain();
        BeanUtils.copyProperties(editForm, deviceMaintain);

        //校验设备是否存在
        Device redisDevice = deviceCache.getDeviceBySn(deviceMaintain.getDeviceId());
        if (redisDevice == null) {
            throw new AcException(DeviceMainEnum.DEVICE_NOT_EXIST.getCode(), DeviceMainEnum.DEVICE_NOT_EXIST.getMsg());
        }

        //校验设备名称是否唯一
        DeviceMaintain nameDeviceInfo = this.getOne(
                new QueryWrapper<DeviceMaintain>().lambda()
                        .eq(DeviceMaintain::getDeviceName, deviceMaintain.getDeviceName())
                        .ne(DeviceMaintain::getDeviceId, deviceMaintain.getDeviceId())
        );
        if (nameDeviceInfo != null) {
            throw new AcException(DeviceMainEnum.DEVICE_NAME_NOT_ONLY.getCode(), DeviceMainEnum.DEVICE_NAME_NOT_ONLY.getMsg());
        }

        // 如果库的平台名/编码和入参的平台/编码一致，直接执行update更新自己；否则根据入参查询库，看是否唯一
        if (!redisDevice.getDeviceId().equals(deviceMaintain.getDeviceId())) {
            throw new AcException(DeviceMainEnum.ID_NOT_ALLOW_UPDATE.getCode(), DeviceMainEnum.ID_NOT_ALLOW_UPDATE.getMsg());
        }

        //数据库交互
        if (editForm.getDeviceIdNew() != null && !editForm.getDeviceIdNew().isEmpty()) {
            //删除
            deviceMaintainMapper.deleteById(editForm.getDeviceId());
            Device device = new Device();
            device.setDeviceId(editForm.getDeviceId());
            deviceCache.delDeviceCache(device);
            //新加
            deviceMaintain.setDeviceId(editForm.getDeviceIdNew());
            deviceMaintainMapper.insert(deviceMaintain);
            deviceCache.updateDeviceCache(deviceMaintain);
        } else {
            deviceMaintainMapper.updateById(deviceMaintain);
            deviceCache.updateDeviceCache(deviceMaintain);
        }
        return "success";
    }


    @Override
    public DeviceMaintain selectById(String deviceId) {

        log.info("selectById||={}在缓存中数据：={}", deviceId, deviceCache.getDeviceBySn(deviceId));
        DeviceMaintain deviceMaintain = deviceMaintainMapper.selectById(deviceId);
        if (deviceMaintain == null) {
            throw new AcException(DeviceMainEnum.ORACLE_SELECT_FAIL.getCode(), DeviceMainEnum.ORACLE_SELECT_FAIL.getMsg());
        }
        return deviceMaintain;
    }


    @Override
    public List<DeviceMaintain> selectByIds(List<String> deviceIdList) {


        log.info("查看详情接口数据deviceIdList={}", deviceIdList);
        //数据库交互
        List<DeviceMaintain> deviceMaintainList = deviceMaintainMapper.selectBatchIds(deviceIdList);
        if (deviceMaintainList == null) {
            throw new AcException(DeviceMainEnum.ORACLE_INTER_FAIL.getCode(), DeviceMainEnum.ORACLE_INTER_FAIL.getMsg());
        }
        return deviceMaintainList;
    }


    @Override
    public DeviceState selectDeviceState() {

        DeviceState deviceState = new DeviceState();

        /**
         * 在线  未发送告警信息   normal
         * 在线  发送告警信息   alarm
         * 离线  offline
         * sum = normal+alarm+offline
         * 除了在线的，都算离线
         */
        //查询设备总数
        int count = deviceMaintainMapper.selectCount(new QueryWrapper<DeviceMaintain>());
        deviceState.setSum(count);

        //查询告警设备(在线且告警)
        int alarmCount = deviceMaintainMapper.selectCount(new QueryWrapper<DeviceMaintain>()
                .eq("online_state", "1").eq("alarm_state", "1"));
        deviceState.setAlarm(alarmCount);

        //查询正常设备(在线未告警)
        int normalCount = deviceMaintainMapper.selectCount(new QueryWrapper<DeviceMaintain>().
                eq("online_state", "1").eq("alarm_state", "0"));
        deviceState.setNormal(normalCount);

        //查询离线设备(离线)
        int offLineCount = count - (alarmCount + normalCount);
        deviceState.setOffLine(offLineCount);

        return deviceState;
    }


    @Override
    public String excelImport(List<DeviceMaintain> deviceMaintainList) {
        log.info("开始执行excelImport,入参deviceMaintainList={}", deviceMaintainList);
        List<DeviceMaintain> oldDeviceList = new ArrayList<>();
        List<DeviceMaintain> newDeviceList = new ArrayList<>();


        //将已存在的设备编码放入一个List,执行更改操作，将未存在的设备编码放入一个list，执行插入操作
        for (int i = 0; i < deviceMaintainList.size(); i++) {
            Device redisDevice = deviceCache.getDeviceBySn(deviceMaintainList.get(i).getDeviceId());
            //设备编码已存在时
            if (redisDevice != null && Objects.equals(redisDevice.getDeviceId(), deviceMaintainList.get(i).getDeviceId())) {
                oldDeviceList.add(deviceMaintainList.get(i));
            } else {
                newDeviceList.add(deviceMaintainList.get(i));
            }
        }
        log.info("旧数据转换完成||oldDeviceList={}||新数据转换完成||newDeviceList={}", oldDeviceList, newDeviceList);

        //已存在设备执行批量更新，新设备执行批量添加
        try {
            int count1, count2;
            if (!newDeviceList.isEmpty()) {
                count1 = deviceMaintainMapper.excelImport(newDeviceList);
                log.info("新数据添加完成||r1={}", count1);
                if (count1 <= 0) {
                    throw new AcException(DeviceMainEnum.ORACLE_INTER_FAIL.getCode(), DeviceMainEnum.ORACLE_INTER_FAIL.getMsg());
                }


            }
            if (!oldDeviceList.isEmpty()) {
                count2 = deviceMaintainMapper.excelUpdate(oldDeviceList);
                log.info("旧数据添加完成||r2={}", count2);
                if (count2 <= 0) {
                    throw new AcException(DeviceMainEnum.ORACLE_INTER_FAIL.getCode(), DeviceMainEnum.ORACLE_INTER_FAIL.getMsg());
                }
            }
            //更新redis缓存
            log.info("开始插入缓存");
            deviceCache.updateDeviceCache(deviceMaintainList);
        } catch (Exception e) {
            log.error("数据导入失败", e);
            Throwable cause = e.getCause();
            if (cause instanceof java.sql.SQLIntegrityConstraintViolationException) {
                log.info("字段违反唯一约束条件");
                throw new AcException(DeviceMainEnum.EXCEL_NOT_ALLOW_UPDATE.getCode(), DeviceMainEnum.EXCEL_NOT_ALLOW_UPDATE.getMsg());
            }
            e.printStackTrace();
        }
        return "success";
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deviceSave(DeviceMaintain deviceMaintain) {

        //对编码跟名称唯一性进行校验，如果编码不唯一，说明已手动注册，不再执行，如果编码不唯一说明未注册过，执行
        DeviceMaintain idDeviceInfo = this.getOne(new QueryWrapper<DeviceMaintain>().lambda().eq(DeviceMaintain::getDeviceId, deviceMaintain.getDeviceId()));
        if (idDeviceInfo != null) {
            log.warn("设备已注册");
            return;
        }

        log.info("自动注册准备插入数据库的deviceMaintain:={}", deviceMaintain);
        try {
            deviceMaintainMapper.insert(deviceMaintain);
            deviceCache.updateDeviceCache(deviceMaintain);
            Device device = deviceCache.getDeviceBySn(deviceMaintain.getDeviceId());
            log.info("自动注册数据插入成功后redis已有缓存为device={}", device);
        } catch (Exception e) {
            throw new AcException(DeviceMainEnum.ORACLE_INTER_FAIL.getCode(), DeviceMainEnum.ORACLE_INTER_FAIL.getMsg());
        }
    }


}
