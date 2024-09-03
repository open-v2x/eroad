package cn.eroad.videocast.model;

import lombok.Data;

/**
 * @Description: 服务器信息
 * @Param:
 * @return:
 * @Author: nbr
 * @Date: 2022/4/28
 */
@Data
public class ServerAddress {
    //剩余安全登录错误次数,超过这些次数用户会被锁定
    private Long RemainLockTimes;

    //用户密码错误次数超过上限被锁定后，下次安全登录剩余时间,单位：分钟
    private Long RemainUnlockTime;
}
