<template>
  <div
    class="floatpopup"
    :style="{
      top: top,
      left: left,
      //width:width+'px',
      //height:height+'px'
    }"
    v-if="isclose"
  >
    <div class="close" @click="close"></div>
      <component ref="pop" :is="viewCom" style="margin:10px" :videoUrl="urls" :camera="camera" :startTime="startTime" :record="record" :playSsrc="playSsrc" :timeList="timeList" :mediaServerId="mediaServerId" :switchInPlace="switchInPlace"></component>
  </div>
</template>

<script>
import LivePlayer from "./LivePlayer"
import CreateGUID from "./CreateGUID";
import * as echartSetSize from '../../../assets/js/echartsSetSize'

var center = null;
export default {
  data() {
    return {
      top: 0,
      left: 0,
      width: 496,
      height: 336,
      isclose: false,
      location:null,
      Guid: CreateGUID(),
      titlex: "标题",
      innerValue: "",
      viewCom:'',
      propertity:{'displayTime':'17-09-13/9:52','name':'55555555','phone':'1234567910','content':'222222222,1111111111'},
      urls: null,//切换频道相关
      record: false,
      mediaServerId: null,
      playSsrc: null,
      timeList:null,
      camera: null,
      switchInPlace: null,//原频道切换
      startTime: null
    };
  },
  components:{
    LivePlayer
  },
  props: {},
  mounted() {
    this.checkout()//切换直播
    this.checkoutRecord()//时间线变化导致的录像变化
    this.$bus.on('pop2D', d => {//仅传参用
      // console.log('pop2D',d)
      this.isclose = false
      this.$nextTick(()=>{
        this.propertity = d['propertity'];
        this.viewCom = d['propertity']['viewCom'];//使用Liveplayer
        center = [d['propertity']['lng'],d['propertity']['lat']];//获取坐标
          // this.mouseEnterLeave()
        this.urls = d['propertity']['url']//目前直播，url是一个或俩通道的链接，历史回放是链接/摄像头ID/通道1ID/通道2ID,预计去掉，单独装到一个对象中。
        this.camera = d['propertity']['camera']//获取摄像头ID和频道号
        this.record = d['propertity']['record']//这段刚才放在下面导致切换回直播仍然无法改变record变量
        if(d['propertity']['record']){
          this.mediaServerId = d['propertity']['mediaServerId']//获取录像跳转所需相关属性
          this.playSsrc = d['propertity']['playSsrc']
          this.timeList = d['propertity']['timeList']
          this.startTime = d['propertity']['startTime']
          // console.log('录像starttime,this和传入',this.startTime,d['propertity']['startTime'])
        }
        this.switchInPlace = false
        map.on('mapmove', this.watchPreEvent);
        this.isclose = true;
        this.sendPopupWindowVisibility()
      })
    })
  },
  updated(){
    this.$nextTick( ()=> {
      // 仅在整个视图都被重新渲染完毕之后才会运行的代码
      if(this.isclose&&this.$refs.pop){
      this.width = this.$refs.pop.width+20;
      this.height = this.$refs.pop.height+60;
      }
    })
  },
  unmounted(){
    this.$bus.off('checkout')
    this.$bus.off('pop2D')
    this.$bus.off('checkoutRecordExecute')
  },
  methods: {
    watchPreEvent() {
      let location = map.lngLatToContainer(center);
      this.top = location.y - this.height + 'px';
      this.left = location.x - (this.width * 0.5) + 'px';
    },
    close(){
      map.off('mapmove', this.watchPreEvent);
      //this.$bus.off('pop2D');
      this.isclose = false
      this.sendPopupWindowVisibility()
    },
    sendPopupWindowVisibility(){//开启和关闭视频窗口时发出视频窗口开关信号给layertool
      this.$bus.emit('popupWindowVisibility',this.isclose)
    },
    checkout(){//点击切换更改直播执行完后切换相关参数
      this.$bus.on('checkout', d => {
        this.urls = d['propertity']['url']
        this.record = d['propertity']['record']
        this.switchInPlace = d['propertity']['switchInPlace']
      })
    },
    checkoutRecord(){
      this.$bus.on('checkoutRecordExecute', d => {//时间线变化更改录像执行完后切换相关参数
        this.urls = d['url']
        this.record = true
        this.switchInPlace = d['switchInPlace']
        this.mediaServerId = d['mediaServerId']
        this.playSsrc = d['playSsrc']
        this.timeList = d['timeList']
        this.startTime = d['startTime']
        // console.log('时间线切换视频starttime,this和传入',this.startTime,d['startTime'])
      })
    }
  },
};
</script>

<style scoped lang="less">
.floatpopup {
  position: absolute;
  background: url('../../../assets/img/Frame 427322059.png') no-repeat;
  background-size:100% 100%;
  color: white;
  text-align: center;
  z-index: 999;
  padding-top: 10px;
  // justify-content: center;
  // align-items: center;;
}


.close{
  position: absolute;
  z-index: 2;
  right: 0px;
  top: 0px;
  height: 10px;
  width: 30px;
}
.close:hover {
  cursor:pointer;
}
</style>
