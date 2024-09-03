package cn.eroad.util;

import cn.eroad.util.service.HeartbeatListener;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @project: eroad-frame
 * @ClassName: Incrementor
 * @author: liyongqiang
 * @creat: 2022/8/19 10:36
 * 描述: 用于计算业务数据接收量，接收到一定值之后清零并触发事件
 */
public class Incrementor {

    private String sn;

    private int maximalCount;

    private AtomicInteger ai;

    private HeartbeatListener heartbeatListener;

    public Incrementor(String sn, int maximalCount, HeartbeatListener heartbeatListener) {
        this.sn = sn;
        this.maximalCount = maximalCount;
        this.heartbeatListener = heartbeatListener;
        this.ai = new AtomicInteger(0);
    }

    public void add() {
        ai.incrementAndGet();
        boolean reset = ai.compareAndSet(maximalCount, 0);

        if (reset) {
            heartbeatListener.heartbeat(sn);
        }
    }

    public int value() {
        return ai.get();
    }


    public static void main(String[] args) {
        Incrementor in = new Incrementor("1234", 3, new HeartbeatListener() {

            public void heartbeat(String sn) {
                System.out.println("触发事件");
            }
        });

        for (int i = 0; i < 4; i++) {
            in.add();
            System.out.println(in.value());
        }
    }
}
