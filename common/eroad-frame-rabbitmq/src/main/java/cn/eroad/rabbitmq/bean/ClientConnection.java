package cn.eroad.rabbitmq.bean;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

/**
 * @project: eroad-frame
 * @ClassName: ClientConnection
 * @author: liyongqiang
 * @creat: 2022/5/31 11:09
 * 描述:
 */
@Data
@NoArgsConstructor
@ToString
public class ClientConnection {

    private String clientAddress; //客户端地址
    private String clientId; //客户端Id
    private Date connectedTime; //连上时间
    private boolean isRunning; //是否在工作状态
    private ClientType clientType;
    private String protocol;

    @Data
    @NoArgsConstructor
    public static class Bindings {


        private String source;
        private String vhost;
        private String destination;
        private String destination_type;
        private String routing_key;
        private Arguments arguments;
        private String properties_key;

        @NoArgsConstructor
        @Data
        public static class Arguments {
        }
    }

    public static enum ClientType {
        //RabbitMQ类型
        RABBIT_MQ,
        //MQTT类型
        MQTT
    }

    @NoArgsConstructor
    @Data
    public static class Connections {


        private int total_count;
        private int item_count;
        private int filtered_count;
        private int page;
        private int page_size;
        private int page_count;
        private List<Items> items;

        @NoArgsConstructor
        @Data
        public static class Items {
            private String auth_mechanism;
            private int channel_max;
            private int channels;
            private ClientProperties client_properties;
            private long connected_at;
            private int frame_max;
            private GarbageCollection garbage_collection;
            private String host;
            private String name;
            private String node;
            private Object peer_cert_issuer;
            private Object peer_cert_subject;
            private Object peer_cert_validity;
            private String peer_host;
            private long peer_port;
            private long port;
            private String protocol;
            private long recv_cnt;
            private long recv_oct;
            private RecvOctDetails recv_oct_details;
            private long reductions;
            private ReductionsDetails reductions_details;
            private long send_cnt;
            private long send_oct;
            private SendOctDetails send_oct_details;
            private int send_pend;
            private boolean ssl;
            private Object ssl_cipher;
            private Object ssl_hash;
            private Object ssl_key_exchange;
            private Object ssl_protocol;
            private String state;
            private int timeout;
            private String type;
            private String user;
            private String user_provided_name;
            private String user_who_performed_action;
            private String vhost;
            private VariableMap variable_map;

            public String getClientAddress() {
                return peer_host + ":" + peer_port;
            }

            /**
             * 是否MQTT客户端
             *
             * @return
             */
            public boolean isMqttClient() {
                return !StringUtils.isEmpty(client_properties.getProduct()) && client_properties.getProduct().startsWith("MQTT");
            }

            public ClientType getClientType() {
                if (isMqttClient()) {
                    return ClientType.MQTT;
                }
                return ClientType.valueOf(client_properties.getProduct());
            }

            public String getClientId() {
                return variable_map != null ? variable_map.getClient_id() : null;
            }

            public boolean isRunning() {
                return "running".equals(state);
            }

            @NoArgsConstructor
            @Data
            public static class ClientProperties {
                private Capabilities capabilities;
                private String connection_name;
                private String copyright;
                private String information;
                private String platform;
                private String product;
                private String version;

                @NoArgsConstructor
                @Data
                public static class Capabilities {
                    private boolean authentication_failure_close;
                    //                    private boolean "basicNack;// FIXME check this code

                    private boolean consumer_cancel_notify;
                    private boolean exchange_exchange_bindings;
                    private boolean publisher_confirms;
                }
            }

            @NoArgsConstructor
            @Data
            public static class GarbageCollection {
                private int fullsweep_after;
                private int max_heap_size;
                private int min_bin_vheap_size;
                private int min_heap_size;
                private int minor_gcs;
            }

            @NoArgsConstructor
            @Data
            public static class RecvOctDetails {
                private double rate;
            }

            @NoArgsConstructor
            @Data
            public static class ReductionsDetails {
                private double rate;
            }

            @NoArgsConstructor
            @Data
            public static class SendOctDetails {
                private double rate;
            }

            @NoArgsConstructor
            @Data
            public static class VariableMap {
                private String client_id;
            }
        }
    }

}
