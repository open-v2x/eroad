package cn.eroad.rad.util;

import cn.eroad.core.utils.StringUtil;
import cn.eroad.rad.config.DataConfig;
import cn.eroad.rad.model.ConfData;
import cn.eroad.rad.model.Network;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class SendMessageEncode {
    private static final String ipPatternStr = "([0-9]+).([0-9]+).([0-9]+).([0-9]+)";
    private static final Pattern ipPattern = Pattern.compile(ipPatternStr);
    private static final String macPatternStr = "([A-Za-z0-9]{2}):([A-Za-z0-9]{2}):([A-Za-z0-9]{2}):([A-Za-z0-9]{2}):([A-Za-z0-9]{2}):([A-Za-z0-9]{2})";
    private static final Pattern macPattern = Pattern.compile(macPatternStr);

    private static String getDistrictCode() {
        String code = "";
        code = code + fromIntToString(DataConfig.Province, 1) + fromIntToString(DataConfig.city, 1) + fromIntToString(DataConfig.County, 1);
        return code;
    }

    // 获取公共头部信息，包括链路地址+发送方标识+接收方标识+协议版本
    private static String getCommonHeader(String sn) {
        // 接收方标识


        String receiveId = encodeSendAndRecieve(sn);
        String receive = getDistrictCode() + fromIntToString(7, 2) + receiveId;
        String header = "";
        String sendId = encodeSendAndRecieve("caikong");
        header = header + "0000" + getDistrictCode() + fromIntToString(DataConfig.ECAI_TYPE, 2) +
                sendId + receive + "10";
        return header;
    }

    // 4.3毫米波雷达网络参数查询
    public static String encodeNetworkDataQuery(String sn) {
        String parm = "";
        parm = parm + getCommonHeader(sn);
        System.out.println("commond" + parm);
        // 操作类型(80)	+对象标识(0206)
        parm = parm + DataConfig.operation_query_request + DataConfig.object_mmw_network_data;
        parm = completionParameters(parm);
        return parm;
    }


    // 4.3毫米波雷达网络参数设置
    public static String encodeNetworkDataSet(Network network, String sn) throws Exception {
        String parm = "";
        parm = parm + getCommonHeader(sn);
        // 操作类型(81)	+对象标识(0206)
        parm = parm + DataConfig.operation_set_request + DataConfig.object_mmw_network_data;
        // IPV4网关
        parm = parm + fromIpToHexString(network.getIpv4Gateway());
        // IPV4子网掩码
        parm = parm + fromIpToHexString(network.getIpv4Mask());
        // IPV4地址
        parm = parm + fromIpToHexString(network.getIpv4Address());
        // IPV6-网关
        parm = parm + fromIpV6ToHexString(network.getIpv6Gateway());
        // IPV6子网掩码
        parm = parm + fromIpV6ToHexString(network.getIpv6Mask());
        // IPV6-LLA地址
        parm = parm + fromIpV6ToHexString(network.getIpv6LlaAddress());
        // IPV6-GUA地址
        parm = parm + fromIpV6ToHexString(network.getIpv6GuaAddress());
        // 本地端口号
        parm = parm + fromIntToString(network.getLocalPort(), 2);
        // 目标端口号
        parm = parm + fromIntToString(network.getTargetPort(), 2);
        // 目标ip
        parm = parm + fromIpToHexString(network.getTargetIp());
        // 点云数据上报端口号
        parm = parm + fromIntToString(network.getPcUpPort(), 2);
        //心跳周期
        parm = parm + fromIntToString(network.getHeartbeatCycle(), 2);
        Matcher matcher = macPattern.matcher(network.getMac());
        if (matcher.find()) {
            parm = parm + matcher.group(1) + matcher.group(2) + matcher.group(3) + matcher.group(4) + matcher.group(5) + matcher.group(6);
        } else {
            parm = parm + "000000000000";
        }
        parm = completionParameters(parm);
        return parm;
    }

    // 4.3毫米波雷达配置参数查询
    public static String encodeConfDataQuery(String sn) {
        String parm = "";
        parm = parm + getCommonHeader(sn);
        // 操作类型(80)	+对象标识(0204)
        parm = parm + DataConfig.operation_query_request + DataConfig.object_mmw_conf_data;
        parm = completionParameters(parm);
        return parm;
    }

    // 毫米波雷达配置参数设置
    public static String encodeConfDataSet(ConfData confData, String sn) throws Exception {
        String parm = "";
        parm = parm + getCommonHeader(sn);
        // 操作类型(81)	+对象标识(0206)
        parm = parm + DataConfig.operation_set_request + DataConfig.object_mmw_conf_data;

        // 交通目标 实时轨迹信息
        parm = parm + fromIntToString(confData.getTraceFre(), 1);
        //过车信息
        parm = parm + fromIntToString(confData.getPassFre(), 1);
        //交通状态实时信息
        parm = parm + fromIntToString(confData.getTrafficStatusFre(), 1);
        //交通状态周期信息,交通流
        parm = parm + fromIntToString(confData.getFlowFre(), 2);
        //异常交通事件信息
        parm = parm + fromIntToString(confData.getEventUp(), 1);
        for (int i = 0; i < 16; i++) {
            parm = parm + "00";
        }
        parm = completionParameters(parm);
        return parm;
    }

    // 毫米波雷达工作状态查询
    public static String encodeWorkingStatusQuery(String sn) {
        String parm = "";
        parm = parm + getCommonHeader(sn);
        // 操作类型(80)	+对象标识(0205)
        parm = parm + DataConfig.operation_query_request + DataConfig.object_mmw_Working_status_data;
        parm = completionParameters(parm);
        return parm;
    }

    // 注册回复
    public static String encodeRegisterRespond(String deviceId) {
        String parm = "";
        parm = parm + getCommonHeader(deviceId);
        // 操作类型(85)	+对象标识(0205)
        parm = parm + DataConfig.operation_initiative_report_answer + DataConfig.object_register_data;
        parm = parm + fromIntToString(0, 1);
        parm = completionParameters(parm);
        return parm;
    }


    // 心跳回复
    public static String encodeHeartRespond(String ip) {
        String parm = "";
        parm = parm + getCommonHeader(ip);
        // 操作类型(82)	+对象标识(0102)
        parm = parm + DataConfig.operation_initiative_report + DataConfig.object_heart;
        parm = parm + fromIntToString(0, 1);
        parm = completionParameters(parm);
        return parm;
    }

    // 恢复出厂设置
    public static String encodeReset(String sn) {
        String parm = "";
        parm = parm + getCommonHeader(sn);
        // 操作类型(87)	+对象标识(0207)
        parm = parm + DataConfig.operation_Maintenance_management_request + DataConfig.object_mmw_reset;
        parm = completionParameters(parm);
        return parm;
    }


    // 设备重启
    public static String encodeReboot(String sn) {
        String parm = "";
        parm = parm + getCommonHeader(sn);
        // 操作类型(87)	+对象标识(0208)
        parm = parm + DataConfig.operation_Maintenance_management_request + DataConfig.object_mmw_reboot;
        parm = completionParameters(parm);
        return parm;
    }


    private static String fromIpToHexString(String ip) {
        if (StringUtil.isEmpty(ip)) {
            return "00000000";
        }
        String result = "";
        Matcher matcher = ipPattern.matcher(ip);
        if (matcher.find()) {
            String one = matcher.group(1);
            String two = matcher.group(2);
            String three = matcher.group(3);
            String four = matcher.group(4);
            result = fromIntToString(Integer.valueOf(one), 1) + fromIntToString(Integer.valueOf(two), 1) + fromIntToString(Integer.valueOf(three), 1) + fromIntToString(Integer.valueOf(four), 1);
        } else {
            result = "00000000";
        }
        return result;

    }


    private static String fromIpV6ToHexString(String ip) {
        if (StringUtil.isEmpty(ip)) {
            return "00000000000000000000000000000000";
        }
        String result = ip.replaceAll(":", "").trim();
        return result;
    }

    // 补全参数
    private static String completionParameters(String parm) {
        byte[] bytes = parm.getBytes();
        int checkCode = Integer.valueOf(CRC16.getCRC16Result(parm), 16);
        parm = parm + fromIntToString(checkCode, 2);
        parm = replace(parm);
        parm = "C0" + parm + "C0";
        return parm;

    }


    private static String fromIntToString(int parm, int index) {

        Integer a = Integer.valueOf(parm);
        String result = Integer.toHexString(a);
        Integer count = index * 2 - result.length();
        if (result.length() < index * 2) {
            for (int i = 0; i < count; i++) {
                result = "0" + result;
            }
        }
        return fromBigToSmall(result).toUpperCase();
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


    // 替换DB和DC的值
    private static String replace(String msg) {
        if (msg.contains("DB") || msg.contains("C0")) {
            Integer index = 0;
            String result = "";
            while (index <= msg.length() - 2) {
                String tempMsg = msg.substring(index, index + 2);

                if (tempMsg.equals("DB")) {

                    result = result + "DBDD";
                    index = index + 2;
                } else if (tempMsg.equals("C0")) {
                    result = result + "DBDC";
                    index = index + 2;
                } else {
                    result = result + tempMsg;
                    index = index + 2;
                }
            }
            return result;
        } else {
            return msg;
        }
    }


    public static String encodeSendAndRecieve(String msg) {
        if (StringUtil.isNotEmpty(msg)) {
            String result = ByteUtil.strTo16(msg);
            Integer fix = 32 - result.length();
            if (result.length() < 32) {
                for (int i = 0; i < fix; i++) {
                    result = "0" + result;

                }
            }
            return result;

        } else {
            return "00000000000000000000000000000000";
        }
    }
}
