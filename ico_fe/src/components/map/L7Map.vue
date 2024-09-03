<template>
  <div>
    <MapLoading v-if="showMask"></MapLoading>
    <div id="l7Container">
    </div>
  </div>
</template>
<script>
import { Scene } from "@antv/l7";
import { GaodeMap, GaodeMapV2 } from "@antv/l7-maps";
import MapLoading from "../common/MapLoading.vue";

export default {
  components: { MapLoading },
  data() {
    return {
      showMask: true
    }
  },
  props: {
    option: {
      type: Object,
      default: () => {
        return {  //设置地图容器id
          viewMode: "3D",    //是否为3D地图模式
          zoom: 15,           //初始化地图级别
          mapStyle: 'amap://styles/darkblue',
          center: [115.933873, 39.069010],
          pitch: 0
        }
      }
    }
  },
  mounted() {
  },
  beforeUnmount() {
    window.scene && window.scene.destroy();
  },
  methods: {
    initMap(callback) {

      AMapLoader.reset();
      AMapLoader.load({
        key: "15cd8a57710d40c9b7c0e3cc120f1200",             // 申请好的Web端开发者Key，首次调用 load 时必填
        //version:"2.0",      // 指定要加载的 JSAPI 的版本，缺省时默认为 1.4.15
        //plugins:["AMap.Adaptor","AMap.OldClusterMarker"],//他妈的OldClusterMarker官网找都找不见
      }).then((AMap) => {

        //let map = new GaodeMap(this.option);
        let map = new AMap.Map("l7Container", {
          zoom: this.option.zoom,
          resizeEnable: true,
          mapStyle: "amap://styles/darkblue", //设置地图的显示样式
          center: this.option.center,
          pitch: this.option.pitch,
          viewMode: "3D", //设置地图模式
          lang: "zh_cn", //设置地图语言类型
        });
        window.scene = new Scene({
          id: 'l7Container',
          map: new GaodeMap({
            mapInstance: map,
          }),
        });

        scene.on("loaded", () => {
          //this.$bus.emit("loaded",true)
          setTimeout(() => {
            this.showMask = false
          }, 250)
          return callback(scene);
        })
      })
    }
  }
}

</script>
<style lang="less" scoped>
#l7Container {
  width: 100%;
  height: 100vh;
  position: absolute;
  top: 0;
  left: 0;
  z-index: 1;
}

</style>
