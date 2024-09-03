# -*- coding: utf-8 -*-
import pymysql
from flask import Flask, request
import os

db_config = {
    'host': os.getenv('DB_HOST', 'default_host'),
    'user': os.getenv('DB_USER', 'default_user'),
    'passwd': os.getenv('DB_PASSWD', 'default_passwd'),
    'db': os.getenv('DB_NAME', 'default_db'),
    'charset': 'utf8',
    'port': int(os.getenv('DB_PORT', 3306))  # 将端口转换为整数
}
app = Flask(__name__)

@app.route('/service-api/exposureAPIS/path/rad/road_cross_relation_list' , methods=['POST' , 'get'])
def road_cross_relation_list():
    try:

        conn = pymysql.connect(**db_config)
        cursor = conn.cursor()

        sql = """select 
                road_name,
                crossroad_name
                from road_cross_relation_list
                """

        cursor.execute(sql)
        results = cursor.fetchall()

        columns = [column[0] for column in cursor.description]

        data = []
        for row in results:
            data.append(dict(zip(columns , row)))

        cursor.close()
        conn.close()

        end_data = {

            "code":200 , "msg":"do success" , "success":True , "data":data

        }

        return end_data

    except Exception as e:
        return str(e)


@app.route('/service-api/exposureAPIS/path/rad/road_tongxing_tian' , methods=['POST' , 'get'])
def road_tongxing_tian():
    try:
        start_date = request.json['start_date']
        end_date = request.json['end_date']
        road_name = request.json['road_name']

        conn = pymysql.connect(**db_config)
        cursor = conn.cursor()

        sql = f"""SELECT LEFT
	( data_time, 10 ) AS data_time,
	avg( tra_efficiency ) AS tra_efficiency,
	avg( saturation ) AS saturation 
        FROM
  (select data_time,tra_efficiency,saturation from 	`ads_sc_rad_road_thematic_four_index_h` 
        WHERE
		data_time>=concat('2024-06-10',' 00')
	AND data_time <= concat('2024-06-16',' 23')
	AND road_name = '{road_name}')a
        GROUP BY
	LEFT (
		data_time,
	10)
                """
        cursor.execute(sql)
        results = cursor.fetchall()

        columns = [column[0] for column in cursor.description]

        data = []
        for row in results:
            data.append(dict(zip(columns , row)))

        cursor.close()
        conn.close()

        end_data = {

            "code":200 , "msg":"do success" , "success":True , "data":data

        }

        return end_data

    except Exception as e:
        return str(e)



@app.route('/service-api/exposureAPIS/path/rad/road_tongxing_duan' , methods=['POST' , 'get'])
def road_tongxing_duan():
    try:
        road_name = request.json['road_name']
        conn = pymysql.connect(**db_config)
        cursor = conn.cursor()

        sql = f"""SELECT RIGHT(LEFT(data_time, 13), 2) AS data_time,
            avg(tra_efficiency) AS tra_efficiency,
            avg(saturation) AS saturation 
        FROM
            (select data_time, tra_efficiency, saturation from `ads_sc_rad_road_thematic_four_index_h` 
            WHERE
                LEFT(data_time, 10) >= '2024-06-10'
                AND LEFT(data_time, 10) <= '2024-06-16'
                AND road_name = '{road_name}')a
        GROUP BY RIGHT(LEFT(data_time, 13), 2)
        """
        cursor.execute(sql)

        results = cursor.fetchall()

        columns = [column[0] for column in cursor.description]

        data = []
        for row in results:
            data.append(dict(zip(columns , row)))

        cursor.close()
        conn.close()

        end_data = {

            "code":200 , "msg":"do success" , "success":True , "data":data

        }

        return end_data

    except Exception as e:
        return str(e)



@app.route('/service-api/exposureAPIS/path/rad/road_yongdu_tian' , methods=['POST' , 'get'])
def road_yongdu_tian():
    try:

        start_date = request.json['start_date']
        end_date = request.json['end_date']
        road_name = request.json['road_name']

        conn = pymysql.connect(**db_config)
        cursor = conn.cursor()

        sql = f"""SELECT 
	a.data_time,
	avg( a.delay_time_prop ) AS delay_time_prop,
	avg( a.tra_time_index ) AS tra_time_index 
        FROM
  (select left(data_time,10) as data_time,delay_time_prop,tra_time_index from `ads_sc_rad_road_thematic_four_index_h` 
        WHERE
		data_time>=concat('2024-06-10',' 00:00:00')
	AND data_time <= concat('2024-06-16',' 23:50:00')
	AND road_name = '{road_name}')a

        GROUP BY
	a.data_time
        """

        cursor.execute(sql)
        results = cursor.fetchall()

        columns = [column[0] for column in cursor.description]

        data = []
        for row in results:
            data.append(dict(zip(columns , row)))

        cursor.close()
        conn.close()

        end_data = {

            "code":200 , "msg":"do success" , "success":True , "data":data

        }

        return end_data

    except Exception as e:
        return str(e)



