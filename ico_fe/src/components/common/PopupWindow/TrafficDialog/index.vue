<template>
  <el-dialog
    v-model="dialogVisible"
    width="60%"
    :modal="false"
    :before-close="handleClose"
    custom-class="custom-traffic-light"
  >

    <template #title>
      <div class="title">
        <img src="../../../../assets/img/dialog-title-left-icon.png"/>
        <span>{{ getTitle }}</span>
        <img src="../../../../assets/img/dialog-title-right-icon.png"/>
      </div>
    </template>
    <div class="main" ref="myElement">
      <div class="traffic">
        <TrafficLight v-if="isEast" ref="eastLight" class="eastLight"/>
        <TrafficLight v-if="isWest" ref="westLight" class="westLight" />
        <TrafficLight v-if="isSouth" ref="southLight" class="southLight"/>
        <TrafficLight v-if="isNorth" ref="northLight" class="northLight" />
      </div>
      <div class="table_body">
        <div class="table_th">
          <div class="tr1 th_style">路口事件</div>
          <div class="tr2 th_style">发生时间</div>
          <div class="tr3 th_style">抓拍图片</div>
        </div>
        <div class="table_main_body" v-if="tableData.length">
          <div class="scroll-me table_inner_body">
            <div v-for="(item,index) in tableData" :key="index" class="table_tr">
              <div class="tr1 tr" :title="item.actionType">{{ item.actionType }}</div>
              <div class="tr2 tr" :title="item.insertTime">{{ item.insertTime }}</div>
              <div class="tr3 tr" :title="item.pictureAddress">
                <el-image
                  style="width: 33px; height: 20px"
                  :src="item.pictureAddress"
                  :preview-src-list="[item.pictureAddress]">
                </el-image>
              </div>
            </div>
          </div>
        </div>
        <div v-else class="empty">
          <span>暂无数据</span>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script>
import moment from "moment";
import { wsUrl } from "@/utils/baseUrl";
import Socket from "@/assets/js/socket";
import TrafficLight from './TrafficLight.vue';
import { getEventDay } from '../../../../assets/js/api'

let ws = null;
let wsT = null;
export default {
  components: {
    TrafficLight
  },

  props: {
    currentData: {
      type: Object,
      default: () => {}
    }
  },

  data() {
    return {
      nameId: this.currentData.crossroad_name,
      dialogVisible: true,
      tableData: [],
      isEast: false,
      isWest: false,
      isSouth: false,
      isNorth: false
    }
  },

  computed: {
    getTitle() {
      return this.nameId + '交叉口车次流量统计'
    }
  },

  methods: {
    // 获取红绿灯数据
    getTrafficLightData() {
      console.log(ws, 111222);
      ws = new Socket({
        url: wsUrl + '/caikong',
      })
      ws.onopen(() => {
        ws.send(this.nameId);
      }) 
      ws.onmessage((ms)=>{
        if(ms == '连接成功')
            return;
        let data = JSON.parse(ms);
        const {east, north, south, west} = data;
        this.isEast = !!Object.keys(east).length;
        this.isWest = !!Object.keys(west).length;
        this.isSouth = !!Object.keys(south).length;
        this.isNorth = !!Object.keys(north).length;
        this.$nextTick(()=>{
          // 东
          if (this.isEast) {
            this.$refs.eastLight.lightCurrentTime = {
              left: east['2']?.countdownTimer,
              forward: east['1']?.countdownTimer,
              right: east['3']?.countdownTimer,
            }
            this.$refs.eastLight.lightCurrentColor = {
              left: east['2']?.lightCurrentColor,
              forward: east['1']?.lightCurrentColor,
              right: east['3']?.lightCurrentColor,
            }
          }
          // 西
          if(this.isWest) {
            this.$refs.westLight.lightCurrentTime = {
              left: west['2']?.countdownTimer,
              forward: west['1']?.countdownTimer,
              right: west['3']?.countdownTimer,
            }
            this.$refs.westLight.lightCurrentColor = {
              left: west['2']?.lightCurrentColor,
              forward: west['1']?.lightCurrentColor,
              right: west['3']?.lightCurrentColor,
            }
          }
          // 南
          if(this.isSouth) {
            this.$refs.southLight.lightCurrentTime = {
              left: south['2']?.countdownTimer,
              forward: south['1']?.countdownTimer,
              right: south['3']?.countdownTimer,
            }
            this.$refs.southLight.lightCurrentColor = {
              left: south['2']?.lightCurrentColor,
              forward: south['1']?.lightCurrentColor,
              right: south['3']?.lightCurrentColor,
            }
          }
          // 北
          if(this.isNorth) {
            this.$refs.northLight.lightCurrentTime = {
              left: north['2']?.countdownTimer,
              forward: north['1']?.countdownTimer,
              right: north['3']?.countdownTimer,
            }
            this.$refs.northLight.lightCurrentColor = {
              left: north['2']?.lightCurrentColor,
              forward: north['1']?.lightCurrentColor,
              right: north['3']?.lightCurrentColor,
            }
          }
        })
      })
    },
    // 获取table数据ws
    fetchTableWs() {
      wsT = new Socket({
        url: wsUrl + '/event',
      })
      wsT.onopen(() => {
        wsT.send(this.nameId);
      }) 
      wsT.onmessage((ms)=>{
        if(ms == '连接成功')
            return;
        let data = JSON.parse(ms);
        this.tableData.unshift(data)
        this.tableData.pop()
      })
    },
    // 获取table数据http
    fetchTableHttp() {
      getEventDay({
        name: this.nameId
      }).then(res => {
        this.tableData = res.data.map(item => {
          return {
            ...item,
            insertTime: moment(item.insertTime).format('YYYY-MM-DD HH:mm:ss')
          }
        })
      })
    },
    handleClose() {
      this.$emit('close')
    }
  },

  mounted() {
    this.getTrafficLightData()
    this.fetchTableHttp()
    this.fetchTableWs()
  },

  beforeUnmount() {
    ws.close()
    wsT.close()
    ws = null
    wsT = null
  },
}
</script>

