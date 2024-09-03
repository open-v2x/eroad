package cn.eroad.device.operate;

import cn.eroad.core.context.SpringContextHolder;
import cn.eroad.core.dto.DeviceConfigDTO;
import cn.eroad.core.dto.DeviceOnlineDTO;
import cn.eroad.core.utils.StringUtil;
import cn.eroad.device.constants.DeviceConstant;
import cn.eroad.device.dto.CacheDTO;
import cn.eroad.device.event.CacheLoadedEvent;
import cn.eroad.device.event.DeviceChangeEvent;
import cn.eroad.device.utils.IpUtil;
import cn.eroad.device.utils.MacUtil;
import cn.eroad.device.vo.*;
import cn.eroad.redis.utils.RedisUtil;
import com.alibaba.fastjson.JSONArray;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * 描述:设备缓存处理类，通过缓存更新和从缓存中获取设备
 */
@Component
@Slf4j
public class DeviceCache {
    protected static final String DEVICE_CACHE_KEY_PREFIX = "AC_DEVICE_CACHE";
    protected static final String DEVICE_CACHE_UNIQUE_KEY = "ac_device_cache_unique_key";
    protected static final String DEVICE_CACHE_CONFIG = "device_config";
    protected static final String DEVICE_CACHE_HEART_KEY = "device_heart_config";
    protected static final String DEVICE_CACHE_KEY_TYPE_PREFIX = "AC_DEVICE_CACHE_TYPE_";
    protected static final String DEVICE_CACHE_KEY_PARENT_PREFIX = "AC_DEVICE_CACHE_PARENT_";

    // sn-Device
    private static final Map<String, Device> snDevices = new ConcurrentHashMap<>();
    // ip-Device
    private static final Map<String, Device> ipDevices = new ConcurrentHashMap<>();

    // name-Device
    private static final Map<String, Device> nameDevices = new ConcurrentHashMap<>();

    // mac-Device
    private static final Map<String, Device> macDevices = new ConcurrentHashMap<>();


    private static Map<String, String> snGroupMap = new ConcurrentHashMap<>();

    private static Map<String, Set<Device>> typeMap = new ConcurrentHashMap<>();

    @Autowired
    @Qualifier("objectSerializerRedisTemplate")
    private RedisTemplate redisTemplate;

    private String cacheUniqueKey() {
        try {
            return DeviceConstant.DEVICE_CACHE_CHANGE_KEY + redisTemplate.opsForValue().increment(DEVICE_CACHE_UNIQUE_KEY, 1);
        } catch (Exception e) {
            e.printStackTrace();
            return DeviceConstant.DEVICE_CACHE_CHANGE_KEY;
        }
    }