@app.route('/service-api/exposureAPIS/path/rad/road_yongdu_duan' , methods=['POST' , 'get'])
def road_yongdu_duan():
    try:
        road_name = request.json['road_name']

        conn = pymysql.connect(**db_config)
        cursor = conn.cursor()

        sql = f"""SELECT RIGHT
	( LEFT ( data_time, 13 ), 2 ) AS data_time,
	avg( delay_time_prop ) AS delay_time_prop,
	avg( tra_time_index ) AS tra_time_index 
        FROM

  (select data_time,delay_time_prop,tra_time_index from 	`ads_sc_rad_road_thematic_four_index_h` 
        WHERE
	LEFT ( data_time, 10 ) >=  '2024-06-10'
	AND LEFT ( data_time, 10 ) <= '2024-06-16'
	AND road_name = '{road_name}' )a
        GROUP BY
	RIGHT (
		LEFT ( data_time, 13 ),
	2)
	        """

        cursor.execute(sql)
        results = cursor.fetchall()

        columns = [column[0] for column in cursor.description]

        data = []
        for row in results:
            data.append(dict(zip(columns , row)))

        cursor.close()
        conn.close()

        end_data = {

            "code":200 , "msg":"do success" , "success":True , "data":data

        }

        return end_data

    except Exception as e:
        return str(e)


@app.route('/service-api/exposureAPIS/path/rad/event_day' , methods=['POST' , 'get'])
def event_day():
    try:
        start_date = request.json['start_date']
        end_date = request.json['end_date']
        road_name = request.json['road_name']

        conn = pymysql.connect(**db_config)
        cursor = conn.cursor()

        sql = f"""	
	SELECT
t.data_time,
t.road_name,
ifnull(a.event_num,0) as chaosu,
ifnull(b.event_num,0) as chaogaosu,
ifnull(c.event_num,0) as weiting
FROM
(select
	 LEFT (data_time,10) as data_time,road_name
	from
   ads_sc_rad_traffic_event_count_h where 	LEFT ( data_time, 10 ) >= '2024-06-10'
	AND LEFT ( data_time, 10 ) <= '2024-06-16' and road_name = '{road_name}'
	group by LEFT (data_time,10),road_name)t
  left join
(select
	 LEFT (data_time,10) as data_time,road_name,event_type,sum(event_num) as event_num
	from
   ads_sc_rad_traffic_event_count_h
  where
	LEFT ( data_time, 10 ) >= '2024-06-10'
	AND LEFT ( data_time, 10 ) <= '2024-06-16'
	AND road_name = '{road_name}'
  AND event_type='超速'
    and cross_name is not null
	group by LEFT (data_time,10),road_name,event_type)a
  on t.data_time = a.data_time and t.road_name = a.road_name
  left join
(select
	 LEFT (data_time,10) as data_time,road_name,event_type,sum(event_num) as event_num
	from
   ads_sc_rad_traffic_event_count_h
  where
	LEFT ( data_time, 10 ) >= '2024-06-10'
	AND LEFT ( data_time, 10 ) <= '2024-06-16'
	AND road_name = '{road_name}'
  AND event_type='超高速'
    and cross_name is not null
	group by LEFT (data_time,10),road_name,event_type)b
    on t.data_time = b.data_time and t.road_name = b.road_name
  left join
(select
	 LEFT (data_time,10) as data_time,road_name,event_type,sum(event_num) as event_num
	from
   ads_sc_rad_traffic_event_count_h
  where
	LEFT ( data_time, 10 ) >= '2024-06-10'
	AND LEFT ( data_time, 10 ) <= '2024-06-16'
	AND road_name = '{road_name}'
  AND event_type='违停'
    and cross_name is not null
	group by LEFT (data_time,10),road_name,event_type)c
  on t.data_time = c.data_time and t.road_name = c.road_name

"""

        cursor.execute(sql)
        results = cursor.fetchall()

        columns = [column[0] for column in cursor.description]

        data = []
        for row in results:
            data.append(dict(zip(columns , row)))

        cursor.close()
        conn.close()

        end_data = {

            "code":200 , "msg":"do success" , "success":True , "data":data

        }

        return end_data

    except Exception as e:
        return str(e)



@app.route('/service-api/exposureAPIS/path/rad/event_hour' , methods=['POST' , 'get'])
def event_hour():
    try:
        road_name = request.json['road_name']

        conn = pymysql.connect(**db_config)
        cursor = conn.cursor()

        sql = f"""SELECT RIGHT
	( LEFT ( data_time, 13 ), 2 ) AS data_time,road_name,event_type,
	sum( event_num ) AS event_num 
FROM
	`ads_sc_rad_traffic_event_count_h` 
WHERE
	LEFT ( data_time, 10 ) >=  '2024-06-10'
	AND LEFT ( data_time, 10 ) <= '2024-06-16'
	AND road_name = '{road_name}'
  AND event_type!='全部违规事件'
    and cross_name is not null
GROUP BY
	RIGHT (LEFT ( data_time, 13 ),2),road_name,event_type"""

        cursor.execute(sql)
        results = cursor.fetchall()

        columns = [column[0] for column in cursor.description]

        data = []
        for row in results:
            data.append(dict(zip(columns , row)))

        cursor.close()
        conn.close()

        end_data = {

            "code":200 , "msg":"do success" , "success":True , "data":data

        }

        return end_data

    except Exception as e:
        return str(e)



@app.route('/service-api/exposureAPIS/path/rad/road_section_cross_flow_tian' , methods=['POST' , 'get'])
def road_section_cross_flow_tian():
    try:
        road_name = request.json['road_name']
        crossroad_name = request.json['crossroad_name']
        small_date = request.json['small_date']
        big_date = request.json['big_date']

        conn = pymysql.connect(**db_config)
        cursor = conn.cursor()

        sql = f"""	
	SELECT
  crossroad_name,
  road_name,
  data_date,
  sum(traffic_flow) as traffic_flow
FROM
  ads_sc_rad_road_section_cross_flow_1h
where 
  road_name = '{road_name}'  
  and crossroad_name = '{crossroad_name}' 
  and data_date >= '2024-06-10'    
  and data_date <= '2024-06-16' 
group by  data_date,road_name,crossroad_name
"""

        cursor.execute(sql)
        results = cursor.fetchall()

        columns = [column[0] for column in cursor.description]

        data = []
        for row in results:
            data.append(dict(zip(columns , row)))

        cursor.close()
        conn.close()

        end_data = {

            "code":200 , "msg":"do success" , "success":True , "data":data

        }

        return end_data

    except Exception as e:
        return str(e)



