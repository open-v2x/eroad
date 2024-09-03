<template>
  <div class="mouseTool" :class="{active:editing}" @click="startRectangle">设备圈选</div>
</template>
<script setup>
import { onMounted, onUnmounted, ref, getCurrentInstance } from 'vue';
let { proxy } = getCurrentInstance()
let editing = ref(false)
let overlays = []
let mouseTool
let cameras
let rectangleLngLat
let camerasInRectangle
let emit = defineEmits(['changeVisible'])
onMounted(() => {

})
onUnmounted(() => {

})
let CoordTransform = require('coordtransform');

const startRectangle = () => {
  if (!mouseTool) mouseTool = new AMap.MouseTool(map);
  if (!cameras) {
    cameras = JSON.parse(window.localStorage.getItem('cameras'))
    // cameras.forEach(item=>{
    //   console.log(item)
    // })
  }
  editing.value = !editing.value
  if (editing.value) {
    mouseTool.on('draw', function (e) {
      window.map.remove(overlays)
      overlays = []
      setTimeout(() => {
        overlays.push(e.obj);
        console.log(e.obj)
        rectangleLngLat = []
        camerasInRectangle = []
        rectangleLngLat.push(CoordTransform.gcj02towgs84(e.obj.ir.northEast.lng, e.obj.ir.northEast.lat))
        rectangleLngLat.push(CoordTransform.gcj02towgs84(e.obj.ir.southWest.lng, e.obj.ir.southWest.lat))
        cameras.forEach(item => {
          if (item['经度'] < rectangleLngLat[0][0] && item['纬度'] < rectangleLngLat[0][1] && item['经度'] > rectangleLngLat[1][0] && item['纬度'] > rectangleLngLat[1][1]) {
            console.log(item)
            camerasInRectangle.push(item)
          }
        })
        proxy.$bus.emit('camerasInRectangle',camerasInRectangle)
        emit('changeVisible',true)
        editing.value = false
        mouseTool.close(true)
      })

    })
    mouseTool.rectangle({
      fillColor: '#00b0ff',
      strokeColor: '#80d8ff'
      //同Polygon的Option设置
    });
  } else {
    mouseTool.close(true)
    window.map.remove(overlays)
    overlays = []
  }


}
</script>
<style lang='less' scoped>
.mouseTool {
  width: 90px;
  height: 30px;
  line-height: 30px;
  position: absolute;
  left: 450px;
  top: 200px;
  background: url('../../../assets/img/button-cell-normal.png');
  background-size: 100% 100%;
  text-align: center;
  color: #909399;
  z-index: 9;
  &:hover {
    cursor: pointer;
    color: aqua;
  }
}
.active {
  background: url('../../../assets/img/button-cell-select.png');
  background-size: 100% 100%;
  color: aqua;
}
</style>
