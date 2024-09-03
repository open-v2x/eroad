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
  <div style="width: 100%; height: 100%">
    <echarts-chart :options="transit"/>
  </div>
</template>

<script>
import echartsChart from '../echartsChart.vue'
import selectButtons from './selectButtons.vue'
import selectStandard from './selectStandard.vue'
import { road_intersection, road_tongxing_tian, road_tongxing_duan } from '../../api'
import dayjs from 'dayjs'
import { setSize } from '@/utils/setSize'
import * as echarts from 'echarts'
export default {
  name: 'transitChart',
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
          top: setSize(75),
          bottom: setSize(40), //也可设置left和right设置距离来控制图表的大小
          left: setSize(42),
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
          data: ["通行效率", "饱和度"],
          icon: "roundRect",
          top: "4%",
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
            axisLine: {
              show: true, //隐藏X轴轴线
              lineStyle: {
                color: "rgba(219, 223, 240, 0.3)",
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
            data: [],
            position: "bottom",
            offset: 16,
            axisLine: {
              show: true, //隐藏X轴轴线
              lineStyle: {
                color: "rgba(219, 223, 240, 0.3)",
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
        ],
        yAxis: [
          {
            type: "value",
            name: "单位：%",

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
          {
            type: "value",
            name: "单位：%",
            nameTextStyle: {
              color: "rgba(219, 223, 240, 1)",
            },
            position: "right",
            splitLine: {
              show: false,
            },
            axisTick: {
              show: false,
            },
            axisLine: {
              show: false,
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
            name: "通行效率",
            // xAxisIndex:props.weekData.length > 0 ? 1 : 0,
            zlevel: "10",
            type: "bar",
            // barWidth: setSize(computeWidth()),
            showBackground: true,
            backgroundStyle: {color:'rgba(49, 206, 255, 0.2)', borderColor:'#31CEFF',borderWidth:1},
            itemStyle: {
              barBorderRadius: [setSize(6), setSize(6), 0, 0],
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                {
                  offset: 0,
                  color: "rgba(71, 235, 235, 1)",
                },
                {
                  offset: 1,
                  color: "rgba(6, 27, 71, 0.7)",
                },
              ]),
            },
            data: [],
          },
          {
            name: "饱和度",
            // xAxisIndex:props.weekData.length > 0 ? 1 : 0,
            type: "line",
            zlevel: "11",
            yAxisIndex: 1, //使用的 y 轴的 index，在单个图表实例中存在多个 y轴的时候有用
            smooth: true, //平滑曲线显示
            symbol: "none", //显示所有图形。
            symbolSize: setSize(10),
            areaStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                {
                  offset: 0,
                  color: "rgba(229, 255, 71, 0.232)",
                },
                {
                  offset: 1,
                  color: "rgba(225, 255, 37, 0.008)",
                },
              ]),
            },
            itemStyle:{
              shadowColor: 'rgba(238, 255, 41, 1)'
            },
            lineStyle: {
              color: "rgba(238, 255, 41, 1)",
              shadowColor: "rgba(238, 255, 41, 1)",
              shadowBlur: 15,
              width: setSize(3),
            },
            data: [],
          }
        ]
      }
    }
  },

  methods: {
    generate(data) {
      if (data.timeQuantumActive) {
        road_tongxing_duan({
          "road_name": data.roadSection
        }).then(res => {
          if (res.code === 200) {
            this.transit.xAxis[0].data = res.data.map(item => item.data_time)
            this.transit.xAxis[1].data = []
            this.transit.series[0].data = res.data.map(item => item.saturation) // 饱和度
            this.transit.series[1].data = res.data.map(item => item.tra_efficiency) // 效率
          }
        })
      } else {
        road_tongxing_tian({
          "start_date": dayjs(data.date[0]).format("YYYY-MM-DD"),
          "end_date": dayjs(data.date[1]).format("YYYY-MM-DD"),
          "road_name": data.roadSection
        }).then(res => {
          if (res.code === 200) {
            this.transit.xAxis[0].data = res.data.map(item => this.week[dayjs(item.data_time).day()])
            this.transit.xAxis[1].data = res.data.map(item => item.data_time)
            this.transit.series[0].data = res.data.map(item => item.saturation) // 饱和度
            this.transit.series[1].data = res.data.map(item => item.tra_efficiency) // 效率
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