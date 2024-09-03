package cn.eroad.rad.config;

public class DataConfig {
    // 下发消息时，编码
    // 行政区编码
    public static final Integer Province = 112;
    public static final Integer city = 112;
    public static final Integer County = 112;
    // 采控平台类型
    public static final Integer ECAI_TYPE = 112;
    // 采控平台编码
    public static final Integer ECAI_CODE = 112;

    // 操作类型
    // 查询请求
    public static final String operation_query_request = "80";
    // 设置请求
    public static final String operation_set_request = "81";
    // 主动上传
    public static final String operation_initiative_report = "82";
    // 查询应答
    public static final String operation_query_answer = "83";
    // 设置应答
    public static final String operation_set_answer = "84";
    // 主动上传应答
    public static final String operation_initiative_report_answer = "85";
    // 出错应答,接收到的数据包存在错误
    public static final String operation_error_answer = "86";
    // 维护管理发送,发送方向设备发送维护管理消息，恢复出厂设置请求
    public static final String operation_Maintenance_management_request = "87";
    //维护管理请求,设备向发送方应答维护管理消息，恢复出厂设置应答
    public static final String operation_Maintenance_management_report = "88";


    // 对象标识：标识数据表的操作对象
    // 通信链路监测
    // 通信连接,描述道路交通信号控制机与车辆毫米波雷达间通信链路的建立与维护，如：连接请求、连接请求应答、心跳上传等
    public static final String object_Communication_connection = "0101";

    // 设备管理

    // 毫米波雷达配置参数
    public static final String object_mmw_conf_data = "0204";
    public static final String type_mmw_conf_data = "confData";
    // 毫米波雷达工作状态
    public static final String object_mmw_Working_status_data = "0205";
    public static final String type_mmw_Working_status_data = "workingStatusData";
    // 毫米波雷达网络参数
    public static final String object_mmw_network_data = "0206";
    public static final String type_mmw_network_data = "networkData";
    //设备恢复出厂设置
    public static final String object_mmw_reset = "0207";
    //设备重启
    public static final String object_mmw_reboot = "0208";

    // 检测数据
    //交通目标轨迹信息
    public static final String object_target_Trajectory_data = "0301";
    public static final String type_target_Trajectory_data = "targetTrajectoryData";
    //检测断面过车信息
    public static final String object_Detection_section_passing_information = "0302";
    public static final String type_Detection_section_passing_information = "passingInformation";

    //交通状态信息
    public static final String object_Traffic_status_information = "0303";
    public static final String type_Traffic_status_information = "trafficStatusInformation";

    //交通流信息
    public static final String object_Traffic_flow_information = "0304";
    public static final String type_Traffic_flow_information = "trafficFlowInformation";
    //异常事件信息
    public static final String object_Abnormal_event_information = "0305";
    public static final String type_Abnormal_event_information = "event";

    //点云数据
    public static final String object_Point_cloud_data = "0306";
    public static final String type_Point_cloud_data = "pointCloudData";

    // 设备注册
    public static final String object_register_data = "0101";
    public static final String type_register_data = "registerData";

    // 心跳
    public static final String object_heart = "0102";
    public static final String type_heart = "heart";


}

