<template>
  <div class="background"  ref="background"  >
    <div class="voice" ref="voice" >
        <div  class="voiceSvg" :class="[pause ? 'audioOff':'audioOn']" @click="videoPause"></div>
        <!-- <div v-show="false" class="sliderbg"  ref="slider">
            <el-slider class="vertical" placement="right"  vertical @click.stop v-model="pause" />
        </div> -->
    </div>
    <span class="time">{{record ? format(timeValue) : ''}}{{time}}</span>
    <el-slider v-show="showSlider" class="slider"  v-model="timeValue" :format-tooltip="format" :max="max" />
    <!--这里取到的视频全都是600S，在TOOLLAYER里设置的，max设置600-->
    <div class="tools">
        <span v-show="showSlider&&notEvent" class="live" title="切换直播" @click="live">直播</span>
        <!-- <span class="speed " title="倍速" @click="speed">x1</span> -->
        <span class="snap" title="快照" @click="snap()"></span>
        <span class="fullScreen" :title="!fullScreenOff? '全屏' : '关闭全屏'" :class="[!fullScreenOff ? 'fullScreenOn' : 'fullScreenOff']" @click="screen();"></span>
    </div>
  </div>
</template>

<script>
import bus from '@/assets/js/bus';
let resetInterval = false
export default {
    props:['sendSnap','sendTime','sendScreen','sendSpeed','sendInterval','record','timeList','camera','rightScreen','sendPause'],
    data(){
        return{
            timeValue: 0,
            pause: false,
            audioOff: null,
            fullScreenOff: false,
            time: '/10:00',
            now: '00:00',
            showSlider: true,
            notEvent: true,
            max:600,
        }
    },
    watch:{
        record(now,old){
            if(!now){
                this.time = 'LIVE'
                this.showSlider = false
            }else{
                this.showSlider = true//不用判断120S，因为事故不看直播
                this.listenMouseUp()
                // console.log(this.timeList)
                this.computeTime()//计算进度条多少时间
            }
        },
        timeList(now,old){
            if(!now){
                this.time = 'LIVE'
                this.showSlider = false
            }else{
                this.showSlider = true
                this.listenMouseUp()
                this.computeTime()//计算进度条多少时间
            }
        }
    },
    mounted(){
        this.changeRecord()
        this.isEvent()
        // console.log(this.record)
        // this.hide()
        this.getSlider()
        if(this.record){
            this.showSlider = true
            this.listenMouseUp()
            this.computeTime()//计算进度条多少时间
        }else{
            this.time = 'LIVE'
            this.showSlider = false
        }
    },
    unmounted(){
        document.removeEventListener('mouseup',this.setTimeValue)
        bus.off('sendCheckoutRecord')
    },
    methods:{
        computeTime(){
            if(this.timeList[4]==120){
                this.time = '/02:00'
                this.max = 120             
            }else if(!this.timeList[1]&&!this.timeList[4]){
                this.time = '/' + this.format(this.timeList[0].endTime - this.timeList[3])
                let videoLength = this.timeList[0].endTime - this.timeList[0].startTime
                let allLegnth = this.timeList[0].endTime - this.timeList[3] 
                this.max = allLegnth > videoLength ? videoLength : allLegnth 
            }else if(!this.timeList[2]&&!this.timeList[4]){
                this.time = '/' + this.format(this.timeList[0].endTime - this.timeList[3] + this.timeList[1].timeLen)
                this.max = this.timeList[0].endTime - this.timeList[3] + this.timeList[1].timeLen
            }
            else{
                this.time = '/10:00'
                this.max = 600                
            }
        },
        videoPause(){
            this.pause = !this.pause
            this.sendPause(this.pause)
        },
        setOldTime(){//点击进度条触发，此时关闭定时器，方便移动。
            resetInterval = true
            this.sendInterval(1)
        },
        listenMouseUp(){
            document.addEventListener('mouseup',this.setTimeValue)
        },
        setTimeValue(){
            if(resetInterval){
                this.sendTime(this.timeValue)
                resetInterval = false
            }
        },
        snap(){
            this.sendSnap()
        },
        getTimeValue(value){
            this.timeValue = Math.ceil(value)
        },
        speed(){
            this.sendSpeed(3)
        },
        getScreen(screen){
            this.fullScreenOff = screen
        },
        screen(){
            this.fullScreenOff = !this.fullScreenOff
            this.sendScreen(this.fullScreenOff)
        },
        hide(){
            this.$refs.voice.addEventListener('mouseenter',()=>{
                this.$refs.slider.style.display = 'flex'
            })
            this.$refs.voice.addEventListener('mouseleave',()=>{
                this.$refs.slider.style.display = 'none'
            })
        },
        format(data){
                let minute =  Math.floor(data/60)
                let second = data%60
                if(minute<10){
                    minute = '0' + minute
                }
                if (second<10){
                    second = '0' + second
                }
                return minute + ':' + second
        },
        live(){
            let request = {...this.camera,rightScreen:this.rightScreen}
            bus.emit('live',request)
        },
        getSlider(){
            // console.log('%c打印节点','font-size:30px')
            // console.log(document.querySelectorAll('.el-slider__runway')[0])
            document.querySelectorAll('.el-slider__runway')[0].addEventListener('mousedown',this.setOldTime) 
        },
        changeRecord(){
            bus.on('sendCheckoutRecord',()=>{//接收到切换录像的命令
                let request = {...this.camera}
                console.log('接到切换record的信号,执行切换信号')
                bus.emit('checkoutRecord',request)//发出执行切换的命令，传递切换参数
            })
        },
        isEvent(){
            bus.on('isEvent',()=>{//接收到切换录像的命令
                this.notEvent = false
            })
        }
    }
}
</script>