@app.route('/service-api/exposureAPIS/path/rad/road_section_cross_flow_duan' , methods=['POST' , 'get'])
def road_section_cross_flow_duan():
    try:
        road_name = request.json['road_name']
        crossroad_name = request.json['crossroad_name']

        conn = pymysql.connect(**db_config)
        cursor = conn.cursor()

        sql = f"""SELECT
  crossroad_name,
  road_name,
  substr(data_time,12,2) as data_hour,
  sum(traffic_flow) as traffic_flow
FROM
  ads_sc_rad_road_section_cross_flow_1h
where data_date >= '2024-06-10'
  and data_date <= '2024-06-16'
  and road_name =  '{road_name}'
  and crossroad_name = '{crossroad_name}'
group by substr(data_time,12,2),road_name,crossroad_name""".format(road_name , crossroad_name)

        cursor.execute(sql)
        results = cursor.fetchall()

        columns = [column[0] for column in cursor.description]

        data = []
        for row in results:
            data.append(dict(zip(columns , row)))

        cursor.close()
        conn.close()

        end_data = {

            "code":200 , "msg":"do success" , "success":True , "data":data

        }

        return end_data

    except Exception as e:
        return str(e)



@app.route('/service-api/exposureAPIS/path/rad/cross_prop' , methods=['POST' , 'get'])
def cross_prop():
    try:
        start_date = request.json['start_date']
        end_date = request.json['end_date']

        conn = pymysql.connect(**db_config)
        cursor = conn.cursor()

        sql = f"""SELECT
	a.cross_name,
	avg( a.tra_efficiency ) AS tra_efficiency,
	avg( a.saturation ) AS saturation,
	avg( a.tra_time_index ) AS tra_time_index,
	avg( a.delay_time_prop ) AS delay_time_prop 
FROM
  (select cross_name,tra_efficiency,saturation,tra_time_index,delay_time_prop from `ads_sc_rad_cross_thematic_four_index_h` 
WHERE
	data_time>=concat('2024-06-10',' 00')
	AND data_time <= concat('2024-06-16',' 23')
  and cross_name != ''
  and not cross_name  regexp '^[1-9A-Za-z]'
  )a
GROUP BY
	a.cross_name """

        cursor.execute(sql)
        results = cursor.fetchall()

        columns = [column[0] for column in cursor.description]

        data = []
        for row in results:
            data.append(dict(zip(columns , row)))

        cursor.close()
        conn.close()

        end_data = {

            "code":200 , "msg":"do success" , "success":True , "data":data

        }

        return end_data

    except Exception as e:
        return str(e)



@app.route('/service-api/exposureAPIS/path/rad/cross_prop_m_n' , methods=['POST' , 'get'])
def cross_prop_m_n():
    try:
        start_date = request.json['start_date']
        end_date = request.json['end_date']
        special_time = request.json['special_time']

        conn = pymysql.connect(**db_config)
        cursor = conn.cursor()

        sql = f"""SELECT
	a.cross_name,
	avg( tra_efficiency ) AS tra_efficiency,
	avg( saturation ) AS saturation,
	avg( tra_time_index ) AS tra_time_index,
	avg( delay_time_prop ) AS delay_time_prop 
FROM
	(
	SELECT
		data_time,
		cross_name,
		tra_efficiency,
		saturation,
		tra_time_index,
		delay_time_prop,
	CASE

			WHEN RIGHT ( data_time, 2 ) >= '07' 
			AND RIGHT ( data_time, 2 ) < '09' THEN '早高峰' WHEN RIGHT ( data_time, 2 ) >= '17' 
				AND RIGHT ( data_time, 2 ) < '19' THEN '晚高峰' END AS special_time 

  FROM `ads_sc_rad_cross_thematic_four_index_h` 

  WHERE 	 data_time>=concat('2024-06-10',' 00')
	AND data_time <= concat('2024-06-16',' 23')
        AND cross_name != '' AND NOT cross_name REGEXP '^[1-9A-Za-z]' 
        AND (( RIGHT ( data_time, 2 ) >= '07' 
						AND RIGHT ( data_time, 2 ) < '09' ) OR ( RIGHT ( data_time, 2 ) >= '17' 
						AND RIGHT ( data_time, 2 ) < '19' 
					))) a 
		WHERE
			a.special_time = '{special_time}'
    GROUP BY
			a.cross_name """

        cursor.execute(sql)
        results = cursor.fetchall()
        columns = [column[0] for column in cursor.description]
        data = []
        for row in results:
            data.append(dict(zip(columns , row)))

        cursor.close()
        conn.close()

        end_data = {

            "code":200 , "msg":"do success" , "success":True , "data":data

        }

        return end_data

    except Exception as e:
        return str(e)



