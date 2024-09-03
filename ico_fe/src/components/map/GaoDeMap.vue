<template>
  <div>
    <MapLoading v-if="showMask" ></MapLoading>
    <div id="gdContainer"></div>
  </div>
</template>
<script>
import MapLoading from "../common/MapLoading.vue";
export default {
  components: { MapLoading },
  data() {
    return {
      option: {  //设置地图容器id
        viewMode: "3D",    //是否为3D地图模式
        zoom: 15,           //初始化地图级别
        mapStyle: 'amap://styles/darkblue',
        center: [115.933873, 39.069010],
      },
      showMask: true
    }
  },
  created() {
    this.initMap();
  },
  beforeUnmount() {
    window.map && window.map.destroy();
    delete window.Loca;
    this.$bus.off('complete');
  },
  methods: {
    initMap() {
      AMapLoader.reset();
      AMapLoader.load({
        key: "d7b2e1dddf3c582586a723b81c5c048d",             // 申请好的Web端开发者Key，首次调用 load 时必填
        version: "2.0",      // 指定要加载的 JSAPI 的版本，缺省时默认为 1.4.15
        plugins: ["AMap.Adaptor", "AMap.OldClusterMarker", "AMap.ElasticMarker", "AMap.MouseTool"],//他妈的OldClusterMarker官网找都找不见
        Loca: {                // 是否加载 Loca， 缺省不加载
          version: '2.0.0'  // Loca 版本，缺省 1.3.2
        },
      }).then((AMap) => {
        window.map = new AMap.Map('gdContainer', {
          zoom: 15,
          resizeEnable: true,
          mapStyle: 'amap://styles/darkblue', //设置地图的显示样式
          center: [115.933873, 39.069010],
          viewMode: '3D',  //设置地图模式
          lang: 'zh_cn',  //设置地图语言类型
        });

        window.loca = new Loca.Container({ map });

        map.on('complete', () => {
          // 地图图块加载完成后触发
          setTimeout(() => {
            this.showMask = false
          }, 250)

          this.$bus.emit('complete', true);
        });
        this.$emit('hasMap', true)
      })
    },
  }
}

</script>
<style scoped>
#gdContainer {
  width: 100%;
  height: 100vh;
  position: absolute;
  top: 0;
  left: 0;
  z-index: 1;
}

</style>