<style lang="less">
.custom-traffic-light {
  background: url('../../../../assets/img/dialog-bg.png');
  background-repeat: no-repeat;
  background-size: 100% 100%;
  .el-dialog__headerbtn {
    margin-top: -10px;
  }
  .title {
    font-size: 20px;
    font-weight: 700;
    color: #47EBEB;
    text-align: center;
  }
  .main {
    display: flex;
    min-height: 540px;
    .traffic {
      width: 683px;
      height: 540px;
      margin-right: 20px;
      background: url('../../../../assets/img/traffic-light-bg.png');
      background-repeat: no-repeat;
      background-size: 100% 100%;
      position: relative;
      .eastLight {
        left: 55%;
        top: 32%;
      }
      .westLight {
        left: 19%;
        top: 25%;
      }
      .southLight {
        left: 25%;
        top: 42%;
      }
      .northLight {
        left: 52%;
        top: 14%;
      }
    }
    .table_body {
      flex: 1;
      height: 100%;
    }
    .table_th {
      display: flex;
      height: 40px;
      line-height: 40px;
    }
    .tr {
      font-size: 12px;
      padding: 8px 5px;
      border-bottom: 0.5px solid rgba(55, 122, 174, 0.12);
      text-align: center;
      display: flex;
      align-items: center;
      justify-content: center;
    }
    .tr1 {
      flex: 2;
    }
    .tr2 {
      flex: 3;
      text-align: center;
    }
    .tr3 {
      flex: 2;
      text-align: center;
    }

    .th_style {
      color: #D1DEFF;
      font-size: 14px;
      font-weight: bold;
      text-align: center;
      background: rgba(58, 98, 177, 0.5);
      box-shadow: 0px 0px 8px 0px rgba(51, 142, 248, 0.5) inset;
      margin: 0 0.5px;
    }
    .table_main_body {
      width: 100%;
      height: 508px;
      overflow: hidden;
      position: relative;
      background: none;
    }
    .table_inner_body {
      width: 100%;
      height: 508px;
      overflow: auto;
    }
    .table_tr {
      display: flex;
      color: #D1DEFF;
    }
    .empty {
      display: flex;
      justify-content: center;
      margin-top: 50px;
      color: #909399;
    }
  }
  .dialog-footer button:first-child {
    margin-right: 10px;
  }
}

</style>
