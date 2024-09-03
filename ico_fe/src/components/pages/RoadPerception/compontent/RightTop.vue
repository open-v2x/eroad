<template>
  <div class="main">
    <div class="title">
      <span class="caption">当前拥堵指数</span>
    </div>
    <div class="cell-2">
      <div class="myChart" ref="RightTop"></div>
      <div class="cell-index">
        <div>
          <div :class="imgclass[1]"></div>
          <div>
            <span>环比</span>
            <span>{{ h_ratio }}</span>
          </div>
        </div>
        <div>
          <div :class="imgclass[0]"></div>
          <div>
            <span>同比</span>
            <span>{{ t_ratio }}</span>
          </div>
        </div>
      </div>

      <!-- <div class="hotPic"></div> -->
    </div>
    <div class="cell-3">
      <span class="explain-text" :style="{
        color: explain.color,
        left: explain.left
      }">{{ explain.text }}</span>
      <div class="hot-bar"></div>
    </div>
  </div>
</template>

<script>
var labelItemArr = [];
import * as echarts from 'echarts'
import { ads_sc_rad_tci_10min } from '../../../../assets/js/api'
import { setSize } from '../../../../assets/js/echartsSetSize'
let myChart
export default {
  data() {
    return {
      h_ratio: 0,
      t_ratio: 0,
      indexValue: [1.2],
      imgclass: ['downclass', 'downclass'],
      explain: {
        text: '顺畅',
        color: '#4FD27D',
        left: '6%'
      },
    }
  },
  mounted() {
    myChart = echarts.init(this.$refs.RightTop);
    this.$bus.on('resize',()=>{
      myChart.resize()
    })
    var option = {
      grid: {
        top: setSize(0),
        left: setSize(0),
        right: setSize(0),
        bottom: setSize(0),
        containLabel: true
      },
      backgroundColor: 'transparent',
      title: {
        show: false,
        text: this.explain.text,
        left: 'center',
        bottom: setSize(20),
        textStyle: {
          color: this.explain.color,
          fontSize: setSize(18),
        },
      },
      series: [
        {
          data: this.indexValue,
          min: 0, // 最小的数据值,默认 0 。映射到 minAngle。
          max: 10, // 最大的数据值,默认 100 。映射到 maxAngle。
          name: '最外层',
          type: 'gauge',
          roundCap: true,
          radius: '60%',
          center: ['50%', '50%'],
          splitNumber: 0, //刻度数量
          startAngle: 225,
          endAngle: -45,
          z: 4,
          zlevel: 0,
          axisLine: {
            show: true,
            // roundCap: true,
            lineStyle: {
              // 轴线样式
              width: setSize(6), // 宽度
              color: [
                [
                  1 / 5,
                  new echarts.graphic.LinearGradient(0, 1, 0, 0, [
                    {
                      offset: 0,
                      color: "rgba(0, 128, 0, 1)",
                    },
                    {
                      offset: 1,
                      color: "rgba(0, 128, 0, 1)",
                    },
                  ])
                ],
                [2 / 5, new echarts.graphic.LinearGradient(0, 1, 0, 0, [
                  {
                    offset: 0,
                    color: "rgba(153, 204, 0, 1)",
                  },
                  {
                    offset: 1,
                    color: "rgba(153, 204, 0, 1)",
                  },
                ])
                ],
                [3 / 5, new echarts.graphic.LinearGradient(0, 0, 1, 0, [
                  {
                    offset: 0,
                    color: "rgba(255, 255, 0, 1)",
                  },
                  {
                    offset: 1,
                    color: "rgba(255, 255, 0, 1)",
                  },
                ])
                ],
                [4 / 5, new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                  {
                    offset: 0,
                    color: "rgba(255, 153, 0, 1)",
                  },
                  {
                    offset: 1,
                    color: "rgba(255, 153, 0, 1)",
                  },
                ])
                ],
                [1, new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                  {
                    offset: 0,
                    color: "rgba(255, 0, 0, 1)",
                  },
                  {
                    offset: 1,
                    color: "rgba(255, 0, 0, 1)",
                  },
                ])
                ],
              ],
            },
          },
          //分隔线样式
          splitLine: {
            show: false,
          },
          axisLabel: {
            show: false,
          },
          axisTick: {
            show: false,
          },
          anchor: {
            show: false,
            size: setSize(10),
            showAbove: true,
            itemStyle: {
              color: '#0e1327',
            },
          },
          pointer: {
            icon:
              'path://M2090.36389,615.30999 L2090.36389,615.30999 C2091.48372,615.30999 2092.40383,616.194028 2092.44859,617.312956 L2096.90698,728.755929 C2097.05155,732.369577 2094.2393,735.416212 2090.62566,735.56078 C2090.53845,735.564269 2090.45117,735.566014 2090.36389,735.566014 L2090.36389,735.566014 C2086.74736,735.566014 2083.81557,732.63423 2083.81557,729.017692 C2083.81557,728.930412 2083.81732,728.84314 2083.82081,728.755929 L2088.2792,617.312956 C2088.32396,616.194028 2089.24407,615.30999 2090.36389,615.30999 Z',
            show: true,
            length: '95%',
            width: setSize(6),
            offsetCenter: [0, '10%'],
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 1, 0, 0, [
                {
                  offset: 0,
                  color: 'rgba(1, 255, 255, 0.7)',
                },
                {
                  offset: 1,
                  color: 'rgba(1, 255, 255, 1)',
                },
              ]),
            },
          },
          detail: {
            color: this.explain.color,
            fontSize: setSize(18),
            offsetCenter: [0, '60%'],
          },
        },

        {
          name: '内部细刻度',
          type: 'gauge',
          center: ['50%', '50%'],
          radius: '100%',
          startAngle: 225,
          endAngle: -45,
          axisLine: {
            show: true,
            lineStyle: {
              width: setSize(24),
              color: [
                [
                  1 / 4,
                  new echarts.graphic.LinearGradient(0, 1, 0, 0, [
                    {
                      offset: 0,
                      color: "rgba(1, 255, 255, 0)",
                    },
                    {
                      offset: 1,
                      color: "rgba(1, 255, 255, 0.075)",
                    },
                  ])
                ],
                [1 / 2, new echarts.graphic.LinearGradient(0, 0, 1, 0, [
                  {
                    offset: 0,
                    color: "rgba(1, 255, 255, 0.075)",
                  },
                  {
                    offset: 1,
                    color: "rgba(1, 255, 255, 0.15)",
                  },
                ])
                ],
                [3 / 4, new echarts.graphic.LinearGradient(0, 0, 1, 0, [
                  {
                    offset: 0,
                    color: "rgba(1, 255, 255, 0.15)",
                  },
                  {
                    offset: 1,
                    color: "rgba(1, 255, 255, 0.225)",
                  },
                ])
                ],
                [1, new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                  {
                    offset: 0,
                    color: "rgba(1, 255, 255, 0.225)",
                  },
                  {
                    offset: 1,
                    color: "rgba(1, 255, 255, 0.3)",
                  },
                ])
                ],
              ],
            },
          }, //仪表盘轴线
          axisTick: {
            show: true,
            distance: 0,
            lineStyle: {
              color: "rgba(1, 255, 255, 0.3)",
              width: setSize(1),
            },
            length: setSize(-8),
          },
          //刻度线文字
          axisLabel: {
            show: false,
          },
          splitLine: {
            show: true,
            distance: 0,
            length: setSize(-12),
            lineStyle: {
              color: "rgba(1, 255, 255, 0.3)",
              width: setSize(1.5)
            },
          },
          detail: {
            show: false,
          },
          pointer: {
            show: false,
          },
        },
        {

          name: "ring",
          type: 'custom',
          coordinateSystem: "none",
          renderItem: function (params, api) {
            return {
              type: 'arc',
              shape: {
                cx: api.getWidth() / 2,
                cy: api.getHeight() / 2,
                r: Math.min(api.getWidth(), api.getHeight()) / 2 * 0.98,
                startAngle: (-225) * Math.PI / 180,
                endAngle: (45) * Math.PI / 180
              },
              style: {
                stroke: "rgba(1, 255, 255, 0.3)",
                fill: "transparent",
                lineWidth: setSize(1)
              },
              silent: true
            };
          },
          data: [0]
        },
        {

          name: "ring",
          type: 'custom',
          coordinateSystem: "none",
          renderItem: function (params, api) {
            return {
              type: 'arc',
              shape: {
                cx: api.getWidth() / 2,
                cy: api.getHeight() / 2,
                r: Math.min(api.getWidth(), api.getHeight()) / 2 * 0.73,
                startAngle: (-225) * Math.PI / 180,
                endAngle: (45) * Math.PI / 180
              },
              style: {
                stroke: "rgba(1, 255, 255, 0.3)",
                fill: "transparent",
                lineWidth: setSize(1)
              },
              silent: true
            };
          },
          data: [0]
        },
      ],
    };


    this.$bus.on('ads_sc_rad_tci_10min', res => {
      console.log(res, '----res----');
      this.t_ratio = Math.abs(res.data[0].t_ratio);
      res.data[0].t_ratio < 0 ? this.imgclass[0] = 'downclass' : this.imgclass[0] = 'upclass'
      this.h_ratio = Math.abs(res.data[0].h_ratio);
      res.data[0].h_ratio < 0 ? this.imgclass[1] = 'downclass' : this.imgclass[1] = 'upclass'
      this.indexValue = [res.data[0].tci];
      option.series[0].data = [res.data[0].tci];
      myChart.setOption(option);
      myChart.resize();

      if (this.indexValue < 2) {
        this.explain.text = '畅通';
        this.explain.color = 'rgba(0, 128, 0, 1)';
        this.explain.left = '6%';

      } else if (this.indexValue < 4) {
        this.explain.text = '基本畅通';
        this.explain.color = 'rgba(153, 204, 0, 1)';
        this.explain.left = '22%';
      } else if (this.indexValue < 6) {
        this.explain.text = '轻度拥堵';
        this.explain.color = 'rgba(255, 255, 0, 1)';
        this.explain.left = '41%';
      } else if (this.indexValue < 8) {
        this.explain.text = '中度拥堵';
        this.explain.color = 'rgba(255, 153, 0, 1)';
        this.explain.left = '61%';
      } else {
        this.explain.text = '严重拥堵';
        this.explain.color = 'rgba(255, 0, 0, 1)';
        this.explain.left = '81%';
      }

    })


  },
  unmounted() {
    if (!myChart) return
    this.$bus.off('ads_sc_rad_tci_10min')
    this.$bus.off('resize')
    myChart.dispose()
    myChart = null
  },
}
</script>
<style lang="less" scoped>
@height: 10.8vh;


