package cn.eroad.app.ioc
import cn.eroad.ioc_spark_utils.utilsSpark
import cn.eroad.ioc_spark_utils.utilsSpark.{kafkaBootstrapServersRadSdk, tenMinutesAgo,tmpPath}
import org.apache.spark.sql.functions.{avg, count, max, sum}
import org.apache.spark.sql.streaming.Trigger
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}

import java.util.Properties



object AdsScAiRadSixMerage {
  def main(args: Array[String]): Unit = {
    val spark: SparkSession = SparkSession.builder().master("local[1]").appName("data_six_merge").enableHiveSupport().getOrCreate()
    val url = utilsSpark.url
    val prop = new Properties()
    prop.put("user", utilsSpark.username)
    prop.put("password", utilsSpark.password)
    spark.sqlContext.setConf("spark.sql.shuffle.partitions", "20")
    spark.sqlContext.setConf("spark.default.parallelism", "20 ")
    spark.sql("select '' as cross_name, '' as cross_id,'' as device_id,'' as laneno,'' as  avg_queuelength,'' as max_queuelength").repartition(1).write.mode(SaveMode.Overwrite).save(s"file:///${tmpPath}status_df_six\\")
    spark.sql("select '' as cross_name,'' as  cross_id,'' as device_id,'' as laneno, '' as sum_flow,'' as  delay_time_prop,'' as  tra_efficiency,'' as avg_speed").repartition(1).write.mode(SaveMode.Overwrite).save(s"file:///${tmpPath}flow_df_six\\")
    val enterDirection = spark.read.jdbc(url, table = "dim_own_szdl_section_relation",prop).selectExpr("device_id","section_direction as enter_direction").groupBy("device_id","enter_direction").count()
    def stream3Data(spark: SparkSession, df: DataFrame): Unit = {
      val crossName = df.groupBy("cross_name", "cross_id", "device_id", "laneno").count()
      val secondDf = df.selectExpr("device_id", "laneno", "targetid", "unix_timestamp(timestamp) as report_time", "cast(speed as double) as speed")
      val trackCount = df.groupBy("cross_name", "cross_id", "device_id", "laneno", "targetid").count().groupBy("cross_name", "cross_id", "device_id", "laneno").agg(count("targetid").as("cross_count"))
      val threeDf = secondDf.selectExpr("device_id", "laneno", "targetid", "report_time", "speed").orderBy("report_time").selectExpr("device_id", "laneno", "targetid", "report_time", "speed", "lag(speed,1) over(partition by device_id,targetid order by report_time) as on_speed", "lead(speed,1) over(partition by device_id,targetid order by report_time) as next_speed").selectExpr("device_id", "laneno", "targetid", "on_speed", "speed", "next_speed", "report_time", "case when speed =0 and on_speed is null then report_time when speed = 0 and next_speed !=0  then report_time when speed =0  and on_speed !=0 then report_time when speed =0 and next_speed is null then report_time when on_speed !=0 and speed = 0 and next_speed !=0 then NULL else NULL end as new_report_time").where("new_report_time is not null")
      val fourDf = threeDf.selectExpr("device_id", "laneno", "targetid", "new_report_time").orderBy("new_report_time").selectExpr("device_id", "laneno", "targetid", "(lead(new_report_time,1) over(partition by device_id,targetid order by new_report_time) - new_report_time) as time_cha", "row_number() over(partition by device_id,targetid order by new_report_time) as rank").where("rank % 2=1").selectExpr("device_id", "laneno", "targetid", "time_cha")
      val fiveDf = fourDf.selectExpr("device_id", "laneno", "targetid", "case when time_cha >=10 and time_cha < 120 then 1  else 0 end as num").groupBy("device_id", "laneno", "targetid").agg(sum("num").as("wait_num")).where("wait_num>0 and wait_num <3")
      val crossWaitNum = fiveDf.join(crossName, Seq("device_id", "laneno"), "inner").groupBy("cross_name", "cross_id", "device_id", "laneno").agg(sum("wait_num").as("cross_wait_num"))
      val avgStopNum = crossWaitNum.join(trackCount, Seq("cross_name", "cross_id", "device_id", "laneno"), "inner").selectExpr("cross_name", "cross_id", "device_id", "laneno", "(cross_wait_num/cross_count) as avg_stop_num")
      val idWaitTime = df.selectExpr("cross_name", "cross_id", "targetid", "speed", "device_id", "laneno").where("speed =0").groupBy("cross_name", "cross_id", "targetid", "device_id", "laneno").agg((count("targetid") * 0.1).as("id_wait_time")).where("id_wait_time <120").groupBy("cross_name", "cross_id", "device_id", "laneno").agg(sum("id_wait_time").as("cross_device_laneNo_wait_time"))
      val waitTimeCount = df.groupBy("cross_name", "cross_id", "device_id", "targetid", "laneno").count().groupBy("cross_name", "cross_id", "device_id", "laneno").count()
      val avgWaitTime = idWaitTime.join(waitTimeCount, Seq("cross_name", "cross_id", "device_id", "laneno"), "inner").selectExpr("cross_name", "cross_id", "device_id", "laneno", "(cross_device_laneNo_wait_time/count) as avg_wait_time")
      val energy =spark.read.jdbc(url, table = "dim_own_szdl_section_relation",prop).selectExpr("road_name", "start_end_dot", "device_id").groupBy("road_name", "start_end_dot", "device_id").count()
      val length = spark.read.jdbc(url, table = "ads_traffic_signal_control_optimizing_laneno_index_10min",prop).groupBy("device_id","length").count()
      val roadLength = energy.join(length,Seq("device_id"),"inner").selectExpr("road_name", "start_end_dot", "device_id","length")
      val carLength = df.filter("device_id is not null").filter("laneNo is not null").
        groupBy("device_id", "cross_id", "cross_name", "targetId", "targetLength", "laneNo", "timeStamp")
        .agg(sum("targetLength").as("sum_tid_length"), count("targetLength").as("car_num"))
        .selectExpr("device_id", "cross_id", "cross_name", "targetId", "sum_tid_length / car_num  as avg_tid_length", "laneNo", "timeStamp")
        .groupBy("device_id", "cross_id", "cross_name", "laneNo", "timeStamp")
        .agg(sum("avg_tid_length").as("car_total_length"), count("targetId").as("total_flow"))
        .selectExpr("cross_name", "device_id", "cross_id", "laneNo", "car_total_length", "total_flow", "timeStamp")
      val spaceRate: DataFrame = carLength.join(roadLength, Seq("device_id"), "left")
        .selectExpr("device_id", "cross_name", "cross_id", "laneNo as laneno", "start_end_dot", "total_flow", "(nvl(car_total_length / length,0) *100) as space_rate", "nvl(total_flow / length,0)*100 as traffic_density", "timeStamp as data_time", "length")
        .groupBy("cross_name", "cross_id", "device_id", "laneno", "length")
        .agg(avg("space_rate").as("space_rate"), avg("traffic_density").as("traffic_density"))
      val crossAllTime = df.groupBy("cross_name", "cross_id", "device_id", "laneno").agg((count("targetid") * 0.1).as("cross_all_time"))
      val avgTime = crossAllTime.join(trackCount, Seq("cross_name", "cross_id", "device_id", "laneno"), "inner")
        .selectExpr("cross_name", "cross_id", "device_id", "laneno", "(cross_all_time / cross_count) as avg_time")
      val trackDf = avgStopNum
        .join(avgWaitTime, Seq("cross_name", "cross_id", "device_id", "laneno"), "full")
        .join(spaceRate, Seq("cross_name", "cross_id", "device_id", "laneno"), "full")
        .join(avgTime, Seq("cross_name", "cross_id", "device_id", "laneno"), "left")
        .selectExpr("cross_name", "cross_id", "device_id", "laneno", "avg_stop_num", "avg_wait_time", "length", "space_rate","avg_time")
      val statusDf = spark.read.parquet(s"file:///${tmpPath}status_df_six\\")
        .selectExpr("cross_name", "cross_id", "device_id", "laneno", "avg_queuelength", "max_queuelength","overflow_rate")
      val flowDf = spark.read.parquet(s"file:///${tmpPath}flow_df_six\\")
        .selectExpr("cross_name", "cross_id", "device_id", "laneno", "sum_flow", "delay_time_prop", "tra_efficiency", "avg_speed","saturation")
      val merageDf = flowDf.join(trackDf, Seq("cross_name", "cross_id", "device_id", "laneno"), "full")
        .join(statusDf, Seq("cross_name", "cross_id", "device_id", "laneno"), "full")
        .selectExpr(s"'$tenMinutesAgo' as data_time", "cross_name", "cross_id", "device_id", "laneno", "length", "sum_flow", "delay_time_prop", "tra_efficiency", "avg_queuelength", "max_queuelength", "avg_wait_time", "avg_stop_num", "space_rate", "avg_speed","saturation","overflow_rate","(avg_time * delay_time_prop) as avg_delay_time")
      val merageDfTotal = merageDf.agg(sum("sum_flow").as("sum_flow"),
        avg("delay_time_prop").as("delay_time_prop"),
        avg("tra_efficiency").as("tra_efficiency"),
        avg("avg_queuelength").as("avg_queuelength"),
        max("max_queuelength").as("max_queuelength"),
        avg("avg_wait_time").as("avg_wait_time"),
        avg("avg_stop_num").as("avg_stop_num"),
        avg("space_rate").as("space_rate"),
        avg("avg_speed").as("avg_speed"),
        avg("saturation").as("saturation"),
          avg("overflow_rate").as("overflow_rate"),
            avg("avg_delay_time").as("avg_delay_time")
      )
        .selectExpr(s"'$tenMinutesAgo' as data_time", "'total' as cross_name", "'total' as cross_id", "'total' as device_id", "'total' as laneno", "'total' as length", "sum_flow", "delay_time_prop", "tra_efficiency", "avg_queuelength", "max_queuelength", "avg_wait_time", "avg_stop_num", "space_rate", "avg_speed","saturation","overflow_rate","avg_delay_time")
      val merage = merageDf.union(merageDfTotal)
      val merageEndDf = merage.join(enterDirection, Seq("device_id"), "left")
        .selectExpr("data_time", "cross_name", "cross_id", "device_id", "ifnull(enter_direction,'') as enter_direction", "laneno", "ifnull(length,0.0) as length", "ifnull(sum_flow,0.0) as sum_flow", "case when delay_time_prop is null then 0.0 when delay_time_prop <0 then 0.0 else delay_time_prop end as delay_time_prop", "ifnull(tra_efficiency,0.0) as tra_efficiency", "ifnull(avg_queuelength,0.0) as avg_queuelength", "ifnull(max_queuelength,0.0) as max_queuelength", "ifnull(avg_wait_time,0.0) as avg_wait_time", "ifnull(avg_stop_num,0.0) as avg_stop_num", "ifnull(space_rate,0.0) as space_rate", "ifnull(avg_speed,0.0) as avg_speed", "ifnull(saturation,0.0) as saturation", "ifnull(overflow_rate,0.0) as overflow_rate","case when avg_delay_time is null then 0.0 when avg_delay_time <0 then 0.0 else avg_delay_time end as avg_delay_time")
        .groupBy("data_time","cross_name","cross_id","device_id","enter_direction","laneno","length","sum_flow","delay_time_prop","tra_efficiency","avg_queuelength","max_queuelength","avg_wait_time","avg_stop_num","space_rate","avg_speed","saturation","overflow_rate","avg_delay_time").count().selectExpr("data_time","cross_name","cross_id","device_id","enter_direction","laneno","length","sum_flow","delay_time_prop","tra_efficiency","avg_queuelength","max_queuelength","avg_wait_time","avg_stop_num","space_rate","avg_speed","saturation","overflow_rate","avg_delay_time")
      merageEndDf.show()
    }
    val flowDf = spark.readStream.format("kafka")
      .option("kafka.bootstrap.servers", kafkaBootstrapServersRadSdk)
      .option("subscribe", "caikong-rad-flow")
      .option("serializer.encoding", "UTF8")
      .option("startingOffsets", "latest")
      .option("kafka.max.poll.interval.ms", "600000")
      .load()
    import org.apache.spark.sql.functions._
    import spark.implicits._
    val InputStreamSchem2 = new StructType()
      .add("cross_name", "string")
      .add("cross_id", "string")
      .add("device_id", "string")
      .add("trafficFlows", "string")
    val type3 = new StructType()
      .add("aisleId", "string")
      .add("totalFlowA", "string")
      .add("totalFlowB", "string")
      .add("totalFlowC", "string")
      .add("averageSpeed", "string")
      .add("averageHeadwayDistance", "string")
    val flowDff: DataFrame = flowDf.selectExpr("cast(value as string)").as[String]
      .select(from_json($"value", InputStreamSchem2).as("data"))
      .select("data.*")
      .select($"cross_name", $"cross_id", $"device_id", explode(split(regexp_replace(regexp_replace($"trafficFlows", "\\[|\\]", ""), "\\}\\,\\{", "\\}\\;\\{"), "\\;")))
      .select($"cross_name", $"cross_id", $"device_id", from_json($"col", type3).as("data2"))
      .selectExpr("cross_name", "cross_id", "ifnull(device_id,'') as device_id", "data2.averageSpeed", "data2.totalFlowA", "data2.totalFlowC", "data2.totalFlowB", "data2.averageHeadwayDistance", "(data2.totalFlowA+data2.totalFlowB+data2.totalFlowC) as flow", "ifnull(data2.aisleId,'') as laneNo")
    val stream1 = flowDff.writeStream
      .format("parquet")
      .outputMode("append")
      .foreachBatch((df: DataFrame, num: Long) => {
        df.cache()
        val energy =spark.read.jdbc(url, table = "dim_own_szdl_section_relation",prop).selectExpr("road_name", "start_end_dot", "device_id", "250 AS energy").groupBy("road_name", "start_end_dot", "device_id", "energy").count()
        val saturation = df.join(energy, Seq("device_id"), "inner")
          .selectExpr("cross_name", "device_id", "laneno", "cross_id", "road_name", "start_end_dot", "flow", "energy")
          .groupBy("cross_name", "cross_id", "device_id", "laneno", "road_name", "start_end_dot", "energy")
          .agg(sum("flow").as("road_flow"))
          .selectExpr("cross_name", "cross_id", "device_id", "laneno", "road_name", "start_end_dot", "(road_flow/energy) as road_saturation")
          .groupBy("cross_name", "cross_id", "device_id", "laneno")
          .agg((avg("road_saturation") * 100).as("saturation"))
          .selectExpr("cross_name", "cross_id", "device_id", "laneno", "case when saturation >90 then 90 else saturation end as saturation")
        val sumFlow = df
          .groupBy("cross_name", "cross_id", "device_id", "laneno")
          .agg(
            sum("flow").as("sum_flow")
          )
        val avgSpeedDf = df
          .where("averageSpeed >0 and averageSpeed <120")
          .groupBy("cross_name", "cross_id", "device_id", "laneno")
          .agg(avg("averageSpeed").as("avg_speed")
          )
        val dwmFreeRoadsec = spark.read.jdbc(url, table = "ads_sc_rad_road_control_optimizing_index_10min",prop).selectExpr("road_name", "crossroad_s_e", "avg_speed").groupBy("road_name", "crossroad_s_e").agg(max("avg_speed").as("free_flow_speed"))
        val flowHaha= spark.read.jdbc(url, table = "dim_own_szdl_section_relation",prop).selectExpr("device_id","road_name","start_end_dot as crossroad_s_e")
        val freeDevice = dwmFreeRoadsec.join(flowHaha, Seq("road_name", "crossroad_s_e"), "inner")
          .selectExpr("device_id", "road_name", "crossroad_s_e", "free_flow_speed")
        val avgAvgspeed = df
          .where("averageSpeed >0 and averageSpeed <120")
          .groupBy("cross_name", "cross_id", "device_id", "laneno")
          .agg(avg("averageSpeed").as("avgspeed"))
        val speedNewState = avgAvgspeed
          .join(freeDevice, Seq("device_id"), "inner")
          .selectExpr("cross_name", "cross_id", "device_id", "laneno", "(free_flow_speed - avgspeed)/free_flow_speed as delay_time_prop", "case when ((avgspeed/free_flow_speed)*100) >100 then 100 else ((avgspeed/free_flow_speed)*100) end as tra_efficiency")
        val flowDf = sumFlow
          .join(speedNewState, Seq("cross_name", "cross_id", "device_id", "laneno"), "full")
          .join(avgSpeedDf, Seq("cross_name", "cross_id", "device_id", "laneno"), "full")
          .join(saturation, Seq("cross_name", "cross_id", "device_id", "laneno"), "full")
          .selectExpr("cross_name", "cross_id", "device_id", "laneno", "sum_flow", "delay_time_prop", "tra_efficiency", "avg_speed","saturation")
        flowDf.repartition(1).write.mode(SaveMode.Overwrite).save(s"file:///${tmpPath}flow_df_six\\")
        df.unpersist()
        ()
      })
      .trigger(Trigger.ProcessingTime("10 minutes"))
      .start()
    val statusDf = spark.readStream.format("kafka")
      .option("kafka.bootstrap.servers", kafkaBootstrapServersRadSdk)
      .option("subscribe", "caikong-rad-status")
      .option("serializer.encoding", "UTF8")
      .option("startingOffsets", "latest")
      .option("kafka.max.poll.interval.ms", "600000")
      .load()
    val InputStreamSchem3 = new StructType()
      .add("cross_name", "string")
      .add("cross_id", "string")
      .add("device_id", "string")
      .add("trafficStatusInformations", "string")
    val type4 = new StructType()
      .add("queueLength", "double")
      .add("aisleId", "string")
      .add("queueVehicleNumber", "double")
    val statusDff: DataFrame = statusDf.selectExpr("cast(value as string)").as[String]
      .select(from_json($"value", InputStreamSchem3).as("data"))
      .select("data.*")
      .select($"cross_name", $"cross_id", $"device_id", explode(split(regexp_replace(regexp_replace($"trafficStatusInformations", "\\[|\\]", ""), "\\}\\,\\{", "\\}\\;\\{"), "\\;")))
      .select($"cross_name", $"cross_id", $"device_id", from_json($"col", type4).as("data2"))
      .selectExpr("cross_name", "cross_id", "ifnull(device_id,'') as device_id", "case when cast(data2.queueLength as double) >=150 then 150 else cast(data2.queueLength as double) end as queueLength", "(cast(data2.queueVehicleNumber as double)) as queueVehicleNumber", "ifnull(data2.aisleId,'') as laneNo")
      .where("queueVehicleNumber !=0 and queueLength >0")
    val stream2 = statusDff.writeStream
      .format("parquet")
      .outputMode("append")
      .foreachBatch((df: DataFrame, num: Long) => {
        df.cache()
        val maxQueueLength = df.selectExpr("cross_name", "cross_id", "device_id", "laneno", "case when queueLength >150 then 150 else queueLength end as queueLength").groupBy("cross_name", "cross_id", "device_id", "laneno").agg(max("queueLength").as("max_queueLength"))
        spark.read.jdbc(url, table = "ads_traffic_signal_control_optimizing_laneno_index_10min",prop).createOrReplaceTempView("ads_traffic_signal_control_optimizing_laneno_index_10min")
        val weekQueueLength = spark.sql("""select device_id,laneno,length as road_length,percentile(max_queuelength,0.90) as per_max_queueLength from ads_traffic_signal_control_optimizing_laneno_index_10min where substring(data_time,1,10) = (select substring(data_time,1,10) from  ads_traffic_signal_control_optimizing_laneno_index_10min order by data_time desc limit 1) group by device_id,laneno,length""")
        val overflowRate = maxQueueLength.join(weekQueueLength, Seq("device_id", "laneno"), "left").selectExpr("cross_name", "cross_id", "laneno", "device_id", "per_max_queueLength", "road_length", "case when max_queueLength/per_max_queueLength <1.5 and max_queueLength/per_max_queueLength>=1 then max_queueLength/per_max_queueLength  when max_queueLength/road_length <1.5 and max_queueLength/road_length>=1  then max_queueLength/road_length else 0 end as overflow_rate").groupBy("cross_name", "cross_id", "device_id", "laneno").agg(avg("overflow_rate").as("overflow_rate")).where("cross_name is not null and cross_id is not null and device_id is not null and laneno is not null and overflow_rate is not null")
        val avgQueuelength = df
          .where("queueLength !=0")
          .groupBy("cross_name", "cross_id", "device_id", "laneno")
          .agg(avg("queueLength").as("avg_queuelength"), max("queueLength").as("max_queuelength"))
          .selectExpr("cross_name", "cross_id", "device_id", "laneno", "avg_queuelength", "max_queuelength")
        avgQueuelength
          .join(overflowRate,Seq("cross_name", "cross_id", "device_id", "laneno"),"full").repartition(1).write.mode(SaveMode.Overwrite).save(s"file:///${tmpPath}status_df_six\\")
        df.unpersist()
        import java.io.File
        val checkpointDir = new File(s"${tmpPath}track\\_temporary")
        while (checkpointDir.exists()) {
          Thread.sleep(10000)
          println()
          println("tmp_file 存在")
          println()
        }
        println("track")
        val sparkDf = spark.read.parquet(s"file:///${tmpPath}track\\").selectExpr("timestamp as timeStamp", "cross_name", "cross_id", "device_id", "target_id as targetId", "speed", "ifnull(lane_no,'') as laneNo", "target_length as targetLength").cache()
        stream3Data(spark, sparkDf)
        sparkDf.unpersist()
        ()
      })
      .trigger(Trigger.ProcessingTime("10 minutes"))
      .start()

    try {
      spark.streams.awaitAnyTermination()
    }
    catch {
      case ex1: Exception =>
        println(ex1)
        stream1.stop()
        stream2.stop()

    }
  }
}
