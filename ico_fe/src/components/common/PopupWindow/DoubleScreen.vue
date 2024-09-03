<template>
    <div class="bg" v-if="isShow">

        <div class="main" >
            <div class="left-dialog">
                <LivePlayer class="video"
                    fluent live stretch
                    :autoplay="true"
                    :hasaudio="false"
                    :videoUrl="videoUrl"
                    >
                </LivePlayer>
                <div class="btnDiv">
                    <div class="btn" v-for="(video1,index) in videoList" :key="index" @click="play(video1,index)">
                        <span>
                            {{video1.name}}
                        </span>
                    </div>
                </div>
            </div>
            <div class="right-dialog">
                <div id="container1"></div>
                <div class="index_bottom">
                    <div class="index_cell">
                        <div class="cell">
                            <div class="cell_row">
                                <div class="icon1"></div>
                                <span class="cell1">{{indexList.zs}}</span>
                            </div>
                            <span class="cell2">路口拥堵指数</span>
                        </div>
                    </div>
                    <div class="index_cell">
                        <div class="cell">
                            <div class="cell_row">
                                <div class="icon2"></div>
                                <span class="cell1">{{indexList.sum}}</span><span class="dw">辆</span>
                            </div>
                            <span class="cell2">车流总量</span>
                        </div>
                    </div>
                    <div class="index_cell">
                        <div class="cell">
                            <div class="cell_row">
                                <div class="icon3"></div>
                                <span class="cell1">{{indexList.now}}</span><span class="dw">辆/h</span>
                            </div>
                            <span class="cell2">当前车流</span>
                        </div>
                    </div>
                    <div class="index_cell">
                        <div class="cell">
                            <div class="cell_row">
                                <div class="icon4"></div>
                                <span class="cell1">{{indexList.md}}</span><span class="dw">辆/s</span>
                            </div>
                            <span class="cell2">车流密度</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="title-bg"></div>
        <div class="close" @click="back"></div>
        <div class="title-cell">
            <div class="cell1"></div>
            <div class="title">{{crossName}}</div>
            <div class="cell2"></div>
        </div>
    </div>
