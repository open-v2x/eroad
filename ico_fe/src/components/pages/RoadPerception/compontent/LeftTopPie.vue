<template>
  <div class="pieOuter" ref="pie"></div>
</template>

<script>
import * as echart from "echarts";
import { setSize } from "../../../../assets/js/echartsSetSize";
import { waitNum } from '@/assets/js/api.js'
let pie;
let waitData
export default {
  data() {
    return {};
  },
  created() {
    waitData = [
      {
        value: "70",
        name: "直接通过",
        itemStyle: {
          color: new echart.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: "rgba(51, 153, 255, 0)" }, // 开始位置
            { offset: 1, color: "rgba(51, 153, 255, 1)" }, // 结束位置
          ]),
        },
      },
      {
        value: "40",
        name: "一次等待",
        itemStyle: {
          color: new echart.graphic.LinearGradient(0, 1, 0, 0, [
            { offset: 0, color: "rgba(6, 254, 254, 0)" }, // 开始位置
            { offset: 1, color: "rgba(6, 254, 254, 1)" }, // 结束位置
          ]),
        },
      },
      {
        value: "1",
        name: "二次等待",
        itemStyle: {
          color: new echart.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: "rgba(255, 159, 25, 0.3)" }, // 开始位置
            { offset: 1, color: "rgba(255, 159, 25, 1)" }, // 结束位置
          ]),
        },
      },
    ]
  },
  mounted() {
    waitNum().then(res => {
      res.data.forEach((item) => {
        if (item.cnt_ratio != undefined && item.cnt_ratio != null) {
          if (item.wait_num == 0) {
            waitData[0].value = item.cnt_ratio
          } else if (item.wait_num == 1) {
            waitData[1].value = item.cnt_ratio
          } else if (item.wait_num == 2) {
            waitData[2].value = item.cnt_ratio
          }
        }
      })
      this.initPie();
    })

  },
  unmounted() {
    if (pie) pie.dispose();
    pie = null;
    this.$bus.off('resize')
  },
  methods: {
    initPie() {
      let color = ["#199FFF", "#01FFFF", "#FF7C33"];
      pie = echart.init(this.$refs.pie);
      let option = {
        series: [
          {
            name: "访问来源",
            type: "pie",
            color,
            radius: ["34%", "50%"],
            center: ["50%", "50%"],
            left: "0%",
            top: "0%",
            minAngle: 18,
            startAngle: "80",
            labelLine: {
              length: "6%",
              length2: "3%",
            },
            label: {
              position: "outer",
              alignTo: 'labelLine',
              fontSize: setSize(10),
              formatter: (params) => {
                // console.log(params);
                return `{d|${params.percent.toFixed(1)}%} \n {b|${params.name
                  }}`;
              },
              rich: {
                d: {
                  align: "left",
                  padding: 2,
                  fontWeight: 700,
                  fontSize: setSize(14),
                },
                b: {
                  align: "left",
                  fontSize: setSize(10),
                },
              },
            },
            data: waitData.map((item, index) => {
              item.label = {
                color: color[index],
              };
              item.labelLine = {
                lineStyle: {
                  color: color[index],
                },
              };
              return item;
            }),
          },
        ],
      };
      pie.setOption(option);
      this.$bus.on('resize', () => {
        pie.resize()
        pie.clear()
        pie.setOption(option)
      })
    },
  },
};
</script>

<style lang="less" scoped>
.pieOuter {
  width: 100%;
  height: 100%;
}
</style>
