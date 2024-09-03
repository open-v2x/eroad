# -*- coding: utf-8 -*-
import asyncio
import datetime
import gc
from kafka import KafkaConsumer , KafkaProducer
import json
from pyspark import Row
from pyspark.sql import SparkSession
from pyspark.sql.functions import *


bootstrap_servers = ['bootstrap_servers']
producer = KafkaProducer(bootstrap_servers=bootstrap_servers)
def ye_min_10_ago_data_time():
    now = datetime.datetime.now()
    ten_minutes_ago = now - datetime.timedelta(minutes=10 + 10 * 6 * 24)
    yesterday = ten_minutes_ago
    ye_str = str(yesterday.minute).zfill(2)
    if str(int(ye_str) // 10 * 10) == '0':
        return ten_minutes_ago.strftime("%Y-%m-%d %H:") + str(int(ye_str) // 10 * 10) + "0:00"
    else:
        return ten_minutes_ago.strftime("%Y-%m-%d %H:") + str(int(ye_str) // 10 * 10) + ":00"
def week_min_10_ago_data_time():
    now = datetime.datetime.now()
    ten_minutes_ago = now - datetime.timedelta(minutes=10 + 10 * 6 * 24 * 7)
    yesterday = ten_minutes_ago
    ye_str = str(yesterday.minute).zfill(2)
    if str(int(ye_str) // 10 * 10) == '0':
        return ten_minutes_ago.strftime("%Y-%m-%d %H:") + str(int(ye_str) // 10 * 10) + "0:00"
    else:
        return ten_minutes_ago.strftime("%Y-%m-%d %H:") + str(int(ye_str) // 10 * 10) + ":00"
def minute_10_ago():
    now = datetime.datetime.now()
    ten_minutes_ago = now - datetime.timedelta(minutes=10)
    minute_str = str(ten_minutes_ago.minute).zfill(2)
    if str(int(minute_str) // 10 * 10) == '0':
        return ten_minutes_ago.strftime("%Y-%m-%d %H:") + str(int(minute_str) // 10 * 10) + "0:00"
    else:
        return ten_minutes_ago.strftime("%Y-%m-%d %H:") + str(int(minute_str) // 10 * 10) + ":00"
def week_00():
    now = datetime.datetime.now()
    ten_minutes_ago = now - datetime.timedelta(minutes=10 * 6 * 24 * 7)
    return ten_minutes_ago.strftime("%Y-%m-%d") + " 00:00:00"
def week_23():
    now = datetime.datetime.now()
    ten_minutes_ago = now - datetime.timedelta(minutes=10 * 6 * 24 * 7)
    return ten_minutes_ago.strftime("%Y-%m-%d") + " 23:59:59"

def process_df(spark , df):
    url = "url"
    username = "username"
    password = "password"
    properties = {
    "user": username,
    "password": password,
    "useUnicode": "true",
    "characterEncoding": "utf-8",
    "driver": "com.mysql.cj.jdbc.Driver"
    }
    tci = spark.read.jdbc(url=url , table="ads_sc_rad_tci_10min" , properties=properties)
    tci.show()
    tci_ye_df = tci.where(f"data_time = '{ye_min_10_ago_data_time()}'")
    tci_week_df = tci.where(f"data_time = '{week_min_10_ago_data_time()}'")

    if tci_ye_df.count() == 0:
        tci_ye = 1
    else:
        tci_ye = tci_ye_df.select("tci").first()[0]
    if tci_week_df.count() == 0:
        tci_week = 1.5
    else:
        tci_week = tci_week_df.select("tci").first()[0]
    tci_cross = spark.read.jdbc(url=url , table="ads_sc_rad_road_tci_10min" , properties=properties).where(f"data_time = '{week_min_10_ago_data_time()}' or data_time = '{ye_min_10_ago_data_time()}'")
    tci_cross_ye_df = tci_cross.where(f"data_time = '{ye_min_10_ago_data_time()}'").selectExpr("ifnull(crossroad_name,'') as crossroad_name" , "tci as tci_ye")
    tci_cross_week_df = tci_cross.where(f"data_time = '{week_min_10_ago_data_time()}'").selectExpr("ifnull(crossroad_name,'') as crossroad_name" , "tci as tci_week")
    df = df.selectExpr("cross_name" , "cross_id" , "device_id" , "laneno" , "avg_speed")
    status = spark.read.parquet("file:///path/tmp_file/status_df_six/").selectExpr("cross_name" , "cross_id" , "device_id" , "laneno" , "avg_queuelength")
    flow = spark.read.parquet("file:///path/tmp_file/flow_df_six/").selectExpr("cross_name" , "cross_id" , "device_id" , "laneno" , "sum_flow")
    device_free_speed = spark.read.jdbc(url=url, table = "dim_own_szdl_section_relation",properties=properties).selectExpr("device_id","40 as free_flow_speed").groupBy("device_id","free_flow_speed").count()
    merge_df = df.join(status , ["cross_name" , "cross_id" , "device_id" , "laneno"] , "left").join(flow , ["cross_name" , "cross_id" , "device_id" , "laneno"] , "left").join(device_free_speed , ["device_id"] , "left").selectExpr("cross_name" , "cross_id" , "device_id" , "laneno" ,
        "case when avg_queuelength is null then 0 else avg_queuelength end as avg_queuelength" , "ifnull(sum_flow,0) as sum_flow" , "avg_speed" , "case when free_flow_speed is null then avg_speed when avg_speed >free_flow_speed  then avg_speed else free_flow_speed end as free_flow_speed" ,
        "case when free_flow_speed is null then 1 when avg_speed >free_flow_speed then 1 else free_flow_speed/avg_speed end  as tra_time_index")
    status_gy = spark.read.jdbc(url=url , table="ads_traffic_signal_control_optimizing_laneno_index_10min" , properties=properties).where(f"data_time >= '{week_00()}' and data_time <= '{week_23()}' and avg_queuelength>0").selectExpr("data_time" , "cross_name" , "cross_id" , "device_id" , "laneno" ,
        "avg_queuelength")
    flow_gy = spark.read.jdbc(url=url , table="ads_traffic_signal_control_optimizing_laneno_index_10min" , properties=properties).where(f"data_time >= '{week_00()}' and data_time <= '{week_23()}'").selectExpr("data_time" , "cross_name" , "cross_id" , "device_id" , "laneno" ,
        "sum_flow")
    merge_df_gui_yi = status_gy.join(flow_gy , ["cross_name" , "cross_id" , "device_id" , "laneno"] , "full").groupBy("cross_name" , "cross_id" , "device_id" , "laneno").agg(max("sum_flow").alias("max_sum_flow") , min("sum_flow").alias("min_sum_flow") ,
        max("avg_queuelength").alias("max_avg_queuelength") , min("avg_queuelength").alias("min_avg_queuelength")).selectExpr("cross_name" , "cross_id" , "device_id" , "laneno" , "case when max_sum_flow is null then 90 when max_sum_flow<90 then 90 else max_sum_flow end as max_sum_flow" ,
        "ifnull(min_sum_flow,0) as min_sum_flow" , "case when max_avg_queuelength is null then 50 when max_avg_queuelength <50 then 50 else max_avg_queuelength end  as max_avg_queuelength" , "ifnull(min_avg_queuelength,0) as min_avg_queuelength")
    alpha = 0.7
    beta = 0.3
    merge_df_qz = merge_df.join(merge_df_gui_yi , ["cross_name" , "cross_id" , "device_id" , "laneno"] , "left").selectExpr("cross_name" , "cross_id" , "device_id" , "laneno" , "avg_speed" , "free_flow_speed" , "tra_time_index" , "sum_flow" ,
        "case when max_sum_flow < sum_flow then sum_flow else max_sum_flow end as max_sum_flow" , "min_sum_flow" , "avg_queuelength" , "case when max_avg_queuelength < avg_queuelength then avg_queuelength else max_avg_queuelength end as max_avg_queuelength" , "min_avg_queuelength").selectExpr(
        "cross_name" , "cross_id" , "device_id" , "laneno" , "avg_speed" , "free_flow_speed" , "tra_time_index" , "sum_flow" , "max_sum_flow" , "min_sum_flow" ,
        "case when ((sum_flow-min_sum_flow)/(max_sum_flow-min_sum_flow)) is null then 0 when ((sum_flow-min_sum_flow)/(max_sum_flow-min_sum_flow)) < 0 then 0 else ((sum_flow-min_sum_flow)/(max_sum_flow-min_sum_flow)) end as sum_flow_gui_yi" , "avg_queuelength" , "max_avg_queuelength" ,"min_avg_queuelength" ,
        "case when ((avg_queuelength-min_avg_queuelength)/(max_avg_queuelength-min_avg_queuelength)) <0 then 0 when  ((avg_queuelength-min_avg_queuelength)/(max_avg_queuelength-min_avg_queuelength)) is null then 0 else  ((avg_queuelength-min_avg_queuelength)/(max_avg_queuelength-min_avg_queuelength)) end as avg_queuelength_gui_yi").selectExpr(
        "cross_name" , "cross_id" , "device_id" , "laneno" , "avg_speed" , "free_flow_speed" , f"(tra_time_index * ({alpha}*sum_flow_gui_yi + {beta}*avg_queuelength_gui_yi)) as tra_time_index" , "sum_flow" , "avg_queuelength").cache()
    merge_df_qz.selectExpr(f"cross_name as crossroad_name" , "cross_id as crossroad_id" , "device_id" , "laneno" , "avg_speed" , "(tra_time_index-0)/(2.5-0)*(10-0)+0 as tci" , "sum_flow as tci_cnt" , "avg_queuelength").groupBy("crossroad_name" , "crossroad_id" , "device_id" , "laneno" , "tci").agg(
        count("tci")).selectExpr(f"'{minute_10_ago()}' as data_time" , "crossroad_name" , "crossroad_id" , "device_id" , "laneno" , "tci")
    cross_merge = merge_df_qz.groupBy("cross_name" , "cross_id").agg(sum("sum_flow").alias("tci_cnt") , avg("avg_queuelength").alias("avg_queuelength") , max("tra_time_index").alias("tci")).selectExpr("cross_name" , "cross_id" , "tci_cnt" , "avg_queuelength" , "tci" , "1")
    cross_merge_df = cross_merge.selectExpr("tci" , "cross_name" , "cross_id" , "tci_cnt" , "avg_queuelength").selectExpr("(tci-0)/(2.5-0)*(10-0)+0 as tci" , "cross_name" , "cross_id" , "tci_cnt" , "avg_queuelength")
    cross_merge_df_state = cross_merge_df.withColumn("congestion_state" ,
        when(cross_merge_df.tci < 2 , "畅通").when((cross_merge_df.tci >= 0) & (cross_merge_df.tci < 2) , "畅通").when((cross_merge_df.tci >= 2) & (cross_merge_df.tci < 4) , "基本畅通").when((cross_merge_df.tci >= 4) & (cross_merge_df.tci < 6) , "轻度拥堵").when(
            (cross_merge_df.tci >= 6) & (cross_merge_df.tci < 8) , "中度拥堵").when((cross_merge_df.tci >= 8) & (cross_merge_df.tci < 10) , "严重拥堵").otherwise("严重拥堵")).selectExpr("cross_name as crossroad_name" , "cross_id as crossroad_id" , "tci" , "tci/10 as tci_rate" , "congestion_state" ,
        "tci_cnt" , "avg_queuelength")
    cross_total_merge_df = cross_merge_df_state.agg(avg("tci").alias("tci") , sum("tci_cnt").alias("tci_cnt") , avg("avg_queuelength").alias("avg_queuelength"))
    cross_total_merge_df_state = cross_total_merge_df.withColumn("congestion_state" ,
        when(cross_total_merge_df.tci < 2 , "畅通").when((cross_total_merge_df.tci >= 0) & (cross_total_merge_df.tci < 2) , "畅通").when((cross_total_merge_df.tci >= 2) & (cross_total_merge_df.tci < 4) , "基本畅通").when((cross_total_merge_df.tci >= 4) & (cross_total_merge_df.tci < 6) ,
            "轻度拥堵").when((cross_total_merge_df.tci >= 6) & (cross_total_merge_df.tci < 8) , "中度拥堵").when((cross_total_merge_df.tci >= 8) & (cross_total_merge_df.tci < 10) , "严重拥堵").otherwise("严重拥堵")).selectExpr("'total' as crossroad_name" , "'total' as crossroad_id" , "tci" ,
        "tci/10 as tci_rate" , "congestion_state" , "tci_cnt" , "avg_queuelength")
    all_tci_df = cross_merge_df_state.union(cross_total_merge_df_state).join(tci_cross_ye_df , ["crossroad_name"] , "left").join(tci_cross_week_df , ["crossroad_name"] , "left").selectExpr(f"'{minute_10_ago()}' as data_time" , "crossroad_name" , "crossroad_id" , "tci" , "tci_rate" ,
        "congestion_state" , "tci_cnt" , "avg_queuelength" , "ifnull(tci/tci_ye-1,0) as h_ratio" , "ifnull(tci/tci_week-1,0) as t_ratio")
    all_tci_df.where("crossroad_name = 'total'").selectExpr("data_time" , "tci" , "tci_rate" , "congestion_state" , "h_ratio" , "t_ratio" , "tci_cnt" , "avg_queuelength")#.write.jdbc(url=url , table="ads_sc_rad_tci_10min" , mode="append" , properties=properties)
    all_tci_df.selectExpr("data_time" , "crossroad_name" , "crossroad_id" , "tci" , "congestion_state" , "h_ratio" , "t_ratio" , "row_number() over(partition by data_time order by tci desc) as crossroad_rank" , "tci_cnt" , "avg_queuelength")#.write.jdbc(url=url , table="ads_sc_rad_road_tci_10min" ,mode="append" , properties=properties)
    all_tci_df.show()
    merge_df_qz.unpersist()
async def consume_messages():
    consumer = KafkaConsumer('caikong-rad-track' ,
        bootstrap_servers=bootstrap_servers)
    await poll_consumer(consumer)
one_list = []
async def poll_consumer(consumer):
    global one_list
    print("瞬时拉取kafka")
    while True:
        msg = await loop.run_in_executor(None , consumer.poll)
        if not msg:
            continue
        for tp , msgs in msg.items():
            for msg in msgs:
                value = msg.value.decode('utf-8')
                dict_msg = json.loads(value)
                device_id = dict_msg.get("device_id")
                cross_name = dict_msg.get("cross_name")
                timestamp = dict_msg.get("timeStamp")
                cross_id = dict_msg.get("cross_id")
                if 'targets' in dict_msg.keys() and dict_msg.get("targets") != '[]':
                    for target in dict_msg["targets"]:
                        targetType = str(target.get("targetType"))
                        if targetType == '3' or targetType == '4' or targetType == '5':
                            target_id = target.get("targetId")
                            speed = target.get("speed")
                            lane_no = target.get("laneNo")
                            target_length = target.get("targetLength")
                            one_list.append((timestamp , cross_name , cross_id , device_id , target_id , speed , lane_no , target_length))

async def statistics():
    global one_list
    try:
        spark = SparkSession.builder.config("spark.jars.packages" , "org.apache.spark:spark-sql-kafka-0-10_2.12:3.0.0").config("spark.jars", "./mysql-connector-java-8.0.25.jar").appName("py_spark_tci").getOrCreate()
        spark.conf.set("spark.sql.shuffle.partitions" , "2")
        spark.conf.set("spark.default.parallelism" , "2")
        spark.sparkContext.setLogLevel("warn")
        while True:
            now = datetime.datetime.now()
            if now.minute < 10:
                target_time = now.replace(minute=10 , second=0 , microsecond=0)
            elif now.minute < 20:
                target_time = now.replace(minute=20 , second=0 , microsecond=0)
            elif now.minute < 30:
                target_time = now.replace(minute=30 , second=0 , microsecond=0)
            elif now.minute < 40:
                target_time = now.replace(minute=40 , second=0 , microsecond=0)
            elif now.minute < 50:
                target_time = now.replace(minute=50 , second=0 , microsecond=0)
            else:
                target_time = (now + datetime.timedelta(hours=1)).replace(minute=0 , second=0 , microsecond=0)
            print('下一个整分：' , target_time)
            time_diff = (target_time - now).total_seconds()
            print(time_diff)
            await asyncio.sleep(20)
            print(len(one_list))
            if len(one_list) > 0:
                rdd = spark.sparkContext.parallelize(one_list)
                sample_rdd = rdd.sample(False , 0.1)
                start = sample_rdd.map(lambda x:Row(timestamp=x[0] , cross_name=x[1] , cross_id=x[2] , device_id=x[3] , target_id=x[4] , speed=x[5] , lane_no=x[6] , target_length=x[7])).toDF(
                    ["timestamp" , "cross_name" , "cross_id" , "device_id" , "target_id" , "speed" , "lane_no" , "target_length"]).repartition(4).cache()
                one_list = []
                df = start.selectExpr("ifnull(cross_name,'') as cross_name" , "ifnull(cross_id,'') as cross_id" , "device_id" , "abs(speed) as speed" , "target_id" , "lane_no as laneno").where("speed >=20").groupBy("cross_name" , "cross_id" , "device_id" , "target_id" , "laneno").agg(
                    avg("speed").alias("target_avg_speed")).groupBy("cross_name" , "cross_id" , "device_id" , "laneno").agg(avg("target_avg_speed").alias("avg_speed")).repartition(4).cache()
                process_df(spark , df)
                print("任务执行完成时间:" , datetime.datetime.now())
                start.unpersist()
                df.unpersist()
                spark.catalog.clearCache()
                gc.collect()
    except Exception as e:
        print(f"Error occurred in statistics: {e}")
if __name__ == '__main__':
    loop = asyncio.get_event_loop()
    loop.create_task(consume_messages())
    loop.create_task(statistics())
    try:
        loop.run_forever()
    except KeyboardInterrupt:
        pass

    loop.stop()
    loop.close()
