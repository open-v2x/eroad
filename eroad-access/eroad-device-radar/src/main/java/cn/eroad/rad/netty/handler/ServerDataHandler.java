package cn.eroad.rad.netty.handler;

import cn.eroad.rad.config.DataConfig;
import cn.eroad.rad.model.ParsedData;
import cn.eroad.rad.schedule.HeartbeatSchedule;
import cn.eroad.rad.service.OperationTypeHandler;
import cn.eroad.rad.util.ByteUtil;
import cn.eroad.rad.util.RadarMsgParser;
import cn.eroad.device.operate.DeviceCache;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 业务处理
 *
 * @author mrChen
 * @date 2022/7/7 9:02
 */
@Slf4j
@Component("serverDataHandler")
public class ServerDataHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    @Autowired
    private OperationTypeHandler activeReportHandler;
    @Autowired
    private OperationTypeHandler maintenanceManagementReportHandler;
    @Autowired
    private OperationTypeHandler queryAnswerHandler;
    @Autowired
    private OperationTypeHandler setAnswerHandler;

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, DatagramPacket packet) {
        ByteBuf byteBuf = packet.content();
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);
        String body = ByteUtil.byteToHexString(bytes).toUpperCase();
        String ip = packet.sender().getAddress().getHostAddress().replace("%0", "");
        try {
            ParsedData parsedData = RadarMsgParser.decodeMsgToJson(body, ip);
            if (parsedData == null) {
                return;
            }
            if (DeviceCache.getSnByIpFromMap(ip) == null && !parsedData.getObjectIDStr().equals(DataConfig.object_register_data)) {


                log.error("设备：{} 没有初始化成功", ip);
                return;
            }
            parsedData.setPort(packet.sender().getPort());
            parsedData.setChannelHandlerContext(channelHandlerContext);
            parsedData.setPacket(packet);

            switch (parsedData.getOperationTypeStr()) {
                case DataConfig.operation_initiative_report:
                    activeReportHandler.handler(parsedData);
                    break;
                case DataConfig.operation_Maintenance_management_report:
                    maintenanceManagementReportHandler.handler(parsedData);
                    break;
                case DataConfig.operation_query_answer:
                    queryAnswerHandler.handler(parsedData);
                    break;
                case DataConfig.operation_set_answer:
                    setAnswerHandler.handler(parsedData);
                    break;
                default:
                    String sn = DeviceCache.getSnByIpFromMap(ip);
                    HeartbeatSchedule.otherSn.add(sn);
                    log.info("收到{}其他上报消息 {}，原始消息是：{}", ip, parsedData.getOperationTypeStr(), body);
                    break;
            }
        } catch (Exception e) {
            log.error("处理数据发生异常：", e);
            e.printStackTrace();
        }
    }
}
