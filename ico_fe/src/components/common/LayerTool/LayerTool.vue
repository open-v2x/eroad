<template>
  <div class="layerTool">
    <Overflow :time="time" @getMessage="getStaticsPopMessageFromOverflowPoint" @dontShow='dontShowPoint'
      @getAxisShowList="getAxisShowList" ref="Overflow" />
    <HeatLine :time="time" @getMessage="getStaticsPopMessageFromHeatLine" @getAxisShowList="getAxisShowList"
      ref="heatLine" />
    <HeatPoint :time="time" @getMessage="getStaticsPopMessageFromHeatPoint" @dontShow='dontShowPoint'
      @getAxisShowList="getAxisShowList" ref="heatPoint" />
  </div>
  <TimeAxis ref="timer" v-show="axisShow" />


  <div class="hotLegen" v-show="axisShow"></div>

  <StatisticsPop :popMode="showMode" ref="statisticsPop" @close="dontShowPoint" :style="{
    left: sLeft + 'px',
    top: sTop + 'px',
  }" v-show='sShow' />
</template>
<script>
let timer = null
import HeatLine from './HeatLine.vue'
import HeatPoint from './HeatPoint.vue'
import TimeAxis from "../TimeAxis.vue"
import StatisticsPop from "../PopupWindow/StatisticsPop.vue"
import Overflow from "./Overflow.vue"
export default {
  components: { TimeAxis, StatisticsPop, HeatLine, HeatPoint, Overflow },
  data() {
    return {
      axisShow: false,
      axisShowList: [false, false, false, false],
      time: null,
      showMode: '',
      sShow: false,
      sLeft: 0,
      sTop: 0,
      sRight: 0,
    }
  },
  mounted() {
    this.$bus.on('timelinechanged', res => {//timeAxis五分钟更新一次，或鼠标点击，都会触发timelinechanged事件。
      this.$refs.camera.unlock = res.unlock ? true : false
      this.time = res.time;
      console.log('res.time', res.time)
    })
    this.time = this.getTime();
    // console.log(this.time)
  },
  unmounted() {
    this.$bus.off('timelinechanged')
    clearTimeout(timer)
    timer = null
  },
  methods: {
    init(i) {// 全部取消
      this.$refs.heatLine.active = false
      this.$refs.heatPoint.active = false
      if (i != null) {
        this.$refs.heatPoint.breathPointMap();// 显示呼吸球
      }
    },
    getTime() {
      var today = new Date();
      var m = Math.floor(today.getMinutes() / 10) * 10 - 10;
      var h = today.getHours();
      if (m < 0) {
        h -= 1;
        m = 50;
      }//todo h<0
      m = m < 10 ? '0' + m : m;
      h = h < 10 ? '0' + h : h;
      return today.toJSON().split('T').join(' ').split(' ')[0] + ' ' + h + ':' + m + ':' + '00'
    },
    getStaticsPopMessageFromHeatPoint(msg) {// heatPoint控制打开悬浮窗口并设置位置及内容
      this.showMode = "crossRoad";
      this.sShow = true;
      this.sLeft = msg.x+10; // +10防止鼠标移入弹框触发mouseout
      this.sTop = msg.y+10;
      this.$refs.statisticsPop.attribute = msg.attribute;
    },
    getStaticsPopMessageFromOverflowPoint(msg) {// heatPoint控制打开悬浮窗口并设置位置及内容
      this.showMode = "overflow";
      this.sShow = true;
      this.sLeft = msg.x+10;
      this.sTop = msg.y+10;
      this.$refs.statisticsPop.attribute = msg.attribute;
    },
    dontShowPoint() {//heatPoint控制关闭悬浮窗口
      this.sShow = false
    },
    getStaticsPopMessageFromHeatLine(msg) {// heatLine控制打开悬浮窗口设置位置内容及定时关闭
      if(timer){
        clearTimeout(timer)
        timer = null
      }
      this.showMode = "road";
      this.sShow = true;
      this.sLeft = msg.x;
      this.sTop = msg.y;
      this.$refs.statisticsPop.attribute = msg.attribute;
      timer = setTimeout(() => {
        this.sShow = false;
      }, 3000);
    },
    getAxisShowList(msg) {// 控制图例及时间轴显示，4个icon有一个出现即显示图例及时间轴
      this.axisShowList[msg.index] = msg.value
      // console.log(this.axisShowList)
      this.axisShow = this.axisShowList.includes(true) ? true : false
    }
    // inToIntersection(attribute){
    //     let n = attribute['路口名称'].split('-');
    //     let channelName = n[0] + '与' + n[1];
    //     queryByChannelName({'channelName':channelName}).then(res => {
    //         console.log('channelName',res)
    //         var list = res.list;
    //         //this.$bus.emit('isShow', {'videoList':list,'attribute':{'lng':attribute.lng,'lat':attribute.lat,'crossName': channelName+'交叉口'}});
    //         localStorage.setItem('intersection_data', JSON.stringify({'videoList':list,'attribute':{'lng':attribute.lng,'lat':attribute.lat,'crossName': channelName+'交叉口'}}))
    //         this.$router.push('/holographicIntersection');
    //     })
    // },
  }
}
</script>
<style scoped lang="less">
.layerTool {
  position: absolute;
  left: 1440px;
  bottom: 50px;
  width: 48px;
  //height: 192px;
  display: flex;
  flex-direction: column;
  z-index: 998;

  .layer {
    //flex: 1;
    margin-top: 24px;
    width: 48px;
    height: 48px;
    background: radial-gradient(50% 50% at 50% 50%, rgba(25, 159, 255, 0) 0%, rgba(25, 159, 255, 0.45) 100%);
    backdrop-filter: blur(4px);
    border-radius: 24px;
    cursor: pointer;
  }

  .active {
    background: radial-gradient(50% 50% at 50% 50%, rgba(1, 255, 255, 0) 0%, rgba(1, 255, 255, 0.45) 100%) !important;
  }

  .layer_icon_1 {
    width: 28px;
    height: 28px;
    margin: auto;
    margin-top: 10px;
    background: url('../../../assets/img/layer_icon_1.png') no-repeat;
    background-size: 100% 100%;
  }

  .layer_icon_2 {
    width: 28px;
    height: 28px;
    margin: auto;
    margin-top: 10px;
    background: url('../../../assets/img/layer_icon_2.png') no-repeat;
    background-size: 100% 100%;

  }

  .layer_icon_3 {
    width: 28px;
    height: 28px;
    margin: auto;
    margin-top: 10px;
    background: url('../../../assets/img/layer_icon_3.png') no-repeat;
    background-size: 100% 100%;
  }

}


.hotLegen {
  position: absolute;
  left: 420px;
  bottom: 60px;
  width: 168px;
  height: 155px;
  background: url(../../../assets/img/Group2706.png);
  background-repeat: no-repeat;
  background-size: 100% 100%;
  z-index: 998;
}

/deep/.toolTipPanel {
  position: absolute;
  z-index: 999;
  margin-left: 20px;
  width: 80px;
  height: 30px;
  // background-color: pink;
  // background: url('../../../assets/img/Frame 1000002311.png') no-repeat;
  background: rgba(0, 0, 51, 0.6);
  background-size: 100% 100%;
  color: #FFFFFF;
  height: 25px;
  font-family: 'Noto Sans SC';
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  line-height: 22px;
  text-align: center;
}</style>
