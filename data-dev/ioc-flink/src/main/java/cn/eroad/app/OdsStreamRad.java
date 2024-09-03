package cn.eroad.app;

import cn.eroad.ConnProp;
import cn.eroad.impl.AbstractFlinkStreamingApp;
import cn.eroad.utils.DateTimeUtils;
import cn.eroad.utils.KafkaUtil;
import com.alibaba.fastjson2.JSONObject;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
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
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.co.KeyedCoProcessFunction;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.connectors.kafka.partitioner.FlinkFixedPartitioner;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class OdsStreamRad extends AbstractFlinkStreamingApp {
    private static final Logger LOG = LoggerFactory.getLogger(OdsStreamRad.class);
    private final String kafkaServer = ConnProp.STORAGE_KAFKA_SERVER;

    @Override
    protected String appName() {
        return "ODS-CAIKONG-RAD-RT";
    }

    @Override
    protected void restartConfig(StreamExecutionEnvironment env) {
        env.setRestartStrategy(RestartStrategies.failureRateRestart(
                4,
                Time.of(3, TimeUnit.MINUTES),
                Time.of(60, TimeUnit.SECONDS)
        ));
    }

    @Override
    protected void processCode(StreamExecutionEnvironment env, String[] args) {
        DataStreamSource<CrossRoad> matchDS = env.addSource(new DimSource()).setParallelism(1);

        OutputTag<String> errMsgTag = new OutputTag<String>("err-msg-out") {
        };
        OutputTag<String> trackMsgTag = new OutputTag<String>("track-msg-out") {
        };
        OutputTag<String> flowMsgTag = new OutputTag<String>("flow-msg-out") {
        };
        OutputTag<String> eventMsgTag = new OutputTag<String>("event-msg-out") {
        };
        OutputTag<String> statusMsgTag = new OutputTag<String>("status-msg-out") {
        };
        OutputTag<String> crossMsgTag = new OutputTag<String>("cross-msg-out") {
        };


        SingleOutputStreamOperator<String> resultDS = env
                .fromSource(getKafkaSource(), WatermarkStrategy.noWatermarks(), "rad-source")
                .setParallelism(1)
                .keyBy(ConsumerRecord::topic)
                .process(new AddDeviceInfo())
                .name("match device info")
                .connect(matchDS.setParallelism(1).name("get cross info"))
                .keyBy(r -> r.f0, r -> r.device_id)
                .process(new MatchCross())
                .name("match cross info and replace ts")
                .keyBy(r -> r.f0)
                .process(new DataCleanProcess())
                .name("data cleaning")
                .keyBy(r -> r.f0)
                .process(new KeyedProcessFunction<String, Tuple2<String, String>, String>() {
                    @Override
                    public void processElement(Tuple2<String, String> value, KeyedProcessFunction<String, Tuple2<String, String>, String>.Context ctx, Collector<String> out) throws Exception {
                        switch (value.f0) {
                            case "track":
                                ctx.output(trackMsgTag, value.f1);
                                break;
                            case "cross":
                                ctx.output(crossMsgTag, value.f1);
                                break;
                            case "status":
                                ctx.output(statusMsgTag, value.f1);
                                break;
                            case "flow":
                                ctx.output(flowMsgTag, value.f1);
                                break;
                            case "event":
                                ctx.output(eventMsgTag, value.f1);
                                break;
                            default:
                        }
                    }
                }).name("choose outputTag");


        resultDS
                .getSideOutput(errMsgTag)
                .sinkTo(KafkaUtil.getStringSink(kafkaServer, KafkaUtil.ProduceMode.AT_LEAST_ONCE, "err-rad-msg"));
        resultDS
                .getSideOutput(trackMsgTag)
                .sinkTo(getKafkaSink("caikong-rad-track"))
                .setParallelism(4)
                .name("sink to caikong-rad-track");
        resultDS
                .getSideOutput(flowMsgTag)
                .sinkTo(getKafkaSink("caikong-rad-flow"))
                .setParallelism(1)
                .name("sink to caikong-rad-flow");
        resultDS
                .getSideOutput(crossMsgTag)
                .sinkTo(getKafkaSink("caikong-rad-cross"))
                .setParallelism(1)
                .name("sink to caikong-rad-cross");
        resultDS
                .getSideOutput(statusMsgTag)
                .sinkTo(getKafkaSink("caikong-rad-status"))
                .setParallelism(1)
                .name("sink to caikong-rad-status");
        resultDS
                .getSideOutput(eventMsgTag)
                .sinkTo(getKafkaSink("caikong-rad-event"))
                .setParallelism(1)
                .name("caikong-rad-event");
    }

    private static class DimSource implements SourceFunction<CrossRoad> {
        private boolean running = true;

        @Override
        public void run(SourceContext<CrossRoad> ctx) throws Exception {
            String url = ConnProp.IOC_MYSQL_URL;
            String sql = "select * from `dim-rad-rd-device-crossroad`";

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
                    ctx.collect(new CrossRoad(
                            resultSet.getString("device_id"),
                            resultSet.getString("lamp_id"),
                            resultSet.getString("lamp_longitude"),
                            resultSet.getString("lamp_latitude"),
                            resultSet.getString("cross_id"),
                            resultSet.getString("cross_name"),
                            resultSet.getString("cross_longitude"),
                            resultSet.getString("cross_latitude"),
                            resultSet.getString("supplier")
                    ));
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


    private static class AddDeviceInfo extends KeyedProcessFunction<String, ConsumerRecord<String, String>, Tuple2<String, String>> {

        ValueState<TopicName> TopicNamePojoValueState;
        OutputTag<String> errMsgTag = new OutputTag<String>("err-msg-out") {
        };

        @Override
        public void open(Configuration parameters) throws Exception {
            super.open(parameters);
            TopicNamePojoValueState = getRuntimeContext().getState(new ValueStateDescriptor<>("caikong_TopicNamePojo_01", Types.POJO(TopicName.class)));
        }

        @Override
        public void processElement(ConsumerRecord<String, String> value, KeyedProcessFunction<String, ConsumerRecord<String, String>, Tuple2<String, String>>.Context ctx, Collector<Tuple2<String, String>> out) throws Exception {
            if (TopicNamePojoValueState.value() == null) {
                try {
                    String[] topicValue = value.topic().split("-");
                    TopicNamePojoValueState.update(new TopicName(topicValue[0], topicValue[1], topicValue[2], topicValue[3]));
                } catch (Exception e) {
                    LOG.warn("topic解析出错，topicName:{}", value.topic());
                    return;
                }
            }

            TopicName TopicNamePojo = TopicNamePojoValueState.value();
            //放入topic信息device_id
            try {
                JSONObject jsonObject = JSONObject.parseObject(value.value());
                jsonObject.put("system", TopicNamePojo.system);
                jsonObject.put("device_type", TopicNamePojo.device_type);
                jsonObject.put("device_id", TopicNamePojo.device_id);
                jsonObject.put("data_class", TopicNamePojo.data_class);
                out.collect(Tuple2.of(jsonObject.getString("device_id"), jsonObject.toString()));
            } catch (Exception e) {
                e.printStackTrace();
                JSONObject errMsg = new JSONObject();
                errMsg.put("errType", "json format err");
                errMsg.put("errMsg", " TOPIC is " + value.topic() + "MSG is " + value.value());
                errMsg.put("errDevice", "");
                ctx.output(errMsgTag, errMsg.toString());
            }

        }
    }

    private static class MatchCross extends KeyedCoProcessFunction<String, Tuple2<String, String>, CrossRoad, Tuple2<String, String>> {
        ValueState<CrossRoad> CrossRoadValueState;

        @Override
        public void open(Configuration parameters) throws Exception {
            CrossRoadValueState = getRuntimeContext().getState(new ValueStateDescriptor<>("caikong_cross_detail_01", CrossRoad.class));
        }

        @Override
        public void processElement1(Tuple2<String, String> tuple2, KeyedCoProcessFunction<String, Tuple2<String, String>, CrossRoad, Tuple2<String, String>>.Context ctx, Collector<Tuple2<String, String>> out) throws Exception {
            if (CrossRoadValueState.value() == null)
                CrossRoadValueState.update(new CrossRoad());

            JSONObject jsonObject = JSONObject.parse(tuple2.f1);
            CrossRoad crossRoad = CrossRoadValueState.value();
            crossRoad.addToJson(jsonObject);

            long reportTimeStamp;
            String reportTime;
            try {
                reportTimeStamp = jsonObject.getLong("timeStamp");
                reportTime = DateTimeUtils.format(reportTimeStamp, DateTimeUtils.Formatter.MILES);
            } catch (Exception e) {
                reportTime = jsonObject.getString("timeStamp");
            }
            jsonObject.put("report_time", reportTime);
            jsonObject.remove("timeStamp");
            jsonObject.put("timeStamp", DateTimeUtils.format(ctx.timerService().currentProcessingTime(), DateTimeUtils.Formatter.SECOND));
            jsonObject.put("epoch", ctx.timerService().currentProcessingTime());
            out.collect(Tuple2.of(jsonObject.getString("data_class"), jsonObject.toJSONString()));
        }

        @Override
        public void processElement2(CrossRoad value, KeyedCoProcessFunction<String, Tuple2<String, String>, CrossRoad, Tuple2<String, String>>.Context ctx, Collector<Tuple2<String, String>> out) throws Exception {
            CrossRoadValueState.update(value);
        }
    }

    private static class DataCleanProcess extends KeyedProcessFunction<String, Tuple2<String, String>, Tuple2<String, String>> {
        final OutputTag<String> errMsgTag = new OutputTag<String>("err-msg-out") {
        };

        final String[] trackKeyArr = {"targets", "device_id"};
        final String[] flowKeyArr = {"number", "trafficFlows", "device_id", "steeringRatio"};
        final String[] statusKeyArr = {"number", "trafficStatusInformations", "device_id"};
        final String[] crossKeyArr = {"passingImformations", "device_id"};
        final String[] eventKeyArr = {"eventId", "targetId", "latitude", "scope", "eventType",
                "lane", "longitude", "height", "device_id"};


        @Override
        public void processElement(Tuple2<String, String> value, KeyedProcessFunction<String, Tuple2<String, String>, Tuple2<String, String>>.Context ctx, Collector<Tuple2<String, String>> out) throws Exception {
            JSONObject valueJson = JSONObject.parseObject(value.f1);

            if ("flow".equals(value.f0) && isErrFlowData(valueJson)) {
                ctx.output(errMsgTag, mkErrMsg(valueJson, "format-flow"));
            } else if ("status".equals(value.f0) && isErrStatusData(valueJson)) {
                ctx.output(errMsgTag, mkErrMsg(valueJson, "format-status"));
            } else if ("cross".equals(value.f0) && isErrCrossData(valueJson)) {
                ctx.output(errMsgTag, mkErrMsg(valueJson, "format-cross"));
            } else if ("event".equals(value.f0) && isErrEventData(valueJson)) {
                ctx.output(errMsgTag, mkErrMsg(valueJson, "format-event"));
            } else if ("track".equals(value.f0) && isErrTrackData(valueJson)) {
                ctx.output(errMsgTag, mkErrMsg(valueJson, "format-track"));
            } else {
                // 结构无问题
                out.collect(Tuple2.of(valueJson.getString("data_class"), value.f1));
            }
        }

        private String mkErrMsg(JSONObject value, String errType) {
            String deviceId = value.getString("device_id");
            JSONObject errMsg = new JSONObject();
            errMsg.put("errType", errType);
            errMsg.put("errMsg", value);
            errMsg.put("errDevice", deviceId == null ? "" : deviceId);
            return errMsg + "";
        }

        private boolean isErrFlowData(JSONObject value) {
            for (String s : flowKeyArr) {
                if (!value.containsKey(s))
                    return true;
            }
            return false;
        }

        private boolean isErrStatusData(JSONObject value) {
            for (String s : statusKeyArr) {
                if (!value.containsKey(s))
                    return true;
            }
            return false;
        }

        private boolean isErrCrossData(JSONObject value) {
            for (String s : crossKeyArr) {
                if (!value.containsKey(s))
                    return true;
            }
            return false;
        }

        private boolean isErrEventData(JSONObject value) {
            for (String s : eventKeyArr) {
                if (!value.containsKey(s))
                    return true;
            }
            return false;
        }

        private boolean isErrTrackData(JSONObject value) {
            for (String s : trackKeyArr) {
                if (!value.containsKey(s))
                    return true;
            }
            return false;
        }

    }

    public static class CrossRoad {
        public String device_id;
        public String lamp_id;
        public String lamp_longitude;
        public String lamp_latitude;
        public String cross_id;
        public String cross_name;
        public String cross_longitude;
        public String cross_latitude;
        public String supplier;

        public CrossRoad() {
        }

        public CrossRoad(String device_id, String lamp_id, String lamp_longitude, String lamp_latitude, String cross_id, String cross_name, String cross_longitude, String cross_latitude, String supplier) {
            this.device_id = device_id;
            this.lamp_id = lamp_id;
            this.lamp_longitude = lamp_longitude;
            this.lamp_latitude = lamp_latitude;
            this.cross_id = cross_id;
            this.cross_name = cross_name;
            this.cross_longitude = cross_longitude;
            this.cross_latitude = cross_latitude;
            this.supplier = supplier;
        }

        public void addToJson(JSONObject jsonObject) {
            jsonObject.put("lamp_id", this.lamp_id);
            jsonObject.put("lamp_longitude", this.lamp_longitude);
            jsonObject.put("lamp_latitude", this.lamp_latitude);
            jsonObject.put("cross_id", this.cross_id);
            jsonObject.put("cross_name", this.cross_name);
            jsonObject.put("cross_longitude", this.cross_longitude);
            jsonObject.put("cross_latitude", this.cross_latitude);
            jsonObject.put("supplier", this.supplier);
        }

        @Override
        public String toString() {
            return "CrossRoad{" +
                    "device_id='" + device_id + '\'' +
                    ", lamp_id='" + lamp_id + '\'' +
                    ", lamp_longitude='" + lamp_longitude + '\'' +
                    ", lamp_latitude='" + lamp_latitude + '\'' +
                    ", cross_id='" + cross_id + '\'' +
                    ", cross_name='" + cross_name + '\'' +
                    ", cross_longitude='" + cross_longitude + '\'' +
                    ", cross_latitude='" + cross_latitude + '\'' +
                    ", supplier='" + supplier + '\'' +
                    '}';
        }
    }

    private KafkaSource<ConsumerRecord<String, String>> getKafkaSource() {
        String groupID = "DataDev-OdsStreamRad";
        //TODO here
        String patternStr = "caikong-rad-.*-.*";
        return KafkaSource.<ConsumerRecord<String, String>>builder()
                .setBootstrapServers(kafkaServer)
                .setTopicPattern(Pattern.compile(patternStr))
                .setGroupId(groupID)
                .setStartingOffsets(OffsetsInitializer.latest())
                .setProperty("partition.discovery.interval.ms", String.valueOf(60 * 60 * 1000L))
                //跳过错误
                .setProperty("errors.tolerance", "all")
                .setDeserializer(new KafkaRecordDeserializationSchema<ConsumerRecord<String, String>>() {
                    @Override
                    public void deserialize(ConsumerRecord<byte[], byte[]> record, Collector<ConsumerRecord<String, String>> out) {
                        out.collect(new ConsumerRecord<>(
                                record.topic(),
                                record.partition(),
                                record.offset(),
                                record.topic(),
                                new String(record.value())
                        ));
                    }

                    @Override
                    public TypeInformation<ConsumerRecord<String, String>> getProducedType() {
                        return TypeInformation.of(new TypeHint<ConsumerRecord<String, String>>() {
                        });
                    }
                })
                .build();
    }

    private KafkaSink<String> getKafkaSink(String topicName) {
        return KafkaSink.<String>builder()
                .setBootstrapServers(kafkaServer)
                .setRecordSerializer(KafkaRecordSerializationSchema.builder()
                        .setTopic(topicName)
                        .setValueSerializationSchema(new SimpleStringSchema())
                        .setPartitioner(new FlinkFixedPartitioner<>())
                        .build()
                )
                .setDeliverGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
                .build();
    }

    public static class TopicName {
        public String system = "";
        public String device_type = "";
        public String device_id = "";
        public String data_class = "";

        public TopicName(String system, String device_type, String device_id, String data_class) {
            this.system = system;
            this.device_type = device_type;
            this.device_id = device_id;
            this.data_class = data_class;
        }

        @Override
        public String toString() {
            return "TopicNamePOJO{" +
                    "system='" + system + '\'' +
                    ", device_type='" + device_type + '\'' +
                    ", device_id='" + device_id + '\'' +
                    ", data_class='" + data_class + '\'' +
                    '}';
        }
    }
}
