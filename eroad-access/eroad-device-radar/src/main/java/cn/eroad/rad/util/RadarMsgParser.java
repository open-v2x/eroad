package cn.eroad.rad.util;

import cn.eroad.rad.model.ParsedData;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;


public class RadarMsgParser {
    private static String FLAME_HEADER = "C0";
    private static String FLAME_END = "C0";
    private static org.slf4j.Logger log = LoggerFactory.getLogger(RadarMsgParser.class);


    public static ParsedData decodeMsgToJson(String msg, String ip) {
        if (!(msg.startsWith(FLAME_HEADER) && msg.endsWith(FLAME_END))) {
            log.error("{}设备上传数据有误，错误数据为{}", ip, msg);
            return null;
        }
        msg = msg.substring(msg.indexOf(FLAME_HEADER) + FLAME_HEADER.length(), msg.lastIndexOf(FLAME_END));
        msg = replace(msg);
        // 生成校验码的数据
        String checkCodeParm = msg.substring(0, msg.length() - 4);
        int checkCodeStandard = Integer.valueOf(CRC16.getCRC16Result(checkCodeParm), 16);
        // 校验码
        String checkCode = msg.substring(msg.length() - 4);
        String newcheckCode = fromBigToSmall(checkCode);
        int checkCodeReceive = Integer.valueOf(newcheckCode, 16);
        if (checkCodeStandard != checkCodeReceive) {
            log.error("{}数据校验有误，错误数据为{}", ip, msg);
            return null;
        }
        // 去除校验码
        msg = msg.substring(0, msg.lastIndexOf(checkCode));
        // 数据表部分
        // 链路地址：链路地址由2个字节组成，保留，取值0x0000
        msg = msg.substring(4);

        // 发送方标识：发送方唯一身份，长度7字节。编制规则为：行政区划代码3+类型2+编号2
        String deviceIdStr = msg.substring(10, 42);

        String deviceId = ByteUtil.decodeHexStr(deviceIdStr);
        deviceId = deviceId.replaceAll("\\u0000", "");
        deviceId = deviceId.replaceAll("\u0000", "");


        msg = msg.substring(42);
        // 接收方标识

        msg = msg.substring(42);

        msg = msg.substring(2);
        // 操作类型：标识数据表的操作类型，用1个字节表示,operationTypeStr保留String格式
        String operationTypeStr = msg.substring(0, 2);
        msg = msg.substring(2);
        // 对象标识：标识数据表的操作对象，用2个字节表示,objectIDStr保留String格式
        String objectIDStr = msg.substring(0, 4);
        // objectIDStr=fromBigToSmall(objectIDStr);命令号，不用反
        msg = msg.substring(4);

        return ParsedData.builder()
                .ip(ip)
                .realSn(deviceId)
                .msg(msg)
                .objectIDStr(objectIDStr)
                .operationTypeStr(operationTypeStr)
                .build();
    }

    private static String fromBigToSmall(String parm) {
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

    private static String replace(String msg) {
        if (msg.contains("DBDC") || msg.contains("DBDD")) {
            int index = 0;
            String result = "";
            while (index <= msg.length() - 4) {
                String tempMsg = msg.substring(index, index + 4);
                if (tempMsg.equals("DBDC")) {
                    result = result + "C0";
                    index = index + 4;
                    if (index == msg.length() - 2) {
                        result = result + msg.substring(index, index + 2);
                    }
                } else if (tempMsg.equals("DBDD")) {
                    result = result + "DB";
                    index = index + 4;
                    if (index == msg.length() - 2) {
                        result = result + msg.substring(index, index + 2);
                    }

                } else {
                    if (index == msg.length() - 4) {
                        result = result + tempMsg;
                    } else {
                        result = result + tempMsg.substring(0, 2);
                    }
                    index = index + 2;
                }
            }
            return result.trim();
        } else {
            return msg.trim();
        }
    }


}
