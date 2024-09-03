<template>
  <div class="layer" ref="road" :class="{ activeLayer: active }" @click="showHandel">
    <div class="layer_icon" :class="{ active }"></div>
    <div class="toolTipPanel" v-show="showTooltip">溢出</div>
  </div>
</template>
<script>

import { crossroad_detail_list } from '../../../assets/js/api'
let CoordTransform = require('coordtransform');
let zoneList = [];
let msg
export default {
  props: {
    time: String
  },
  watch: {
    time(now, old) {
      if (now) {
        this.firstQuery(now)
      }
    }
  },
  data() {
    return {
      active: false,
      geoJsonTemp: {},
      showTooltip: false,
    }
  },
  mounted() {
    this.tooltip()
  },
  methods: {
    showHandel() {
      this.breathPointMap();
    },
    firstQuery() {
      this.firstQuery = function (time) {
        if(this.active)this.query(time)
      }
    },
    query(time) {
      //接口
      time = time ? time : null
      crossroad_detail_list({
        "data_time": time
      }).then(res => {
        let data = res.data;
        if (!data) return;
        //加点击热区
        let filteredData = data.filter(item=>item.overflow_rate>=1)
        this.addHotZone(filteredData);
      })
    },
    breathPointMap() {
      if (this.active) {//取消方法
        this.active = false;
        this.$emit('getAxisShowList', { index: 3, value: false })
        map.remove(zoneList);  //取消热区圆点
        zoneList = []
      } else {
        this.active = true;
        this.$emit('getAxisShowList', { index: 3, value: true })
        this.query(this.time);
      }
    },
    addHotZone(data) {
      let styles = [{
        icon: {
            img: require('../../../assets/img/icon/overflow.png'),
            size: [27, 39],//可见区域的大小
            anchor: 'bottom-center',//锚点
            fitZoom: 15,//最合适的级别
            scaleFactor: 2,//地图放大一级的缩放比例系数
            maxScale: 3,//最大放大比例
            minScale: .5//最小放大比例
        }
      }];
      let zoomStyleMapping = {
        13: 0,
        14: 0,  // 14级使用样式 0
        15: 0,
        16: 0,
        17: 0,
        18: 0,
        19: 0,
        20: 0,
      };
      if (window.map){
        window.map.remove(zoneList);
        zoneList = []
      }
      data.forEach(item => {
        let zone = new AMap.ElasticMarker({
          position: CoordTransform.wgs84togcj02(Number(item.crossroad_lon), Number(item.crossroad_lat)),
          radius: 50,
          zIndex: 117,
          cursor: 'pointer',
          styles,
          zoomStyleMapping,
          extData: {
            '溢出指数':item['overflow_rate'],
            '路口名称': item['crossroad_name'],
            '实时车流量': item['car_cnt'],
            '拥堵指数': item['tci'],
            '高峰时段': item['peak_h'],
            'lng': item['crossroad_lon'],
            'lat': item['crossroad_lat']
          }
        })
        zone.on('mousemove', (e) => {
          msg = {}
          msg.x = e.pixel.x;
          msg.y = e.pixel.y;
          msg.attribute = e.target._opts.extData;
          this.$emit('getMessage', msg)
        });
        zone.on('mouseout', (e) => {
          this.$emit('dontShow', null)
        });

        zoneList.push(zone);
      })
      window.map.add(zoneList);
    },
    tooltip() {
      this.$refs.road.addEventListener('mouseenter', () => {
        this.showTooltip = true
      })
      this.$refs.road.addEventListener('mouseleave', () => {
        this.showTooltip = false
      })
    }
  }
}
</script>
<style scoped lang="less">
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

.activeLayer {
  background: radial-gradient(50% 50% at 50% 50%, rgba(255, 210, 51, 0) 0%, rgba(255, 246, 40, 0.3) 95.31%) !important;
}

.layer_icon {
  width: 48px;
  height: 48px;
  margin: auto;
  background: url('../../../assets/img/icon/overflowDefault.png') no-repeat;
  background-size: 100% 100%;
}

.active {
  background: url('../../../assets/img/icon/overflowIconActive.png') no-repeat;
  background-size: 100% 100%;
}
</style>
