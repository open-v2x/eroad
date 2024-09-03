<template>
  <GaoDeMap></GaoDeMap>
  <div class="holoIndex">
    <top-bar :active="1"></top-bar>
    <component :is='viewModel'></component>
    <!-- <mouseTool @changeVisible="changeVisible"></mouseTool> -->
    <div v-show="marker" class="delete" @click="deleteMarkerPolyline">关闭</div>
    <CameraList v-show="listVisible" @changeVisible="changeVisible"></CameraList>
    <LayerTool ref="layerTool" />
    <popupWindow2D ref="popupWindow2D" />
  </div>
</template>

<script>
import { initEarth, destoryEarth } from "../../../assets/js/3d/initEarth"
import { heatLine1 } from '../../../assets/js/3d/common/drawingHelper'
import { road_section_jam, crossroad_detail_list } from '../../../assets/js/api'
import GaoDeMap from "../../map/GaoDeMap.vue"
import TopBar from '../../common/TopBar.vue'
import LayerTool from '../../common/LayerTool/LayerTool.vue'
import mouseTool from '../../common/LayerTool/mouseTool.vue'
import CameraList from './compontent/CameraList.vue'
import Tracffic from './Tracffic.vue'
import dayjs from 'dayjs'
import DoubleScreen from "../../common/PopupWindow/DoubleScreen.vue"
import popupWindow2D from '../../common/PopupWindow/popupWindow2D.vue';
var redPolylines, yellowPolylines, greenPolylines;
let RD_position = [115.933873, 39.069010]
let CoordTransform = require('coordtransform')

