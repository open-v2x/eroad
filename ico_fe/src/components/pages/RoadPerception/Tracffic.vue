<template>
  <div class="changePanel">
    <div class="tracfficLeftBar">
      <LeftTop />
      <AllRegionsFlowDetection></AllRegionsFlowDetection>
      <LeftCenter />
    </div>
    <div class="tracfficCenterBar">
      <CenterLeft />
      <CenterMid />
      <CenterRight />
    </div>
    <div class="tracfficRightBar">
      <RightTop />
      <RightBottom />
      <InterSectionOverflow></InterSectionOverflow>
    </div>
    <PopWindow1 ref="pop1" />
    <PopWindow2 ref="pop2" />
  </div>
</template>
<script>
import { rad_tra_flow_10min, ads_sc_rad_tci_10min, ads_sc_rad_road_accum_tra_flow, ads_sc_rad_road_tci_10min, road_section_avgspeed_10min, total_avgspeed, getTotalSpeed, overflow_rate, real_flow, predict_flow } from '../../../assets/js/api'

import LeftTop from './compontent/LeftTop.vue'
import LeftCenter from './compontent/LeftCenter.vue'
import LeftBottom from './compontent/LeftBottom.vue'
import RightTop from './compontent/RightTop.vue'
import RightCenter from './compontent/RightCenter.vue'
import RightBottom from './compontent/RightBottom.vue'
import CenterLeft from './compontent/CenterLeft.vue'
import CenterRight from './compontent/CenterRight.vue'
import CenterMid from './compontent/CenterMid.vue'
import PopWindow1 from './compontent/PopWindow1.vue'
import PopWindow2 from './compontent/PopWindow2.vue'
import AllRegionsFlowDetection from './compontent/AllRegionsFlowDetection.vue'
import InterSectionOverflow from './compontent/InterSectionOverflow.vue'

let timer
export default {
  components: { LeftTop, LeftCenter, LeftBottom, RightTop, RightCenter, RightBottom, CenterLeft, CenterMid, CenterRight, PopWindow1, PopWindow2, AllRegionsFlowDetection, InterSectionOverflow },
  data() {
    return {
      active: 1,
      see: false
    }
  },
  mounted() {
    this.query();
    timer = setInterval(() => { this.query() }, 300000)
  },
  beforeUnmount() {
    clearInterval(timer)
    timer = null
  },
  methods: {
    query() {
      var today = new Date();
      var now = today.toJSON().split('T').join(' ').substr(0, 19);
      var date = now.split(' ')[0];
      ads_sc_rad_road_accum_tra_flow({ data_time: this.getTime(), rk: 5 }).then(res => {
        console.log('leftCenter', res);
        if (res.success) {
          this.$bus.emit('ads_sc_rad_road_accum_tra_flow', res)
        } else {

        }
      }),
        rad_tra_flow_10min({
          "data_time": date,
          "crossroad_name": "total"
        }).then(res => {
          console.log('leftBottom', res);
          if (res.success) {
            this.$bus.emit('rad_tra_flow_10min', res)
          } else {

          }
        })
      ads_sc_rad_tci_10min({ data_time: this.getTime() }).then(res => {
        console.log('rightTop', res);
        if (res.success) {
          this.$bus.emit('ads_sc_rad_tci_10min', res)
        } else {

        }
      })
      ads_sc_rad_road_tci_10min({ data_time: null, crossroad_rank: 5 }).then(res => {
        console.log('rightCenter', res);
        if (res.success) {
          this.$bus.emit('ads_sc_rad_road_tci_10min', res)
        } else {

        }
      })
      road_section_avgspeed_10min({ data_time: this.getTime() }).then(res => {
        console.log('rightBottom', res);
        if (res.success) {
          this.$bus.emit('road_section_avgspeed_10min', res)
        } else {

        }
      })
      total_avgspeed({ data_time: this.getTime() }).then(res => {
        console.log('centerRight', res);
        if (res.success) {
          this.$bus.emit('total_avgspeed', res)
        }
      })


      let real_flow_data = real_flow()
      let predict_flow_data = predict_flow()
      Promise.all([real_flow_data,predict_flow_data]).then(([real,predict])=>{
        this.$bus.emit('volumeProjections', [real.data,predict.data])
      }).catch((error)=>{
        console.log(error)
      })


      overflow_rate().then(res => {
        if (res.success) {
          res.data.sort((a, b) => {
            return b.overflow_rate - a.overflow_rate
          })
          res.data.forEach(item => {
            item.overflow_rate === 1 ? item.overflow_rate = '1.000' : item.overflow_rate.toFixed(3)
          })
          this.$bus.emit('intersectionOverflow', res.data)
        }
      })
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
    }
  }
}
</script>