@app.route('/service-api/exposureAPIS/path/rad/event_num_rank' , methods=['POST' , 'get'])
def event_num_rank():
    try:
        start_date = request.json['start_date']
        end_date = request.json['end_date']
        event_type = request.json['event_type']
        order_rule = request.json['order_rule']

        conn = pymysql.connect(**db_config)
        cursor = conn.cursor()

        sql = f"""	select c.order_rule,c.cross_name,c.event_type,c.event_num
	from 
	(
	select *
	from
	(SELECT
	'倒序' as order_rule,
	cross_name,
  event_type,
	sum(event_num) as event_num
FROM
	`ads_sc_rad_traffic_event_count_h` 
WHERE
	data_time>=concat('2024-06-10',' 00:00:00')
	AND data_time <= concat('2024-06-16',' 23:50:00')
  and cross_name != '' and cross_name is not null
  and not cross_name  regexp '^[1-9A-Za-z]'
	and event_type='{event_type}'
GROUP BY
	cross_name 
	ORDER BY  event_num desc
	LIMIT 15)a
	union
select * from	
(	SELECT
	'正序' as order_rule,
	cross_name,
  event_type,
	sum(event_num) as event_num
FROM
	`ads_sc_rad_traffic_event_count_h` 
WHERE
	data_time>=concat('2024-06-10',' 00:00:00')
	AND data_time <= concat('2024-06-16',' 23:50:00')
  and cross_name != ''
  and not cross_name  regexp '^[1-9A-Za-z]'
	and event_type='{event_type}'
GROUP BY
	cross_name 
	ORDER BY  event_num asc
	LIMIT 15)b)c
	where c.order_rule='{order_rule}'
	"""

        cursor.execute(sql)
        results = cursor.fetchall()

        columns = [column[0] for column in cursor.description]

        data = []
        for row in results:
            data.append(dict(zip(columns , row)))

        cursor.close()
        conn.close()

        end_data = {

            "code":200 , "msg":"do success" , "success":True , "data":data

        }

        return end_data

    except Exception as e:
        return str(e)



@app.route('/service-api/exposureAPIS/path/rad/eventcount_rank_gaofeng' , methods=['POST' , 'get'])
def eventcount_rank_gaofeng():
    try:
        start_date = request.json['start_date']
        end_date = request.json['end_date']
        event_type = request.json['event_type']
        special_time = request.json['special_time']
        order_rule = request.json['order_rule']

        conn = pymysql.connect(**db_config)
        cursor = conn.cursor()

        sql = f"""select * 
from
(
select *
from
(
select '倒序' as order_rule,cross_name,sum(event_num) as event_num,special_time
from
(SELECT
	cross_name,
  event_type,
	event_num,
	CASE		
			WHEN RIGHT ( data_time, 8 ) >= '07:00:00' 
			AND RIGHT ( data_time, 8 ) <= '08:50:00' THEN '早高峰' WHEN RIGHT ( data_time, 8 ) >= '17:00:00' 
				AND RIGHT ( data_time, 8 ) <= '18:50:00' THEN '晚高峰' END AS special_time 
FROM
	`ads_sc_rad_traffic_event_count_h` 
WHERE
	data_time>=concat('2024-06-10',' 00:00:00')
	AND data_time <= concat('2024-06-16',' 23:50:00')
  and cross_name != ''
  and not cross_name  regexp '^[1-9A-Za-z]'
	and event_type='{event_type}'
	        AND (( RIGHT ( data_time, 8 ) >= '07:00:00' 
						AND RIGHT ( data_time, 8 ) <= '08:50:00' ) OR ( RIGHT ( data_time, 8 ) >= '17:00:00' 
						AND RIGHT ( data_time, 8 ) <= '18:50:00' 
					)))a
where special_time='{special_time}'  and cross_name is not null
GROUP BY
	cross_name 
ORDER BY  event_num desc 
	LIMIT 15
	)c
	union
select *
from
(
select '正序' as order_rule,cross_name,sum(event_num) as event_num,special_time
from
(SELECT
	cross_name,
  event_type,
	event_num,
	CASE		
			WHEN RIGHT ( data_time, 8 ) >= '07:00:00' 
			AND RIGHT ( data_time, 8 ) <= '08:50:00' THEN '早高峰' WHEN RIGHT ( data_time, 8 ) >= '17:00:00' 
				AND RIGHT ( data_time, 8 ) <= '18:50:00' THEN '晚高峰' END AS special_time 
FROM
	`ads_sc_rad_traffic_event_count_h` 
WHERE
	data_time>=concat('2024-06-10',' 00:00:00')
	AND data_time <= concat('2024-06-16',' 23:50:00')
  and cross_name != ''
  and not cross_name  regexp '^[1-9A-Za-z]'
	and event_type='{event_type}'
	        AND (( RIGHT ( data_time, 8 ) >= '07:00:00' 
						AND RIGHT ( data_time, 8 ) <= '08:50:00' ) OR ( RIGHT ( data_time, 8 ) >= '17:00:00' 
						AND RIGHT ( data_time, 8 ) <= '18:50:00' 
					)))b
where special_time='{special_time}'
GROUP BY
	cross_name 
ORDER BY  event_num asc 
	LIMIT 15
	)d	
	)e
	where e.order_rule='{order_rule}'

	"""

        cursor.execute(sql)
        results = cursor.fetchall()

        columns = [column[0] for column in cursor.description]

        data = []
        for row in results:
            data.append(dict(zip(columns , row)))

        cursor.close()
        conn.close()

        end_data = {

            "code":200 , "msg":"do success" , "success":True , "data":data

        }

        return end_data

    except Exception as e:
        return str(e)



