<template>
    <div class="eventOuter" :class="[data.Result_path ? '' : 'cutHeight']" v-show="vShow">
        <div class="filter"></div>
        <div class="close" @click="close"  ></div>
        <div class="title-cell1">
            <div class="cell1"></div>
            <div class="title">{{data.crossroad_name+"&nbsp;&nbsp;事件详情"}}</div>
            <div class="cell1 rotate"></div>
        </div>
        <table>
            <tr class="header">
                <th>事件发生地点</th>
                <th>事件发生时间</th>
                <th>事件类型</th>
                <th>事件名称</th>
                <th>事件参与者</th>
            </tr>
            <tr class="tableContent">
                <td>{{data.crossroad_name}}</td>
                <td>{{data.data_time}}</td>
                <td>异常事件</td>
                <td>{{data.event_type}}</td>
                <td>{{data.Class_type ? data.Class_type : '--'}}</td>
            </tr>
        </table>
        <div class="evidence-cell" v-show="data.Result_path">
            <div class="video-evidence-cell">
                <div class="title-cell">
                    <span>视频监控</span>
                    <span class="decoration"></span>
                </div>
                <div  class="vedioBg">
                    <LivePlayer v-if="flag" :videoUrl="url" :camera="camera" :startTime="startTime" :record="record" :playSsrc="playSsrc" :timeList="timeList" :mediaServerId="mediaServerId" :switchInPlace="switchInPlace" ></LivePlayer>
                </div>
            </div>
            <div class="photo-evidence-cell">
                <div class="title-cell ">
                    <span>证据链截图</span>
                    <span class="decoration"></span>
                </div>
                <div class="photo">
                    <img :src="data.Result_path" class="resultImg">
                </div>
            </div>
        </div>
    </div>
</template>
<script>
import { nextTick } from 'vue-demi';
import 撒点图标 from '../../../assets/img/icon/撒点图标.png'