    /**
     * 根据sn获取设备详情信息
     *
     * @param sn
     * @return
     */
    public Device getDeviceBySn(String sn) {
        try {
            Device device = (Device) redisTemplate.opsForHash().get(DEVICE_CACHE_KEY_PREFIX, sn);
            return device;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Device setDevice2Redis(Device device) {
        try {
            redisTemplate.opsForHash().put(DEVICE_CACHE_KEY_PREFIX, device.getDeviceId(), device);
            return device;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 初始化设备缓存
     *
     * @param list
     * @return
     */
    public boolean initDeviceCache(List<?> list) {
        try {
            delDeviceCache();
            if (list == null || list.size() == 0) {
                return false;
            }
            Map<String, Device> map = list.stream().map(item -> {
                Device device = new Device();
                BeanUtils.copyProperties(item, device);
                return device;
            }).collect(Collectors.toMap(Device::getDeviceId, t -> t));
            redisTemplate.opsForHash().putAll(DEVICE_CACHE_KEY_PREFIX, map);


            setDeviceCacheWithType(map);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 批量更新设备缓存
     *
     * @param list
     * @return
     */
    public boolean updateDeviceCache(List<?> list) {
        try {
            if (list == null || list.size() == 0) {
                return false;
            }
            Map<String, Device> map = list.stream().map(item -> {
                Device device = new Device();
                BeanUtils.copyProperties(item, device);
                return device;
            }).collect(Collectors.toMap(Device::getDeviceId, t -> t));
            redisTemplate.opsForHash().putAll(DEVICE_CACHE_KEY_PREFIX, map);


            setDeviceCacheWithType(map);
            //触发设备变更事件
            SpringContextHolder.publishEvent(new DeviceChangeEvent(map.values()));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean setDeviceCacheWithType(Device device) {
        try {
            String prefix = DEVICE_CACHE_KEY_TYPE_PREFIX + device.getDeviceType();
            redisTemplate.opsForHash().put(prefix, device.getDeviceId(), device);

            if (!StringUtils.isEmpty(device.getParentDeviceId())) {
                redisTemplate.opsForHash().put(DEVICE_CACHE_KEY_PARENT_PREFIX + device.getParentDeviceId(), device.getDeviceId(), device);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean setDeviceCacheWithType(Map<String, Device> map) {
        try {
            map.values().forEach(device -> {
                String prefix = DEVICE_CACHE_KEY_TYPE_PREFIX + device.getDeviceType();
                redisTemplate.opsForHash().put(prefix, device.getDeviceId(), device);

                if (!StringUtils.isEmpty(device.getParentDeviceId())) {
                    redisTemplate.opsForHash().put(DEVICE_CACHE_KEY_PARENT_PREFIX + device.getParentDeviceId(), device.getDeviceId(), device);
                }
            });
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Device> getDeviceListByType(String type) {
        try {
            if (StringUtil.isEmpty(type) || type.trim().equals("all")) {
                List<Device> list = redisTemplate.opsForHash().values(DEVICE_CACHE_KEY_PREFIX);
                return list;
            } else {
                String[] types = type.split(",");
                if (types.length == 1) {
                    String prefix = DEVICE_CACHE_KEY_TYPE_PREFIX + type;
                    List<Device> list = redisTemplate.opsForHash().values(prefix);
                    return list;
                }
                List<Device> list = new ArrayList<>();
                for (String t : types) {
                    String prefix = DEVICE_CACHE_KEY_TYPE_PREFIX + t;
                    List<Device> l = redisTemplate.opsForHash().values(prefix);
                    list.addAll(l);
                }
                return list;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    private static RedisUtil redisUtil;

    @Autowired
    public void setRedisUtil(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }

    private static long CACHE_KEY_TIME = 30L;

    /**
     * 更新单个设备缓存
     *
     * @param item
     * @return
     */
    public boolean updateDeviceCache(Object item) {
        try {
            if (item == null) {
                return false;
            }
            Device device = new Device();
            BeanUtils.copyProperties(item, device);
            if (device.getDeviceId() == null) {
                return false;
            }
            redisTemplate.opsForHash().put(DEVICE_CACHE_KEY_PREFIX, device.getDeviceId(), device);
            setDeviceCacheWithType(device);
            //触发设备变更事件
            SpringContextHolder.publishEvent(new DeviceChangeEvent(device));

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 更新单个设备缓存到redis，不触发缓存更新事件
     *
     * @param item
     * @return
     */
    public boolean updateDeviceCacheWithoutEvent(Object item) {
        try {
            if (item == null) {
                return false;
            }
            Device device = new Device();
            BeanUtils.copyProperties(item, device);
            if (device.getDeviceId() == null) {
                return false;
            }
            redisTemplate.opsForHash().put(DEVICE_CACHE_KEY_PREFIX, device.getDeviceId(), device);
            setDeviceCacheWithType(device);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 按照类型更新单个设备缓存到redis，不触发缓存更新事件
     *
     * @param device
     * @return
     */
    public boolean updateDeviceCacheByType(Device device) {
        try {
            if (device == null) {
                return false;
            }
            if (device.getDeviceId() == null) {
                return false;
            }
            String prefix = DEVICE_CACHE_KEY_TYPE_PREFIX + device.getDeviceType();
            redisTemplate.opsForHash().put(prefix, device.getDeviceId(), device);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean addDeviceCache(Map<String, Device> map) {
        try {
            redisTemplate.opsForHash().putAll(DEVICE_CACHE_KEY_PREFIX, map);
            redisUtil.set(cacheUniqueKey(), map.keySet(), CACHE_KEY_TIME);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delDeviceCache(Device device) {
        try {
            log.info("删除设备缓存数据 {}", device.getDeviceId());
            redisTemplate.opsForHash().delete(DEVICE_CACHE_KEY_PREFIX, device.getDeviceId());

            redisTemplate.opsForHash().delete(DEVICE_CACHE_KEY_TYPE_PREFIX + device.getDeviceType(), device.getDeviceId());

            if (!StringUtils.isEmpty(device.getParentDeviceId())) {
                redisTemplate.opsForHash().delete(DEVICE_CACHE_KEY_PARENT_PREFIX + device.getParentDeviceId(), device.getDeviceId(), device);
            }

            snDevices.remove(device.getDeviceId());
            snGroupMap.remove(device.getDeviceId());
            nameDevices.remove(device.getDeviceName());
            if (!StringUtils.isEmpty(device.getDeviceIp())) {
                ipDevices.remove(device.getDeviceIp());
            }

            if (typeMap.containsKey(device.getDeviceType())) {
                typeMap.get(device.getDeviceType()).remove(device);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    //todo
    public boolean delDeviceCache(List<String> snList) {
        try {
            if (CollectionUtils.isEmpty(snList)) {
                return true;
            }
            List<Device> list = redisTemplate.opsForHash().multiGet(DEVICE_CACHE_KEY_PREFIX, snList);
            redisTemplate.opsForHash().delete(DEVICE_CACHE_KEY_PREFIX, snList.toArray());
            if (CollectionUtils.isEmpty(list)) {
                return true;
            }
            list.stream().forEach(device -> {
                redisTemplate.opsForHash().delete(DEVICE_CACHE_KEY_TYPE_PREFIX + device.getDeviceType(), device.getDeviceId());

                if (!StringUtils.isEmpty(device.getParentDeviceId())) {
                    redisTemplate.opsForHash().delete(DEVICE_CACHE_KEY_PARENT_PREFIX + device.getParentDeviceId(), device.getDeviceId(), device);
                }

                log.info("设备数据为：{}", device);
                device.setOperateSign(0);
                SpringContextHolder.publishEvent(new DeviceChangeEvent(device));
            });

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delDeviceCache() {
        try {
            redisTemplate.delete(DEVICE_CACHE_KEY_PREFIX);

            //按照类型删除设备
            Set<String> keys = redisTemplate.keys(DEVICE_CACHE_KEY_TYPE_PREFIX + "*");
            if (!CollectionUtils.isEmpty(keys)) {
                redisTemplate.delete(keys);
            }

            //删除父ID组的设备
            keys = redisTemplate.keys(DEVICE_CACHE_KEY_PARENT_PREFIX + "*");
            if (!CollectionUtils.isEmpty(keys)) {
                redisTemplate.delete(keys);
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Device> getAllDeviceByParentId(String parentId) {
        try {
            List<Device> list = redisTemplate.opsForHash().values(DEVICE_CACHE_KEY_PARENT_PREFIX + parentId);
            return list;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 设备IP可达超时次数自增
     *
     * @param key
     * @return
     */
    public static long timeOutIncr(String key) {
        return redisUtil.getIncr(key, 0);
    }


    /**
     * 处理本地设备分组映射关系缓存 更新或删除
     *
     * @param device
     * @param del
     */
    public void groupCacheHandler(Device device, boolean del) {
        if (del) {
            delGroupCache(device);
        } else {
            mergeGroupCache(device);
        }
    }

    /**
     * 根据增量的设备信息更新设备编码与设备分组的本地缓存
     *
     * @param device
     * @return
     */
    public boolean mergeGroupCache(Device device) {
        try {
            snGroupMap.put(device.getDeviceId(), device.getDomainName());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 根据删除的设备信息更新设备编码与设备分组的本地缓存
     *
     * @param device
     * @return
     */
    public boolean delGroupCache(Device device) {
        try {
            snGroupMap.remove(device.getDeviceId());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * 根据sn获取当前所属分组
     *
     * @param sn
     * @return
     */
    public String getDomainBySn(String sn) {
        return snGroupMap.get(sn);
    }


    public boolean updateLocalCache(String key) {
        try {
            Object value = redisUtil.get(key);

            if (value instanceof String) {
                Device device = getDeviceBySn(value.toString());
                updateFilter(device);
            } else if (value instanceof JSONArray) {
                List<Device> list = redisTemplate.opsForHash().multiGet(DEVICE_CACHE_KEY_PREFIX, (JSONArray) value);
                list.forEach(this::updateFilter);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateLocalCache(Device device) {
        try {

            if (device.getOperateSign() != null && device.getOperateSign() == 0) {
                delDeviceCache(device);
            } else {
                updateFilter(device);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 过滤条件，更新map
     *
     * @param device
     */
    public void updateFilter(Device device) {
        setDeviceCache(device);







    }

    public static Device getDeviceFromCache(String sn) {
        Device device = snDevices.get(sn);
        if (device == null) {
            device = (Device) redisUtil.hget(DEVICE_CACHE_KEY_PREFIX, sn);
            if (device != null) {
                snDevices.put(device.getDeviceId(), device);
            }
        }
        return device;
    }

    public static Collection<Device> getAllDeviceFromCache() {
        return snDevices.values();
    }

    public static Collection<Device> getAllDeviceFromCacheByType(String type) {
        return typeMap.getOrDefault(type, new HashSet<>());
    }

    /**
     * 根据sn获取Device信息
     *
     * @param sn
     * @return
     */
    public static Device getDeviceBySnFromMap(String sn) {
        return snDevices.get(sn);
    }

    /**
     * 根据sn获取ip
     *
     * @param sn
     * @return
     */
    public static String getIpBySnFromMap(String sn) {
        Device device = snDevices.get(sn);
        return device != null ? device.getDeviceIp() : null;
    }

    /**
     * 根据ip获取sn
     *
     * @param ip
     * @return
     */
    public static String getSnByIpFromMap(String ip) {
        Device device = ipDevices.get(ip);
        return device != null ? device.getDeviceId() : null;
    }

    /**
     * 根据ip获取设备信息
     *
     * @param ip
     * @return
     */
    public static Device getDeviceByIpFromMap(String ip) {
        return ipDevices.get(ip);
    }

    /**
     * 根据mac地址获取设备sn
     *
     * @param mac
     * @return
     */
    public static String getSnByMacFromMap(String mac) {
        Device device = macDevices.get(mac);
        return device != null ? device.getDeviceId() : null;
    }

    /**
     * 根据mac地址获取设备信息
     *
     * @param mac
     * @return
     */
    public static Device getDeviceByMacFromMap(String mac) {
        return macDevices.get(mac);
    }

    public static void setDeviceCache(Device device) {
        snDevices.put(device.getDeviceId(), device);
        if (!StringUtils.isEmpty(device.getDomain())) {
            snGroupMap.put(device.getDeviceId(), device.getDomain());
        }
        if (!StringUtils.isEmpty(device.getDeviceIp())) {
            ipDevices.put(IpUtil.parseAbbreviationToFullIP(device.getDeviceIp()), device);
        }

        if (!StringUtils.isEmpty(device.getMac())) {
            macDevices.put(MacUtil.toUpperCase(device.getMac()), device);
        }
        if (!StringUtils.isEmpty(device.getDeviceName())) {
            nameDevices.put(device.getDeviceName(), device);
        }

        typeMap.computeIfAbsent(device.getDeviceType(), key -> {
            return new HashSet<>();
        }).add(device);
    }





    public static boolean setLocalDeviceCache(Device device) {
        log.info("根据自动注册的设备更新本地缓存 {}", device);
        setDeviceCache(device);
        return true;
    }

    /**
     * 根据sn获取port
     *
     * @param sn
     * @return
     */
    public static String getPortBySnFromMap(String sn) {
        Device device = snDevices.get(sn);
        return device != null ? device.getPort() : null;
    }

    /**
     * 根据sn判断是否存在该设备
     *
     * @param sn
     * @return
     */
    public static Boolean existDevice(String sn) {
        return snDevices.containsKey(sn);
    }

    @Value("${eroad.device.cache.url:http://eroad-device:8080/device/cache/list?type=all}")
    private String cacheUrl;

    /**
     * 初始化本地缓存
     *
     * @return
     */
    public boolean initLocalMap() {
        log.info("拉取存量设备数据");
        try {
            Flux<CacheDTO> resp = WebClient.create()
                    .method(HttpMethod.GET)
                    .uri(cacheUrl)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .retrieve().bodyToFlux(CacheDTO.class);

            resp.subscribe(this::handleResponse, null, new Runnable() {
                @Override
                public void run() {
                    log.info("完成缓存拉取，触发事件");
                    //触发缓存加载完成事件
                    SpringContextHolder.publishEvent(new CacheLoadedEvent(new Object()));
                }
            });

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 接受存量设备数据，进行处理
     *
     * @param cacheDTO
     */
    private void handleResponse(CacheDTO cacheDTO) {
        log.info("接受存量设备数据，进行处理 {}", cacheDTO);
        if (cacheDTO == null || cacheDTO.getList() == null) {
            return;
        }
        cacheDTO.getList().forEach(this::updateFilter);
    }


    /**
     * 根据sn集合识别为不同分组下的子集
     * 如果不设置设备服务映射，使用设备类型作为路由
     *
     * @param snList
     * @return
     */
    public Map<String, List<String>> domainSnListHandler(List<String> snList) {
        try {
            if (snList == null || snList.size() == 0) {
                log.warn("groupSnListHandler size:0");
                return null;
            }
            ConcurrentMap<String, List<String>> map = snList.parallelStream().filter(item -> snGroupMap.containsKey(item)).collect(Collectors.groupingByConcurrent(item -> snGroupMap.get(item)));
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * @Description: 存储设备配置
     * @Param:
     * @return:
     * @Author: nbr
     * @Date: 2022/8/24
     */
    public void setDeviceConfig(String key, String config) {
        redisTemplate.opsForHash().put(DEVICE_CACHE_CONFIG, key, config);
    }

    /**
     * @Description: 批量初始化缓存
     * @Param:
     * @return:
     * @Author: nbr
     * @Date: 2022/8/26
     */
    public void saveDeviceConfigList(List<?> list) {
        try {
            delDeviceConfig();
            Map<String, DeviceConfigDTO> map = list.stream().map(item -> {
                DeviceConfigDTO dto = new DeviceConfigDTO();
                BeanUtils.copyProperties(item, dto);
                return dto;
            }).collect(Collectors.toMap(DeviceConfigDTO::getSn, t -> t));
            redisTemplate.opsForHash().putAll(DEVICE_CACHE_CONFIG, map);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @Description: 获取设备配置
     * @Param:
     * @return:
     * @Author: nbr
     * @Date: 2022/8/24
     */
    public String getDeviceConfig(String key) {
        Object config = redisTemplate.opsForHash().get(DEVICE_CACHE_CONFIG, key);
        return config == null ? null : config.toString();
    }

    /**
     * @Description: 删除设备配置缓存
     * @Param:
     * @return:
     * @Author: nbr
     * @Date: 2022/8/26
     */
    public void delDeviceConfig() {
        redisTemplate.delete(DEVICE_CACHE_CONFIG);
    }

    public static void main(String[] args) {

































        List<Integer> l0 = new ArrayList<>();
        l0.add(1);
        l0.add(2);
        List<Integer> l1 = new ArrayList<>();
        l1.add(3);
        l1.add(4);
        List<Integer> l2 = new ArrayList<>();
        l2.add(5);
        l2.add(6);


        Flux.just(
                l0,
                l1,
                l2
        ).interval(Duration.ofMillis(100), Duration.ofMillis(1000)).subscribe(System.out::println);
    }


    //添加设备缓存,设置过期时间 todo
    public boolean updateDeviceHeartCache(Object item) {
        try {
            if (item == null) {
                return false;
            } else {
                DeviceOnlineDTO deviceOnlineDTO = new DeviceOnlineDTO();
                BeanUtils.copyProperties(item, deviceOnlineDTO);
                if (deviceOnlineDTO.getSn() == null) {
                    return false;
                } else {
                    //var1：key  var2：value  var3：是后面时间的倍数 var5：时间单位
                    //this.redisTemplate.opsForValue().set(deviceOnlineDTO.getSn(),deviceOnlineDTO,35, TimeUnit.MINUTES);
                    redisTemplate.opsForHash().put(DEVICE_CACHE_HEART_KEY, deviceOnlineDTO.getSn(), deviceOnlineDTO);
                    return true;
                }
            }
        } catch (Exception var3) {
            var3.printStackTrace();
            return false;
        }
    }

    /**
     * 根据sn获取设备心跳信息
     *
     * @param sn
     * @return
     */
    public DeviceOnlineDTO getDeviceHeartCacheBySn(String sn) {
        try {
            DeviceOnlineDTO deviceOnlineDTO = (DeviceOnlineDTO) this.redisTemplate.opsForHash().get(DEVICE_CACHE_HEART_KEY, sn);
            return deviceOnlineDTO;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean delDeviceHeartCache() {
        try {
            this.redisTemplate.delete(DEVICE_CACHE_HEART_KEY);
            return true;
        } catch (Exception var2) {
            var2.printStackTrace();
            return false;
        }
    }


}