.cell-2 {
  margin: 12px;
  display: flex;
  flex-wrap: wrap;
  flex-direction: row;
  justify-content: center;
  align-items: center;
  // height: (232 / @height);
  width: 376px;

  .myChart {
    width: 176px;
    height: 176px;

    margin-right: 32px;
  }

  .cell-index {
    margin: 0;
    width: 106px;
    height: 134px;
    display: flex;
    flex-direction: column;

    >div {
      //红
      flex: 1;
      display: flex;
      flex-direction: row;
      align-items: center;

      >div:first-child {
        width: 56px;
        height: 56px;
      }

      >div:last-child {
        display: flex;
        flex-direction: column;
        margin: auto;

        >span:first-child {
          color: white;
          font-size: 12px;
        }

        >span:last-child {
          color: white;
          font-size: 20px;
        }

      }

    }
  }
}

.cell-3 {
  position: absolute;
  width: 352px;
  height: 85px;
  top: (200/@height);
  left: 50%;
  transform: translate(-50%, 0);

  .hot-bar {
    position: relative;
    top: 25px;
    width: 100%;
    height: 28px;
    background: url('../../../../assets/img/Group 1000002340.png') no-repeat;
    background-size: 100% 100%;

  }

  .explain-text {
    position: relative;
    font-size: 14px;
    top: 30px;
    //margin-bottom: 15px;
  }
}

.upclass {
  background: url('../../../../assets/img/up-bound.png') no-repeat;
  background-size: 100% 100%;
}

.downclass {
  background: url('../../../../assets/img/down-bound.png') no-repeat;
  background-size: 100% 100%;
}

.main {
  position: relative;
}











</style>
