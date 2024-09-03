package cn.eroad.app;

import cn.eroad.ConnProp;
import cn.eroad.impl.AbstractFlinkStreamingApp;
import cn.eroad.utils.KafkaUtil;
import cn.eroad.utils.TimeUtils;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import com.google.common.collect.Iterables;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.functions.RichAggregateFunction;
import org.apache.flink.api.common.state.*;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.windowing.ProcessAllWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.SlidingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;


public class OnRoadTargetsCnt extends AbstractFlinkStreamingApp {

    private static final Logger LOG = LoggerFactory.getLogger(OnRoadTargetsCnt.class);

    @Override
    protected void checkPointConfig(StreamExecutionEnvironment env) {
        // generate a checkPoint each 30 min
        env.enableCheckpointing(1000 * 60 * 30).getCheckpointConfig().setCheckpointingMode(CheckpointingMode.AT_LEAST_ONCE);
    }

    @Override
    protected void processCode(StreamExecutionEnvironment env, String[] args) {


        KafkaSource<String> source = KafkaUtil.getStringSource(ConnProp.STORAGE_KAFKA_SERVER, "DataDev-OnRoadTargetsCnt", KafkaUtil.ConsumeMode.LATEST, "caikong-rad-track");
        KafkaSink<String> sink = KafkaUtil.getStringSink(ConnProp.STORAGE_KAFKA_SERVER, KafkaUtil.ProduceMode.AT_LEAST_ONCE, "ads_sc_rad_total_tra_flow_acc_instant_realtime_test");


        SingleOutputStreamOperator<Result> eachCrossResultDS = env
                .fromSource(source, WatermarkStrategy.<String>forMonotonousTimestamps(), "source")
                .uid("OnRoadTargetsCnt.Source")
                .map(r -> JSON.parseObject(r, Data.class))
                .assignTimestampsAndWatermarks(WatermarkStrategy.<Data>forMonotonousTimestamps().withTimestampAssigner((element, recordTimestamp) -> element.epoch))
                .keyBy(r -> r.crossName == null ? "NULL" : r.crossName)
                .process(new KeyedProcessFunction<String, Data, Result>() {
                    AggregatingState<Integer, Integer> agg;
                    MapState<String, String> targetSetSec;
                    ValueState<Long> secWindowEnd;
                    ValueState<Long> dayWindowEnd;
                    ValueState<Long> dailyCnt;
                    ValueState<String> crossId;
                    MapState<String, Integer> targetAppearTimes;

                    @Override
                    public void open(Configuration parameters) throws Exception {
                        super.open(parameters);
                        agg = getRuntimeContext().getAggregatingState(new AggregatingStateDescriptor<>("agg", new RichAggregateFunction<Integer, AvgLast30CntData, Integer>() {
                            @Override
                            public AvgLast30CntData createAccumulator() {
                                return new AvgLast30CntData();
                            }

                            @Override
                            public AvgLast30CntData add(Integer value, AvgLast30CntData accumulator) {
                                return accumulator.add(value);
                            }

                            @Override
                            public Integer getResult(AvgLast30CntData accumulator) {
                                return accumulator.getAvg();
                            }

                            @Override
                            public AvgLast30CntData merge(AvgLast30CntData a, AvgLast30CntData b) {
                                return a;
                            }
                        }, Types.POJO(AvgLast30CntData.class)));
                        targetSetSec = getRuntimeContext().getMapState(new MapStateDescriptor<>("targetSetSec", Types.STRING, Types.STRING));
                        secWindowEnd = getRuntimeContext().getState(new ValueStateDescriptor<>("secWindowEnd", Types.LONG));
                        dayWindowEnd = getRuntimeContext().getState(new ValueStateDescriptor<>("dayWindowEnd", Types.LONG));
                        dailyCnt = getRuntimeContext().getState(new ValueStateDescriptor<>("dailyCnt", Types.LONG));
                        crossId = getRuntimeContext().getState(new ValueStateDescriptor<>("crossId", Types.STRING));

                        StateTtlConfig ttlConfig = StateTtlConfig
                                .newBuilder(Time.minutes(5))
                                .setUpdateType(StateTtlConfig.UpdateType.OnReadAndWrite)
                                .setStateVisibility(StateTtlConfig.StateVisibility.NeverReturnExpired)
                                .build();
                        MapStateDescriptor<String, Integer> stateDescriptor = new MapStateDescriptor<>("targetAppearTimes", Types.STRING, Types.INT);
                        stateDescriptor.enableTimeToLive(ttlConfig);
                        targetAppearTimes = getRuntimeContext().getMapState(stateDescriptor);
                    }

                    @Override
                    public void processElement(Data value, KeyedProcessFunction<String, Data, Result>.Context ctx, Collector<Result> out) throws Exception {

                        if (secWindowEnd.value() == null || secWindowEnd.value() < ctx.timerService().currentProcessingTime()) {
                            long l = ctx.timerService().currentProcessingTime() + 2000; // 定时器设定为两秒后触发
                            secWindowEnd.update(l);
                            ctx.timerService().registerProcessingTimeTimer(l); // 使用处理时间的定时器
                        }
                        if (crossId.value() == null)
                            crossId.update(value.crossId);
                        if (dailyCnt.value() == null)
                            dailyCnt.update(0L);

                        for (Target target : value.targets) {
                            if (target.targetType < 3)
                                continue;
                            target.targetId = value.deviceId + target.targetId;
                            targetSetSec.put(target.targetId, null);

                            if (target.speed != null && Math.abs(target.speed) < 17)
                                continue;

                            Integer integer = targetAppearTimes.get(target.targetId);
                            integer = integer == null ? 1 : integer + 1;

                            if (integer < 20) {
                                targetAppearTimes.put(target.targetId, integer);
                            } else if (integer == 20) {
                                dailyCnt.update(dailyCnt.value() + 1);
                                targetAppearTimes.put(target.targetId, integer);
                            }
                        }
                    }

                    @Override
                    public void onTimer(long timestamp, KeyedProcessFunction<String, Data, Result>.OnTimerContext ctx, Collector<Result> out) throws Exception {

                        if (dayWindowEnd.value() == null) {
                            dailyCnt.update(0L);
                            dayWindowEnd.update(System.currentTimeMillis() + 24 * 60 * 60 * 1000L); // 一天后的时间点
                        }
                        if (timestamp >= dayWindowEnd.value()) {
                            dailyCnt.update(0L);
                            dayWindowEnd.update(dayWindowEnd.value() + 24 * 60 * 60 * 1000L);
                        }

                        Result result = new Result();
                        result.crossName = ctx.getCurrentKey();
                        result.crossId = crossId.value();
                        result.carInstantCnt = Iterables.size(targetSetSec.keys());
                        targetSetSec.clear();
                        result.carAccCnt = dailyCnt.value();
                        result.dataTime = TimeUtils.convertToString(timestamp, TimeUtils.DATE_TIME_FORMATTER_WITH_SECOND);
                        out.collect(result);
                        ctx.timerService().registerProcessingTimeTimer(timestamp + 1000); // 下一次的定时器
                    }
                })
                .uid("OnRoadTargetsCnt.PerCross.CountFunction");

        eachCrossResultDS
                .windowAll(TumblingProcessingTimeWindows.of(org.apache.flink.streaming.api.windowing.time.Time.seconds(1)))
                .aggregate(new AggregateFunction<Result, Result, Result>() {
                    @Override
                    public Result createAccumulator() {
                        Result result = new Result();
                        result.crossName = "total";
                        result.crossId = "total";
                        result.carAccCnt = 0L;
                        result.carInstantCnt = 0;

                        return result;
                    }

                    @Override
                    public Result add(Result value, Result accumulator) {
                        accumulator.carAccCnt += value.carAccCnt;
                        accumulator.carInstantCnt += value.carInstantCnt;
                        if (accumulator.dataTime == null)
                            accumulator.dataTime = value.dataTime;
                        return accumulator;
                    }

                    @Override
                    public Result getResult(Result accumulator) {
                        return accumulator;
                    }

                    @Override
                    public Result merge(Result a, Result b) {
                        a.carInstantCnt += b.carInstantCnt;
                        a.carAccCnt += b.carAccCnt;
                        return a;
                    }
                })
                .windowAll(SlidingProcessingTimeWindows.of(org.apache.flink.streaming.api.windowing.time.Time.seconds(30), org.apache.flink.streaming.api.windowing.time.Time.seconds(1)))
                .process(new ProcessAllWindowFunction<Result, String, TimeWindow>() {
                    @Override
                    public void process(ProcessAllWindowFunction<Result, String, TimeWindow>.Context context, Iterable<Result> elements, Collector<String> out) throws Exception {
                        Result result = new Result();
                        int sum = 0;
                        int size = 0;
                        Iterator<Result> iterator = elements.iterator();
                        while (iterator.hasNext()) {
                            Result next = iterator.next();
                            sum += next.carInstantCnt;
                            size += 1;
                            if (!iterator.hasNext()) {
                                result.dataTime = next.dataTime;
                                result.carAccCnt = next.carAccCnt;
                                result.crossName = next.crossName;
                                result.crossId = next.crossId;
                                result.carInstantCnt = sum / size;
                            }
                        }
                        String output = JSON.toJSONString(result);
                        out.collect(output);
                    }
                })
                .sinkTo(sink);
    }

