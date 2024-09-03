package cn.eroad.app.ioc

import cn.eroad.ioc_spark_utils.utilsSpark
import cn.eroad.ioc_spark_utils.utilsSpark.{kafkaBootstrapServersRadSdk, tenMinutesAgo,url, password, username}
import org.apache.spark.sql.streaming.Trigger
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}
import org.apache.spark.storage.StorageLevel
import java.util.Properties

object AdsEventCount {
  def main(args: Array[String]): Unit = {
    println("utilsSpark:"+kafkaBootstrapServersRadSdk+" "+utilsSpark.url+" "+username+" "+password+" "+utilsSpark.tmpPath)
    val spark: SparkSession = SparkSession.builder().master("local[1]").appName("adsEventCount").enableHiveSupport().getOrCreate()
    spark.sql("select '启动程序运行中，运行时需确认kafka,mysql配置是否正确' as spark").show(false)
    spark.conf.set("spark.sql.autoBroadcastJoinThreshold", -1)
    spark.sqlContext.setConf("spark.sql.shuffle.partitions", "12")
    spark.sqlContext.setConf("spark.default.parallelism", "12")
    val prop = new Properties()
    prop.put("user",username)
    prop.put("password", password)
    val df = spark.readStream.format("kafka")
      .option("kafka.bootstrap.servers", kafkaBootstrapServersRadSdk)
      .option("subscribe", "caikong-rad-track")
      .option("serializer.encoding", "UTF8")
      .option("startingOffsets", "latest")
      .option("failOnDataLoss", "false")
      .option("kafka.max.poll.interval.ms", "600000").load()
    import org.apache.spark.sql.functions._
    import spark.implicits._
    val inputStreamSchema = new StructType()
      .add("crossName", "string")
      .add("deviceId", "string")
      .add("targets", "string")
    val typeSchema = new StructType()
      .add("targetId", "string")
      .add("speed", "string")
      .add("targetType", "string")
    val dff: DataFrame = df.selectExpr("cast(value as string)").as[String]
      .select(from_json($"value", inputStreamSchema).as("data"))
      .select("data.*")
      .select($"crossName", $"deviceId", explode(split(regexp_replace(regexp_replace($"targets", "\\[|\\]", ""), "\\}\\,\\{", "\\}\\;\\{"), "\\;")))
      .select($"crossName", $"deviceId", from_json($"col", typeSchema).as("data2")).selectExpr("crossName", "deviceId", "data2.targetId ", "data2.speed", "data2.targetType")

    val query = dff.writeStream
      .format("parquet")
      .outputMode("append")
      .foreachBatch((df: DataFrame, num: Long) => {
        df.where("crossName is not null and targetId is not null and targetType !='1' and targetType !='2' ").persist(StorageLevel.MEMORY_AND_DISK)
        print(df.count())
        val roadName = spark.read.jdbc(url, table = "dim_own_szdl_section_relation", prop).selectExpr("deviceId", "roadName", "start_end_dot as crossroad_s_e")
        val roadLevel = spark.read.jdbc(url, table = "ads_sc_rad_road_control_optimizing_index_10min", prop).selectExpr("roadName", "roadLevel").groupBy("roadName", "roadLevel").count()
        val roadInfo = roadName.join(roadLevel, Seq("roadName"), "inner").selectExpr("deviceId", "roadName", "crossroad_s_e", "roadLevel")
        val roadTrack = df.join(roadInfo, Seq("deviceId"), "inner")
          .selectExpr(s"'$tenMinutesAgo' as dataTime", "roadName", "crossroad_s_e", "roadLevel", "deviceId", "crossName", "cast(abs(speed) as double) as speed", "targetId")

        val illegalStop = roadTrack.where("speed>=60 and speed<120")
          .selectExpr("roadName", "crossroad_s_e", "roadLevel", "deviceId", "crossName", "speed", "targetId")
          .groupBy("deviceId", "targetId", "crossName", "roadName", "crossroad_s_e", "roadLevel")
          .agg(count("targetId").as("durTime"))
          .selectExpr("targetId", "crossName", "roadName", "crossroad_s_e", "roadLevel", "durTime")
          .where("durTime>1800")
          .groupBy("crossName", "roadName", "crossroad_s_e", "roadLevel")
          .agg(count("targetId").as("eventNum"))
          .selectExpr(s"'$tenMinutesAgo' as dataTime", "crossName", "roadName", "crossroad_s_e", "roadLevel", "'违停' as eventType", "eventNum")
          .persist(StorageLevel.MEMORY_AND_DISK)

        val highSpeed = roadTrack.where("speed>=60 and speed<120")
          .groupBy("deviceId", "targetId", "crossName", "roadName", "crossroad_s_e", "roadLevel")
          .agg(count("targetId").as("durTime"))
          .selectExpr("targetId", "crossName", "roadName", "crossroad_s_e", "roadLevel", "durTime")
          .where("durTime>30 and durTime<=150")
          .groupBy("crossName", "roadName", "crossroad_s_e", "roadLevel")
          .agg(count("targetId").as("eventNum"))
          .selectExpr(s"'$tenMinutesAgo' as dataTime", "crossName", "roadName", "crossroad_s_e", "roadLevel", "'超速' as eventType", "eventNum")
          .persist(StorageLevel.MEMORY_AND_DISK)

        val superHighSpeed = roadTrack.where("speed>=120 and speed<150")
          .groupBy("deviceId", "targetId", "crossName", "roadName", "crossroad_s_e", "roadLevel")
          .agg(count("targetId").as("durTime"))
          .selectExpr("targetId", "crossName", "roadName", "crossroad_s_e", "roadLevel", "durTime")
          .where("durTime>24 and durTime<=75")
          .groupBy("crossName", "roadName", "crossroad_s_e", "roadLevel")
          .agg(count("targetId").as("eventNum"))
          .selectExpr(s"'$tenMinutesAgo' as dataTime", "crossName", "roadName", "crossroad_s_e", "roadLevel", "'超高速' as eventType", "eventNum")
          .persist(StorageLevel.MEMORY_AND_DISK)

        val eventTotal = illegalStop.union(highSpeed).union(superHighSpeed)
          .groupBy("crossName", "roadName", "crossroad_s_e", "roadLevel")
          .agg(sum("eventNum").as("eventNum"))
          .selectExpr(s"'$tenMinutesAgo' as dataTime", "crossName", "roadName", "crossroad_s_e", "roadLevel", "'全部违规事件' as eventType", "eventNum")

        val eventSec = illegalStop.union(highSpeed).union(superHighSpeed)
          .persist(StorageLevel.MEMORY_AND_DISK)

        val illegalStopTotal = eventSec.where("eventType='违停'  ")
          .agg(sum("eventNum").as("eventNum"))
          .selectExpr(s"'$tenMinutesAgo' as dataTime", "'违停' as eventType", "eventNum")

        val highSpeedTotal = eventSec.where("eventType='超速' ")
          .agg(sum("eventNum").as("eventNum"))
          .selectExpr(s"'$tenMinutesAgo' as dataTime", "'超速' as eventType", "eventNum")

        val superHighSpeedTotal = eventSec.where("eventType='超高速' ")
          .agg(sum("eventNum").as("eventNum"))
          .selectExpr(s"'$tenMinutesAgo' as dataTime", "'超高速' as eventType", "eventNum")

        val eventTotal2 = eventSec
          .agg(sum("eventNum").as("eventNum"))
          .selectExpr(s"'$tenMinutesAgo' as dataTime", "'全部违规事件' as eventType", "eventNum")

        val eventCount = illegalStop.union(highSpeed).union(superHighSpeed).union(eventTotal).union(eventTotal2).union(illegalStopTotal)
          .union(highSpeedTotal).union(superHighSpeedTotal)

        eventCount.show()

        eventSec.unpersist()
        eventCount.unpersist()
        df.unpersist()
        illegalStop.unpersist()
        highSpeed.unpersist()
        superHighSpeed.unpersist()
        ()
      })
      .trigger(Trigger.ProcessingTime("10 minutes"))
      .start()

    query.awaitTermination()
  }
}
