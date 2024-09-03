<template>
  <div class="config">
    <span>路口名:</span>
    <el-select v-model="value" class="input1" placeholder="请选择路口" filterable @change="fetchDataCross">
      <el-option
        v-for="item in options"
        :key="item.value"
        :label="item.label"
        :value="item.value"
      />
    </el-select>
  </div>
  <div style="width: 100%; height: 100%">
    <echarts-chart :options="chartOptions"/>
  </div>
</template>

<script>
import echartsChart from '../echartsChart.vue'
import { road_cross_relation_list, road_wait_num } from '../../api'
import { setSize } from '@/utils/setSize'
import * as echarts from 'echarts'
import green from "@/assets/img/table/greenDeco.svg";
import blue from "@/assets/img/table/blueDeco.svg";
import yellow from "@/assets/img/table/yellowDeco.svg";

export default {
  name: 'passChart',
  components: {
    echartsChart,
  },
  data() {
    return {
      value: 'total',
      options: [{ value: "total", label: "全部路口" }],
      chartOptions: {
        legend: {
          show: false,
          orient: "vertical",
          top: "middle",
          right: "5%",
          textStyle: {
            color: "#f2f2f2",
            fontSize: setSize(25),
          },
          icon: "roundRect",
        },
        series: [
          // 主要展示层的
          {
            avoidLabelOverlap:true,
            radius: ["22%", "55%"],
            center: ["50%", "55%"],
            type: "pie",
            z: 1,
            emphasis: {
              scale: false,
              disabled: true
            },
            startAngle: -20,
            minAngle:5,
            labelLine: {
              show: true,
              length: setSize(50),
              length2: setSize(90),
              // align: "right",
            },
            label: {
              position:'outer',
              alignTo:'edge',
              formatter: function (params) {
                var str = "";
                switch (params.name) {
                  case "直接通过":
                    str =
                      "{a|}{nameStyle|直接通过 }\n" +
                      "{rate|" +
                      params.value +
                      "%}\n";
                    break;
                  case "一次等待":
                    str =
                      "{b|}{nameStyle|一次等待 }\n" +
                      "{rate|" +
                      params.value +
                      "%}\n";
                    break;
                  case "二次等待":
                    str =
                      "{c|}{nameStyle|二次等待 }\n" +
                      "{rate|" +
                      params.value +
                      "%}\n";
                    break;
                }
                return str;
              },
              padding: [0, setSize(-100)],
              height: setSize(100),
              rich: {
                a: {
                  width: setSize(5),
                  height: setSize(50),
                  backgroundColor: {
                    image: green,
                  },
                  align: "left",
                },
                b: {
                  width: setSize(5),
                  height: setSize(50),
                  backgroundColor: {
                    image: blue,
                  },
                  align: "left",
                },
                c: {
                  width: setSize(5),
                  height: setSize(50),
                  backgroundColor: {
                    image: yellow,
                  },
                  align: "left",
                },
                nameStyle: {
                  fontSize: setSize(12),
                  lineHeight: setSize(12),
                  color: "#fff",
                  verticalAlign: "top",
                  align: "right",
                  padding: [0, 0, 0, setSize(20)],
                },
                rate: {
                  fontSize: setSize(24),
                  lineHeight: setSize(25),
                  fontFamily: "Microsoft YaHei",
                  color: "#fff",
                  align: "right",
                  verticalAlign: "top",
                  padding: [setSize(-24), setSize(2), 0, setSize(10)],
                },
              },
            },
            data: [
              {
                value: 0,
                name: "一次等待",
                itemStyle: {
                  color: "#01FFFF",
                },
                labelLine: {
                  lineStyle: {
                    color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
                      {
                        offset: 0,
                        color: "rgba(1, 255, 255, 0)",
                      },
                      {
                        offset: 1,
                        color: "rgba(1, 255, 255, 1)",
                      },
                    ]),
                  },
                },
              },
              {
                value: 0,
                name: "二次等待",
                itemStyle: {
                  color: "#FCD920",
                },
                labelLine: {
                  lineStyle: {
                    color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
                      {
                        offset: 0,
                        color: "rgba(252, 217, 32, 0)",
                      },
                      {
                        offset: 1,
                        color: "rgba(252, 217, 32, 1)",
                      },
                    ]),
                  },
                },
              },
              {
                value: 0,
                name: "直接通过",
                itemStyle: {
                  color: "#00FFA3",
                },
                labelLine: {
                  lineStyle: {
                    color: new echarts.graphic.LinearGradient(1, 0, 0, 0, [
                      {
                        offset: 0,
                        color: "rgba(0, 255, 163, 0)",
                      },
                      {
                        offset: 1,
                        color: "rgba(0, 255, 163, 1)",
                      },
                    ]),
                  },
                },
              },
            ],
          },
          // 边框的设置
          {
            radius: ["46%", "55%"],
            center: ["50%", "55%"],
            type: "pie",
            z: 2,
            label: {
              show: false,
            },
            labelLine: {
              show: false,
            },
            itemStyle: {
              color: "rgba(6, 27, 71, 0.7)",
            },
            data: [
              {
                value: 1,
              },
            ],
          },
        ],
      }
    }
  },

  methods: {
    fetchOptions() {
      road_cross_relation_list().then((res) => {
        if (res.code === 200) {
          let roadOptions = res.data.map(item => {
            return { 
              label: item.crossroad_name,
              value: item.crossroad_name,
            }
          })
          this.options = [{ value: "total", label: "全部路段" }, ...roadOptions]
        }
      });
    },

    async fetchDataCross() {
      const res = await road_wait_num({ "crossroad_name": this.value })
      if (res.code === 200) {
        this.chartOptions.series[0].data[0].value = Number(res.data[0].cnt_ratio).toFixed(2)
        this.chartOptions.series[0].data[1].value = Number(res.data[1].cnt_ratio).toFixed(2)
        this.chartOptions.series[0].data[2].value = Number(res.data[2].cnt_ratio).toFixed(2)
      }
    }
  },

  mounted() {
    this.fetchOptions()
    this.fetchDataCross()
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
  margin-left: 15px;
  margin-top: 15px;
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
  width: 150px;
  margin-right: 22px;
}
.input2 {
  width: 140px;
}
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

.config {
  margin-top: 2px;
  margin-left: 320px;
  color: rgba(51, 153, 255, 0.8);
  span {
    margin-right: 6px;
  }
  z-index: 1;
}
</style>