<style lang="less" scoped>
@height: 10.8vh;

.changePanel {
  position: relative;
  height: 100vh;

  .tracfficLeftBar {
    position: absolute;
    width: 400px;
    top: 100px;
    // left: 24px;
    left: 0;
    bottom: 0;
    z-index: 2;
    //background: rgba(13, 41, 100, 0.3);
    display: flex;
    flex-direction: column;

    >div {
      position: relative;
      overflow: hidden;
      box-sizing: border-box;
      //height: 310px;
      flex: 1;
      overflow: hidden;
      background: url('../../../assets/img/Frame 1000002315.png');
      background-repeat: no-repeat;
      background-size: 100% 100%;
      backdrop-filter: blur(3px);
      margin-bottom: 15px;
      display: flex;
      flex-direction: column;

      /deep/.title {
        height: (44/@height);
        width: 400px;
        background: url('../../../assets/img/Frame 427321336.png') no-repeat;
        background-size: 100% 100%;
        color: #fff;
        font-size: 16px;
        .caption {
          margin: 0 0 0 28px;
          height: (44/@height);
          font-size: 20px;
          font-family: 'Noto Sans SC';
          font-style: normal;
          font-weight: 500;
          font-size: 20px;
          line-height: (44/@height);
          /* identical to box height, or 140% */
          color: #FFFFFF;
          text-shadow: 0px 1px 1px rgba(0, 0, 51, 0.15);
        }
      }
    }
  }

  .tracfficCenterBar {
    position: absolute;
    width: 1024px;
    height: 75px;
    top: 100px;
    left: 50%;
    transform: translate(-50%);
    bottom: 0;
    z-index: 2;
    display: flex;
    flex-direction: row;
    justify-content: space-between;

    >div {
      box-sizing: border-box;
      padding: 12px 16px;
      background: url('../../../assets/img/Frame 1000002403.png');
      background-repeat: no-repeat;
      background-size: 100% 100%;
      height: 75px;
      width: 225px;
      backdrop-filter: blur(3px);
      display: flex;
      justify-content: center;
      align-items: center;
    }

  }

  .tracfficRightBar {
    position: absolute;
    width: 400px;
    top: 100px;
    // right: 24px;
    right: 0;
    bottom: 0;
    z-index: 2;
    //background: rgba(13, 41, 100, 0.3);
    display: flex;
    flex-direction: column;

    >div {
      box-sizing: border-box;
      //height: 310px;
      flex: 1;
      overflow: hidden;
      background: url('../../../assets/img/Frame 1000002315.png');
      background-repeat: no-repeat;
      background-size: 100% 100%;
      backdrop-filter: blur(3px);
      margin-bottom: 15px;
      display: flex;
      flex-direction: column;

      /deep/.title {
        height: (44/@height);
        width: 400px;
        background: url('../../../assets/img/Frame 427321336.png') no-repeat;
        background-size: 100% 100%;
        color: #fff;
        font-size: 16px;
        .caption {
          margin: 0 0 0 28px;
          height: (44/@height);
          font-size: 20px;
          font-family: 'Noto Sans SC';
          font-style: normal;
          font-weight: 500;
          font-size: 20px;
          line-height: (44/@height);
          /* identical to box height, or 140% */
          color: #FFFFFF;
          text-shadow: 0px 1px 1px rgba(0, 0, 51, 0.15);
          /* Inside auto layout */
          flex: none;
          order: 1;
          flex-grow: 0;
        }
      }
    }
  }

}</style>
