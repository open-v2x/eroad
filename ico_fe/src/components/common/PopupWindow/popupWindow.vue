<template>
  <div
    class="floatpopup"
    :style="{
      top: top - height - 10 - 200 + 'px',
      left: left - width * 0.5 + 'px',
    }"
    v-if="isclose"
  >
  <!-- <div> -->
    <div
      title="关闭信息框"
      class="close"
      @click="isclose = false"
    ></div>
        <component ref="pop" :is="viewCom" style="margin:10px" :videoUrl="propertity.url"></component>
    <span class="triangle"></span>
  </div>
</template>

<script>
import LivePlayer from "./oldLivePlayer"
import CreateGUID from "./CreateGUID";
import * as echartSetSize from '../../../assets/js/echartsSetSize'
export default {
  data() {
    return {
      top: 0,
      left: 0,
      width: 0,
      height: 0,
      isclose: true,
      position: new Cesium.Cartesian3(),
      Guid: CreateGUID(),
      titlex: "标题",
      innerValue: "",
      viewCom:'',
      urls:[],
      propertity:{'displayTime':'17-09-13/9:52','name':'55555555','phone':'1234567910','content':'222222222,1111111111'}
    };
  },
  components:{
    LivePlayer
  },
  props: {},
  mounted() {
    //this._viewer = window.viewer;
    viewer.scene.preRender.addEventListener(
      this.watchPreEvent
    );
  },
  updated(){
    this.$nextTick( ()=> {
      // 仅在整个视图都被重新渲染完毕之后才会运行的代码
      this.width = this.$refs.pop.width;
      this.height = this.$refs.pop.height;
    })
  },
  computed: {},
  methods: {
    watchPreEvent() {
      // console.log('传递的值', this.propertity)
      if (this.position && Cesium.defined(this.position)) {
        //var position = Cesium.SceneTransforms.wgs84ToWindowCoordinates(_this.viewer.scene,geometry);
        var canvasPosition = viewer.scene.cartesianToCanvasCoordinates(
          this.position,
          new Cesium.Cartesian3()
        );
        if (Cesium.defined(canvasPosition)) {
          this.top = canvasPosition.y;
          this.left = canvasPosition.x;
        }

      }
    },
  },
  unmounted() {
    viewer.scene.preRender.removeEventListener(this.watchPreEvent);
  },
};
</script>

<style>
.floatpopup {
  position: absolute;
  background: url('../../../assets/img/Frame 427322059.png') no-repeat;
  background-size:100% 100%;
  color: white;
  text-align: center;
  z-index: 999;
  padding-top: 10px;
}

.triangle {
  display: block;
  height: 200px;
  width: 2px;
  background: linear-gradient(rgb(119, 188, 194),transparent);
  position: absolute;
  bottom: -200px;
  left: 50%;
  margin-left: -1px;
  border-radius: 0 0 0 2px;
  z-index: 999;
/* margin-left: -1px;
  position: relative;
  display: inline-block;
  height: 200px;
  width: 3px;
  color: rgb(255, 255, 255);
  line-height: 80px;
  background: linear-gradient(transparent,rgb(78, 207, 220));
  background-size: 400%;
  z-index: 1;
  animation: move 5s linear alternate infinite; */
}


/* .triangle::before{
        content: '';
        position: absolute;
        height: 200px;
  width: 50px;
        background: linear-gradient(rgb(39, 122, 218),transparent);
        background-size: 400%;
        opacity: 0;
        z-index: -1;
        border-radius: 45px;
        transition: 0.6s;

       filter: blur(15px);
        opacity: 1;
        animation: move 8s linear alternate infinite;
    } */


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