@app.route('/service-api/exposureAPIS/path/rad/road_wait_num' , methods=['POST' , 'get'])
def road_wait_num():
    try:
        crossroad_name = request.json['crossroad_name']

        conn = pymysql.connect(**db_config)
        cursor = conn.cursor()

        sql = f"""select
	a.data_time,
	a.crossroad_name,
	a.crossroad_id,
	a.wait_num,
	a.car_cnt/b.all_car_cnt *100 as cnt_ratio,
	a.car_cnt 
  from
(SELECT
	data_time,
	crossroad_name,
	crossroad_id,
	wait_num,
	cnt_ratio,
	car_cnt 
FROM
	`ads_sc_rad_road_wait_num_cnt_16h` 
WHERE
	data_time = ( SELECT data_time FROM ads_sc_rad_road_wait_num_cnt_16h  ORDER BY data_time DESC LIMIT 1 ) 
	AND crossroad_name = '{crossroad_name}'
GROUP BY
	data_time,
	crossroad_name,
	crossroad_id,
	wait_num,
	cnt_ratio,
	car_cnt)a

left join

(SELECT
	data_time,
	crossroad_name,
	crossroad_id,
	sum(car_cnt) as all_car_cnt 
FROM
	`ads_sc_rad_road_wait_num_cnt_16h` 
WHERE
	data_time = ( SELECT data_time FROM ads_sc_rad_road_wait_num_cnt_16h  ORDER BY data_time DESC LIMIT 1 ) 
	AND crossroad_name = '{crossroad_name}'
GROUP BY
	data_time,
	crossroad_name,
	crossroad_id)b
on a.data_time = b.data_time and a.crossroad_name = b.crossroad_name
	"""

        cursor.execute(sql)
        results = cursor.fetchall()

        columns = [column[0] for column in cursor.description]

        data = []
        for row in results:
            data.append(dict(zip(columns , row)))

        cursor.close()
        conn.close()

        end_data = {

            "code":200 , "msg":"do success" , "success":True , "data":data

        }

        return end_data

    except Exception as e:
        return str(e)





@app.route('/service-api/exposureAPIS/path/rad/wait_num_cnt_16h' , methods=['POST' , 'get'])
def wait_num_cnt_16h():
    try:
        conn = pymysql.connect(**db_config)
        cursor = conn.cursor()

        sql = f"""select
	a.data_time,
	a.crossroad_name,
	a.crossroad_id,
	a.wait_num,
	a.car_cnt/b.all_car_cnt *100 as cnt_ratio,
	a.car_cnt 
  from
(SELECT
	data_time,
	crossroad_name,
	crossroad_id,
	wait_num,
	cnt_ratio,
	car_cnt 
FROM
	`ads_sc_rad_road_wait_num_cnt_16h` 
WHERE
	data_time = ( SELECT data_time FROM ads_sc_rad_road_wait_num_cnt_16h  ORDER BY data_time DESC LIMIT 1 ) 
	AND crossroad_name = 'total'
GROUP BY
	data_time,
	crossroad_name,
	crossroad_id,
	wait_num,
	cnt_ratio,
	car_cnt)a
  
left join
  
(SELECT
	data_time,
	crossroad_name,
	crossroad_id,
	sum(car_cnt) as all_car_cnt 
FROM
	`ads_sc_rad_road_wait_num_cnt_16h` 
WHERE
	data_time = ( SELECT data_time FROM ads_sc_rad_road_wait_num_cnt_16h ORDER BY data_time DESC LIMIT 1 ) 
	AND crossroad_name = 'total'
GROUP BY
	data_time,
	crossroad_name,
	crossroad_id)b
on a.data_time = b.data_time and a.crossroad_name = b.crossroad_name

	"""

        cursor.execute(sql)
        results = cursor.fetchall()

        columns = [column[0] for column in cursor.description]

        data = []
        for row in results:
            data.append(dict(zip(columns , row)))

        cursor.close()
        conn.close()

        end_data = {

            "code":200 , "msg":"do success" , "success":True , "data":data

        }

        return end_data

    except Exception as e:
        return str(e)



@app.route('/service-api/exposureAPIS/path/jtts/actual_flow' , methods=['POST' , 'get'])
def actual_flow():
    try:
        conn = pymysql.connect(**db_config)
        cursor = conn.cursor()

        sql = f"""
select concat(substr(data_time,1,13),':00:00') as data_time,sum(tci_cnt) as total_flow
from 
ads_sc_rad_road_tci_10min
where substr(data_time,1,10)= '2024-06-16'  and right(data_time,8) <= right(now(),8)
group by concat(substr(data_time,1,13),':00:00')
order by concat(substr(data_time,1,13),':00:00')
	"""
        cursor.execute(sql)
        results = cursor.fetchall()

        columns = [column[0] for column in cursor.description]

        data = []
        for row in results:
            data.append(dict(zip(columns , row)))

        cursor.close()
        conn.close()

        end_data = {

            "code":200 , "msg":"do success" , "success":True , "data":data

        }

        return end_data

    except Exception as e:
        return str(e)



@app.route('/service-api/exposureAPIS/path/jtts/pre_flow' , methods=['POST' , 'get'])
def pre_flow():
    try:
        conn = pymysql.connect(**db_config)
        cursor = conn.cursor()

        sql = f"""
select date_h,pred_flow
from ads_sc_rad_predict_total_flow_h
where left(date_h,10) =  '2024-06-16' 
	"""
        cursor.execute(sql)
        results = cursor.fetchall()

        columns = [column[0] for column in cursor.description]

        data = []
        for row in results:
            data.append(dict(zip(columns , row)))

        cursor.close()
        conn.close()

        end_data = {

            "code":200 , "msg":"do success" , "success":True , "data":data

        }

        return end_data

    except Exception as e:
        return str(e)



