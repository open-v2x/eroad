<template>
    <div class="layerTool">
        <div class="layer" :class="item.active" v-for="item in layers" :key="item.id" @click="hander(item)">
            <div :class="'layer_icon_' + item.id" @mouseenter="enter(item)" @mouseleave="leave(item)"></div>
            <div v-show="item.toolTip" class="toolTipPanel" id="">{{item.name}}</div>
        </div>
    </div>
    <TimeAxis ref="timer"/>
    <div class="hotLegen" v-if="showLegen"></div>
    <StatisticsPop  :popMode="showMode" ref="statisticsPop" :style="{
        left:sLeft+'px',
        top:sTop+'px'
    }" v-show="sShow"/>
</template>
<script>
import { onMounted,nextTick,createApp } from 'vue';
import { queryChannelsByDeviceId,queryAllChannels,sendDevicePush,queryByChannelName,cloudRecord,vedioLogin } from '../../assets/js/api_video'
import { crossroad_detail_list, road_section_jam } from '../../assets/js/api'
import TimeAxis from "./TimeAxis.vue"
import PopupWindow from "./PopupWindow/popupWindow.vue"
import StatisticsPop from "./PopupWindow/StatisticsPop.vue"
import Radar from '../../assets/js/3d/trafficLight/radar'



import cameraData from '../../assets/config/cameraData.json'
import 路口数据 from '../../assets/config/路口数据.json'
import cameraIcon from '../../assets/img/Group 1000002301.png'
import breath_red from'../../assets/img/红色呼吸球.png'
import breath_yellow from'../../assets/img/黄色呼吸球.png'

import breath_orange from'../../assets/img/橙色呼吸球.png'
import breath_green from'../../assets/img/绿色普通点.png'
import { conditionalExpression } from '@babel/types';

var CoordTransform=require('coordtransform');
var node;//document.createElement("div");
var app;
var cluster;
let cameraMarkerList = [];
let viewCom
let lng
let lat
let crossRoad
let position
let deviceId
var channels = [];//所有通道
var onlineCamera = [];//将cameraData清洗完之后的数据
var zoneList = [];
var zoneList_heatMap = [];
var green,yellow,orange,red;
var green_scatter,yellow_scatter,orange_scatter,red_scatter;
var cameraLayerSource;
var heatLineLayer;