    @Override
    protected String appName() {
        return "在路实时流量";
    }

    public static class Target implements Serializable {
        public String targetId;
        public Integer targetType;
        public Double speed;
    }

    public static class Data implements Serializable {

        public List<Target> targets;
        public String deviceId;
        @JSONField(name = "cross_name")
        public String crossName;
        @JSONField(name = "cross_id")
        public String crossId;
        public Long epoch;

        @Override
        public int hashCode() {
            int result = crossName != null ? crossName.hashCode() : 0;
            result = 31 * result + (crossId != null ? crossId.hashCode() : 0);
            return result;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof Data))
                return false;

            Data data = (Data) o;
            if (!Objects.equals(this.crossName, data.crossName))
                return false;
            return Objects.equals(this.crossId, data.crossId);
        }
    }

    public static class Result implements Serializable {
        @JSONField(name = "data_time")
        public String dataTime;
        @JSONField(name = "cross_name")
        public String crossName;
        @JSONField(name = "cross_id")
        public String crossId;
        @JSONField(name = "car_instant_cnt")
        public Integer carInstantCnt;
        @JSONField(name = "car_acc_cnt")
        public Long carAccCnt;
    }

    public static class AvgLast30CntData implements Serializable {

        public int[] data = new int[30];
        public int needToRemove = 0;
        public int size = 0;
        public int sum;

        public AvgLast30CntData add(int in) {
            if (size < 30) {
                data[size++] = in;
                sum += in;
                return this;
            }
            sum -= data[needToRemove];
            sum += in;
            data[needToRemove] = in;
            needToRemove = (needToRemove + 1) % 30;
            return this;
        }

        public int getAvg() {
            return sum / size;
        }

        public int[] getData() {
            return data;
        }

        public void setData(int[] data) {
            this.data = data;
        }

        public int getNeedToRemove() {
            return needToRemove;
        }

        public void setNeedToRemove(int needToRemove) {
            this.needToRemove = needToRemove;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }

        public int getSum() {
            return sum;
        }

        public void setSum(int sum) {
            this.sum = sum;
        }
    }

}