export default {
  components: {
    GaoDeMap, TopBar, LayerTool, Tracffic, DoubleScreen, popupWindow2D, mouseTool, CameraList
  },
  data() {
    return {
      viewModel: 'Tracffic',
      listVisible: false,
      roads: null,
      intersections: null,
      //model:0,
      marker: null,
    }
  },
  created() {
    //图表大小变动从新渲染，动态自适应
    window.addEventListener("resize", () => {
      this.$nextTick(() => {
      })
    });
    if (!window.localStorage.getItem('sectionPositionData') || !window.localStorage.getItem('intersectionPositionData')) {
      this.setLocalStorage()
    } else {
      this.roads = JSON.parse(window.localStorage.getItem('sectionPositionData'))
      this.intersections = JSON.parse(window.localStorage.getItem('intersectionPositionData'))
    }
    window.continerModel = 1
    this.$bus.on('complete', d => {
      if (window.viewer)
        destoryEarth();
      //that.initMap();
      this.$refs.layerTool.init(1);
      //new Traffic();
    })
    this.$bus.on('turnContiner', d => {
      let that = this;
      if (d == 1) {//2d
        if (window.viewer)
          destoryEarth();
      } else if (d == 2) {
        window.map.destroy();
        initEarth('container')
        setTimeout(() => {
          if (viewer) {
            viewer.flyTo(tileset);
            that.roadHeat();
            setTimeout(() => {
              that.$refs.layerTool.init();
            }, 1000)
          }
        }, 4000);
      }
    });
    this.busOn()
  },
  methods: {
    busOn() {
      let styles = [{
        icon: {
          img: require('../../../assets/img/icon/intersectionIcon.png'),
          size: [50, 58],//可见区域的大小
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

      this.$bus.on('protrudeIntersection', (data) => {
        let position
        if (this.marker) map.remove(this.marker)
        if (this.intersections) {
          for (let index = 0; index < this.intersections.length; index++) {
            if (data === this.intersections[index].name) {
              position = this.intersections[index].lonlat
              break
            }
          }
          if (position) {
            this.marker = new AMap.ElasticMarker({
              position,
              radius: 50,
              zIndex: 120,
              cursor: 'pointer',
              styles,
              zoomStyleMapping,
            })
            this.marker.setMap(map);
            map.setZoomAndCenter(15, position)
          }
        }
      })

      this.$bus.on('protrudeRoad', (data) => {
        let path
        if (this.marker) map.remove(this.marker)
        if (this.roads) {
          for (let index = 0; index < this.roads.length; index++) {
            if (data.road === this.roads[index].road_name && data.street === this.roads[index].crossroad_s_e) {
              path = this.roads[index].road_lon_lat
              break
            }
          }
          this.marker = new AMap.Polyline({
            path,
            isOutline: false,
            // outlineColor: '#ffeeff',
            borderWeight: 3,
            strokeColor: "#008000",
            strokeOpacity: 1,
            strokeWeight: 10,
            // 折线样式还支持 'dashed'
            strokeStyle: "solid",
            // strokeStyle是dashed时有效
            // strokeDasharray: [10, 5],
            lineJoin: 'round',
            lineCap: 'round',
            zIndex: 120,
            showDir: true
          })
          map.add(this.marker)
          map.setZoomAndCenter(16, path[0])
        }
      })

      this.$bus.on('changeCameraListShow', (data) => {
        this.changeVisible(data)
      })
    },
    roadHeat() {
      setTimeout(() => {
        redPolylines = viewer.scene.primitives.add(new Cesium.PolylineCollection());
        yellowPolylines = viewer.scene.primitives.add(new Cesium.PolylineCollection());
        greenPolylines = viewer.scene.primitives.add(new Cesium.PolylineCollection());
        heatLine1('Export_Output_65.json', redPolylines, yellowPolylines, greenPolylines);
      }, 5000)
    },
    deleteMarkerPolyline() {
      if (this.marker) map.remove(this.marker)
      this.marker = null
      map.setZoomAndCenter(15, RD_position)
    },
    changeVisible(data) {
      this.listVisible = data
    },
    setLocalStorage() {
      road_section_jam({
        data_time: dayjs().format('YYYY-MM-DD HH:mm:ss')
      }).then(res => {
        let sectionPositionData = []
        res.data.forEach(line => {
          let road_lon_lat = JSON.parse(line.road_lon_lat);
          let lonlat = [CoordTransform.wgs84togcj02(road_lon_lat[0][0], road_lon_lat[0][1]), CoordTransform.wgs84togcj02(road_lon_lat[1][0], road_lon_lat[1][1])]
          sectionPositionData.push({ road_name: line.road_name, crossroad_s_e: line.crossroad_s_e, road_lon_lat: lonlat })
        })
        window.localStorage.setItem('sectionPositionData', JSON.stringify(sectionPositionData))
        this.roads = sectionPositionData
      })
      crossroad_detail_list({
        data_time: dayjs().format('YYYY-MM-DD HH:mm:ss')
      }).then(res => {
        let intersectionPositionData = []
        res.data.forEach(item => {
          intersectionPositionData.push({ name: item.crossroad_name, lonlat: CoordTransform.wgs84togcj02(Number(item.crossroad_lon), Number(item.crossroad_lat)) })
        })
        window.localStorage.setItem('intersectionPositionData', JSON.stringify(intersectionPositionData))
        this.intersections = intersectionPositionData
      })
    },
  },
  beforeUnmount() {
    this.$bus.off('turnContiner');
    this.$bus.off('protrudeIntersection')
    this.$bus.off('protrudeRoad')
    this.$bus.off('changeCameraListShow')
    loca && loca.destroy()
    map && map.destroy();
    window.loca = null;
    window.map = null;
    if (window.viewer)
      destoryEarth();
  },
}
</script>
<style lang="less" scoped>
.holoIndex {
  position: relative;

  #container {
    width: 100%;
    height: 100vh;
    position: absolute;
    top: 0;
    left: 0;
    z-index: 998;
  }

}

.delete {
  width: 90px;
  height: 30px;
  line-height: 30px;
  position: absolute;
  right: 450px;
  top: 200px;
  background: url('../../../assets/img/button-cell-select.png');
  background-size: 100% 100%;
  text-align: center;
  color: aqua;
  z-index: 9999;

  &:hover {
    cursor: pointer;
  }
}
</style>