@app.route('/service-api/exposureAPIS/path/rad/rad_tra_flow_10min' , methods=['POST' , 'get'])
def rad_tra_flow_10min():
    try:
        crossroad_name = request.json['crossroad_name']
        data_time = request.json['data_time']
        conn = pymysql.connect(**db_config)
        cursor = conn.cursor()

        sql = f"""select
a.crossroad_name,
a.data_time,
a.tra_flow,
b.cross_longitude as crossroad_lon,
b.cross_latitude as crossroad_lat,
'car' as obj_type,
0.0 as h_ratio
from
(select crossroad_name,data_time,tci_cnt as tra_flow from ads_sc_rad_road_tci_10min where   substr(data_time,1,10)= '2024-06-16' and right(data_time,8) <= right(now(),8) and crossroad_name = 'total' )a
left join
(select cross_name,cross_longitude,cross_latitude from `dim-rad-rd-device-crossroad` group by cross_name,cross_longitude,cross_latitude)b
on a.crossroad_name = b.cross_name
	"""

        cursor.execute(sql)
        results = cursor.fetchall()

        columns = [column[0] for column in cursor.description]

        data = []
        for row in results:
            data.append(dict(zip(columns , row)))

        cursor.close()
        conn.close()

        end_data = {

            "code":200 , "msg":"do success" , "success":True , "data":data

        }

        return end_data

    except Exception as e:
        return str(e)


@app.route('/service-api/exposureAPIS/path/rad/ads_sc_rad_road_accum_tra_flow' , methods=['POST' , 'get'])
def ads_sc_rad_road_accum_tra_flow():
    try:
        data_time = request.json['data_time']
        rk = request.json['rk']
        conn = pymysql.connect(**db_config)
        cursor = conn.cursor()

        sql = f"""SELECT crossroad_name, crossroad_id, traffic_flow, data_time, data_date, longitude, latitude, 
       (SELECT COUNT(*) FROM (
            SELECT crossroad_name, crossroad_id, sum(tci_cnt) as traffic_flow, max(data_time) as data_time, 
                   left(data_time, 10) as data_date, 0.0 as longitude, 0.0 as latitude
            FROM ads_sc_rad_road_tci_10min
            WHERE right(data_time, 8) <= right(now(), 8)  
                  AND length(crossroad_name) != char_length(crossroad_name) 
            GROUP BY crossroad_name, crossroad_id, left(data_time, 10) 
            ORDER BY traffic_flow DESC
            LIMIT 5
        ) AS subquery 
        WHERE subquery.traffic_flow >= mainquery.traffic_flow) as rk
FROM (
    SELECT crossroad_name, crossroad_id, sum(tci_cnt) as traffic_flow, max(data_time) as data_time, 
           left(data_time, 10) as data_date, 0.0 as longitude, 0.0 as latitude
    FROM ads_sc_rad_road_tci_10min
    WHERE right(data_time, 8) <= right(now(), 8)  
          AND length(crossroad_name) != char_length(crossroad_name) 
    GROUP BY crossroad_name, crossroad_id, left(data_time, 10) 
    ORDER BY traffic_flow DESC
    LIMIT 5
) as mainquery;

	"""
        cursor.execute(sql)
        results = cursor.fetchall()

        columns = [column[0] for column in cursor.description]

        data = []
        for row in results:
            data.append(dict(zip(columns , row)))

        cursor.close()
        conn.close()

        end_data = {

            "code":200 , "msg":"do success" , "success":True , "data":data

        }

        return end_data

    except Exception as e:
        return str(e)


@app.route('/service-api/exposureAPIS/path/rad/ads_sc_rad_tci_10min' , methods=['POST' , 'get'])
def ads_sc_rad_tci_10min():
    try:
        data_time = request.json['data_time']
        conn = pymysql.connect(**db_config)
        cursor = conn.cursor()

        sql = f"""
SELECT
	data_time,
	tci,
	congestion_state,
	tci_rate,
	h_ratio,
	t_ratio 
FROM
	ads_sc_rad_tci_10min 
WHERE
	data_time = '2024-06-16 11:50:00' 
GROUP BY
	data_time,
	tci,
	congestion_state,
	tci_rate,
	h_ratio,
	t_ratio
	"""

        cursor.execute(sql)
        results = cursor.fetchall()

        columns = [column[0] for column in cursor.description]

        data = []
        for row in results:
            data.append(dict(zip(columns , row)))

        cursor.close()
        conn.close()

        end_data = {

            "code":200 , "msg":"do success" , "success":True , "data":data

        }

        return end_data

    except Exception as e:
        return str(e)



@app.route('/service-api/exposureAPIS/path/rad/ads_sc_rad_road_tci_10min' , methods=['POST' , 'get'])
def ads_sc_rad_road_tci_10min():
    try:
        data_time = request.json['data_time']
        crossroad_rank = request.json['crossroad_rank']
        conn = pymysql.connect(**db_config)
        cursor = conn.cursor()

        sql = f"""SELECT
		a.data_time,
		a.crossroad_name,
		a.crossroad_id,
		a.tci,
		a.congestion_state,
		a.crossroad_rank,
		b.cross_longitude AS crossroad_lon,
		b.cross_latitude AS crossroad_lat,
    0 as type

	FROM
		(
		SELECT
			data_time,
			crossroad_name,
			crossroad_id,
			 tci,
			congestion_state,
			crossroad_rank 
		FROM
			ads_sc_rad_road_tci_10min
		WHERE
			crossroad_name != 'total' 
			AND data_time = IFNULL( ( SELECT data_time FROM ads_sc_rad_road_tci_10min WHERE tci>0 and data_time = '2024-06-16 11:50:00' LIMIT 1 ), ( SELECT data_time FROM ads_sc_rad_road_tci_10min where tci >0 ORDER BY data_time DESC LIMIT 1 ) ) 
			AND length( crossroad_name )!= char_length( crossroad_name ) 
			AND '' = LEFT ( crossroad_rank = '5', 0 ) 
		ORDER BY
			tci DESC 
			LIMIT 5 
		) a
		LEFT JOIN ( SELECT cross_name, cross_longitude, cross_latitude FROM ods_sc_rad_devicetocrossroad ) b ON a.crossroad_name = b.cross_name 
	GROUP BY
		a.data_time,
		a.crossroad_name,
		a.crossroad_id,
		a.tci,
		a.congestion_state,
		a.crossroad_rank,
		b.cross_longitude,
		b.cross_latitude 
	ORDER BY
		a.tci DESC 
	"""

        cursor.execute(sql)
        results = cursor.fetchall()

        columns = [column[0] for column in cursor.description]

        data = []
        for row in results:
            data.append(dict(zip(columns , row)))

        cursor.close()
        conn.close()

        end_data = {

            "code":200 , "msg":"do success" , "success":True , "data":data

        }

        return end_data

    except Exception as e:
        return str(e)



