<template>
  <div class="config">
    <selectButtons @changeTimeQuantumActive="changeTimeQuantumActive" :mode="timeQuantumActive">
      <template #left> 时段统计表 </template>
      <template #right> 日期统计表 </template>
    </selectButtons>
    <span>统计路段</span>
    <el-select
      v-model="value"
      class="input1"
      filterable
      placeholder="请选择路段"
      @change="changeSelect"
    >
      <el-option
        v-for="item in options"
        :key="item.value"
        :label="item.label"
        :value="item.value"
      />
    </el-select>
    <selectStandard
      :timeQuantumActive="timeQuantumActive"
      @generate="generate"
      :options="roadOptions"
    >
      <template #left> 统计路口 </template>
      <template #right> 统计区间 </template>
    </selectStandard>
  </div>
  <div style="width: 100%; height: 100%">
    <echarts-chart :options="chartOptions"/>
  </div>
</template>

<script>
import echartsChart from '../echartsChart.vue'
import selectButtons from './selectButtons.vue'
import selectStandard from './selectStandard.vue'
import { road_intersection, road_section_cross_flow_tian, road_section_cross_flow_duan, road_cross_relation_list } from '../../api'
import dayjs from 'dayjs'
import { setSize } from '@/utils/setSize'
import * as echarts from 'echarts'
export default {
  name: 'flowChart',
  components: {
    echartsChart,
    selectButtons,
    selectStandard,
  },
  data() {
    return {
      timeQuantumActive: true,
      value: 'total',
      options: [{ value: "total", label: "全部路段" }],
      roadOptions: [{ value: "total", label: "全部路段" }],
      relationList: [],
      week: [
        "星期日",
        "星期一",
        "星期二",
        "星期三",
        "星期四",
        "星期五",
        "星期六",
      ],
      chartOptions: {
        grid: {
          top: setSize(70),
          bottom: setSize(40), //也可设置left和right设置距离来控制图表的大小
          left: setSize(70),
          right: setSize(50),
        },
        tooltip: {
          trigger: "axis",
          backgroundColor: "rgba(6, 27, 71, 0.7)",
          textStyle: {
            color: "#fff",
          },
          borderWidth: "2",
          borderColor: "rgba(1, 255, 255, 1)",
          axisPointer: {
            type: "line",
          },
        },
        xAxis: [
          {
            boundaryGap:false,
            data: [],
            axisLine: {
              show: true,
              lineStyle: {
                color: "rgba(219, 223, 240, 0.3);",
              },
            },
            axisTick: {
              show: false, //隐藏X轴刻度
            },
            axisLabel: {
              show: true,
              textStyle: {
                color:'rgba(219, 223, 240, 1)'
              },
            },
          },
          {
            boundaryGap:false,
            data: [],
            position:'bottom',
            offset:(setSize(16)),
            axisLine: {
              show: false,
            },
            axisTick: {
              show: false, //隐藏X轴刻度
            },
            axisLabel: {
              show: true,
              textStyle: {
                color:'rgba(219, 223, 240, 1)'
              },
            },
          },
        ],
        yAxis: [
          {
            type: "value",
            name: "单位：车次",

            splitLine: {
              show: true,
              lineStyle: {
                color: "rgba(219, 223, 240, 0.15)",
              },
            },
            axisTick: {
              show: false,
            },
            axisLine: {
              show: false,
              lineStyle: {
                color: "rgba(219, 223, 240, 1)",
              },
            },
            axisLabel: {
              show: true,
              textStyle: {
                color: "rgba(219, 223, 240, 1)",
              },
            },
          },
        ],
        series: [
          {
            name: "车流量",
            xAxisIndex: 0,
            // data: props.Data,
            data: [],
            smooth: true,
            symbolSize: setSize(10),
            symbol: "none",
            itemStyle: {
              color: "rgba(1, 255, 255, 1)",
            },
            lineStyle: {
              color: "rgba(1, 255, 255, 1)",
            },
            areaStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                {
                  offset: 0,
                  color: "rgba(0, 149, 255, 1)",
                },
                {
                  offset: 1,
                  color: "rgba(0, 149, 255, 0)",
                },
              ])
            },
            type: "line",
          },
        ],
      }
    }
  },

  methods: {
    generate(data) {
      if (data.timeQuantumActive) {
        road_section_cross_flow_duan({
          "road_name": this.value,
          "crossroad_name": data.roadSection
        }).then(res => {
          if (res.code === 200) {
            this.chartOptions.xAxis[0].data = res.data.map(item => item.data_hour)
            this.chartOptions.xAxis[1].data = []
            this.chartOptions.series[0].data = res.data.map(item => item.traffic_flow)
          }
        })
      } else {
        road_section_cross_flow_tian({
          "small_date": dayjs(data.date[0]).format("YYYY-MM-DD"),
          "big_date": dayjs(data.date[1]).format("YYYY-MM-DD"),
          "road_name": this.value,
          "crossroad_name": data.roadSection,
        }).then(res => {
          if (res.code === 200) {
            this.chartOptions.xAxis[0].data = res.data.map(item => this.week[dayjs(item.data_date).day()])
            this.chartOptions.xAxis[1].data = res.data.map(item => item.data_date)
            this.chartOptions.series[0].data = res.data.map(item => item.traffic_flow) 
          }
        })
      }
    },
    changeTimeQuantumActive(bool) {
      this.timeQuantumActive = bool;
    },
    fetchOptions() {
      road_intersection().then((res) => {
        // 进来先提供选项列表
        if(res.code === 200){
          let roadOptions = [{ value: "total", label: "全部路段" }];
          let roads = new Set();
          res.data.forEach((item) => {
            roads.add(item.road_name);
          });
          roads = Array.from(roads);
          roads.forEach((item) => {
            roadOptions.push({ label: item, value: item });
          });
          this.options = roadOptions;
        }
      });
    },

    changeSelect() {
      this.roadOptions = this.relationList[this.value]
    },

    buildTree(data) {
      const tree = {};

      data.forEach(item => {
        const roadName = item.road_name;
        const crossroadName = item.crossroad_name;

        // 如果该道路在树中还没有节点，则创建一个新的节点
        if (!tree[roadName]) {
          tree[roadName] = [];
        }

        // 将交叉路口的信息加入该道路的节点中
        tree[roadName].push({
          label: crossroadName,
          value: crossroadName,
        });
      });

      return tree;
    }

  },

  mounted() {
    this.fetchOptions()
    road_cross_relation_list().then(res => {
      if (res.code === 200) {
        this.relationList = this.buildTree(res.data);
      }
    })
  },
}
</script>

<style lang="less" scoped>
.config {
  display: flex;
  justify-content: space-between;
  position: absolute;
  left: 340px;
  top: 20px;
  z-index: 2;
  font-family: "Noto Sans SC";
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  line-height: 32px;
  color: rgba(51, 153, 255, 0.8);
}
.input1 {
  margin-left: 8px;
  width: 120px;
  margin-right: 32px;
}
</style>