<style lang="less" scoped>
.background{
    position: absolute;
    bottom: 0;
    width: 100%;
    height: 40px;
    background-color: rgba(43,51,63,1);
    display: flex;
    align-items: center;
    z-index: 2147483648;
    .tools{
        margin-left: 10px;
        flex: 1;
        height: 100%;
        display: flex;
        justify-content: end;
        align-items: center;
    }
    // .voice{
    //     height: 60%;
    //     width: 40px;
    // }
    .snap,.fullScreen,.speed,.live,.voice{
        height: 60%;
        width: 40px;
        &:hover{
            cursor: pointer;
        }
    }
    .live{
        line-height: 24px;
        font-size: 12px;
        color: #fff;
        border: 2px solid #fff;
        border-radius: 5px;
    }
    .voice{
        height: 100%;
        position: relative;

    }
    .voiceSvg{
        height: 60%;
        position: relative;
        top: 20%;
    }
    /deep/.el-slider__runway{
            margin: 0;
            background-color: rgba(255,255,255,.3) !important;
        }
    /deep/.el-slider__button{
        left: 11.5px;
    }
    .audioOn{
        background: url(../../../assets/img/popup/playerBar/zanting.svg) no-repeat;
        background-size: 100% 100%;
    }
    .audioOff{
        background: url(../../../assets/img/popup/playerBar/kaishi.svg) no-repeat;
        background-size: 100% 100%;
    }
    .snap{
        background: url(../../../assets/img/popup/playerBar/24gl-shutter.svg) no-repeat;
        background-size: 100% 100%;
    }
    .fullScreenOn{
        background: url(../../../assets/img/popup/playerBar/quanping.svg) no-repeat;
        background-size: 100% 100%;
    }
    .fullScreenOff{
        background: url(../../../assets/img/popup/playerBar/quxiaoquanping_o.svg) no-repeat;
        background-size: 100% 100%;
    }
    .time{
        flex: 0 ;
        font-family: 'Noto Sans SC';
        font-style: normal;
        font-weight: 400;
        font-size: 14px;
        color: #fff;
    }
    .slider{
        flex: 100 ;
        margin-left: 15px;
    }
    /deep/.el-slider__button-wrapper{
        display: flex;
        align-items: center;
    }
    /deep/.el-slider__bar{
        background-color: #fff;
    }
    /deep/.el-slider__runway{
        background-color:  rgba(43,51,63,.9);
    }
    /deep/.el-slider__button{
        position: relative;
        left: 10px;
        border: none;
        height: 12px;
        width: 12px;
    }
}

</style>