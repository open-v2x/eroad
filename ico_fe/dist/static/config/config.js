/*
 * @Author: LZonghao
 * @Date: 2023-11-28 08:39:40
 * @LastEditors: LZonghao
 * @LastEditTime: 2024-01-02 15:25:05
 */

var MQTT_SERVICE = "wss://172.29.16.6:30006/ws" // mqtt服务地址 30006实际是映射到15674
var MQTT_NAME = "root"
var MQTT_PASSWORD = "root"
var MQTT_TOPIC = "/topic/eroad"
var user_center = {
    mq_server: 'wss://144.7.108.34:1006/ws',
    mq_name: 'ioc',
    mq_password: 'iocMq2022.',
    mq_queen: '/amq/queue/ioc'
}

var api_environment = 'development'
var UserCenterLogin = {
  client_id: "ioc",
  client_secret: "ioc",
  state: "ejDeIX",
  userCenterHref: "https://city189.cn:1101/"
}

var ioc_index_realtime = "ws://10.1.4.48:1651/ws/iocindex"
var ioc_quanxisdk = "wss://city189.cn:1655/ws/iocquanxisdk"


var roadNetwork_path = '/tiles-server/RD_3dtiles/tileset.json'
var tiles_path = 'tiles-server/tiles/tileset.json'//'tiles-server/tiles3_dm_ktx/tileset.json'//'http://localhost:9003/model/Dy9zmJQo/tileset.json';
var tiles_lk = 'tiles-server/dandu_tiles/tileset.json'//tiles-server/tiles_dm_ktx/tileset.json
var iocquanxitrack = 'wss://city189.cn:1653/ws/iocquanxitrack'
var ws_path = 'wss://city189.cn:1650/ws/iocdata'//'ws://localhost:8090/ws/iocdata'//'ws://localhost:8090/ws/iocdata';
var trafficLightPath = 'wss://city189.cn:1254/trafficLight/websocket'
var vedio_path_one_minute = "/video/test.mp4"
var vedio_path = "/video/toccVideo.mp4"
