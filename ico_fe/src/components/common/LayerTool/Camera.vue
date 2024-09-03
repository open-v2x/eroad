<template>
    <div class="layer" :class="{active}" ref="camera"  @click="showHandel">
        <div class="layer_icon"></div>
        <div class="toolTipPanel" v-show="showTooltip">视频</div>
    </div>
</template>
<script>
import { createApp } from 'vue';
import { queryChannelsByDeviceId,queryAllChannels,sendDevicePush,cloudRecord,vedioLogin } from '../../../assets/js/api_video'
import cameraData from '../../../assets/config/cameraData.json'
import cameraIcon from '../../../assets/img/Group 1000002301.png'
var CoordTransform=require('coordtransform');
var node;//document.createElement("div");
var app;//vedio
var cluster;//vedio
let cameraMarkerList = [];//vedio
var onlineCamera = [];//将cameraData清洗完之后的数据
let viewCom//vedio
let lng//vedio
let lat//vedio
let crossRoad//vedio
let position//vedio
let deviceId//vedio
var cameraLayerSource;//vedio
export default {
    props:{
        time:String
    },
    data(){
        return{
            unlock: false,
            active: false,
            timeStamp:'',
            record: false,
            popupWindowVisibility: false,
            showTooltip: false
        }
    },
    watch:{
        time(now,old){
            let date = new Date(now)
            this.firstChangeTimeStamp(date)
            this.record = true//记录时间戳并转为回放模式
            if(this.popupWindowVisibility&&this.unlock){//触发后根据是否存在视频视窗切换最新录像。
                // console.log('发送切换record的信号')
                this.timeStamp = Math.round(date.getTime()/1000)
                this.$bus.emit('sendCheckoutRecord','')//触发发出切换录像的命令。与playerbar绑定，与全息无关。
            }else if(this.unlock){
                // console.log('修改timeStamp')
                this.timeStamp = Math.round(date.getTime()/1000)
            }
        }
    },
    created(){
        this.checkoutRecord()//绑定总线事件：监听执行切换录像的信号和参数。与全息无关。
    },
    mounted(){
        this.tooltip()
        this.changeRecord()//绑定与liveplayer之间的数据传输（用于record模式同一摄像头切换不同频道）。
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
            window.localStorage.setItem('cameras',JSON.stringify(onlineCamera))
        })//获取所有通道，去重，不在此列的cameraData数据不显示图标。
    },
    unmounted(){

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
        showHandel(){
            if(continerModel == 1){
                this.showCameraMarker();
            }else{
                this.showCameraMarker3d();
            }
        },
        showCameraMarker(){//item有id name active func

            if(this.active){//取消方法
                this.active = false;
                this.$emit('getAxisShowList',{index:2,value:false})
                if(continerModel == 1){
                    map.remove(cameraMarkerList);
                    cluster.setMap(null);
                    cameraMarkerList = [];
                }else{
                    cameraLayerSource.entities.removeAll();
                }
            }else{//添加方法
                this.active = true;
                this.$emit('getAxisShowList',{index:2,value:true})
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
                    cluster = new AMap.MarkerClusterer(window.map, cameraMarkerList, {gridSize: 60});
                    //console.log('1111111111111111',cluster.getMarkers());
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
        showCameraMarker3d(){
            // alert('showCameraIcon3d');
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
                    if(res.code == -1){
                        alert(res.msg)
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
                    if(res.code == -1){
                        alert(res.msg)
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
                this.$bus.emit('pop2D', {propertity:{url:res.url,camera:res.camera,startTime:res.startTime,mediaServerId:res.mediaServerId,playSsrc:res.playSsrc,timeList:res.timeList,lng:lng,lat:lat,交叉路口名称:crossRoad,viewCom:viewCom,record: true}});
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
                    if(res.code == -1){
                        alert(res.msg)
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
        tooltip(){
            this.$refs.camera.addEventListener('mouseenter',()=>{
                this.showTooltip = true
            })
            this.$refs.camera.addEventListener('mouseleave',()=>{
                this.showTooltip = false
            })
        }
    }
}
</script>
<style scoped lang="less">
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

    .layer_icon{
        width: 28px;
        height: 28px;
        margin: auto;
        margin-top: 10px;
        background: url('../../../assets/img/layer_icon_3.png') no-repeat;
        background-size:100% 100%;
    }

</style>
