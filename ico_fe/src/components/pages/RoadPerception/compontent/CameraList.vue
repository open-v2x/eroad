<template>
  <div class="cameraList dashboard-dialog" v-drag>
    <div class="title">
      <span class="caption">范围内摄像头列表</span>
      <el-icon class="close" @click="$emit('changeVisible', false)">
        <Close />
      </el-icon>
    </div>
    <div class="body">
      <el-table :data="cameraList" style="width: 100%;font-size: 16px;">
        <el-table-column label="交叉路口名称" prop="交叉路口名称" width="300">
        </el-table-column>
        <el-table-column label="设备编号" prop="SIP用户名/设备编号" width="200">
        </el-table-column>
        <el-table-column label="操作" width="200" align="center">
          <template #default="scope">
            <!--:class="{ activeButton: scope.row.active }"-->
            <div class="button activeButton"  @click="video(scope.row, scope.$index)">视频</div>
            <div class="button activeButton" @click="locate(scope.row, scope.$index)">定位</div>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-pag="currentPage" small @current-change="changeCurrent" layout="prev, pager, next"
        :page-size="10" :total="total" />
    </div>
  </div>
</template>
<script>
let CoordTransform = require('coordtransform');
let border = require('../../../../assets/img/border.png')
let borderMarker
let timer
import { queryChannelsByDeviceId, sendDevicePush } from '../../../../assets/js/api_video'
export default {
  data() {
    return {
      cameras: [],
      cameraList: [],
      currentPage: 1,
      total: 0
    }
  },
  created() {
    this.$bus.on('camerasInRectangle', (cameras) => {
      cameras.forEach(item => {
        item.active = false
      })
      console.log(cameras)
      this.cameras = cameras
      this.cameraList = this.cameras.slice(0, 10)
      this.total = cameras.length
    })
  },
  mounted() {
  },
  methods: {
    changeCurrent(data) {
      console.log(data)
      this.cameraList = this.cameras.slice((data - 1) * 10, (data) * 10)
      console.log(this.cameraList)
    },
    video(data, index) {
      this.cameras.forEach(item => {
        item.active = false
      })
      console.log(data, index)
      data.active = true
      let position = CoordTransform.wgs84togcj02(data.经度, data.纬度)
      let viewCom = 'LivePlayer'
      let lat = position[1]
      let lng = position[0]
      let crossRoad = data['交叉路口名称']
      this.play(data['SIP用户名/设备编号'], (res) => {
        map.setZoomAndCenter(15, position)
        this.$bus.emit('pop2D', { propertity: { url: res.url, camera: res.camera, lng: lng, lat: lat, 交叉路口名称: crossRoad, viewCom: viewCom, record: false } });
      })
    },
    locate(data, index) {
      clearTimeout(timer)
      if (borderMarker) map.remove(borderMarker)
      map.setZoomAndCenter(19, CoordTransform.wgs84togcj02(data.经度, data.纬度))
      let borderIcon = new AMap.Icon({
        map,
        image: border,
        size: new AMap.Size(36, 36),
        imageSize: new AMap.Size(36, 36)
      })
      borderMarker = new AMap.Marker({
        position: CoordTransform.wgs84togcj02(data.经度, data.纬度),
        icon: borderIcon,
        offset: new AMap.Pixel(7, 7)
      })
      console.log(borderIcon)
      map.add(borderMarker)
      timer = setTimeout(() => {
        if (borderMarker) map.remove(borderMarker)
      }, 3000)
    },
    play(deviceId, callback) {
      queryChannelsByDeviceId({
        deviceId: deviceId
      }).then((res) => {
        // console.log('查找摄像头通道',res)
        var channelId
        var flag = false
        let url = []
        let camera = { deviceId: deviceId }
        let response = {}
        if (res.length == 0) {
          alert('已查询到摄像头信息,但该设备未建立通道')
          return;
        } else if (res.length == 1) {
          channelId = res[0]['channelId']
          // console.log('打印摄像头唯一通道',channelId)
          camera.channel = [channelId]
        } else {
          flag = true
          channelId = [res[0]['channelId'], res[1]['channelId']]
          camera.channel = [res[0]['channelId'], res[1]['channelId']]
        }
        sendDevicePush({
          deviceId: deviceId,
          channelId: flag ? channelId[0] : channelId
        }).then((re) => {
          if (re['data'] && !flag) {
            response.url = re['data']['flv'];
            response.camera = camera
            callback(response);
          } else if (re['data'] && flag) {
            url.push(re['data']['flv'])
          } else {
            alert(re['msg']);
          }
        }).then(() => {
          if (flag) {
            sendDevicePush({
              deviceId: deviceId,
              channelId: channelId[1]
            }).then(res => {
              if (res.data) {
                url.push(res.data.flv)
                response.url = url
                response.camera = camera
                callback(response)
              } else {
                alert(res.msg)
              }
            })
          }
        })

      })
    },
  }
}
</script>

<style lang='less' scoped>
@height: 10.8vh;

.cameraList {
  user-select: none;
  color: #fff;
  max-width: 1000px;
  max-height: 600px;
  background: url("../../../../assets/img/Frame 1000002315.png");
  background-size: 100% 100%;
  top: 50%;
  left: 20%;
  transform: translate(-50%, -50%);
  position: absolute;
  z-index: 1000000;
  backdrop-filter: blur(3px);
  display: flex;
  flex-direction: column;

  .body {
    flex: 1;
    overflow: hidden;
    padding: 4px;
  }

  .button {
    display: inline-block;
    width: 50px;
    height: 25px;
    line-height: 25px;
    background: url('../../../../assets/img/button-cell-normal.png');
    background-size: 100% 100%;
    margin-right: 10px;
    &:hover {
      cursor: pointer;
      color: aqua;
    }
  }
  .activeButton {
    background: url('../../../../assets/img/button-cell-select.png');
    background-size: 100% 100%;
  }
  .title {
    height: (50/@height);
    background: url('../../../../assets/img/Frame 427321336.png') no-repeat;
    background-size: 100% 100%;
    display: flex;
    justify-content: space-between;

    .close {
      margin-right: 10px;
      font-size: 24px;
      color: #fff;
      height: (50/@height);

      &:hover {
        cursor: pointer;
      }
    }

    .caption {
      margin: 0 38px;
      height: (50/@height);
      font-size: 20px;
      font-family: 'Noto Sans SC';
      font-style: normal;
      font-weight: 500;
      font-size: 20px;
      line-height: (50/@height);
      /* identical to box height, or 140% */
      color: #FFFFFF;
      text-shadow: 0px 1px 1px rgba(0, 0, 51, 0.15);
      /* Inside auto layout */
      flex-grow: 0;
    }
  }
}
</style>