</template>
<script>
import LivePlayer from '@liveqing/liveplayer-v3';
import Socket from '@/assets/js/socket';
import { nextTick } from 'vue-demi';
import AmapImageryProvider from '../../../assets/js/3d/imagery/provider/AmapImageryProvider';
import { newHightLightPolyline } from '../../../assets/js/3d/common/drawingHelper';
import FullRoad from '../../../assets/js/3d/FullRoad/FullRoad'
import FullRoad_mec from '../../../assets/js/3d/FullRoad/FullRoad_mec';
import { rectify } from '../../../assets/js/3d/initEarth';
import { getTrafficJam } from '../../../assets/js/api';
import { deviceProxyPlay } from '../../../assets/js/api_video';
import svgEntityImage from '../../../assets/img/u114.svg';
let timer;
let viewer1;
let twins;
export default {
  components: {LivePlayer},
  data() {
    return {
      isShow: false,
      crossName: '',
      videoUrl: '',
      videoList: [],
      deviceList: [],
      indexList: {
        zs: 0, sum: 0, now: 0, md: 0,
      },
      attribute: {},
      status: 'status',
      crossroad_name: '',
    };
  },
  setup() {
  },
  mounted() {
    this.$bus.on('isShow', (d) => {
      this.isShow = true;
      // this.$store.commit('clearPop2D')
      // DoubleScreen已不再在综合态势layerTool触发，目前已无需解决视频覆盖问题。
      this.crossName = d.attribute.crossName;
      this.crossroad_name = `${this.crossName.split('与')[0]}-${this.crossName.split('与')[1].split('交叉口')[0]}`;
      this.videoUrl = '';
      this.videoList = [];
      this.deviceList = [];
      this.indexList = {
        zs: 0, sum: 0, now: 0, md: 0,
      };
      this.attribute = {};
      this.status = 'status';
      if (d) {
        let i = 1;
        d.videoList.forEach((element) => {
          if (element[this.status] == undefined) this.status = 'online';
          if (this.deviceList.indexOf(element.deviceId) == -1 && element[this.status] == 1) {
            this.deviceList.push(element.deviceId);
            this.videoList.push({ name: `摄像头${i++}`, deviceId: element.deviceId, channelId: element.channelId });
          }
        });
        this.attribute = d.attribute;
        nextTick(() => {
          this.inToIntersection();
        });
        setTimeout(() => {
          this.play(this.videoList[0], 0);
          if (this.crossroad_name == '文营西路-双文街') {
            twins = new FullRoad_mec(viewer1);
          } else {
            twins = new FullRoad(viewer1);
            if (twins) {
              //fullRoad.changeViewer(viewer1);
              //fullRoad = new FullRoad(viewer1);
              twins.setH(0);
            }
          }
        }, 3000);
      }
      this.getTrafficJamNumber();
    });
    this.$bus.on('trafficDensity',(d)=>{
      this.indexList.md = d
    });
    this.$bus.on('carTotal',(d)=>{
      this.indexList.sum = d
    });
    this.$bus.on('carHour',(d)=>{
      this.indexList.now = d
    });
  },
  methods: {
    inToIntersection() {
      if (window.tileset) window.tileset.show = false;
      Cesium.Ion.defaultAccessToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJqdGkiOiI0ZmIxMDkxMy0xY2Q0LTRlZjUtYjg1MC01MTU2MDU4OGI0ZGQiLCJpZCI6NjYzOTQsImlhdCI6MTYzMDkxOTE1Nn0.oaYfPD_JHJZT6Zo3zRwygnd9ozYH5U590WNHa6b8_3g';
      if (!viewer1) { // 已有viewer1则不重复建立cesium
        viewer1 = new Cesium.Viewer('container1', {
          animation: false, // 左下角的动画仪表盘
          baseLayerPicker: false, // 右上角的图层选择按钮
          geocoder: false, // 搜索框
          homeButton: false, // home按钮
          sceneModePicker: false, // 模式切换按钮
          timeline: false, // 底部的时间轴
          navigationHelpButton: false, // 右上角的帮助按钮，
          fullscreenButton: false, // 右下角的全屏按钮
          infoBox: false, // 小弹窗
          selectionIndicator: false,
          zoomIndicatorContainer: false,
          navigation: false, // 指南针
          shadows: false, // 阴影
          shouldAnimate: true,
          scene3DOnly: true,
        });
      }

      viewer1._cesiumWidget._creditContainer.style.display = 'none'; //	去除版权信息
      // viewer1.scene.sun.show = false; //在Cesium1.6(不确定)之后的版本会显示太阳和月亮，不关闭会影响展示
      // viewer1.scene.moon.show = false;
      // viewer1.scene.skyBox.show = false;//关闭天空盒，否则会显示天空颜色

      viewer1.scene.undergroundMode = true; // 重要，开启地下模式，设置基色透明，这样就看不见黑色地球了
      // viewer.scene.underGlobe.show = true;
      // viewer1.scene.underGlobe.baseColor = new Cesium.Color(0, 0, 0, 0);
      // viewer1.scene.globe.show = false; //不显示地球，这条和地球透明度选一个

      if (Cesium.FeatureDetection.supportsImageRenderingPixelated()) { // 判断是否支持图像渲染像素化处理
        let vtxf_dpr = window.devicePixelRatio;
        // 适度降低分辨率
        while (vtxf_dpr >= 2.0) {
          vtxf_dpr /= 2.0;
        }
        viewer1.resolutionScale = vtxf_dpr;
      }
      // 是否开启抗锯齿
      viewer1.scene.fxaa = true;
      viewer1.scene.postProcessStages.fxaa.enabled = true;

      let path = 'tiles-server/tiles_dm_ktx/tileset.json';
      // if (this.crossroad_name == '文营西路-双文街') {
      //   path = tiles_lk;
      // } else {
      //   path = 'tiles-server/tiles_dm_ktx/tileset.json';
      // }

      // var tileset = new Cesium.Cesium3DTileset({
      //     url:path,//tiles_lk,
      // });
      // tileset.readyPromise.then((t) => {
      //     rectify(t);
      //     viewer1.scene.primitives.add(t);
      // })
      // var imageryLayer = new AmapImageryProvider({
      //     url:'http://webst02.is.autonavi.com/appmaptile?style=8&x={x}&y={y}&z={z}',
      //     crs:'WGS84'
      // })
      // viewer1.imageryLayers.addImageryProvider(imageryLayer);
      const layer = new Cesium.MapboxStyleImageryProvider({
        url: 'https://api.mapbox.com/styles/v1/',
        username: '18333286662',
        styleId: 'ckujk1slp8ibs18rxuis5se1g',
        accessToken: 'pk.eyJ1IjoiMTgzMzMyODY2NjIiLCJhIjoiY2t0d2J2YTF0MDk3YTJ2dGhsd25uZTVsOCJ9.ZqW3569-uLO-4OclJw7JVA',
        scaleFactor: true,
      });
      viewer1.imageryLayers.addImageryProvider(layer);
      newHightLightPolyline(viewer1, 'IBD_LANE_BOUNDARY.json', Cesium.Color.WHITE, false);
      // Export_Output_4
      newHightLightPolyline(viewer1, 'Export_Output_4.json', Cesium.Color.WHITE, false);
      viewer1.camera.flyTo({ // 115.9106004,39.06528125
        destination: Cesium.Cartesian3.fromDegrees(
          Number(this.attribute.lng - 0.00025), Number(this.attribute.lat - 0.0007), 50,
        ),

        orientation: {
          heading: 0.10856833278113953, // Cesium.Math.toRadians(-30.0),
          pitch: -0.5297995260871158, // Cesium.Math.toRadians(-45.0),
          roll: 6.283185101108353,
        },
        // {x: -2169438.2162051983, y: 4459243.346447918, z: 3997783.960889403},
        // orientation: {
        //     // 指向
        //     heading: 1.8392909011219958,
        //     // 视角
        //     pitch: -0.3187164884436109,
        //     roll: 6.283185049241538
        // }
      });
    },
    addSvg() {
      viewer1.entities.add({
        position: Cesium.Cartesian3.fromDegrees(
          Number(this.attribute.lng), Number(this.attribute.lat), 1,
        ), // {x: -2169438.2162051983, y: 4459243.346447918, z: 3997783.960889403},
        billboard: {
          image: svgEntityImage,
        },
      });
    },
    play(video, index) {
      if (index == 0) {
        // viewer1.camera.flyTo({
        //     destination: {x: -2169438.2162051983, y: 4459243.346447918, z: 3997783.960889403},
        //     orientation: {
        //         // 指向
        //         heading: 1.8392909011219958,
        //         // 视角
        //         pitch: -0.3187164884436109,
        //         roll: 6.283185049241538
        //     }
        // });
      } else {
        // viewer1.camera.flyTo({
        //     destination: {x: -2169534.3977991426, y: 4459137.530259172, z: 3997799.3350397297},
        //     orientation: {
        //         // 指向
        //         heading: 3.398811777091888,
        //         // 视角
        //         pitch: -0.3187164884436109,
        //         roll: 6.283185049241538
        //     }
        // });
      }

      deviceProxyPlay({
        channelId: video.channelId,
        deviceId: video.deviceId,
        channelNum: video.channelId.charAt(video.channelId.length - 1),
        subtype: 1, // 0主码 1辅码
      }).then((res) => {
        if (res.data) {
          this.videoUrl = res.data.flv;
          var name = this.crossName;
          let json = {};
          var p = {
            position:viewer1.scene.camera.position,
            heading: viewer1.scene.camera.heading.toFixed(15),//Cesium.Math.toRadians(-30.0),
            pitch: viewer1.scene.camera.pitch.toFixed(15),//Cesium.Math.toRadians(-45.0),
            roll: viewer1.scene.camera.roll.toFixed(15),

            channelId: video.channelId,
            deviceId: video.deviceId
          }
          json[name] = p;
          console.log(JSON.stringify(json));
        } else {
          alert(res.msg);
        }
      });

      // sendDevicePush({
      //     deviceId:video.deviceId,
      //     channelId:video.channelId
      // }).then(res => {
      //     if(res.code == -1){
      //         alert(res.msg)
      //     }else{
      //         this.videoUrl = res.data.flv;
      //     }
      // })
    },
    back() {
      if (window.tileset) {
        window.tileset.show = true;
      }
      this.destroyed();// 离开组件前销毁数字地球，防止viewer1不释放
      this.isShow = false;
      // this.$store.commit('clearPop2D')
      // DoubleScreen已不再在综合态势页面触发，目前已无需解决视频覆盖问题。
    },
    getTrafficJamNumber() {
      const that = this;
      function fn() {
        const params = {
          crossroad_name: that.crossroad_name,
        };
        getTrafficJam(params).then((res) => {
          that.indexList.zs = res.data[0].tci;
        });
        return fn;
      }
      timer = setInterval(fn(), 1000 * 60 * 10);
    },
    destroyed() {
      if (fullRoad) {
        //fullRoad.changeViewer(viewer);
        //fullRoad.setH(5);
      }
      if (twins) {
        twins.close();
      }
      if (viewer1 != null && !viewer1.isDestroyed()) {
        // alert(viewer.isDestroyed());
        viewer1.destroy();
        viewer1 = null;
      }
    },
  },
  unmounted() {
    clearInterval(timer)
    timer = null
    this.destroyed();// 销毁之前也摧毁数字地球，否则路由跳转不会重置viewer1，再次加载无数字地球。
    this.$bus.off('isShow');
    this.$bus.off('trafficDensity');
    this.$bus.off('carTotal');
    this.$bus.off('carHour');
  },
};
</script>
<style lang="less" scoped>
.bg{
    height: 100vh;
    width: 100vw;
    //backdrop-filter: blur(3px);
    background: url('../../../assets/img/Frame1000002381.jpg') no-repeat;
    background-size: 100% 100%;
    position: absolute;
    left: 0;
    top: 0;
    z-index: 999;
    .title-bg{
        background: url('../../../assets/img/Breaking the boundary between reality and virtualization.png') no-repeat;
        background-size: 100% 100%;
        position: absolute;
        width: 617px;
        height: 88px;
        left: 50%;
        transform: translate(-50%);
        bottom: 54px;
        z-index: 999;
    }
    .close {
        position: absolute;
        //margin-right: 48px;
        width: 128px;
        height: 52px;
        right:48px;
        top: 906px;
        cursor: pointer;
        background: url('../../../assets/img/Frame 427321975.png');
        background-repeat: no-repeat;
        background-size:100% 100%;
    }
    .title-cell{
        position: absolute;
        left: 50%;
        transform: translate(-50%);
        bottom: 84px;
        align-items: center;
        gap: 6px;
        width: 720px;
        height: 28px;
        display: flex;
        flex-direction: row;
        .title{
            //width: 220px;
            height: 28px;
            font-family: 'Noto Sans SC';
            font-style: normal;
            font-weight: 700;
            font-size: 32px;
            line-height: 28px;
            color: #47EBEB;
        }
        .cell1{
            //width: 84px;
            height: 1px;
            background: linear-gradient(270deg, rgba(71, 235, 235, 0) 0%, #47EBEB 100%);
            transform: matrix(-1, 0, 0, 1, 0, 0);
            flex: none;
            order: 0;
            flex-grow: 1;
        }
        .cell2{
            //width: 84px;
            height: 1px;
            background: linear-gradient(270deg, rgba(71, 235, 235, 0) 0%, #47EBEB 100%);
            flex: none;
            order: 0;
            flex-grow: 1;
        }
    }

    .main {
      width: 1824px;
      height: 100%;
      position: absolute;
      left: 50%;
      top: 128px;
      bottom: 170px;
      transform: translate(-50%);
      overflow:hidden;
      .left-dialog{
          background: url('../../../assets/img/Frame 427322062.png') no-repeat;
          backdrop-filter: blur(3px);
          background-size: 100% 100%;
          width: 888px;
          //height: 732px;
          float: left;
          .video {
              width: 848px;
              height: 478px;
              margin: 0 auto;
              margin-top: 100px;
              object-fit: fill;
              display: block;
          }

          .btnDiv{
              margin-left: 30px;
              margin-top: 36px;
              margin-bottom: 36px;
              height: 48px;
              display: flex;
              flex-direction: row;
              >div{
                  height: 48px;
                  width: 150px;
                  background: url('../../../assets/img/Frame 427321529.png') no-repeat;
                  background-size: 100% 100%;
                  margin-left: 10px;
                  color: white;
                  cursor: pointer;
                  >span{
                      font-family: 'Noto Sans SC';
                        font-style: normal;
                        font-weight: 400;
                        font-size: 16px;
                        height: 24px;
                        line-height: 47px;
                        color: #CCCCCC;
                        //margin-top: 200px;
                        margin-left: 60px;
                  }
              }
          }
      }
      .right-dialog{
          background: url('../../../assets/img/Frame 427322061.png') no-repeat;
          background-size: 100% 100%;
          width: 888px;
          backdrop-filter: blur(3px);
          //height: 692px;
          float:right;
          #container1{
              width: 848px;
              height: 478px;
              margin: 0 auto;
              margin-top: 100px;
              object-fit: fill;
              display: block;
          }
          .index_bottom{
              margin-top: 24px;
              margin-bottom: 24px;
              width: 848px;
              height: 72px;
              display: flex;
              flex-direction: row;
              margin-left: 20px;
              .index_cell{
                  width: 192px;
                  height: 72px;
                  margin-left: 20px;
                  background: url('../../../assets/img/Frame 427321609.png') no-repeat;
                  background-size: 100% 100%;
                  margin: auto;
                  .cell{
                    display: flex;
                    flex-direction: column;
                    align-items: flex-start;
                    width: 119px;
                    height: 52px;
                    margin: auto;
                    margin-top: 10px;
                    .cell_row{
                        display: flex;
                        flex-direction: row;
                        width: 100%;
                        height: 50%;
                        .cell1{
                            color:#01FFFF;
                            font-family: 'DINPro';
                            font-style: normal;
                            font-weight: 700;
                            font-size: 20px;
                            line-height: 28px;
                            margin-left: 5px;
                        }
                        .dw{
                            margin-left: 2px;
                            color: #fff;
                          line-height: 28px;
                        }
                        .icon1{
                            margin-top: 4px;
                            background: url('../../../assets/img/Slice 123@2x.png') no-repeat;
                            background-size: 100% 100%;
                            width: 20px;
                            height: 20px;
                        }
                        .icon2{
                            margin-top: 4px;
                            background: url('../../../assets/img/Slice110.png') no-repeat;
                            background-size: 100% 100%;
                            width: 20px;
                            height: 20px;
                        }
                        .icon3{
                            margin-top: 4px;
                            background: url('../../../assets/img/Slice110.png') no-repeat;
                            background-size: 100% 100%;
                            width: 20px;
                            height: 20px;
                        }
                        .icon4{
                            margin-top: 4px;
                            background: url('../../../assets/img/Slice110.png') no-repeat;
                            background-size: 100% 100%;
                            width: 20px;
                            height: 20px;
                        }
                    }
                    .cell2{
                        flex: 1;
                        font-family: 'Noto Sans SC';
                        font-style: normal;
                        font-weight: 400;
                        font-size: 16px;
                        line-height: 24px;
                        color: #fff;
                    }

                  }
              }
          }
      }

    }

}
</style>