import LivePlayer from './LivePlayer.vue';
import { queryChannelsByDeviceId,cloudRecord,vedioLogin } from '../../../assets/js/api_video'
import dayjs from 'dayjs'
let deviceId = '13310206041320302370'//先写死一个摄像头ID，之后再说
let pointIdArr = [];
var entityCollection = null;
export default {
    components:{LivePlayer},
    props:['eventData'],
    data(){
        return{
            vShow:false,
            hasEvidence:true,
            resultPath: '',
            data:{
                crossroad_name:'sdaffasfd',
                data_time:'fasdfsddfas',
            },
            timeStamp: null,
            intersectionName:'阿斯顿更好地发挥的风格和',
            flag: false,//下半部分是否显示
            //录像相关
            url:[],//切换频道相关
            record: true,//这边只有录像，不切直播
            mediaServerId: null,
            playSsrc: null,
            timeList:[],
            camera: null,
            switchInPlace: false,//原频道切换，此页面录像操作不需要这个属性
            startTime: null
        }
    },
    watch:{
        eventData:{
            handler(newValue){
                let value = this.transfield(newValue);
                this.addPointer(value);
            }
        }
    },
    mounted(){
        this.changeRecord()
        nextTick(()=>{
            //entityCollection = new Cesium.EntityCollection();
            //window.viewer.scene.primitives.add(entityCollection);
            //this.clickEvent(window.viewer);
        })
    },
    unmounted(){
        this.$bus.off('recordParam')
    },
    methods:{
        close(){
            pointIdArr.forEach(p=>{
                viewer.entities.removeById(p);
            })
            pointIdArr = [];
            this.vShow = false;
            this.flag = false
            this.url = []
        },
        addPointer(data){
            //let mark = new BounceMarker(viewer,[Number(data.longitude),Number(data.latitude),8]);
            let height = 8;
            let bounceHeight = 100
            let increment = 0.05
            const i = height + bounceHeight;
            let t = 0;
            let s = 0;
            let bounceMarker = viewer.entities.add({
                id:data.data_time,
                name:'eventPoint',
                position: Cesium.Cartesian3.fromDegrees(Number(data.longitude), Number(data.latitude), 6),
                // new Cesium.CallbackProperty(() => {
                //     s += increment;
                //     t += s;
                //     if (t > bounceHeight) {
                //         t = bounceHeight;
                //         s *= -1;
                //         s *= 0.55;
                //     }
                //     return Cesium.Cartesian3.fromDegrees(Number(data.longitude), Number(data.latitude), i - t);
                // }, false),
                billboard: {
                    image: 撒点图标,
                    verticalOrigin: Cesium.VerticalOrigin.BOTTOM,
                },
                scaleByDistance: new Cesium.NearFarScalar(0, 0, 1, 1),
                pixelOffsetScaleByDistance: new Cesium.NearFarScalar(0, 0, 1, 1),
                attributes:data,
                click:(t)=>{
                    if(t.name != 'eventPoint') return;
                    this.data = t.attributes;
                    pointIdArr.push(t.id);

                    this.vShow = true;
                    this.timeStamp = dayjs(this.data.data_time).unix() - 60
                    if(t.attributes){// 避免undefined报错暂时这么写
                        if(t.attributes.Result_path){
                            this.playRecord(deviceId)
                        }
                    }


                    //TODO点了就消失

                }
            });
        },
        transfield(data){
            if(data.topic == 'ads_sc_rad_event_statistics_realtime' ){
                data.Class_type = data.pati_name
                return data;
            }else{

                let Event_name,Event_type,Class_type = '';
                switch (data.Event_type){
                    case 0:
                        Event_type = '逆行'
                        break;
                    case 1:
                        Event_type = '非机动车逆行'
                        break;
                }

                switch (data.Event_name){
                    case 0:
                        Event_name = '机动车逆行'
                        break;
                    case 1:
                        Event_name = '非机动车逆行'
                        break;
                }

                switch (data.Class_type){
                    case 'Person':
                        Class_type = '人'
                        break;
                    case 'person_motorcycle':
                        Class_type = '人骑摩托车'
                        break;
                    case 'person_electrocar':
                        Class_type = '人骑电动车'
                        break;
                    case 'person_bicycle':
                        Class_type = '人骑自行车'
                        break;
                    case 'car':
                        Class_type = '小汽车'
                        break;
                    case 'truck':
                        Class_type = '货车'
                        break;
                    case 'heavy_truck':
                        Class_type = '重型卡车'
                        break;
                    case 'bus':
                        Class_type = '公交'
                        break;
                }
                return {
                    crossroad_name:'崇文北路与乐民街交口',
                    data_time:data.Time,
                    Event_type:Event_type,
                    event_type:Event_name,
                    Class_type:Class_type,
                    Result_path:data.Result_path,
                    longitude:115.91628441772625,
                    latitude:39.06930366603143
                }
            }
        },

        async login(){//登录，与全息有关，全息也需要自动登录
            return vedioLogin().then(res=>{
                // console.log(res)
                localStorage.setItem('vedioToken', JSON.stringify(res.data))
            })
        },
        playRecord(deviceId){//播放录像，与全息有关，而且有自动登录，不过可能不需要回调函数。
            queryChannelsByDeviceId({
                deviceId:deviceId
            }).then((res)=>{//查找所有通道
                // console.log('查找摄像头通道',res)
                var channelId
                var flag = false
                this.camera = {deviceId:deviceId}//通过点击图像进入录像时将摄像头ID和所有频道号存入camera对象供切换视频等使用
                if(res.length == 0){
                    alert('已查询到摄像头信息,但该设备未建立通道')
                    return;
                }else if(res.length == 1){
                    channelId = res[0]['channelId']
                    this.camera.channel = [res['channelId']]//即使只有一个频道，也装到数组，方便reverse不设置判断条件
                }else{
                    flag = true
                    channelId = [res[0]['channelId'],res[1]['channelId']]
                    this.camera.channel = [res[0]['channelId'],res[1]['channelId']]
                }
                //请求录像回放
                cloudRecord({deviceId: deviceId,channelId: flag? channelId[0] : channelId,recordStartTime:this.timeStamp,recordEndTime:this.timeStamp+120,playStartTime:this.timeStamp})
                .then(res=>{//使用第一个频道进入录像
                    if(res.code == 401){
                        console.log('账号登录')
                        this.login().then(()=>{
                            this.playRecord(deviceId)
                        })
                        return
                    }
                    if(res.code != -1){
                        this.url.push(res.data.playBackUrl)//只有一个录像，但也存入数组中，因为录像即便有两个通道也只存一个链接。Player中通过链接是值还是数组判断是否增加左右箭头。
                        this.mediaServerId = res.data.mediaServerId
                        this.playSsrc = res.data.playSsrc
                        this.timeList = [res.data.recordList[0]]//录像链接和调整进度相关属性
                        this.startTime = this.timeStamp
                        if(res.data.recordList[1]){
                            this.timeList[1] = res.data.recordList[1]
                        }
                        if(res.data.recordList[2]){
                            this.timeList[2] = res.data.recordList[2]
                        }
                        this.timeList[3] = this.timeStamp
                        this.timeList[4] = 120//给Playerbar判断用
                        // console.log(this.timeList)
                        this.flag = true//显示下半部分
                        this.$nextTick(()=>{
                            this.$bus.emit('isEvent','')
                        })
                    }
                })
            })
        },
        changeRecord(){//切换频道的，与全息有关，除切换频道外，重连视频也使用这个。
            this.$bus.on('recordParam',res=>{//切换同一摄像头不同频道录像,这个录像请求需要保持当前请求时间不变，即便TIMEAXIS刷新他也不能变。
                let startTime = res.startTime
                cloudRecord({deviceId: res.deviceId,channelId: res.channelId,recordStartTime:res.startTime,recordEndTime:res.startTime+120,playStartTime:res.startTime})
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

    }
}
</script>
<style lang="less" scoped>
    @height: 10.8vh;
    /deep/.video-wrapper{
        width: 99%;
        padding-bottom:56% !important
    }
    .eventOuter::before{
        content: '';
        position: absolute;
        z-index: -1;
        height: 86%;
        width: 96%;
        backdrop-filter: blur(3px);
    }
    .eventOuter{
        box-sizing: border-box;
        position: absolute;
        display: flex;
        flex-direction: column;
        align-items: center;
        top: (170/@height);
        left: 50%;
        transform: translateX(-50%);
        width: 928px;
        height: 640px;
        z-index: 10000;
        padding: 30px 0px;
        background: url('../../../assets/img/Overview/Frame 1000002472.png') no-repeat;
        background-size: 100% 100%;
        // .filter{
        //     position: absolute;
        //     backdrop-filter: blur(3px);
        //     width: 100%;
        //     height: 100%;
        //     z-index: -1;
        // }
        .close{
            position: absolute;
            height: 28px;
            width: 70px;
            right: 10px;
            &:hover{
                cursor: pointer;
            }
            background-image: url('../../../assets/img/Overview/Group 1000002494.png');
            background-size: 100% 100%;
            z-index: 10001;
        }

        .title-cell1{
            align-items: center;
            gap: 6px;
            margin-top: 5px;
            width: 100%;
            height: 26px;
            // height: 2.4vh;
            display: flex;
            justify-content: center;
            align-items: flex-end;
            flex-direction: row;
            .title{
                //width: 220px;
                height: 28px;
                font-family: 'Noto Sans SC';
                font-style: normal;
                font-weight: 700;
                font-size: 20px;
                line-height: 28px;
                color: #47EBEB;
            }
            .cell1{
            height: 8px;
            width: 140px;
            margin-bottom: 8px;
            background: url(../../../assets/img/Overview/decoration.png) no-repeat;
            background-size: 100% 100% ;
            }
            .rotate{
            transform: rotateY(180deg);
            }
        }

        table{
            width: 856px;
            color: white;
            margin-top: 64px;
            border: 0;
            // rules:none;
            background:transparent;
            .header{
                height: 32px;
                background: linear-gradient(90deg, rgba(51, 153, 255, 0) 0%, rgba(51, 153, 255, 0.3) 51.87%, rgba(51, 153, 255, 0) 100%);
            }
            .tableContent{
                td{
                    padding-top: 20px;
                    font-family: 'Noto Sans SC';
                    font-style: normal;
                    font-weight: 400;
                    font-size: 14px;
                    line-height: 22px;
                    color: rgba(255, 255, 255, 0.9);
                }
            }
            th{
                font-family: 'Noto Sans SC';
                font-style: normal;
                font-weight: 400;
                font-size: 16px;
                line-height: 24px;
                color: rgba(255, 255, 255, 0.8);
            }
            td{
                text-align: center;
            }
        }

        .evidence-cell{
            width: 806px;
            margin-top: 60px;
            display: flex;
            flex-direction: row;
            justify-content: space-between;
            .title-cell{
                height: 24px;
                font-family: 'Noto Sans SC';
                font-style: normal;
                font-weight: 400;
                font-size: 16px;
                line-height: 24px;
                color: #fff;
            }
            .video-evidence-cell{
                /deep/.lp{
                    width: 388px;
                    height: 218px;
                }
                .vedioBg{
                    width: 388px;
                    height: 218px;
                    margin-top: 29px;
                    padding: 1px;
                    background-image: url('../../../assets/img/Overview/Frame 427322007.png');
                    background-size: 100% 100%;
                }

            }

            .photo-evidence-cell{
                .photo{
                    margin-top: 29px;
                    padding: 2px 0 0 2px;
                    width: 388px;
                    height: 218px;
                    background-image: url('../../../assets/img/Overview/Frame 427322007.png');
                    background-size: 100% 100%;
                    .resultImg{
                        height: 99%;
                        width: 99.5%;
                    }
                }
            }
            .decoration{
                display: inline-block;
                height: 6px;
                width: 140px;
                margin-left: 16px;
                background: url(../../../assets/img/Overview/decoration2.png) no-repeat;
                background-size: 100% 100% ;
            }
        }
    }
    .cutHeight{
        height: 280px;
    }
</style>
