<template>
<div class="outer" ref="outer">
    <div class="leftArrow"  ref="leftArrow" @click="changeUrl"></div>
    <div class="rightArrow" ref="rightArrow" @click="changeUrl"></div>
    <div  :class="[!rightScreen ? 'hide hideLeft' : '']" ref="hideLeft"></div>
    <div  :class="[rightScreen||notArr ? 'hide hideRight' : '']" ref="hideRight"></div>
    <LivePlayer id="player" ref="player" class="lp"
        :playUrl="url" 
        @onVideoEnd="vedioEnd"
        >
        <!-- <iframe style="width:100%;height:100%" src="http://192.168.137.101:8081/#/play/wasm/ws%3A%2F%2F192.168.137.1%3A80%2Frtp%2F34020000001320000003_003.flv"></iframe> -->
    </LivePlayer>
    <playerBar ref="playerBar" v-show="show" :sendSnap="sendSnap" :sendScreen="sendScreen"  :sendSpeed="sendSpeed" :sendTime="sendTime" :sendInterval="sendInterval"  :record="record" :camera="camera" :rightScreen="rightScreen" :sendPause="sendPause"
    :timeList="timeList"></playerBar>
</div>
</template>
<script>
import {ref,onMounted,onUnmounted,nextTick,toRef,watch} from 'vue'
import LivePlayer from '../PopupWindow/Jessibuca.vue' // vue3
import playerBar from './playerBar.vue'
import * as echartSetSize from '../../../assets/js/echartsSetSize'
import {seekPlayBack,setPlayBackSpeed,setPlayBackPaused} from '../../../assets/js/api_video'
let timer = null
let reconnectFlag = false
let lastFileIndex = null
let lastStamp = null
export default {
    components: {
        LivePlayer,
        playerBar
    },
    props: ['videoUrl','camera','record','mediaServerId','playSsrc','timeList','switchInPlace','startTime'],//url可能一个可能两个，record下的videoUrl存放一个url和摄像头ID以及其通道ID。但要进行修改，不再往url放摄像头id和通道Id,而在直播和回放中都存摄像头ID和通道号，用对象。
    setup(props){
        let width = ref(echartSetSize.setSize(500))
        let height = ref(echartSetSize.setSize(290))
        let url = ref(null)//视频Url
        let newMediaServerId = ref(null)
        let newPlaySsrc = ref(null)
        let newTimeList = ref(null)
        let rightScreen = ref(false)//是否在二号摄像头
        let playerBar = ref(null)
        let notArr = ref(false)//传入videoUrl非arr
        let leftArrow = ref(null)//左箭头dom
        let rightArrow = ref(null)//右箭头dom
        let outer = ref(null)//外层div dom
        let show = ref(false)
        let addTime = ref(0) //鼠标拖动时间轴抬起时选择的时间
        let cutTime = ref(0) //已经播放的时间
        let pause = ref(false)

        function  mouseEnterLeaveAndBar(){//鼠标进入离开录像区域，箭头和工具栏的变化
            nextTick(()=>{
                outer.value.addEventListener('mouseenter',()=>{
                show.value = true//工具栏显示
                if(!rightScreen.value){
                    rightArrow.value.style.opacity = 1//若当前不在右频道，显示右箭头

                }else{
                    leftArrow.value.style.opacity = 1//若当前在右频道，显示左箭头
                }
                })
                outer.value.addEventListener('mouseleave',()=>{
                show.value = false
                leftArrow.value.style.opacity = 0  
                rightArrow.value.style.opacity = 0
                })
            })
        }

        function  showBar(){
            nextTick(()=>{
                outer.value.addEventListener('mouseenter',()=>{
                show.value = true
                })
                outer.value.addEventListener('mouseleave',()=>{
                show.value = false
                })
            })
        }

        function urlsIsArray(arr){
            // console.log(arr)
            if(props.camera.channel.length==2){
                if(!props.switchInPlace){
                    rightArrow.value.style.opacity = 1//若非原频道切换，直接重置箭头即可，显示向右。
                }
                mouseEnterLeaveAndBar()
                url.value = arr[0]
            }else if(Array.isArray(arr)&&props.camera.channel.length==1){
                url.value = arr[0]
                showBar()
                notArr.value = true //若单频道，隐藏箭头
            }else{
                showBar()
                url.value = arr
                notArr.value = true//若单频道，隐藏箭头
            }
        }

        watch(()=>props.videoUrl,
        (now,old)=>{
            cutTime.value = 0
            newMediaServerId.value = props.mediaServerId
            newPlaySsrc.value = props.playSsrc
            newTimeList.value = props.timeList
            if(Array.isArray(now)){
                url.value = now[0]
            }else{
                url.value = now
            }
            if(!props.switchInPlace){
                rightScreen.value = false//非原频道跳转，直接将右频道判断置false，显示右箭头即可
            }
            if(!reconnectFlag){
                addTime.value = 0
                playerBar.value.getTimeValue(0)
                if(pause.value){
                    pause.value = false
                    playerBar.value.pause = false//取消暂停状态
                    // player.play()
                }
            }
            //这个监听，导致录像切换直播后箭头还原成默认方向了。加上一个switchInplace判断是否原地切换。
        })

        onMounted(()=>{
            urlsIsArray(props.videoUrl)
            newMediaServerId.value = props.mediaServerId
            newPlaySsrc.value = props.playSsrc
            newTimeList.value = props.timeList
            console.log(newTimeList.value)
        })
        return {
            url,
            rightScreen,
            leftArrow,
            rightArrow,
            videoUrl: toRef(props,'videoUrl'),
            camera: toRef(props,'camera'),
            record: toRef(props,'record'),
            startTime: toRef(props,'startTime'),
            newMediaServerId,
            newPlaySsrc,
            newTimeList,
            playerBar,
            outer,
            width,
            height,
            notArr,
            show,
            addTime,
            cutTime,//已播放时间
            pause,
        };
    },
    mounted(){
        this.$bus.on('recordChange',(res)=>{//等待切换历史回放后返回新的url及媒体服务ID以及playssrc，与全息有关
            // console.log(res)
            this.url = res.url
            this.newMediaServerId = res.mediaServerId
            this.newPlaySsrc = res.playSsrc
            this.newtimeList = res.timeList
            this.cutTime = 0            
            if(!reconnectFlag){//如果现在不是重连而是切换，执行下方
                this.addTime = 0
                this.$refs.playerBar.getTimeValue(0)//seek失败后这俩不归零其他照常即可
                this.setPause()
            }else{//如果现在是重连，执行下方
                reconnectFlag = false
                this.$refs.playerBar.pause = false // 重连后一般url不变，直接在这里设置进度条变成播放状态。
                this.seekPlayBackRequestReconnect(lastFileIndex,lastStamp)//用于视频播放完后仍跳转，先重连，之后执行此函数跳转。由于后端可能跳转失败，存在重连-跳转死循环问题，因此只执行一次。
            }
        })
        this.changeScreen()//控制全屏
        this.setInterval()//开启进度条定时器
    },
    unmounted(){
        this.$bus.off('recordChange')
        clearInterval(timer)
        timer = null
    },
    methods:{
        sendSnap(){
          this.$refs.player.jessibuca.player._opt.operateBtns.screenshotFn('screenshot')
        },
        snapOutside(data){
            this.downloadImage(data)//外部截屏函数
        },
        downloadImage(base64Url) {//下载截图
            // console.log(base64Url)
            let imgUrl = base64Url;
                let a = document.createElement("a");
                a.href = imgUrl;
                a.setAttribute("download", "screenshot");
                a.click();
            
        },
        reconnect(){
            // console.log("直接重连视频")
            reconnectFlag = true//设置重连标记，用于区分切换频道和重连
            this.$bus.emit('recordParam',{deviceId:this.camera.deviceId,channelId:this.camera.channel[0],startTime:this.startTime})
                //单通道摄像头没有这些参数无法重连，直接所有摄像头都往这传摄像头和通道号。不要往url Push了，直接单独放对象里传，而且属性为空打印也不报错。
                //并且这个判断条件可以覆盖单双通道，因为单通道rightScreen必然为false，仍请求一号通道。到时候只吧参数换成对象属性即可。不用添加代码。                                                                                     
        },
        
        seekPlayBackRequest(fileIndex,stamp){//跳转
            seekPlayBack({playSsrc:this.newPlaySsrc,fileIndex:fileIndex,mediaServerId:this.newMediaServerId,timeStamp:stamp}).then((res)=>{
                // console.log(res)
                this.cutTime = this.$refs.player.pts
                clearInterval(timer)
                timer = null
                // console.log('收到重启定时器')
                if(res.code==0){
                    this.setInterval()
                    this.setPause()//若目前为暂停，执行函数内代码
                }else if(stamp||stamp==0){
                    lastFileIndex = fileIndex
                    lastStamp = stamp
                    this.reconnect()
                }
            })
        },
        seekPlayBackRequestReconnect(fileIndex,stamp){//重连后跳转
            seekPlayBack({playSsrc:this.newPlaySsrc,fileIndex:fileIndex,mediaServerId:this.newMediaServerId,timeStamp:stamp}).then((res)=>{
                // console.log(res)
                this.cutTime = this.$refs.player.pts
                clearInterval(timer)
                timer = null
                // console.log('收到重启定时器')
                if(res.code!=0){
                    this.addTime = 0
                }
                this.setPause()//若目前暂停，执行内部代码
                this.setInterval()
            })
        },
        sendScreen(msg){
            if(msg){
                this.$refs.outer.requestFullscreen()
                // console.log('收到开启全屏')
            }else{
                document.exitFullscreen()
                // console.log('收到关闭全屏')
            }
        },
        sendSpeed(msg){
            // console.log('收到调整倍速',msg)
            setPlayBackSpeed({mediaServerId:this.newMediaServerId,playSsrc:this.newPlaySsrc,speed:msg}).then((res)=>{
                console.log(res)
            })
        },
        sendTime(msg){
            let stamp = 0
            let fileIndex
            // console.log('收到调整时间',msg)
            this.addTime = msg
            if(msg<this.newTimeList[0].endTime - this.newTimeList[3]){
                fileIndex = 0
                stamp = this.newTimeList[3] + this.addTime
                // console.log(stamp,msg + this.newTimeList[3] - this.newTimeList[0].startTime,this.newTimeList[0].timeLen)
            }else if(this.newTimeList[3] + msg < this.newTimeList[1].endTime){
                fileIndex = 1
                stamp = this.newTimeList[3] + this.addTime
                // console.log(stamp,)
            }else{
                // console.log(this.newTimeList)
                fileIndex = 2
                stamp = this.newTimeList[3] + this.addTime
                // console.log(stamp)
            }
            this.seekPlayBackRequest(fileIndex,stamp)
        },
        setIntervalCallBack(){
            this.$refs.playerBar.getTimeValue(this.$refs.player.pts+this.addTime-this.cutTime)//根据当前播放时间，跳转处时间以及之前播放时间，计算进度给进度条
            return this.setIntervalCallBack
        },
        setInterval(){
            timer = setInterval(this.setIntervalCallBack(),1000)
        },
        sendInterval(msg){
            if(msg){
                clearInterval(timer)
                timer = null
                // console.log('收到清除定时器')
            }
        }
        ,
        changeUrl(){//切换频道
            this.leftArrow.style.opacity = 0
            this.rightArrow.style.opacity = 0
            this.rightScreen = !this.rightScreen
            if(this.rightScreen){ 
                this.leftArrow.style.opacity = 1
            }else{
                this.rightArrow.style.opacity = 1
            }
            //这边根据pop2传入的另一个判断是历史还是直播的flag来决定采取何种操作，重新请求/直接切换链接
            this.camera.channel.reverse()
            // console.log(this.camera.channel)
            if(!this.record){
                // console.log(this.videoUrl)
                this.videoUrl.push(this.videoUrl.shift())
                this.url = this.videoUrl[0]//直播可以直接用这个切换链接
            }else{
                //下面为历史回放请求，方便传值和避免切换出错，使用两个Bus绑定函数直接与layertool交互。
                // console.log(this.vedioUrl)
                    this.$bus.emit('recordParam',{deviceId:this.camera.deviceId,channelId:this.camera.channel[0],startTime:this.startTime})//回放没有直接reverse链接的原因是，几秒不访问直接把flv链接释放了。
            }
        },
        changeScreen(){//控制全屏
            this.$refs.outer.addEventListener("fullscreenchange", ()=>{
                if (document.fullscreenElement) {
                    // console.log('进入全屏')
                } else {
                    // console.log('退出全屏')//不通过点击方式关闭全屏，改换图标，将全屏标记置否
                    this.$refs.playerBar.getScreen(false)
                }
            })
        },
        sendPause(msg){//暂停与继续
            // console.log('pause',msg)
            this.pause = msg
            let params = {playSsrc: this.newPlaySsrc,mediaServerId: this.newMediaServerId}
            if(msg){
                params.paused = 1
                setPlayBackPaused(params).then(()=>{
                    this.$refs.player.pause()
                    document.querySelector('.jessibuca-play-big').style.display = 'none'
                    document.querySelector('.jessibuca-controls').style.display = 'none'
                    this.addTime = this.$refs.playerBar.timeValue
                    this.cutTime = 0
                    this.$refs.player.pts = 0
                    this.$refs.playerBar.getTimeValue(this.$refs.player.pts+this.addTime-this.cutTime)//根据当前播放时间，跳转处时间以及之前播放时间，计算进度给进度条
                },err=>{
                    this.$refs.player.pause()
                })

            }else{
                params.paused = 0
                setPlayBackPaused(params).then(res=>{
                    this.$refs.player.play()
                },err => {
                    this.$refs.player.play()
                })
            }
        },
        setPause(){//若目前暂停，开始播放
            if(this.pause){
                this.pause = false
                this.$refs.playerBar.pause = false
                // let params = {mediaServerId: this.newMediaServerId,playSsrc: this.newPlaySsrc,paused: false}
                // setPlayBackPaused(params)
                this.$refs.player.play(this.url)
            }
        },
        vedioEnd(){// 视频结束回调，告知Playerbar置暂停按钮状态
            this.$refs.playerBar.pause = true
            this.$refs.player.destroy()

        },
    }
}
</script>
<style lang="less" scoped>
    /deep/.video-wrapper{
        padding-bottom:58% !important
    }
    :-webkit-full-screen #player {
    width: 100%;
    height: 100vh;
    }
    .outer{
        position: relative;
        width: 500px;
        height: 290px;
    }
    // .lp{
        // width: 500px;
        // height: 290px;
    // }
    .leftArrow,.rightArrow,.hide{
    position: absolute;
    top: 50%;
    width: 40px;
    height: 40px;
    // display: none;
    z-index: 2;
    }
    .hide{
        display: block;
        transform: translateY(-50%);
        z-index: 9999;
    }
    .hideLeft{
        left: 0;
    }
    .hideRight{
        right: 0;
    }
    .rightArrow{
        opacity: 0;
        transition: opacity 0.5s;
        right: 0;
        background-image: url(../../../assets/img/pop2Arrow.png);
        background-size: 100% 100%;
        transform: translateY(-50%);
    }
    .leftArrow{
        opacity: 0;
        transition: opacity 0.5s;
        background-image: url(../../../assets/img/pop2Arrow.png);
        background-size: 100% 100%;
        transform: translateY(-50%) rotate(180deg);
        left: 0;
    }
    .rightArrow:hover{//切换频道相关
        cursor: pointer
    }
    .leftArrow:hover{//切换频道相关
        cursor: pointer;
    }
    :deep(.jessibuca-container .jessibuca-controls-show-auto-hide .jessibuca-controls){
      opacity: 0 !important;
    }
</style>
