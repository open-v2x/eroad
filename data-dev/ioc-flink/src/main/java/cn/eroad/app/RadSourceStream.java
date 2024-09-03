package cn.eroad.app;

import cn.eroad.ConnProp;
import cn.eroad.app.ods.rad.rt.*;
import cn.eroad.impl.AbstractFlinkStreamingApp;
import cn.eroad.utils.KafkaUtil;
import cn.eroad.utils.TimeUtils;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.connector.kafka.source.reader.deserializer.KafkaRecordDeserializationSchema;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.KeyedCoProcessFunction;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.connectors.kafka.partitioner.FlinkFixedPartitioner;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.regex.Pattern;


public class RadSourceStream extends AbstractFlinkStreamingApp {
    private static final Logger LOG = LoggerFactory.getLogger(OdsStreamRad.class);

    @Override
    protected void processCode(StreamExecutionEnvironment env, String[] args) {
        OutputTag<String> errMsgTag = new OutputTag<String>("rad-err-msg") {
        };
        DataStreamSource<DeviceCrossInfo> mySQLSource = env.addSource(new MySQLSource());
        DataStreamSource<RtTrack> trackSource = env.fromSource(getKafkaSource("track", RtTrack.class), WatermarkStrategy.noWatermarks(), "trackSource");
        DataStreamSource<RtFlow> flowSource = env.fromSource(getKafkaSource("flow", RtFlow.class), WatermarkStrategy.noWatermarks(), "flowSource");
        DataStreamSource<RtEvent> eventSource = env.fromSource(getKafkaSource("event", RtEvent.class), WatermarkStrategy.noWatermarks(), "eventSource");
        DataStreamSource<RtCross> crossSource = env.fromSource(getKafkaSource("cross", RtCross.class), WatermarkStrategy.noWatermarks(), "crossSource");
        DataStreamSource<RtStatus> statusSource = env.fromSource(getKafkaSource("status", RtStatus.class), WatermarkStrategy.noWatermarks(), "statusSource");

        SingleOutputStreamOperator<RtTrack> trackMappedDS = trackSource
                .connect(mySQLSource)
                .keyBy(r -> r.deviceId, r -> r.deviceId)
                .process(new MatchProcess<>())
                .returns(RtTrack.class);
        trackMappedDS
                .keyBy(r -> r.deviceId)
                .sinkTo(getKafkaSink("caikong-rad-track-test"));

        SingleOutputStreamOperator<RtFlow> flowMappedDS = flowSource
                .connect(mySQLSource)
                .keyBy(r -> r.deviceId, r -> r.deviceId)
                .process(new MatchProcess<>())
                .returns(RtFlow.class);
        flowMappedDS
                .keyBy(r -> r.deviceId)
                .sinkTo(getKafkaSink("caikong-rad-flow-test"));

        SingleOutputStreamOperator<RtEvent> eventMappedDS = eventSource
                .connect(mySQLSource)
                .keyBy(r -> r.deviceId, r -> r.deviceId)
                .process(new MatchProcess<>())
                .returns(RtEvent.class);
        eventMappedDS
                .keyBy(r -> r.deviceId)
                .sinkTo(getKafkaSink("caikong-rad-event-test"));

        SingleOutputStreamOperator<RtCross> crossMappedDS = crossSource
                .connect(mySQLSource)
                .keyBy(r -> r.deviceId, r -> r.deviceId)
                .process(new MatchProcess<>())
                .returns(RtCross.class);
        crossMappedDS
                .keyBy(r -> r.deviceId)
                .sinkTo(getKafkaSink("caikong-rad-cross-test"));

        SingleOutputStreamOperator<RtStatus> statusMappedDS = statusSource
                .connect(mySQLSource)
                .keyBy(r -> r.deviceId, r -> r.deviceId)
                .process(new MatchProcess<>())
                .returns(RtStatus.class);
        statusMappedDS
                .keyBy(r -> r.deviceId)
                .sinkTo(getKafkaSink("caikong-rad-status-test"));

        trackMappedDS.getSideOutput(errMsgTag)
                .union(flowMappedDS.getSideOutput(errMsgTag))
                .union(eventMappedDS.getSideOutput(errMsgTag))
                .union(crossMappedDS.getSideOutput(errMsgTag))
                .union(statusMappedDS.getSideOutput(errMsgTag))
                .sinkTo(KafkaUtil.getStringSink(ConnProp.STORAGE_KAFKA_SERVER, KafkaUtil.ProduceMode.AT_LEAST_ONCE, "rad-err-msg"));

    }

    /*
     * UTF-8 charset
     * */
    private <T> KafkaSink<T> getKafkaSink(String topic) {
        return KafkaSink.<T>builder()
                .setBootstrapServers(ConnProp.STORAGE_KAFKA_SERVER)
                .setRecordSerializer(KafkaRecordSerializationSchema.<T>builder()
                        .setTopic(topic)
                        .setPartitioner(new FlinkFixedPartitioner<>())
                        .setValueSerializationSchema((SerializationSchema<T>) element -> JSON.toJSONString(element).getBytes(StandardCharsets.UTF_8))
                        .build()
                )
                .setDeliverGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
                .build();
    }

    @Override
    protected String appName() {
        return "ODS_Rad_Stream";
    }

