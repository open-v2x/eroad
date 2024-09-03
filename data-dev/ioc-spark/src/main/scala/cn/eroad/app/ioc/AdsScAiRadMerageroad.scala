package cn.eroad.app.ioc

import cn.eroad.ioc_spark_utils.utilsSpark.tmpPath
import cn.eroad.ioc_spark_utils.utilsSpark
import cn.eroad.ioc_spark_utils.utilsSpark.{kafkaBootstrapServersRadSdk, nowday, tenMinutesAgo}
import org.apache.spark.sql.functions.{avg, count, max, sum}
import org.apache.spark.sql.streaming.Trigger
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}

import java.io.File
import java.util.Properties

object AdsScAiRadMerageroad {
  def main(args: Array[String]): Unit = {
    val spark: SparkSession = SparkSession.builder().master("local[1]").appName("data_merge_road").enableHiveSupport().getOrCreate()
    val url = utilsSpark.url
    val prop = new Properties()
    prop.put("user", utilsSpark.username)
    prop.put("password", utilsSpark.password)
    spark.sqlContext.setConf("spark.sql.shuffle.partitions", "3")
    spark.sqlContext.setConf("spark.default.parallelism", "3")
    spark.sql("select '' as road_name, '' as start_end_dot,'' as  avg_queuelength,'' as  max_queuelength").repartition(1).write.mode(SaveMode.Overwrite).save(s"file:///${tmpPath}status_df_road\\")
    spark.sql("select '' as road_name,'' as  start_end_dot, '' as sum_flow,'' as  avg_speed,'' as  saturation,'' as  avg_headwaydistance,'' as  tra_time_index,'' as  delay_time_prop,'' as  tra_efficiency").repartition(1).write.mode(SaveMode.Overwrite).save(s"file:///${tmpPath}flow_df_road\\")

    def stream3Data(spark: SparkSession, df: DataFrame): Unit = {
      val roadName = spark.read.jdbc(url, table = "dim_own_szdl_section_relation",prop).selectExpr("device_id","road_name","start_end_dot")
      val roadFlow = df.join(roadName, Seq("device_id"), "inner")
      val crossName = roadFlow.groupBy("road_name", "start_end_dot", "device_id").count()
      val secondDf = roadFlow.selectExpr("device_id", "targetid", "unix_timestamp(timestamp) as report_time", "cast(speed as double) as speed")
      val trackCount = roadFlow.groupBy("road_name", "start_end_dot", "device_id", "targetid").count().groupBy("road_name", "start_end_dot").agg(count("targetid").as("cross_count"))
      val threeDf = secondDf.selectExpr("device_id", "targetid", "report_time", "speed").orderBy("report_time").selectExpr("device_id", "targetid", "report_time", "speed", "lag(speed,1) over(partition by device_id,targetid order by report_time) as on_speed", "lead(speed,1) over(partition by device_id,targetid order by report_time) as next_speed").selectExpr("device_id", "targetid", "on_speed", "speed", "next_speed", "report_time", "case when speed =0 and on_speed is null then report_time when speed = 0 and next_speed !=0  then report_time when speed =0  and on_speed !=0 then report_time when speed =0 and next_speed is null then report_time when on_speed !=0 and speed = 0 and next_speed !=0 then NULL else NULL end as new_report_time").where("new_report_time is not null")
      val fourDf = threeDf.selectExpr("device_id", "targetid", "new_report_time").orderBy("new_report_time").selectExpr("device_id", "targetid", "(lead(new_report_time,1) over(partition by device_id,targetid order by new_report_time) - new_report_time) as time_cha", "row_number() over(partition by device_id,targetid order by new_report_time) as rank").where("rank % 2=1").selectExpr("device_id", "targetid", "time_cha")
      val fiveDf = fourDf.selectExpr("device_id", "targetid", "case when time_cha >=10 and time_cha < 120 then 1  else 0 end as num").groupBy("device_id", "targetid").agg(sum("num").as("wait_num")).where("wait_num >0 and wait_num <3")
      val sixDf = fiveDf.where("wait_num>0 and wait_num <3").groupBy("device_id", "wait_num").agg(count("targetid").as("device_wait_car_num"))
      val crossWaitCarNum = sixDf.join(crossName, Seq("device_id"), "inner").groupBy("road_name", "start_end_dot", "wait_num").agg(sum("device_wait_car_num").as("car_cnt")).selectExpr("road_name", "start_end_dot", "wait_num", "car_cnt")
      val waitId = secondDf.groupBy("device_id", "targetid", "speed").count().where("speed =0 and count >50").groupBy("device_id", "targetid").count().selectExpr("device_id", "targetid", "-1 as state")
      val deviceNoWaitCarId = secondDf.groupBy("device_id", "targetid").count().where("count >= 60").selectExpr("device_id", "targetid").join(waitId, Seq("device_id", "targetid"), "left").where("state is null").selectExpr("device_id", "targetid")
      val crossNoWaitNum = deviceNoWaitCarId.join(roadName, Seq("device_id"), "inner").groupBy("road_name", "start_end_dot").count().selectExpr("road_name", "start_end_dot", "0 as wait_num", "count as car_cnt")
      val allCarCnt = crossWaitCarNum.union(crossNoWaitNum).groupBy("road_name", "start_end_dot").agg(sum("car_cnt").as("all_car_cnt"))
      val waitNum012 = crossWaitCarNum.union(crossNoWaitNum).join(allCarCnt, Seq("road_name", "start_end_dot"), "inner").selectExpr("road_name", "start_end_dot", "wait_num", "(car_cnt/all_car_cnt)*100 as ratio")
      val adsScRadRoadWaitNumCntOne = waitNum012.where("wait_num = 0").selectExpr("road_name", "start_end_dot", "ratio as one_pass_ratio")
      val adsScRadRoadWaitNumCntTwo = waitNum012.where("wait_num = 1").selectExpr("road_name", "start_end_dot", "ratio as two_pass_ratio")
      val energy =spark.read.jdbc(url, table = "dim_own_szdl_section_relation",prop).selectExpr("road_name", "start_end_dot", "device_id").groupBy("road_name", "start_end_dot", "device_id").count()
      val length = spark.read.jdbc(url, table = "ads_traffic_signal_control_optimizing_laneno_index_10min",prop).groupBy("device_id","length").count()
      val roadLength = energy.join(length,Seq("device_id"),"inner").selectExpr("road_name", "start_end_dot", "device_id","length")
      val carLength = roadFlow.filter("device_id is not null").filter("laneNo is not null").
        groupBy("device_id", "road_name", "start_end_dot", "targetId", "targetLength", "laneNo", "timeStamp")
        .agg(sum("targetLength").as("sum_tid_length"), count("targetLength").as("car_num"))
        .selectExpr("device_id", "road_name", "start_end_dot", "targetId", "sum_tid_length / car_num  as avg_tid_length", "laneNo", "timeStamp")
        .groupBy("device_id", "road_name", "start_end_dot", "laneNo", "timeStamp")
        .agg(sum("avg_tid_length").as("car_total_length"), count("targetId").as("total_flow"))
        .selectExpr("road_name", "start_end_dot", "device_id", "laneNo", "car_total_length", "total_flow", "timeStamp")
      val spaceRate: DataFrame = carLength.join(roadLength, Seq("device_id", "road_name"), "left")
        .selectExpr("device_id", "road_name", "laneNo as aisle_id", "start_end_dot", "total_flow", "(nvl(car_total_length / length,0) *100) as space_rate", "nvl(total_flow / length,0)*100 as traffic_density", "timeStamp as data_time")
        .groupBy("road_name", "start_end_dot")
        .agg(avg("space_rate").as("space_rate"), avg("traffic_density").as("traffic_density"))
      val crossWaitNum = fiveDf.join(roadName, Seq("device_id"), "inner").groupBy("road_name", "start_end_dot").agg(sum("wait_num").as("cross_wait_num"))
      val avgStopNum = crossWaitNum.join(trackCount, Seq("road_name", "start_end_dot"), "inner").selectExpr("road_name", "start_end_dot", "(cross_wait_num/cross_count) as avg_stop_num")
      val idWaitTime = roadFlow.selectExpr("road_name", "start_end_dot", "targetid", "speed", "device_id").where("speed =0").groupBy("road_name", "start_end_dot", "targetid", "device_id").agg((count("targetid") * 0.1).as("id_wait_time")).where("id_wait_time <120").groupBy("road_name", "start_end_dot", "device_id").agg(sum("id_wait_time").as("cross_device_laneNo_wait_time"))
      val waitTimeCount = roadFlow.groupBy("road_name", "start_end_dot", "device_id", "targetid").count().groupBy("road_name", "start_end_dot", "device_id").count()
      val avgWaitTime = idWaitTime.join(waitTimeCount, Seq("road_name", "start_end_dot", "device_id"), "inner").selectExpr("road_name", "start_end_dot", "device_id", "(cross_device_laneNo_wait_time/count) as device_wait_time").groupBy("road_name", "start_end_dot").agg(avg("device_wait_time").as("avg_wait_time"))
      val crossAllTime = roadFlow.groupBy("road_name", "start_end_dot").agg((count("targetid") * 0.1).as("cross_all_time"))
      val avgTime = crossAllTime.join(trackCount, Seq("road_name", "start_end_dot"), "inner")
        .selectExpr("road_name", "start_end_dot", "(cross_all_time / cross_count) as avg_time")
      val trackDf = avgStopNum
        .join(avgWaitTime, Seq("road_name", "start_end_dot"), "left")
        .join(adsScRadRoadWaitNumCntOne, Seq("road_name", "start_end_dot"), "left")
        .join(adsScRadRoadWaitNumCntTwo, Seq("road_name", "start_end_dot"), "left")
        .join(spaceRate, Seq("road_name", "start_end_dot"), "left")
        .join(avgTime, Seq("road_name", "start_end_dot"), "left")
        .selectExpr("road_name", "start_end_dot", "avg_stop_num", "avg_wait_time", "one_pass_ratio", "two_pass_ratio", "space_rate", "traffic_density", "avg_time")
      val statusDf = spark.read.parquet(s"file:///${tmpPath}status_df_road/")
        .selectExpr("road_name", "start_end_dot", "avg_queuelength", "max_queuelength")
      val flowDf = spark.read.parquet(s"file:///${tmpPath}flow_df_road/")
        .selectExpr("road_name", "start_end_dot", "sum_flow", "avg_speed", "saturation", "avg_headwaydistance", "tra_time_index", "delay_time_prop", "tra_efficiency","sum_flow/500*10 as tci")
      val merageDf = flowDf.join(trackDf, Seq("road_name", "start_end_dot"), "full")
        .join(statusDf, Seq("road_name", "start_end_dot"), "full")
        .selectExpr(s"'$tenMinutesAgo' as data_time", "road_name", "start_end_dot", "sum_flow", "saturation", "traffic_density", "avg_headwaydistance", "space_rate", "one_pass_ratio", "two_pass_ratio", "delay_time_prop", "tra_time_index", "tra_efficiency", "max_queuelength", "avg_queuelength", "avg_wait_time", "avg_speed", "avg_stop_num", "(avg_time * delay_time_prop) as avg_delay_time", "tci")
      val merageDfTotal = merageDf.agg(sum("sum_flow").as("sum_flow"),
        avg("saturation").as("saturation"),
        avg("traffic_density").as("traffic_density"),
        avg("avg_headwaydistance").as("avg_headwaydistance"),
        avg("space_rate").as("space_rate"),
        avg("one_pass_ratio").as("one_pass_ratio"),
        avg("two_pass_ratio").as("two_pass_ratio"),
        avg("delay_time_prop").as("delay_time_prop"),
        avg("tra_time_index").as("tra_time_index"),
        avg("tra_efficiency").as("tra_efficiency"),
        max("max_queuelength").as("max_queuelength"),
        avg("avg_queuelength").as("avg_queuelength"),
        avg("avg_wait_time").as("avg_wait_time"),
        avg("avg_speed").as("avg_speed"),
        avg("avg_stop_num").as("avg_stop_num"),
        avg("avg_delay_time").as("avg_delay_time"),
        avg("tci").as("tci")
        )
        .selectExpr(s"'$tenMinutesAgo' as data_time", "'total' as road_name", "'total' as start_end_dot", "sum_flow", "saturation", "traffic_density", "avg_headwaydistance", "space_rate", "one_pass_ratio", "two_pass_ratio", "delay_time_prop", "tra_time_index", "tra_efficiency", "max_queuelength", "avg_queuelength", "avg_wait_time", "avg_speed", "avg_stop_num", "avg_delay_time", "tci")
      val dimRoadMainBranchInfo = spark.read.jdbc(url, table = "ads_sc_rad_road_control_optimizing_index_10min",prop).selectExpr("road_name", "road_level").groupBy("road_name", "road_level").count()
      //空值转换
      val merageEndDf = merageDf.union(merageDfTotal)
        .join(dimRoadMainBranchInfo, Seq("road_name"), "left")
        .selectExpr("data_time", "start_end_dot", "road_name", "road_level", "ifnull(sum_flow,0.0) as sum_flow", "ifnull(saturation,0.0) as saturation", "ifnull(traffic_density,0.0) as traffic_density", "avg_headwaydistance", "ifnull(space_rate,0.0) as space_rate", "case when one_pass_ratio is null and two_pass_ratio is null then 100.0 when one_pass_ratio is null and two_pass_ratio is not null then 100-two_pass_ratio else one_pass_ratio end as one_pass_ratio", "case when one_pass_ratio is null and two_pass_ratio is null then 0.0 when one_pass_ratio is not null and two_pass_ratio is  null then 100-one_pass_ratio else two_pass_ratio end as two_pass_ratio", "case when delay_time_prop is null then 0.0 when delay_time_prop <0 then 0.0 else delay_time_prop end as delay_time_prop", "case when tra_time_index is null then 1.0 when tra_time_index < 1 then 1.0 else tra_time_index end as tra_time_index", "ifnull(tra_efficiency,0.0) as tra_efficiency", "ifnull(max_queuelength,0.0) as max_queuelength", "ifnull(avg_queuelength,0.0) as avg_queuelength", "ifnull(avg_wait_time,0.0) as avg_wait_time", "ifnull(avg_speed,0.0) as avg_speed", "ifnull(avg_stop_num,0.0) as avg_stop_num", "ifnull(tci,0.0) as tci", "case when avg_delay_time is null then 0.0 when avg_delay_time <0 then 0.0 else avg_delay_time end as avg_delay_time")
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
      .add("device_id", "string")
      .add("trafficFlows", "string")
    val type3 = new StructType()
      .add("totalFlowA", "string")
      .add("totalFlowB", "string")
      .add("totalFlowC", "string")
      .add("averageSpeed", "string")
      .add("averageHeadwayDistance", "string")
    val flowDff: DataFrame = flowDf.selectExpr("cast(value as string) as value").as[String]
      .select(from_json($"value", InputStreamSchem2).as("data"))
      .select("data.*")
      .select($"device_id", explode(split(regexp_replace(regexp_replace($"trafficFlows", "\\[|\\]", ""), "\\}\\,\\{", "\\}\\;\\{"), "\\;")))
      .select($"device_id", from_json($"col", type3).as("data2"))
      .selectExpr("device_id", "data2.averageSpeed", "data2.totalFlowA", "data2.totalFlowC", "data2.totalFlowB", "data2.averageHeadwayDistance", "(data2.totalFlowA+data2.totalFlowB+data2.totalFlowC) as flow")
    val stream1 = flowDff.writeStream
      .format("parquet")
      .outputMode("append")
      .foreachBatch((df: DataFrame, num: Long) => {
        df.cache()
        val energy =spark.read.jdbc(url, table = "dim_own_szdl_section_relation",prop).selectExpr("road_name", "start_end_dot", "device_id", "250 AS energy").groupBy("road_name", "start_end_dot", "device_id", "energy").count()
        val saturation = df.join(energy, Seq("device_id"), "inner")
          .selectExpr("road_name", "start_end_dot", "flow", "energy")
          .groupBy("road_name", "start_end_dot", "energy")
          .agg(sum("flow").as("road_flow"))
          .selectExpr("road_name", "start_end_dot", "(road_flow/energy)*100 as saturation")
          .selectExpr("road_name", "start_end_dot", "case when saturation >90 then 90 else saturation end as saturation")
        val roadName = spark.read.jdbc(url, table = "dim_own_szdl_section_relation",prop).selectExpr("device_id","road_name","start_end_dot")
        val roadFlow = df.join(roadName, Seq("device_id"), "inner")
        val avgFlowHeadDf = roadFlow
          .groupBy("road_name", "start_end_dot")
          .agg(
            avg("averageHeadwayDistance").as("avg_headwaydistance"),
            sum("flow").as("sum_flow")
          )
        val avgSpeedDf = roadFlow
          .where("averageSpeed >3 and averageSpeed <120")
          .groupBy("road_name", "start_end_dot")
          .agg(avg("averageSpeed").as("avg_speed")
          )
        if (nowday == "01") {
          spark.sql("msck repair table dwm.dwm_traffic_percep_crossroad_flowspeed_rad_m")
          spark.sql("msck repair table dwm.dwm_traffic_percep_roadsec_flowspeed_rad_m")
        }

        val dwmFreeRoadsec = spark.read.jdbc(url, table = "ads_sc_rad_road_control_optimizing_index_10min",prop).selectExpr("road_name", "start_end_dot", "avg_speed").groupBy("road_name", "start_end_dot").agg(max("avg_speed").as("free_flow_speed"))

        val avgAvgspeed = roadFlow
          .where("averageSpeed >0 and averageSpeed <120")
          .groupBy("road_name", "start_end_dot")
          .agg(avg("averageSpeed").as("avgspeed"))

        val speedNewState = avgAvgspeed.join(dwmFreeRoadsec, Seq("road_name", "start_end_dot"), "inner")
          .selectExpr("road_name", "start_end_dot", "free_flow_speed/avgspeed as tra_time_index", "(free_flow_speed - avgspeed)/free_flow_speed as delay_time_prop", "case when ((avgspeed/free_flow_speed)*100) >100 then 100 else ((avgspeed/free_flow_speed)*100) end as tra_efficiency")
        val flowDf = avgSpeedDf
          .join(avgFlowHeadDf, Seq("road_name", "start_end_dot"), "left")
          .join(saturation, Seq("road_name", "start_end_dot"), "left")
          .join(speedNewState, Seq("road_name", "start_end_dot"), "left")
          .selectExpr("road_name", "start_end_dot", "sum_flow", "avg_speed", "saturation", "avg_headwaydistance", "tra_time_index", "delay_time_prop", "tra_efficiency")
        flowDf.repartition(1).write.mode(SaveMode.Overwrite).save(s"file:///${tmpPath}flow_df_road/")
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
      .add("device_id", "string")
      .add("trafficStatusInformations", "string")
    val type4 = new StructType()
      .add("queueLength", "double")
      .add("queueVehicleNumber", "double")
    val statusDff: DataFrame = statusDf.selectExpr("cast(value as string) as value").as[String]
      .select(from_json($"value", InputStreamSchem3).as("data"))
      .select("data.*")
      .select($"device_id", explode(split(regexp_replace(regexp_replace($"trafficStatusInformations", "\\[|\\]", ""), "\\}\\,\\{", "\\}\\;\\{"), "\\;")))
      .select($"device_id", from_json($"col", type4).as("data2"))
      .selectExpr("device_id", "case when cast(data2.queueLength as double) >=150 then 150 else cast(data2.queueLength as double) end as queueLength", "(cast(data2.queueVehicleNumber as double)) as queueVehicleNumber")
      .where("queueVehicleNumber !=0 and queueLength >0")
    val stream2 = statusDff.writeStream
      .format("parquet")
      .outputMode("append")
      .foreachBatch((df: DataFrame, num: Long) => {
        df.cache()
        val roadName = spark.read.jdbc(url, table = "dim_own_szdl_section_relation",prop).selectExpr("device_id","road_name","start_end_dot")
        val roadFlow = df.join(roadName, Seq("device_id"), "inner")
        val avgQueuelength = roadFlow
          .where("queueLength !=0")
          .groupBy("road_name", "start_end_dot")
          .agg(avg("queueLength").as("avg_queuelength"),
            max("queueLength").as("max_queuelength"))
          .selectExpr("road_name", "start_end_dot", "avg_queuelength", "max_queuelength")
        avgQueuelength.repartition(1).write.mode(SaveMode.Overwrite).save(s"file:///${tmpPath}status_df_road/")
        df.unpersist()
        val checkpointDir = new File(s"${tmpPath}track\\_temporary")
        while (checkpointDir.exists()) {
          Thread.sleep(10000)
          println()
          println("tmp_file 存在")
          println()
        }
        val sparkDf = spark.read.parquet(s"${tmpPath}track\\").selectExpr("timestamp as timeStamp", "device_id", "target_id as targetId", "speed", "lane_no as laneNo", "target_length as targetLength").cache()
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