export default{
    components:{TimeAxis,StatisticsPop},
    data(){
        return{
            showLegen:false,
            layers:[
                {
                    id:1,
                    name:'路段',
                    active:'',
                    func:'roadHeatMap',
                    func3d:'roadHeatMap3d',
                    toolTip: false
                },{
                    id:2,
                    name:'路口',
                    active:'',
                    func:'breathPointMap',
                    func3d:'breathPointMap3d',
                    toolTip: false
                },{
                    id:3,
                    name:'视频',
                    active:'',
                    func:'showCameraMarker',
                    func3d:'showCameraMarker3d',
                    toolTip: false
                }
            ],
            geoJsonTemp:{
                //可以不用引号
                "type": "FeatureCollection",
                "features": [

                ]
            },
            sShow:false,
            sLeft:0,
            sTop:0,
            sRight:0,
            time:'',
            timeStamp:'',
            showMode: '',
            record: false,
            popupWindowVisibility: false,
        }
    },
    created(){

        this.checkoutRecord()//绑定总线事件：监听执行切换录像的信号和参数。与全息无关。
        this.$bus.on('timelinechanged',res => {//timeAxis五分钟更新一次，或鼠标点击，都会触发timelinechanged事件。
            this.time = res.time;
            let date = new Date(res.time)
            this.firstChangeTimeStamp(date)
            this.record = true//记录时间戳并转为回放模式
            if(this.popupWindowVisibility&&res.unlock){//触发后根据是否存在视频视窗切换最新录像。
                // console.log('发送切换record的信号')
                this.timeStamp = Math.round(date.getTime()/1000)
                this.$bus.emit('sendCheckoutRecord','')//触发发出切换录像的命令。与playerbar绑定，与全息无关。
            }else if(res.unlock){
                this.timeStamp = Math.round(date.getTime()/1000)
            }

            this.firstQuery(res.time)

            this.roadHeatMap_lightup();
        })
    },
    mounted(){
        this.changeRecord()//绑定与liveplayer之间的数据传输（用于record模式同一摄像头切换不同频道）。不一定与全息无关。
        this.getPopupWindowVisibility()//绑定事件，popupwindow显示和关闭传来信号。
        this.live()//录像切换到直播
        queryAllChannels().then(res=>{
            const devices = new Map()
            res.filter((item) => {
                if(item.status == 0) return;
                if(devices.has(item.deviceId))
                    devices.get(item.deviceId).push(item)
                else
                    devices.set(item.deviceId,[item])
            })
            onlineCamera = [];
            devices.forEach(item=>{
                let attribute = {
                    "交叉路口名称": item[0].name,
                    "经度": item[0].longitude,
                    "纬度": item[0].latitude,
                    "SIP用户名/设备编号": item[0].deviceId,
                    "全景通道编码": item[0].channelId,
                    "细节通道编码": item[0].channelId
                };
                onlineCamera.push(attribute);
            })
        })//获取所有通道，去重，不在此列的cameraData数据不显示图标。
        this.time = this.getTime();
        // nextTick(()=>{
        //     if(window.viewer){
        //         node.className = "fixposition";
        //         window.viewer.container.appendChild(node);
        //     }
        // })

        //初始状态
        //this.init(1);
    },
    unmounted(){
        this.$bus.off('timelinechanged')
        this.$bus.off('recordParam')
        this.$bus.off('live')
        this.$bus.off('popupWindowVisibility')
        this.$bus.off('checkoutRecord')//off防止多次触发
    },
    methods:{
        firstChangeTimeStamp(date){
            this.timeStamp = Math.round(date.getTime()/1000)
            this.firstChangeTimeStamp = function(){}
        },
        hander(item) {
            if(continerModel == 1){
                if(item.func){
                    //调用item.func名的函数
                    this[item.func](item);
                }
            }else{
               if(item.func3d){
                    //调用item.func名的函数
                    this[item.func3d](item);
                }
            }
        },
        enter(item){
            item.toolTip = true;
        },
        leave(item){
            item.toolTip = false;
        },
        init(i){
            this.layers.forEach(item => {
                item.active = '';
            });
            if(i!=null)
                this.breathPointMap(this.layers[i]);
        },
        getTime(){
            var today = new Date();
            var m = Math.floor(today.getMinutes()/10)*10-10;
            var h = today.getHours();
            if(m<0){
                h -= 1;
                m = 50;
            }//todo h<0
            m = m<10?'0'+m:m;
            h = h<10?'0'+h:h;
            return today.toJSON().split('T').join(' ').split(' ')[0] + ' ' + h +':'+m + ':' + '00'
        },
        showCameraMarker(item){//item有id name active func
            if(!this.layers[0].active&&!this.layers[1].active)
                this.$refs.timer.show = false
            if(item.active){//取消方法
                item.active = '';
                if(!this.layers[0].active&&!this.layers[1].active)
                this.$refs.timer.show = false
                if(continerModel == 1){
                    map.remove(cameraMarkerList);
                    cluster.setMap(null);
                    cameraMarkerList = [];
                }else{
                    cameraLayerSource.entities.removeAll();
                }
            }else{//添加方法
                item.active = 'active';
                this.$refs.timer.show = true
                //2d
                if(continerModel == 1){

                    cameraMarkerList = [];
                    onlineCamera.forEach((item)=>{
                        var marker = new AMap.Marker({
                            icon:cameraIcon,
                            position: CoordTransform.wgs84togcj02(item.经度,item.纬度),   // 经纬度对象，也可以是经纬度构成的一维数组[116.39, 39.9]
                            title: item.交叉路口名称,
                            attribute:{
                                'viewCom':'LivePlayer',
                                '交叉路口名称':item['交叉路口名称'],
                                'SIP用户名/设备编号':item['SIP用户名/设备编号'],
                                '全景通道编码':item['全景通道编码'],
                                '细节通道编码':item['细节通道编码']
                            }
                        });
                         marker.on('click', (e) => {
                            viewCom = e.target._opts['attribute']['viewCom']
                            lat = e.target._position[1]
                            lng = e.target._position[0]
                            crossRoad = e.target._opts['attribute']['交叉路口名称']
                            position = e.target._position
                            deviceId = e.target._opts['attribute']['SIP用户名/设备编号']
                            if(!this.record){//使用record变量决定是否开启回放模式。
                                this.play(deviceId,(res)=>{
                                    map.setCenter(position);
                                    this.$bus.emit('pop2D', {propertity:{url:res.url,camera:res.camera,lng:lng,lat:lat,交叉路口名称:crossRoad,viewCom:viewCom,record: false}});
                                })
                            }else{
                                this.playRecord(deviceId,(res)=>{
                                    map.setCenter(position);
                                    this.$bus.emit('pop2D', {propertity:{url:res.url,camera:res.camera,startTime:res.startTime,mediaServerId:res.mediaServerId,playSsrc:res.playSsrc,timeList:res.timeList,lng:lng,lat:lat,交叉路口名称:crossRoad,viewCom:viewCom,record: true}});
                                })
                            }
                        });
                        cameraMarkerList.push(marker);

                    })
                    cluster = new AMap.MarkerClusterer(map, cameraMarkerList, {gridSize: 60});
                }else{//3d
                    if(window.viewer && !node){
                      node = document.createElement("div")
                      node.className = "fixposition";
                      window.viewer.container.appendChild(node);
                    }
                    cameraLayerSource = new Cesium.CustomDataSource("cameraLayer");
                    viewer.dataSources.add(cameraLayerSource);
                    //var entityCollection = new Cesium.EntityCollection();
                    //viewer.scene.primitives.add(entityCollection);
                    cameraData.forEach((item)=>{
                        // new BounceMarker(viewer,[item.经度,item.纬度,7],
                        //     {
                        //         'viewCom':'LivePlayer',
                        //         '交叉路口名称':item['交叉路口名称'],
                        //         'SIP用户名/设备编号':item['SIP用户名/设备编号'],
                        //         // '全景通道编码':item['全景通道编码'],
                        //         // '细节通道编码':item['细节通道编码']
                        //     },
                        //     {image:cameraIcon}
                        // );

                        var marker = cameraLayerSource.entities.add({
                            position: Cesium.Cartesian3.fromDegrees(Number(item.经度),Number(item.纬度),10),
                            billboard: {
                                image: cameraIcon,
                            },
                            propertity:{
                                'viewCom':'LivePlayer',
                                '交叉路口名称':item['交叉路口名称'],
                                'SIP用户名/设备编号':item['SIP用户名/设备编号'],
                                '全景通道编码':item['全景通道编码'],
                                '细节通道编码':item['细节通道编码'],
                            },
                            click:(position)=>{
                                this.play(item['SIP用户名/设备编号'],(res)=>{
                                    console.log(res,position);
                                    this.showPop(res,position);
                                })
                            }
                        });

                    })
                    //点击事件
                    new Cesium.ScreenSpaceEventHandler(viewer.scene.canvas).setInputAction((i) => {
                        const t = viewer.scene.pick(i.position);
                        if(t && t.id){
                            t.id.click(t.id.position._value);
                        }

                        //弹窗

                    }, Cesium.ScreenSpaceEventType.LEFT_CLICK);
                }

            }
        },
        changeRecord(){//切换频道的，与全息有关，因为除切换频道外，重连视频也使用这个。
            this.$bus.on('recordParam',res=>{//切换同一摄像头不同频道录像,这个录像请求需要保持当前请求时间不变，即便TIMEAXIS刷新他也不能变。
                let startTime = res.startTime
                cloudRecord({deviceId: res.deviceId,channelId: res.channelId,recordStartTime:res.startTime,recordEndTime:res.startTime+600,playStartTime:res.startTime})
                .then(res=>{
                    if(res.code == 401){
                        console.log('账号登录')
                        this.login().then(()=>{
                            this.usePlayRecord()
                        })
                        return
                    }
                    let response = {}
                    response.url = res.data.playBackUrl
                    response.mediaServerId = res.data.mediaServerId
                    response.playSsrc = res.data.playSsrc
                    response.timeList = [res.data.recordList[0]]
                    if(res.data.recordList[1]){
                            response.timeList[1] = res.data.recordList[1]
                    }//十分钟只有俩mp4文件，所以0和1，如果不改接口的话。
                    if(res.data.recordList[2]){
                        response.timeList[2] = res.data.recordList[2]
                    }
                    response.timeList[3] = startTime
                    // console.log('又一个时间列表',response.timeList)
                    this.$bus.emit('recordChange',response)//越过popupwindow，直接与Liveplayer交互
                })
            })
        },
        live(){//录像切换为直播
            this.$bus.on('live',response=>{//等待playerbar传来摄像头信息，根据rightScreen决定是否翻转双（单频道反转也可以，无效，无影响）频道视频链接实现原频道切换。
                this.record = false
                this.play(response.deviceId,(res)=>{
                    if(response.rightScreen){
                        res.url.reverse()
                    }
                    this.$bus.emit('checkout', {propertity:{url:res.url,record: false,switchInPlace: true}});//触发pop2window的视频参数设置
                })
            })
        },
        checkoutRecord(){//时间线变化后自动切换录像，与全息无关
            this.$bus.on('checkoutRecord',request => {
                // console.log('听到切换信号，执行record切换')//拿到切换参数，执行切换
                // console.log(request)
                cloudRecord({deviceId: request.deviceId,channelId: request.channel[0],recordStartTime:this.timeStamp,recordEndTime:this.timeStamp+600,playStartTime:this.timeStamp})
                .then(res=>{
                    if(res.code == 401){
                        console.log('账号登录')
                        this.login().then(()=>{
                            this.usePlayRecord()
                        })
                        return
                    }
                    let response = {}
                    response.url = res.data.playBackUrl
                    response.mediaServerId = res.data.mediaServerId
                    response.playSsrc = res.data.playSsrc
                    response.timeList = [res.data.recordList[0]]
                    response.startTime = this.timeStamp

                    if(res.data.recordList[1]){
                            response.timeList[1] = res.data.recordList[1]
                    }//存入时间列表
                    if(res.data.recordList[2]){
                            response.timeList[2] = res.data.recordList[2]
                    }//存入时间列表
                    response.timeList[3] = this.timeStamp
                    response.switchInPlace = true
                    // console.log('时间列表',response.timeList)

                    this.$bus.emit('checkoutRecordExecute',response)//执行切换录像
                })
            })
        },
        getPopupWindowVisibility(){//监听视频窗口是否存在
            this.$bus.on('popupWindowVisibility',(res)=>{
                this.popupWindowVisibility = res
                // console.log('popupWindowVisibility',this.popupWindowVisibility)
            })
        },
        usePlayRecord(){//与全息无关，但自动登录全息也需要。
            this.playRecord(deviceId,(res)=>{
                map.setCenter(position);
                this.$bus.emit('pop2D', {propertity:{url:res.url,camera:res.camera,mediaServerId:res.mediaServerId,playSsrc:res.playSsrc,timeList:res.timeList,lng:lng,lat:lat,交叉路口名称:crossRoad,viewCom:viewCom,record: true}});
            })
        },
        async login(){//登录，与全息有关，全息也需要自动登录
            return vedioLogin().then(res=>{
                            // console.log(res)
                            localStorage.setItem('vedioToken', JSON.stringify(res.data))
                    })
        },
        playRecord(deviceId,callback){//播放录像，与全息有关，而且有自动登录，不过可能不需要回调函数。
            queryChannelsByDeviceId({
                deviceId:deviceId
            }).then((res)=>{//查找所有通道
                // console.log('查找摄像头通道',res)
                var channelId
                var flag = false
                let url = []//url统一用数组装载
                let camera = {deviceId:deviceId}//通过点击图像进入录像时将摄像头ID和所有频道号存入camera对象供切换视频等使用
                if(res.length == 0){
                    alert('已查询到摄像头信息,但该设备未建立通道')
                    return;
                }else if(res.length == 1){
                    channelId = res[0]['channelId']
                    camera.channel = [res['channelId']]//即使只有一个频道，也装到数组，方便reverse不设置判断条件
                }else{
                    flag = true
                    channelId = [res[0]['channelId'],res[1]['channelId']]
                    camera.channel = [res[0]['channelId'],res[1]['channelId']]
                }
                //请求录像回放
                cloudRecord({deviceId: deviceId,channelId: flag? channelId[0] : channelId,recordStartTime:this.timeStamp,recordEndTime:this.timeStamp+600,playStartTime:this.timeStamp})
                .then(res=>{//使用第一个频道进入录像
                    if(res.code == 401){
                        console.log('账号登录')
                        this.login().then(()=>{
                            this.usePlayRecord()
                        })
                        return
                    }
                    let response = {}
                    url.push(res.data.playBackUrl)//只有一个录像，但也存入数组中，因为录像即便有两个通道也只存一个链接。Player中通过链接是值还是数组判断是否增加左右箭头。
                    response.url = url
                    response.mediaServerId = res.data.mediaServerId
                    response.playSsrc = res.data.playSsrc
                    response.timeList = [res.data.recordList[0]]//录像链接和调整进度相关属性
                    response.startTime = this.timeStamp
                    if(res.data.recordList[1]){
                        response.timeList[1] = res.data.recordList[1]
                    }
                    if(res.data.recordList[2]){
                        response.timeList[2] = res.data.recordList[2]
                    }
                    response.timeList[3] = this.timeStamp
                    response.camera = camera
                    // console.log('%c看看','color:green;font-size:30px')
                    // console.log('一开始的时间列表',response.timeList)
                    callback(response)//该这块注意加回调调用
                })
            })
        },
        play(deviceId,callback){
              queryChannelsByDeviceId({
                deviceId:deviceId
            }).then((res)=>{
                // console.log('查找摄像头通道',res)
                var channelId
                var flag = false
                let url = []
                let camera = {deviceId:deviceId}
                let response = {}
                if(res.length == 0){
                    alert('已查询到摄像头信息,但该设备未建立通道')
                    return;
                }else if(res.length == 1){
                    channelId = res[0]['channelId']
                    // console.log('打印摄像头唯一通道',channelId)
                    camera.channel = [channelId]
                }else{
                    flag = true
                    channelId = [res[0]['channelId'],res[1]['channelId']]
                    camera.channel = [res[0]['channelId'],res[1]['channelId']]
                }
                sendDevicePush({
                    deviceId: deviceId,
                    channelId: flag ? channelId[0] : channelId
                }).then((re)=>{
                    if(re['data']&&!flag){
                        response.url = re['data']['flv'];
                        response.camera = camera
                        callback(response);
                    }else if(re['data']&&flag){
                        url.push(re['data']['flv'])
                    }else{
                        alert(re['msg']);
                    }
                }).then(()=>{
                    if(flag){
                        sendDevicePush({
                            deviceId: deviceId,
                            channelId: channelId[1]
                        }).then(res=>{
                            if(res.data){
                                url.push(res.data.flv)
                                response.url = url
                                response.camera = camera
                                callback(response)
                            }else{
                                alert(res.msg)
                            }
                        })
                    }
                })

            })
        },
        firstQuery(){
            this.firstQuery = function(time){
                this.query(time,() => {
                    this.putOut();
                    this.lightUp();
                })
            }
        },
        query(time,callback){
            //接口
            crossroad_detail_list({
            "data_time": time
            }).then(res => {
                //res包含状态码等一堆信息，只需要其中的data数据
                var data = res.data;
                // console.log("十字路口数据：")
                // console.log(data);
                if(!data)   return;
                red =  JSON.parse(JSON.stringify(this.geoJsonTemp));
                orange = JSON.parse(JSON.stringify(this.geoJsonTemp));
                yellow = JSON.parse(JSON.stringify(this.geoJsonTemp));
                green = JSON.parse(JSON.stringify(this.geoJsonTemp));
                data.forEach(item => {
                    var feature = {
                        "type": "Feature",
                        "geometry":
                            {
                                "type": "Point",
                                //data中的每一个元素的属性赋值
                                "coordinates": [
                                    item.crossroad_lon,
                                    item.crossroad_lat
                                ]
                            }

                    };
                    //item属性赋值
                    var value = item.tci;

                    if(value>7.25){//红
                        red.features.push(feature);
                    }else if(value > 5.00){
                        orange.features.push(feature);
                    }else if(value > 2.75){
                        yellow.features.push(feature);
                    }else{
                        green.features.push(feature);
                    }

                });
                //加点击热区
                this.addHotZone(data);
                // this.addHotZone_heatMap(data);
                callback(true);
            })
        },
        breathPointMap(item){
            // if(!this.layers[0].active && !this.layers[0].active)
            //     this.$refs.timer.show = false;
            if(!this.layers[0].active&&!this.layers[2].active)
                this.$refs.timer.show = false;
            if(item.active){//取消方法
                item.active = '';
                // this.showLegen = false;
                if(!this.layers[0].active&&!this.layers[2].active){
                this.showLegen = false;
                }
                map.remove(zoneList);  //取消热区圆点
                this.putOut();
            }else{
                // this.putOut();
                item.active = 'active';
                if(continerModel == 1){
                    this.query(this.time,() => {
                        this.$refs.timer.show = true;
                        this.showLegen = true;
                        // loca = window.loca = new Loca.Container({
                        //     map,
                        // });
                        this.lightUp();
                    });
                }else{
                    //var radar = new Radar();
                    Cesium.GeoJsonDataSource.load(red
                    , {
                        clampToGround: true, // 设置贴地
                    }).then((dataSource) => {
                        var entities = dataSource.entities.values;
                        for (var o = 0; o < entities.length; o++){
                            var r = entities[o];
                            this.AddCircleScanPostStage(
                                r.position._value,//new Cesium.Cartographic(r.position._value.x, r.position._value.y, 1),
                                100,
                                 Cesium.Color.fromCssColorString('#E80E0E'),
                                3000
                            );

                        }
                    })

                    Cesium.GeoJsonDataSource.load(yellow
                    , {
                        clampToGround: true, // 设置贴地
                    }).then((dataSource) => {
                        var entities = dataSource.entities.values;
                        for (var o = 0; o < entities.length; o++){
                            var r = entities[o];
                            this.AddCircleScanPostStage(
                                r.position._value,//new Cesium.Cartographic(r.position._value.x, r.position._value.y, 1),
                                70,
                                 Cesium.Color.fromCssColorString('#FFD045'),
                                3000
                            );

                        }
                    })

                    Cesium.GeoJsonDataSource.load(green
                    , {
                        clampToGround: true, // 设置贴地
                    }).then((dataSource) => {
                        var entities = dataSource.entities.values;
                        for (var o = 0; o < entities.length; o++){
                            var r = entities[o];
                            this.AddCircleScanPostStage(
                                r.position._value,//new Cesium.Cartographic(r.position._value.x, r.position._value.y, 1),
                                40,
                                 Cesium.Color.fromCssColorString('#4FD27D'),
                                3000
                            );

                        }
                    })
                }
            }
        },
        lightUp(){
            if(this.layers[1].active == '')  return;
            var new_green = this.layerTrans(green);
            var new_yellow = this.layerTrans(yellow);
            var new_orange = this.layerTrans(orange);
            var new_red = this.layerTrans(red);

            // 绿色普通点
            var geo = new Loca.GeoJSONSource({
                data:new_green
                //url: '../../assets/config/green_cross.json',
            });
            green_scatter = new Loca.ScatterLayer({
                // loca,
                zIndex: 111,
                opacity: 1,
                visible: true,
                zooms: [2, 22],
            });
            green_scatter.setSource(geo);
            green_scatter.setStyle({
                // color: 'rgba(43,156,75,1)',
                // unit: 'meter',
                // size: [80, 80],
                // borderWidth: 0,
                unit: 'meter',
                size: [100, 100],
                texture: breath_green,
                borderWidth: 0,

                // unit: 'meter',
                // size: [100, 100],
                // borderWidth: 0,
                // texture: breath_green,
                // duration: 500,
                // animate: true,
            });
            loca.add(green_scatter);

            // 红色呼吸点
            var geoLevelF = new Loca.GeoJSONSource({
                // data: [],
                data: new_red,
            });
            red_scatter = new Loca.ScatterLayer({
                loca,
                zIndex: 113,
                opacity: 1,
                visible: true,
                zooms: [2, 22],
            });
            red_scatter.setSource(geoLevelF);
            red_scatter.setStyle({
                unit: 'meter',
                size: [200, 200],
                borderWidth: 0,
                texture: breath_red,
                duration: 500,
                animate: true,
            });
            //loca.add(breathRed);

            // 黄色呼吸点
            var geoLevelE = new Loca.GeoJSONSource({
                // data: [],
                data: new_yellow,
            });
            yellow_scatter = new Loca.ScatterLayer({
                loca,
                zIndex: 112,
                opacity: 1,
                visible: true,
                zooms: [2, 22],
            });
            yellow_scatter.setSource(geoLevelE);
            yellow_scatter.setStyle({
                unit: 'meter',
                size: [200, 200],
                borderWidth: 0,
                texture: breath_yellow,
                duration: 1000,
                animate: true,
            });

            // 橙色呼吸点
            var geoLevelO = new Loca.GeoJSONSource({
                // data: [],
                data: new_orange,
            });
            orange_scatter = new Loca.ScatterLayer({
                loca,
                zIndex: 114,
                opacity: 1,
                visible: true,
                zooms: [2, 22],
            });
            orange_scatter.setSource(geoLevelO);
            orange_scatter.setStyle({
                unit: 'meter',
                size: [200, 200],
                borderWidth: 0,
                texture: breath_orange,
                duration: 1000,
                animate: true
            });

            // 启动渲染动画
            loca.animate.start();
            //var dat = new Loca.Dat();
            //dat.addLayer(scatter, ' 贴地');
            // dat.addLayer(breathRed, '红色');
            //dat.addLayer(breathYellow, '黄色');

        },
        //灭掉所有
        putOut(){
            loca.remove(green_scatter);
            loca.remove(yellow_scatter);
            loca.remove(orange_scatter);
            loca.remove(red_scatter);

        },
        layerTrans(feature){
            var new_feature = JSON.parse(JSON.stringify(feature));
            for(var i = 0;i<feature.features.length;i++){
                var new_Coord = CoordTransform.wgs84togcj02(feature.features[i].geometry.coordinates[0],feature.features[i].geometry.coordinates[1]);
                new_feature.features[i].geometry.coordinates = new_Coord;
            }

            return new_feature;
        },
        showPop(url,position){
            // var node = document.createElement("div");
            // node.className = "fixposition";
            // viewer.container.appendChild(node);
            if(app){
                app.unmount();
            }
            app = createApp(popupWindow);

            // app.component('position',position);
            // app.component('viewCom','xgPlayer');
            // app.component('url',url);
            // app.compontent('popupWindow',popupWindow);
            let vm = app.mount(node);
            vm.$nextTick(() => {
                vm.$data.position = position;//Cesium.Cartesian3.fromDegrees(position.x, position.y, position.z);

                vm.$data.viewCom = 'LivePlayer';//'messagePop';
                vm.$data.propertity = {'url':url};
            //}
            })
        },
        AddCircleScanPostStage(cartographicCenter,maxRadius, scanColor, duration){
             var ScanSegmentShader =
             "uniform sampler2D colorTexture;\n" +
             "uniform sampler2D depthTexture;\n" +
             "varying vec2 v_textureCoordinates;\n" +
             "uniform vec4 u_scanCenterEC;\n" +
             "uniform vec3 u_scanPlaneNormalEC;\n" +
             "uniform float u_radius;\n" +
             "uniform vec4 u_scanColor;\n" +
             "vec4 toEye(in vec2 uv, in float depth)\n" +
             " {\n" +
             " vec2 xy = vec2((uv.x * 2.0 - 1.0),(uv.y * 2.0 - 1.0));\n" +
             " vec4 posInCamera =czm_inverseProjection * vec4(xy, depth, 1.0);\n" +
             " posInCamera =posInCamera / posInCamera.w;\n" +
             " return posInCamera;\n" +
             " }\n" +
             "vec3 pointProjectOnPlane(in vec3 planeNormal, in vec3 planeOrigin, in vec3 point)\n" +
             "{\n" +
             "vec3 v01 = point -planeOrigin;\n" +
             "float d = dot(planeNormal, v01) ;\n" +
             "return (point - planeNormal * d);\n" +
             "}\n" +
             "float getDepth(in vec4 depth)\n" +
             "{\n" +
             "float z_window = czm_unpackDepth(depth);\n" +
             "z_window = czm_reverseLogDepth(z_window);\n" +
             "float n_range = czm_depthRange.near;\n" +
             "float f_range = czm_depthRange.far;\n" +
             "return (2.0 * z_window - n_range - f_range) / (f_range - n_range);\n" +
             "}\n" +
             "void main()\n" +
             "{\n" +
             "gl_FragColor = texture2D(colorTexture, v_textureCoordinates);\n" +
             "float depth = getDepth( texture2D(depthTexture, v_textureCoordinates));\n" +
             "vec4 viewPos = toEye(v_textureCoordinates, depth);\n" +
             "vec3 prjOnPlane = pointProjectOnPlane(u_scanPlaneNormalEC.xyz, u_scanCenterEC.xyz, viewPos.xyz);\n" +
             "float dis = length(prjOnPlane.xyz - u_scanCenterEC.xyz);\n" +
             "if(dis < u_radius)\n" +
             "{\n" +
             "float f = 1.0 -abs(u_radius - dis) / u_radius;\n" +
             "f = pow(f, 4.0);\n" +
             "gl_FragColor = mix(gl_FragColor, u_scanColor, f);\n" +
             "}\n" +
             "}\n";

         var _Cartesian3Center = cartographicCenter;//Cesium.Cartographic.toCartesian(cartographicCenter);
         var _Cartesian4Center = new Cesium.Cartesian4(_Cartesian3Center.x, _Cartesian3Center.y, _Cartesian3Center.z, 1);
         //var _CartographicCenter1 = new Cesium.Cartographic(cartographicCenter.longitude, cartographicCenter.latitude, cartographicCenter.height + 500);
         var _Cartesian3Center1 = cartographicCenter;//Cesium.Cartographic.toCartesian(_CartographicCenter1);
         var _Cartesian4Center1 = new Cesium.Cartesian4(_Cartesian3Center1.x, _Cartesian3Center1.y, _Cartesian3Center1.z+500, 1);
         var _time = (new Date()).getTime();
         var _scratchCartesian4Center = new Cesium.Cartesian4();
         var _scratchCartesian4Center1 = new Cesium.Cartesian4();
         var _scratchCartesian3Normal = new Cesium.Cartesian3();
         var ScanPostStage = new Cesium.PostProcessStage({
             fragmentShader: ScanSegmentShader,
             uniforms: {
                 u_scanCenterEC: function () {
                     return Cesium.Matrix4.multiplyByVector(viewer.camera._viewMatrix, _Cartesian4Center, _scratchCartesian4Center);
                 },
                 u_scanPlaneNormalEC: function () {
                     var temp = Cesium.Matrix4.multiplyByVector(viewer.camera._viewMatrix, _Cartesian4Center, _scratchCartesian4Center);
                     var temp1 = Cesium.Matrix4.multiplyByVector(viewer.camera._viewMatrix, _Cartesian4Center1, _scratchCartesian4Center1);
                     _scratchCartesian3Normal.x = temp1.x - temp.x;
                     _scratchCartesian3Normal.y = temp1.y - temp.y;
                     _scratchCartesian3Normal.z = temp1.z - temp.z;
                     Cesium.Cartesian3.normalize(_scratchCartesian3Normal, _scratchCartesian3Normal);
                     return _scratchCartesian3Normal;

                 },
                 u_radius: function () {
                     return maxRadius * (((new Date()).getTime() - _time) % duration) / duration;
                 },
                 u_scanColor: scanColor
             }
         });
         viewer.scene.postProcessStages.add(ScanPostStage);
         return (ScanPostStage);
     },
    addHotZone(data){
        map.remove(zoneList);
        data.forEach(item => {
            var zone = new AMap.Circle({
                center:CoordTransform.wgs84togcj02(Number(item.crossroad_lon),Number(item.crossroad_lat)),//[Number(item.crossroad_lon),Number(item.crossroad_lat)],
                radius:45,
                fillOpacity:0,
                strokeOpacity:0,
                zIndex: 117,
                cursor:'pointer',
                attribute:{
                    //'viewCom':'LivePlayer',
                    '路口名称':item['crossroad_name'],
                    '实时车流量':item['car_cnt'],
                    // '实时人流量':item['person_cnt'],
                    '拥堵指数':item['tci'],
                    '高峰时段':item['peak_h'],
                    'lng':item['crossroad_lon'],
                    'lat':item['crossroad_lat']
                }
            })
            zone.on('mouseover', (e) => {
                this.showMode = "crossRoad";
                this.sShow = true;
                this.sLeft = e.pixel.x;
                this.sTop = e.pixel.y;

                this.$refs.statisticsPop.attribute = e.target._opts.attribute;
            });
            zone.on('mouseout', (e) => {
                this.sShow = false;
            });
            zone.on('click', (e) => {
                console.log(e.target._opts.attribute)
                //this.inToIntersection(e.target._opts.attribute);
            });
            zoneList.push(zone);
        })
        map.add(zoneList);
    },
    addHotZone_heatMap(data){
        map.remove(zoneList_heatMap);
        data.forEach(item => {
            let road_lon_lat = JSON.parse(item.road_lon_lat);
            var lngLatStart = CoordTransform.wgs84togcj02(road_lon_lat[0][0],road_lon_lat[0][1]);
            var lngLatEnd = CoordTransform.wgs84togcj02(road_lon_lat[1][0],road_lon_lat[1][1]);
            var polyLineCenter = [(lngLatStart[0]+lngLatEnd[0])/2.00, (lngLatStart[1]+lngLatEnd[1])/2.00];
            var zone = new AMap.Polyline({
                path: [lngLatStart, polyLineCenter, lngLatEnd],   //折线只能输入三个点以上
                strokeColor: '#fff', //线颜色
                strokeOpacity: 0,     //线透明度
                strokeWeight: 20,      //线宽
                strokeStyle: "solid",   //线样式
                strokeDasharray: [10, 5], //补充线样式
                cursor:'pointer',
                zIndex: 116,
                attribute:{
                    '道路名称': item['road_name'],
                    '拥堵指数':item['tci'],
                }
            })
            zone.on('mouseover', (e) => {
                this.showMode = "";
                this.sShow = true;
                this.sLeft = e.pixel.x;
                this.sTop = e.pixel.y;

                this.$refs.statisticsPop.attribute = e.target._opts.attribute;

                setTimeout(()=>{
                    this.sShow = false;
                },3000);

            });
            // zone.on('mouseout', (e) => {
            //     setTimeout(()=>{
            //         this.sShow = false;
            //     },2000);

            // });
            zoneList_heatMap.push(zone);
        })
        map.add(zoneList_heatMap);
    },
     roadHeatMap_lightup(){
        if(!this.layers[0].active)  return;
        // 调用函数如果存在热力曾，就remove
        road_section_jam({
            data_time:this.time
        }).then(res => {
            if(heatLineLayer){
                loca.remove(heatLineLayer);
            }
            // console.log('路段热裤力',res);
            var data = res.data;
            var greenline =  JSON.parse(JSON.stringify(this.geoJsonTemp));
            data.forEach(line => {
                var road_lon_lat = JSON.parse(line.road_lon_lat);
                var feature = {
                    "type": "Feature",
                    "properties":{"tra_flow":Number(line.tra_flow),'crossroad_s_e':line.crossroad_s_e},
                    "geometry":
                        {
                            "type": "LineString",
                            "coordinates": [CoordTransform.wgs84togcj02(road_lon_lat[0][0],road_lon_lat[0][1]),CoordTransform.wgs84togcj02(road_lon_lat[1][0],road_lon_lat[1][1])]
                        }

                };
                greenline.features.push(feature);

            })
            this.addHotZone_heatMap(data);

            heatLineLayer = new Loca.PulseLineLayer({
                // loca,
                zIndex: 115,
                opacity: 1,
                visible: true,
                zooms: [2, 22],
            });

            var greengeo = new Loca.GeoJSONSource({
                data:greenline
            });

            heatLineLayer.setSource(greengeo);
            heatLineLayer.setStyle({
                altitude: 0,
                lineWidth: 3,
                // 脉冲头颜色
                headColor: //,
                (_, feature) => {
                    var value = feature.properties.tci;
                    if(value>7.25){//红
                        return 'rgba(255,0,0,0.8)';
                    }else if(value > 5.00){
                        return 'rgba(255,153,0,0.8)';
                    }else if(value > 2.75){
                        return 'rgba(255,255,0,0.8)';
                    }else{
                        return 'rgba(0,255,0,0.8)';
                    }
                    //return headColors[feature.properties.type - 1];
                },
                // 脉冲尾颜色
                trailColor: 'rgba(10, 26, 41, 0)',
                // 脉冲长度，0.25 表示一段脉冲占整条路的 1/4
                interval: 1,
                // 脉冲线的速度，几秒钟跑完整段路
                duration: 2000
            });
            loca.add(heatLineLayer);
            loca.animate.start();

        })
     },
    roadHeatMap(item){
        if(!this.layers[1].active&&!this.layers[2].active)
            this.$refs.timer.show = false;
        if(item.active){//取消方法
            item.active = '';
            if(!this.layers[1].active&&!this.layers[2].active){
                this.showLegen = false;
            }
            map.off('click');
            if(heatLineLayer){
                loca.remove(heatLineLayer);
                map.remove(zoneList_heatMap);
            }
        }else{
            item.active = 'active';
            if(continerModel == 1){
                this.$refs.timer.show = true;
                this.showLegen = true;
                this.roadHeatMap_lightup(this.time);

            }
        }
    },

    inToIntersection(attribute){
        let n = attribute['路口名称'].split('-');
        let channelName = n[0] + '与' + n[1];
        queryByChannelName({'channelName':channelName}).then(res => {
            console.log('channelName',res)
            var list = res.list;
            //this.$bus.emit('isShow', {'videoList':list,'attribute':{'lng':attribute.lng,'lat':attribute.lat,'crossName': channelName+'交叉口'}});
            localStorage.setItem('intersection_data', JSON.stringify({'videoList':list,'attribute':{'lng':attribute.lng,'lat':attribute.lat,'crossName': channelName+'交叉口'}}))
            this.$router.push('/holographicIntersection');
        })
        },
    }
}
</script>
<style scoped lang="less">
.layerTool{
    position:absolute;
    left: 1440px;
    bottom: 50px;
    width: 48px;
    //height: 192px;
    display: flex;
    flex-direction: column;
    z-index: 999;
    .layer{
        //flex: 1;
        margin-top: 24px;
        width: 48px;
        height: 48px;
        background: radial-gradient(50% 50% at 50% 50%, rgba(25, 159, 255, 0) 0%, rgba(25, 159, 255, 0.45) 100%);
        backdrop-filter: blur(4px);
        border-radius: 24px;
        cursor: pointer;
    }
    .active{
        background: radial-gradient(50% 50% at 50% 50%, rgba(1, 255, 255, 0) 0%, rgba(1, 255, 255, 0.45) 100%) !important;
    }

    .layer_icon_1{
        width: 28px;
        height: 28px;
        margin: auto;
        margin-top: 10px;
        background: url('../../assets/img/layer_icon_1.png') no-repeat;
        background-size:100% 100%;
    }
    .layer_icon_2{
        width: 28px;
        height: 28px;
        margin: auto;
        margin-top: 10px;
        background: url('../../assets/img/layer_icon_2.png') no-repeat;
        background-size:100% 100%;

    }
    .layer_icon_3{
        width: 28px;
        height: 28px;
        margin: auto;
        margin-top: 10px;
        background: url('../../assets/img/layer_icon_3.png') no-repeat;
        background-size:100% 100%;
    }

}


.hotLegen{
    position:absolute;
    left: 420px;
    bottom: 60px;
    width: 168px;
  height: 155px;
    background: url(../../assets/img/Group2706.png);
    background-repeat: no-repeat;
    background-size:100% 100%;
    z-index: 999;
}

.toolTipPanel{
    position: absolute;
    z-index: 999;
    margin-left: 20px;
    width: 80px;
    height: 30px;
    // background-color: pink;
    // background: url('../../../assets/img/Frame 1000002311.png') no-repeat;
    background: url('../../assets/img/Frame 1000002311.png') no-repeat;
    background-size: 100% 100%;
    color: #FFFFFF;
    height: 25px;
    font-family: 'Noto Sans SC';
    font-style: normal;
    font-weight: 400;
    font-size: 14px;
    line-height: 22px;
    text-align: center;
}

</style>
