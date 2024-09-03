package cn.eroad.app.ioc

import cn.eroad.ioc_spark_utils.utilsSpark
import cn.eroad.ioc_spark_utils.utilsSpark.{kafkaBootstrapServersRadSdk, tenMinutesAgo}
import org.apache.spark.sql.streaming.Trigger
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{DataFrame, SparkSession}
import java.util.Properties


object AdsScYiChuindex {
  def main(args: Array[String]): Unit = {
    val spark: SparkSession = SparkSession.builder().master("local[1]").appName("ads_sc_rad_road_overflow_rate_10min").enableHiveSupport().getOrCreate()
    spark.sqlContext.setConf("spark.sql.shuffle.partitions", "3")
    spark.sqlContext.setConf("spark.default.parallelism", "3")
    val statusDf = spark.readStream.format("kafka")
      .option("kafka.bootstrap.servers", kafkaBootstrapServersRadSdk)
      .option("subscribe", "caikong-rad-status")
      .option("serializer.encoding", "UTF8")
      .option("startingOffsets", "latest")
      .option("kafka.max.poll.interval.ms", "600000")
      .load()
    import org.apache.spark.sql.functions._
    import spark.implicits._
    val InputStreamSchem3 = new StructType()
      .add("cross_name", "string")
      .add("cross_id", "string")
      .add("device_id", "string")
      .add("trafficStatusInformations", "string")
    val type4 = new StructType()
      .add("aisleId", "string")
      .add("queueLength", "double")
      .add("queueVehicleNumber", "double")


    val statusDff: DataFrame = statusDf.selectExpr("cast(value as string)").as[String]
      .select(from_json($"value", InputStreamSchem3).as("data"))
      .select("data.*")
      .select($"cross_name", $"cross_id", $"device_id", explode(split(regexp_replace(regexp_replace($"trafficStatusInformations", "\\[|\\]", ""), "\\}\\,\\{", "\\}\\;\\{"), "\\;")))
      .select($"cross_name", $"cross_id", $"device_id", from_json($"col", type4).as("data2"))
      .selectExpr("ifnull(cross_name,'') as cross_name", "ifnull(cross_id,'') as cross_id", "ifnull(device_id,'') as device_id", "case when cast(data2.queueLength as double) >=150 then 150 else cast(data2.queueLength as double) end as queue_length", "cast(data2.queueVehicleNumber as double) as queue_num", "data2.aisleId as laneno")
      .where("queue_num !=0 and queue_length >0")

    val url = utilsSpark.url
    val prop = new Properties()
    prop.put("user", utilsSpark.username)
    prop.put("password", utilsSpark.password)


    val query = statusDff.repartition(1).writeStream
      .format("parquet")
      .outputMode("append")
      .foreachBatch((df: DataFrame, num: Long) => {
        df.cache()
        val maxQueueLength = df.selectExpr("cross_name", "cross_id", "device_id", "laneno", "case when queue_length >150 then 150 else queue_length end as queue_length").groupBy("cross_name", "cross_id", "device_id", "laneno").agg(max("queue_length").as("max_queue_length"))

        spark.read.jdbc(url, table = "ads_traffic_signal_control_optimizing_laneno_index_10min",prop).createOrReplaceTempView("ads_traffic_signal_control_optimizing_laneno_index_10min")

        val weekQueueLength = spark.sql("""select device_id,laneno,length as road_length,percentile(max_queuelength,0.90) as per_max_queue_length from ads_traffic_signal_control_optimizing_laneno_index_10min where substring(data_time,1,10) = (select substring(data_time,1,10) from  ads_traffic_signal_control_optimizing_laneno_index_10min order by data_time desc limit 1) group by device_id,laneno,length""")


        val overflowRateData = maxQueueLength.join(weekQueueLength, Seq("device_id", "laneno"), "left").selectExpr("cross_name", "cross_id", "laneno", "device_id", "max_queue_length", "per_max_queue_length", "road_length").selectExpr("cross_name", "cross_id", "laneno", "device_id", "case when max_queue_length/per_max_queue_length <1.5 and max_queue_length/per_max_queue_length>=1 then max_queue_length/per_max_queue_length  when max_queue_length/road_length <1.5 and max_queue_length/road_length>=1  then max_queue_length/road_length else 0 end as overflow_rate").where("overflow_rate !=0")

        val overflowRate = overflowRateData.groupBy("cross_name", "cross_id").agg(max("overflow_rate").as("overflow_rate"))

        val totalOverflowRate = overflowRate.agg(avg("overflow_rate").as("overflow_rate")).selectExpr(s"'$tenMinutesAgo' as data_time", "'total' as crossroad_name", "'total' as crossroad_id", "overflow_rate")
        totalOverflowRate.show()
        df.unpersist()
        ()
      })
      .trigger(Trigger.ProcessingTime("10 minutes"))
      .start()
    query
      .awaitTermination()
  }
}