    private <T extends RadData> KafkaSource<T> getKafkaSource(String serviceName, Class<T> tClass) {
        String groupID = "DataDev-RadSourceStream";
        //TODO here
        String patternStr = "caikong-rad-.*-" + serviceName;
        return KafkaSource.<T>builder()
                .setBootstrapServers(ConnProp.STORAGE_KAFKA_SERVER)
                .setTopicPattern(Pattern.compile(patternStr))
                .setGroupId(groupID)
                .setClientIdPrefix("DataDev-RadSourceStream-" + serviceName)
                .setStartingOffsets(OffsetsInitializer.latest())
                /*.setValueOnlyDeserializer(new MyDeserializationSchema<>(tClass))*/
                .setDeserializer(new KafkaRecordDeserializationSchema<T>() {
                    @Override
                    public void deserialize(ConsumerRecord<byte[], byte[]> record, Collector<T> out) {
                        // caiKong's ts
                        long timestamp = record.timestamp();
                        T t = JSON.parseObject(record.value(), tClass);
                        t.epoch = timestamp;
                        out.collect(t);
                    }

                    @Override
                    public TypeInformation<T> getProducedType() {
                        return TypeInformation.of(tClass);
                    }
                })
                .setProperty("partition.discovery.interval.ms", String.valueOf(30 * 60 * 1000L))
                .build();
    }

    public static class MyDeserializationSchema<T> implements DeserializationSchema<T> {
        Class<T> tClass;

        public MyDeserializationSchema(Class<T> tClass) {
            this.tClass = tClass;
        }

        // support UTF-8
        @Override
        public T deserialize(byte[] message) {
            return JSON.parseObject(message, tClass);
        }

        @Override
        public boolean isEndOfStream(T nextElement) {
            return false;
        }

        @Override
        public TypeInformation<T> getProducedType() {
            return TypeInformation.of(tClass);
        }
    }

    public static class MySQLSource implements SourceFunction<DeviceCrossInfo> {
        private boolean running = true;

        @Override
        public void run(SourceContext<DeviceCrossInfo> ctx) throws Exception {
            String url = ConnProp.IOC_MYSQL_URL;
            String sql = "select * from `dim_rad_rd_device_crossroad`";

            while (running) {
                Connection conn = null;
                PreparedStatement ps = null;
                try {
                    //数据库连接配置
                    conn = null;
                    ps = conn.prepareStatement(sql);
                } catch (Exception e) {
                    LOG.error("can't connect to {}", url);
                }

                if (conn == null || ps == null)
                    continue;

                ResultSet resultSet = ps.executeQuery();
                while (resultSet.next()) {
                    ctx.collect(new DeviceCrossInfo(resultSet));
                }
                ps.close();
                conn.close();

                Thread.sleep(600000);
            }
        }

        @Override
        public void cancel() {
            running = false;
        }

    }


    public static class MatchProcess<Z extends RadData> extends KeyedCoProcessFunction<String, Z, DeviceCrossInfo, Z> {

        ValueState<DeviceCrossInfo> deviceCrossInfoValueState;
        OutputTag<String> errMsgTag = new OutputTag<String>("rad-err-msg") {
        };

        @Override
        public void open(Configuration parameters) throws Exception {
            deviceCrossInfoValueState = getRuntimeContext().getState(new ValueStateDescriptor<>("deviceCrossInfoValueState", Types.POJO(DeviceCrossInfo.class)));
        }

        @Override
        public void processElement1(Z value, KeyedCoProcessFunction<String, Z, DeviceCrossInfo, Z>.Context ctx, Collector<Z> out) throws Exception {
            if (deviceCrossInfoValueState.value() == null)
                deviceCrossInfoValueState.update(new DeviceCrossInfo());
            DeviceCrossInfo deviceCrossInfo = deviceCrossInfoValueState.value();
            value.setCrossInfo(deviceCrossInfo);
            try {
                value.reportTime = TimeUtils.convertToString(Long.parseLong(value.timeStamp), TimeUtils.DATE_TIME_FORMATTER_WITH_MILE);
            } catch (Exception e) {
                value.reportTime = TimeUtils.convertToString(0L, TimeUtils.DATE_TIME_FORMATTER_WITH_MILE);
                JSONObject jsonObject = JSON.parseObject(value.toString());
                jsonObject.put("dataType", value.getClass().getSimpleName());
                jsonObject.put("nullFiledList", "timeStamp");
                ctx.output(errMsgTag, jsonObject.toJSONString());
            }
            value.timeStamp = TimeUtils.convertToString(value.epoch, TimeUtils.DATE_TIME_FORMATTER_WITH_SECOND);
            if (value.isErrFormat()) {
                JSONObject jsonObject = JSON.parseObject(value.toString());
                jsonObject.put("dataType", value.getClass().getSimpleName());
                jsonObject.put("nullFiledList", value.getNullFiledNameList());
                ctx.output(errMsgTag, jsonObject.toJSONString());
                return;
            }
            out.collect(value);
        }

        @Override
        public void processElement2(DeviceCrossInfo value, KeyedCoProcessFunction<String, Z, DeviceCrossInfo, Z>.Context ctx, Collector<Z> out) throws Exception {
            deviceCrossInfoValueState.update(value);
        }
    }
}