@app.route('/service-api/exposureAPIS/path/sc/road_section_avgspeed_10min' , methods=['POST' , 'get'])
def road_section_avgspeed_10min():
    try:
        data_time = request.json['data_time']
        conn = pymysql.connect(**db_config)
        cursor = conn.cursor()

        sql = f"""
select b.data_time,a.road_name,a.crossroad_s_e,a.road_lon_lat,round(b.avg_speed,0) as avg_speed from (SELECT road_name,start_end_dot as crossroad_s_e,section_location as road_lon_lat FROM dim_own_szdl_section_relation group by road_name,start_end_dot,section_location)a join (SELECT data_time,road_name,crossroad_s_e,avg_speed as avg_speed FROM ads_sc_rad_road_control_optimizing_index_10min where data_time = IFNULL(
(select data_time from ads_sc_rad_road_control_optimizing_index_10min where data_time =  '2024-06-16 11:50:00'  limit 1),
(select data_time from ads_sc_rad_road_control_optimizing_index_10min ORDER BY data_time desc limit 1)
))b on a.road_name = b.road_name and a.crossroad_s_e = b.crossroad_s_e
order by avg_speed desc limit 10
	"""

        cursor.execute(sql)
        results = cursor.fetchall()

        columns = [column[0] for column in cursor.description]

        data = []
        for row in results:
            data.append(dict(zip(columns , row)))

        cursor.close()
        conn.close()

        end_data = {

            "code":200 , "msg":"do success" , "success":True , "data":data

        }

        return end_data

    except Exception as e:
        return str(e)



@app.route('/service-api/exposureAPIS/path/rad/total_avgspeed' , methods=['POST' , 'get'])
def total_avgspeed():
    try:
        data_time = request.json['data_time']
        conn = pymysql.connect(**db_config)
        cursor = conn.cursor()

        sql = f"""
select case when sum(avg_speed)/count(avg_speed) >=33 then  33 when sum(avg_speed)/count(avg_speed) <=28 then 28 else sum(avg_speed)/count(avg_speed) end as avg_speed  from ads_sc_rad_road_control_optimizing_index_10min where data_time=IFNULL(
(select data_time from ads_sc_rad_road_control_optimizing_index_10min where data_time = '2024-06-16 11:50:00' limit 1),
(select data_time from ads_sc_rad_road_control_optimizing_index_10min ORDER BY data_time desc limit 1)
)
	"""

        cursor.execute(sql)
        results = cursor.fetchall()

        columns = [column[0] for column in cursor.description]

        data = []
        for row in results:
            data.append(dict(zip(columns , row)))

        cursor.close()
        conn.close()

        end_data = {

            "code":200 , "msg":"do success" , "success":True , "data":data

        }

        return end_data

    except Exception as e:
        return str(e)





@app.route('/service-api/exposureAPIS/path/jtts/overflow_rate' , methods=['POST' , 'get'])
def overflow_rate():
    try:
        conn = pymysql.connect(**db_config)
        cursor = conn.cursor()

        sql = f"""select
a.crossroad_name,a.crossroad_id,a.data_time,case when b.overflow_rate >=1.5 then  1.5 else overflow_rate end as overflow_rate,a.overflow_cnt 
  from
(SELECT crossroad_name,crossroad_id,max(data_time) as data_time,count(1) as overflow_cnt FROM ads_sc_rad_road_overflow_rate_10min WHERE
			 data_time >= '2024-06-16 00:00:00'
			AND length( crossroad_name )!= char_length( crossroad_name ) 
      group by crossroad_name,crossroad_id)a
  join 
  (SELECT crossroad_name,crossroad_id,data_time,overflow_rate FROM ads_sc_rad_road_overflow_rate_10min where  data_time >= '2024-06-16 00:00:00' )b
  on  a.crossroad_name  = b.crossroad_name  and a.data_time = b.data_time 
  order by b.overflow_rate desc 
limit 10
	"""
        cursor.execute(sql)
        results = cursor.fetchall()

        columns = [column[0] for column in cursor.description]

        data = []
        for row in results:
            data.append(dict(zip(columns , row)))

        cursor.close()
        conn.close()

        end_data = {

            "code":200 , "msg":"do success" , "success":True , "data":data

        }

        return end_data

    except Exception as e:
        return str(e)




