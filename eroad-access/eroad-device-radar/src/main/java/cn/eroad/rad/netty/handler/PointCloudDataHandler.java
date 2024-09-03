package cn.eroad.rad.netty.handler;

import cn.eroad.rad.model.ParsedData;
import cn.eroad.rad.service.EroadKafkaProducer;
import cn.eroad.rad.util.ByteUtil;
import cn.eroad.rad.util.RadarMsgParser;
import cn.eroad.rad.util.TopicUtil;
import cn.eroad.device.operate.DeviceCache;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author mrChen
 * @date 2022/7/7 11:30
 */
@Component("pointCloudDataHandler")
@Slf4j
public class PointCloudDataHandler extends SimpleChannelInboundHandler<DatagramPacket> {
    private static ThreadPoolExecutor pool = new ThreadPoolExecutor(
            32,
            32 * 2,
            1000L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(50000),
            new ThreadFactory() {
                AtomicLong i = new AtomicLong();

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "push-kafka-thread-" + i.getAndIncrement());
                }
            },
            new ThreadPoolExecutor.AbortPolicy());

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, DatagramPacket packet) {
        ByteBuf byteBuf = packet.content();
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);
        String body = ByteUtil.byteToHexString(bytes).toUpperCase();
        String ip = packet.sender().getAddress().getHostAddress().replace("%0", "");
        String sn = DeviceCache.getSnByIpFromMap(ip);
        ParsedData parsedData = RadarMsgParser.decodeMsgToJson(body, ip);
        if (parsedData == null) {
            return;
        }
        String msg = parsedData.getMsg();
        JSONObject dataObject = new JSONObject();
        String timeStamp = getTime(msg);
        dataObject.put("timeStamp", timeStamp);
        String numberStr = msg.substring(16, 20);
        numberStr = fromBigToSmall(numberStr);

        //点云个数
        int number = Integer.valueOf(numberStr, 16);
        dataObject.put("num", number);
        JSONArray ja = new JSONArray();
        msg = msg.substring(20);
        for (int i = 0; i < number; i++) {
            JSONObject pointJs = new JSONObject();
            String targetIdStr = msg.substring(0, 4);
            targetIdStr = fromBigToSmall(targetIdStr);
            Integer targetId = Integer.valueOf(targetIdStr, 16);
            pointJs.put("targetId", targetId);
            String transverseDistanceStr = msg.substring(4, 8);
            transverseDistanceStr = fromBigToSmall(transverseDistanceStr);
            double transverseDistance = Integer.valueOf(transverseDistanceStr, 16) * 0.1;
            transverseDistance = new BigDecimal(transverseDistance).setScale(1, RoundingMode.HALF_UP).doubleValue();
            pointJs.put("transverseDistance", transverseDistance);
            String longitudinalDistanceStr = msg.substring(8, 12);
            longitudinalDistanceStr = fromBigToSmall(longitudinalDistanceStr);
            double longitudinalDistance = Integer.valueOf(longitudinalDistanceStr, 16) * 0.1;
            longitudinalDistance = new BigDecimal(longitudinalDistance).setScale(1, RoundingMode.HALF_UP).doubleValue();
            pointJs.put("longitudinalDistance", longitudinalDistance);
            String transverseSpeedStr = msg.substring(12, 16);
            transverseSpeedStr = fromBigToSmall(transverseSpeedStr);
            double transverseSpeed = Integer.valueOf(transverseSpeedStr, 16) * 0.1;
            transverseSpeed = new BigDecimal(transverseSpeed).setScale(1, RoundingMode.HALF_UP).doubleValue();
            pointJs.put("transverseSpeed", transverseSpeed);
            String longitudinalSpeedStr = msg.substring(16, 20);
            longitudinalSpeedStr = fromBigToSmall(longitudinalSpeedStr);
            double longitudinalSpeed = Integer.valueOf(longitudinalSpeedStr, 16) * 0.1;
            longitudinalSpeed = new BigDecimal(longitudinalSpeed).setScale(1, RoundingMode.HALF_UP).doubleValue();
            pointJs.put("longitudinalSpeed", longitudinalSpeed);
            String angleStr = msg.substring(20, 24);
            angleStr = fromBigToSmall(angleStr);
            double angle = Integer.valueOf(angleStr, 16) * 0.01;
            angle = new BigDecimal(angle).setScale(2, RoundingMode.HALF_UP).doubleValue();
            pointJs.put("angle", angle);
            String signalNoiseRatioStr = msg.substring(24, 26);
            signalNoiseRatioStr = fromBigToSmall(signalNoiseRatioStr);
            Integer signalNoiseRatio = Integer.valueOf(signalNoiseRatioStr, 16);
            pointJs.put("signalNoiseRatio", signalNoiseRatio);
            ja.add(pointJs);
            msg = msg.substring(26);
        }
        dataObject.put("pointCloud", ja);

        String topic = TopicUtil.getTopic(TopicUtil.DeviceEnum.RAD, sn, TopicUtil.ServiceDataEnum.RAD_POINT);
        pool.execute(new Runnable() {
            @Override
            @Async
            public void run() {
                EroadKafkaProducer.send(dataObject.toJSONString(), topic);
            }
        });
    }

    protected static String getTime(String msg) {
        String timeSecondStr = msg.substring(0, 8);
        timeSecondStr = fromBigToSmall(timeSecondStr);
        Long timeSecond = Long.valueOf(timeSecondStr, 16);
        String timeMillStr = msg.substring(8, 16);
        timeMillStr = fromBigToSmall(timeMillStr);
        Long timeMill = Long.valueOf(timeMillStr, 16);
        Long timeLong = timeSecond * 1000 + timeMill;
        return String.valueOf(timeLong);
    }

    protected static String fromBigToSmall(String parm) {
        byte[] bytes = parm.getBytes(StandardCharsets.UTF_8);
        byte temporary;
        for (int i = 0; i < bytes.length / 2; i += 2) {
            temporary = bytes[bytes.length - i - 2];
            bytes[bytes.length - i - 2] = bytes[i];
            bytes[i] = temporary;
            temporary = bytes[bytes.length - i - 1];
            bytes[bytes.length - i - 1] = bytes[i + 1];
            bytes[i + 1] = temporary;
        }
        return new String(bytes);
    }

}
