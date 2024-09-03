package cn.eroad.rad.schedule;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
public class HeartbeatSchedule {

    public static Set<String> otherSn = new HashSet<>();

    public static Set<String> hbSn = new HashSet<>();

}