@app.route('/service-api/exposureAPIS/path/state_data/crossroad_detail_list' , methods=['POST' , 'get'])
def crossroad_detail_list():
    try:
        data_time = request.json['data_time']
        conn = pymysql.connect(**db_config)
        cursor = conn.cursor()

        sql = f"""select 
	t1.crossroad_name 	crossroad_name,
	t1.crossroad_id	  	crossroad_id,
	t1.tci_cnt	car_cnt,
	t2.person_cnt		person_cnt,
	ifnull(t2.crossroad_lon,'')	crossroad_lon,
	ifnull(t2.crossroad_lat,'')	crossroad_lat,
	case when t1.tci > 6 and t2.car_cnt <200 then 2 else t1.tci end as tci,
	CONCAT(t3.h,':00-',t3.h,':59')	peak_h,
	ifnull(t4.overflow_rate,0) as overflow_rate
from
(select * from ads_sc_rad_road_tci_10min where data_time = '2024-06-16 11:50:00')t1
left join 
(
    select
    a.crossroad_name,
    a.crossroad_id,
    a.car_cnt,
    a.person_cnt,
    a.tra_flow,
    a.data_time,
    b.crossroad_lon,
    b.crossroad_lat
    from
	(SELECT
		crossroad_name,
  	crossroad_id,
		tci_cnt as car_cnt,
	    0 as  person_cnt,
		tci_cnt as tra_flow,
		data_time
	FROM ads_sc_rad_road_tci_10min
	WHERE crossroad_name IS NOT NULL AND crossroad_name!=""  and length(crossroad_name)!=char_length(crossroad_name) and  data_time = '2024-06-16 11:50:00')a
	join 
	(select cross_name,cross_longitude as crossroad_lon,cross_latitude as crossroad_lat from `dim-rad-rd-device-crossroad`  where cross_longitude is not null and cross_latitude is not null and cross_longitude != '0'  group by cross_name,cross_longitude,cross_latitude )b
	on a. crossroad_name = b.cross_name
)t2
on  t1.crossroad_name=t2.crossroad_name
left join
(select * from ads_sc_rad_road_tra_flow_peak_d)t3
on  t1.crossroad_name=t3.crossroad_name 
left join
(SELECT crossroad_name,crossroad_id,overflow_rate,data_time FROM ads_sc_rad_road_overflow_rate_10min where overflow_rate >1 and overflow_rate <1.5 and  crossroad_name !='' and left(data_time,10) = '2024-06-16' group by crossroad_name,crossroad_id,overflow_rate,data_time)t4
on  t1.crossroad_name=t4.crossroad_name
where t2.crossroad_lon is not null and t2.crossroad_lat is not null  and t2.crossroad_lon !=0
	"""
        cursor.execute(sql)
        results = cursor.fetchall()

        columns = [column[0] for column in cursor.description]

        data = []
        for row in results:
            data.append(dict(zip(columns , row)))

        cursor.close()
        conn.close()

        end_data = {

            "code":200 , "msg":"do success" , "success":True , "data":data

        }

        return end_data

    except Exception as e:
        return str(e)




@app.route('/service-api/exposureAPIS/path/rad/road_section_jam' , methods=['POST' , 'get'])
def road_section_jam():
    try:
        data_time = request.json['data_time']
        conn = pymysql.connect(**db_config)
        cursor = conn.cursor()

        sql = f"""select ifnull(left(data_time,19), '2024-06-16 11:50:00') as data_time,e.road_name,e.start_end_dot as crossroad_s_e,e.section_location as road_lon_lat,(IFNULL(c.tci,0)+IFNULL(d.tci,0))/2 AS tci 
from
(select road_name,start_end_dot,replace(section_location,'"','') as section_location
from dim_own_szdl_section_relation group by road_name,start_end_dot,replace(section_location,'"',''))e
left join
(select *
from
(select data_time,road_name,crossroad_s_e,CONCAT(road_name,'-',SUBSTRING_INDEX(crossroad_s_e,'-',1)) as s_name,CONCAT(road_name,'-',SUBSTRING_INDEX(crossroad_s_e,'-',-1)) as e_name from ads_sc_rad_road_control_optimizing_index_10min where data_time=IFNULL(
(select data_time from ads_sc_rad_road_control_optimizing_index_10min where data_time = '2024-06-16 11:50:00' limit 1),
(select data_time from ads_sc_rad_road_control_optimizing_index_10min ORDER BY data_time desc limit 1)
))a
LEFT JOIN
(select crossroad_name,tci from ads_sc_rad_road_tci_10min  
where data_time=( SELECT data_time FROM ads_sc_rad_road_tci_10min ORDER BY data_time DESC limit 1))b
on a.s_name =b.crossroad_name  or a.s_name = CONCAT(SUBSTRING_INDEX(b.crossroad_name, '-', -1), '-', SUBSTRING_INDEX(b.crossroad_name, '-', 1))  or a.e_name = b.crossroad_name  or a.e_name = CONCAT(SUBSTRING_INDEX(b.crossroad_name, '-', -1), '-', SUBSTRING_INDEX(b.crossroad_name, '-', 1))
)c
 on c.road_name=e.road_name and c.crossroad_s_e=e.start_end_dot
LEFT JOIN
(select crossroad_name,tci from ads_sc_rad_road_tci_10min
where data_time='2024-06-16 11:50:00'
)d
on c.e_name=d.crossroad_name
where e.road_name is not null and e.road_name != '' and  section_location is not null and (LENGTH(section_location) - LENGTH(REPLACE(section_location, ',', '')))=3
order by  (IFNULL(c.tci,0)+IFNULL(d.tci,0))/2 DESC

	"""
        cursor.execute(sql)
        results = cursor.fetchall()

        columns = [column[0] for column in cursor.description]

        data = []
        for row in results:
            data.append(dict(zip(columns , row)))

        cursor.close()
        conn.close()

        end_data = {

            "code":200 , "msg":"do success" , "success":True , "data":data

        }

        return end_data

    except Exception as e:
        return str(e)


if __name__ == '__main__':
    print(db_config)
    app.run(host="0.0.0.0" , port=1600 , threaded=True)  #
