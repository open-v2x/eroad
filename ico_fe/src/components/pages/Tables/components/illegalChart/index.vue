<template>
  <div class="config">
    <selectButtons @changeTimeQuantumActive="changeTimeQuantumActive" :mode="timeQuantumActive">
      <template #left> 时段统计表 </template>
      <template #right> 日期统计表 </template>
    </selectButtons>
    <selectStandard
      :timeQuantumActive="timeQuantumActive"
      @generate="generate"
      :options="options"
    >
      <template #left> 目标路段 </template>
      <template #right> 统计区间 </template>
    </selectStandard>
  </div>
  <div style="width: 100%; height: 100%;">
    <echarts-chart :options="transit"/>
  </div>
</template>

<script>
import echartsChart from '../echartsChart.vue'
import selectButtons from './selectButtons.vue'
import selectStandard from './selectStandard.vue'
import { road_intersection, event_day, event_hour } from '../../api'
import dayjs from 'dayjs'
import { setSize } from '@/utils/setSize'
import * as echarts from 'echarts'
export default {
  name: 'illegalChart',
  components: {
    echartsChart,
    selectButtons,
    selectStandard,
  },
  data() {
    return {
      timeQuantumActive: true,
      categoryValue: '',
      categoryOptions: [],
      timeValue: '',
      timeOptions: [],
      options: [{ value: "total", label: "全部路段" }],
      week: [
        "星期日",
        "星期一",
        "星期二",
        "星期三",
        "星期四",
        "星期五",
        "星期六",
      ],
      transit: {
        grid: {
          top: setSize(70),
          bottom: setSize(40), //也可设置left和right设置距离来控制图表的大小
          left: setSize(60),
          right: setSize(40),
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
        legend: {
          data: ["违停", "超速", "超高速"],
          icon: "roundRect",
          top: "5%",
          left: "1.5%",
          itemWidth: setSize(16),
          itemHeight: setSize(16),
          textStyle: {
            color: "#ffffff",
            fontSize: setSize(14),
          },
        },
        xAxis: [
          {
            data: [],
            type: "category",
            axisLabel: {
              color: "rgba(219, 223, 240, 1)",
            },
            axisLine: {
              lineStyle: {
                color: "rgba(219, 223, 240, .3)",
              },
              width: 5,
            },
            axisTick: {
              show: false,
            },
          },
          {
            data: [],
            type: "category",
            axisLabel: {
              color: "rgba(219, 223, 240, 1)",
            },
            axisLine: {
              lineStyle: {
                color: "rgba(219, 223, 240, .3)",
              },
              width: 5,
            },
            axisTick: {
              show: false,
            },
          },
        ],
        yAxis: {
          name: "单位：次",
          type: "value",
          axisLabel: {
            color: "rgba(219, 223, 240, 1)",
          },
          axisLine: {
            lineStyle: {
              color: "rgba(219, 223, 240, 1)",
            },
            width: 5,
          },
          axisTick: {
            show: false,
          },
          splitLine: {
            lineStyle: {
              color: "rgba(219, 223, 240, 0.15)",
            },
          },
        },
        series: [
          {
            name: '违停',
            type: "bar",
            stack: 'total',
            barMinHeight:2,
            label: {
              show: false,
            },
            itemStyle: {
              color: 'rgba(48, 218, 255, 1)',
            },
            data: [],
            xAxisIndex: 1
          },
          {
            name: '超速',
            type: "bar",
            stack: 'total',
            barMinHeight:2,
            label: {
              show: false,
            },
            itemStyle: {
              color: 'rgba(24, 142, 251, 1)',
            },
            data: [],
            xAxisIndex: 1
          },
          {
            name: '超高速',
            type: "bar",
            stack: 'total',
            barMinHeight:2,
            label: {
              show: false,
            },
            itemStyle: {
              color: 'rgba(255, 166, 147, 1)',
            },
            data: [],
            xAxisIndex: 1
          }
        ]
      }
    }
  },

  methods: {
    generate(data) {
      if (data.timeQuantumActive) {
        event_hour({
          "road_name": data.roadSection
        }).then(res => {
          if (res.code === 200) {
            this.transit.xAxis[0].data = res.data.map(item => item.data_time)
            this.transit.xAxis[1].data = []
            this.transit.series[0].data = res.data.filter(item => item.event_type === '违停').map(item => item.event_num)
            this.transit.series[1].data = res.data.filter(item => item.event_type === '超速').map(item => item.event_num)
            this.transit.series[2].data = res.data.filter(item => item.event_type === '超高速').map(item => item.event_num)
          }
        })
      } else {
        event_day({
          "start_date": dayjs(data.date[0]).format("YYYY-MM-DD"),
          "end_date": dayjs(data.date[1]).format("YYYY-MM-DD"),
          "road_name": data.roadSection
        }).then(res => {
          if (res.code === 200) {
            this.transit.xAxis[0].data = res.data.map(item => this.week[dayjs(item.data_time).day()])
            this.transit.xAxis[1].data = res.data.map(item => item.data_time)
            this.transit.series[0].data = res.data.map(item => item.weiting)
            this.transit.series[1].data = res.data.map(item => item.chaosu)
            this.transit.series[2].data = res.data.map(item => item.chaogaosu)
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
          localStorage.setItem('roadsData',JSON.stringify(roadOptions))
        }
      });
    }
  },

  mounted() {
    this.fetchOptions()
  },
}
</script>

<style lang="less" scoped>
.selection {
  width: 100%;
  margin-bottom: 22px;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  color: rgba(51, 153, 255, 0.8);
  span {
    margin-right: 6px;
  }
}

input {
  display: inline-block;
  background: none;
  outline: none;
  border: 2px solid rgba(51, 153, 255, 0.8);
  border-radius: 4px;
  color: #fff;
  box-sizing: border-box;
  padding-left: 8px;
  height: 32px;
  width: 140px;
}

.input1 {
  width: 120px;
  margin-right: 22px;
}
.input2 {
  width: 140px;
}

.config {
  display: flex;
  justify-content: space-between;
  position: absolute;
  right: 0;
  top: 22px;
}
</style>