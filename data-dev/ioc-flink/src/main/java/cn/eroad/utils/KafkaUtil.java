package cn.eroad.utils;

import cn.eroad.ConnProp;
import com.alibaba.fastjson2.JSON;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.connectors.kafka.partitioner.FlinkFixedPartitioner;


public final class KafkaUtil {
    /*  If the client id prefix is not set, reuse the consumer group id as the client id prefix, 如果使用多个 */
    public static KafkaSource<String> getStringSource(String bootStrap, String groupId, ConsumeMode mode, String... topics) {
        OffsetsInitializer offsetsInitializer = null;
        switch (mode) {
            case LATEST:
                offsetsInitializer = OffsetsInitializer.latest();
                break;
            case EARLIEST:
                offsetsInitializer = OffsetsInitializer.earliest();
                break;
            case OFFSET:
                offsetsInitializer = OffsetsInitializer.committedOffsets();
                break;
            default:
        }
        return KafkaSource.<String>builder()
                .setBootstrapServers(bootStrap)
                .setGroupId(groupId)
                .setValueOnlyDeserializer(new SimpleStringSchema())
                
                .setProperty("enable.auto.commit", "true")
                .setProperty("auto.commit.interval.ms", "30000")
                .setStartingOffsets(offsetsInitializer)
                .setTopics(topics)
                .build();
    }

    public static KafkaSink<String> getStringSink(String bootStrap, ProduceMode mode, String topic) {
        DeliveryGuarantee deliveryGuarantee = null;
        switch (mode) {
            case AT_LEAST_ONCE:
                deliveryGuarantee = DeliveryGuarantee.AT_LEAST_ONCE;
                break;
            case EXACTLY_ONCE:
                deliveryGuarantee = DeliveryGuarantee.EXACTLY_ONCE;
                break;
            default:
        }

        return KafkaSink.<String>builder()
                .setBootstrapServers(bootStrap)
                .setRecordSerializer(KafkaRecordSerializationSchema.<String>builder()
                        .setTopic(topic)
                        .setValueSerializationSchema(new SimpleStringSchema())
                        .setPartitioner(new FlinkFixedPartitioner<>())
                        .build()
                )
                .setDeliverGuarantee(deliveryGuarantee)
                .build();
    }

    public enum ConsumeMode {
        EARLIEST,
        LATEST,
        OFFSET
    }

    public enum ProduceMode {
        AT_LEAST_ONCE,
        EXACTLY_ONCE
    }

    /*
     * 获取雷达类型的kafka数据源，如Track等
     * */
    private static <T> KafkaSource<T> getRadDataSource(Class<T> tClass, String bootStrapServers, String groupId, String... topics) {
        return KafkaSource.<T>builder()
                .setBootstrapServers(bootStrapServers)
                .setGroupId(groupId)
                .setValueOnlyDeserializer(new RadDataDeserializer<T>(tClass))


                
                .setStartingOffsets(OffsetsInitializer.latest())
                .setTopics(topics)
                .build();
    }

    public static <T> KafkaSource<T> getRadDataSource(Class<T> tClass, String groupId, String... topics) {
        return getRadDataSource(tClass, ConnProp.STORAGE_KAFKA_SERVER, groupId, topics);
    }

    public static <T> KafkaSource<T> getRadDataSourceFromTs(Class<T> tClass, String bootStrapServers, String groupId,Long ts, String... topics) {
        return KafkaSource.<T>builder()
                .setBootstrapServers(bootStrapServers)
                .setGroupId(groupId)
                .setValueOnlyDeserializer(new RadDataDeserializer<T>(tClass))
                .setProperty("enable.auto.commit", "true")
                .setProperty("auto.commit.interval.ms", "30000")
                
                .setStartingOffsets(OffsetsInitializer.timestamp(ts))
                .setTopics(topics)
                .build();
    }

    /*
     * 直接获取Pojo类的Sink
     * */
    public static <T> KafkaSink<T> getKafkaSink(String bootStrap, String topic) {
        return KafkaSink.<T>builder()
                .setBootstrapServers(bootStrap)
                .setRecordSerializer(KafkaRecordSerializationSchema.<T>builder()
                        .setTopic(topic)
                        .setValueSerializationSchema((SerializationSchema<T>) JSON::toJSONBytes)
                        .setPartitioner(new FlinkFixedPartitioner<>())
                        .build()
                )
                .build();
    }

    public static <T> KafkaSink<T> getKafkaSink(String topic) {
        return getKafkaSink(ConnProp.STORAGE_KAFKA_SERVER, topic);
    }


    private KafkaUtil() {
    }
}
