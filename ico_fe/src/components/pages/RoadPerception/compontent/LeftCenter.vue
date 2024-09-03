<template>
  <div>
    <div class="title">
      <span class="caption">路口动态排行</span>
      <div class="checkout">
        <span :class="{ active: !type }" @click="typeChange(0)">流量</span>
        <span :class="{ active: type }" @click="typeChange(1)">拥堵</span>
      </div>
    </div>

    <button class="see" :class="see ? 'seeReport' : ''" @click="window">查看报告</button>
    <div ref="LeftCenter" class="volume-chart"></div>

  </div>
</template>

<script>
import * as echarts from 'echarts'
import { setSize } from '../../../../assets/js/echartsSetSize'
let myChart
export default {
  data() {
    return {
      volumeNameList: ['双文街与文营路路口', '甘棠街与崇文北路路口', '乐民街与佳泰路路口', '悦容街与善容路路口', '乐安街与富家路路口'],
      volumeValueList: [500, 450, 400, 350, 300],
      jamNameList: ['双文街与文营路路口', '甘棠街与崇文北路路口', '乐民街与佳泰路路口', '悦容街与善容路路口', '乐安街与富家路路口'],
      jamValueList: [5.0, 4.5, 4.0, 3.5, 3.0],
      type: 0
    }
  },
  mounted() {
    myChart = echarts.init(this.$refs.LeftCenter);
    myChart.on('click', (params) => {
      this.$bus.emit('protrudeIntersection', params.name)
    });
    this.query(this.setChart);
  },
  unmounted() {
    if (!myChart) return
    this.$bus.off('ads_sc_rad_road_accum_tra_flow')
    this.$bus.off('resize')
    myChart.dispose()
    myChart = null
  },
  methods: {
    query(callback) {
      let data
      this.$bus.on('ads_sc_rad_road_accum_tra_flow', res => {
        data = this.sortVolumeData(res.data)
        this.volumeNameList = []
        this.volumeValueList = []
        data.forEach(element => {
          this.volumeNameList.push(element.crossroad_name);
          this.volumeValueList.push(element.traffic_flow);
        })
        if (!this.type) callback(this.volumeNameList, this.volumeValueList);

      })
      this.$bus.on('ads_sc_rad_road_tci_10min', res => {
        data = this.sortJamData(res.data)
        this.jamNameList = []
        this.jamValueList = []
        data.forEach(element => {
          this.jamNameList.push(element.crossroad_name);
          this.jamValueList.push(element.tci);
        })
        if (this.type) callback(this.jamNameList, this.jamValueList);
      })
    },
    setChart(nameList, valueList) {
      if (nameList.length > 0) {
        if (myChart) myChart.clear();
        let lineY = [];
        for (var i = 0; i < nameList.length; i++) {
          var data = {
            name: nameList[i],
            color: '#01FFFF',
            value: valueList[i],
            itemStyle: {
              normal: {
                show: true,
                color: new echarts.graphic.LinearGradient(
                  0,
                  0,
                  1,
                  0,
                  [
                    {
                      offset: 0,
                      color: 'rgba(25,159,255,1)',
                    },
                    {
                      offset: 1,
                      color: 'rgba(1,255,255,1)',
                    },
                  ],
                  false
                ),
                barBorderRadius: 10,
              },
              emphasis: {
                shadowBlur: 15,
                shadowColor: "rgba(0, 0, 0, 0.1)",
              },
            },
          };
          lineY.push(data);
        }
        var j = 1;
        var option = {
          title: {
            show: false,
          },
          tooltip: {
            trigger: "item",
          },
          grid: {
            borderWidth: 0,
            top: "10%",
            left: "4%",
            right: this.type ? "18%" : "26%",
            bottom: "0%",
          },
          color: '#01FFFF',
          yAxis: [
            {
              inverse: true,
              type: "category",
              position: 'right',
              axisLine: {
                show: false,
              },
              axisTick: {
                show: false,
              },
              axisLabel: {
                show: true,
                inside: false,
                textStyle: {
                  color: "#01FFFF",
                  fontSize: setSize(20),
                  fontWeight: 'bold'
                },
                rich: {
                  a: {
                    fontSize: setSize(12)
                  }
                },
                formatter: (val) => {
                  return this.type ? val : `${val}{a|车次}`;
                },
              },
              splitArea: {
                show: false,
              },
              splitLine: {
                show: false,
              },
              data: valueList,
            },
          ],
          xAxis: {
            type: "value",
            axisTick: {
              show: false,
            },
            axisLine: {
              show: false,
            },
            splitLine: {
              show: false,
            },
            axisLabel: {
              show: false,
            },
          },
          series: [
            {
              realtimeSort: false,
              name: "",
              type: "bar",
              zlevel: 2,
              barWidth: setSize(8),
              data: lineY,
              animationDuration: 1500,
              showBackground: true,
              backgroundStyle: {
                borderRadius: 10,
                color: 'rgba(25, 159, 255, 0.2)'
              },
              label: {
                normal: {
                  color: "#fff",
                  show: true,
                  position: [0, -setSize(20)],
                  textStyle: {
                    fontSize: setSize(14),
                    fontWeight: 'normal',
                  },
                  rich: {
                    a: {
                      fontSize: setSize(14),
                      fontWeight: 'bold',
                      color: '#01FFFF'
                    }
                  },
                  formatter: (a, b) => {
                    return `{a|TOP.${j++}  }` + a.name;
                  },
                },
              },
            },
          ],
          animationEasing: "cubicOut",
        };
        myChart.setOption(option);
        this.$bus.on('resize', () => {
          myChart.resize()
        })
      }
    },
    typeChange(type) {
      this.type = type
      if (type) {
        this.setChart(this.jamNameList, this.jamValueList)
      } else {
        this.setChart(this.volumeNameList, this.volumeValueList)
      }
    },
    sortVolumeData(data) {
      return data.sort((a, b) => {
        return b.traffic_flow - a.traffic_flow
      })
    },
    sortJamData(data) {
      return data.sort((a, b) => {
        return b.tci - a.tci
      })
    },
    window() {
      if (this.type) {
        this.$parent.$refs.pop1.see = false;
        this.$parent.$refs.pop2.see = !this.$parent.$refs.pop2.see;
      } else {
        this.$parent.$refs.pop2.see = false;
        this.$parent.$refs.pop1.see = !this.$parent.$refs.pop1.see;
      }
      //this.$bus.emit('windowShow',this.see)
    }
  }
}
</script>

<style lang="less" scoped>
@height: 10.8vh;

.pop-window {
  position: absolute;
  background: url('../../../../assets/img/Overview/Frame 1000002470.png') no-repeat;
  background-size: 100% 100%;
  width: 600px;
  height: 1000px;
  left: 600px;
  top: 0;
  margin: 0 auto;
  //transform: translate(50%,50%);
  z-index: 99;
}

.volume-chart {
  height: 100%;
  width: 100%;
}

.see {
  position: absolute;
  top: 42px;
  right: 20px;
  padding: 0 5px;
  height: 24px;
  border: 1px solid #3399FF;
  border-radius: 2px;
  font-family: 'Noto Sans SC';
  font-weight: 400;
  font-size: 12px;
  line-height: 20px;
  color: #3399FF;
  background: rgba(0, 0, 51, 0.6);
  z-index: 5;
}

.see:hover {
  cursor: pointer;
}

.seeReport {
  background-color: #3399FF;
  color: #fff;
}

.checkout {
  position: absolute;
  right: 0px;
  top: (8/@height);
  width: 164px;
  height: (30/@height);
  display: flex;
  justify-content: space-between;
  margin-right: 16px;

  span {
    box-sizing: border-box;
    width: 76px;
    border: 2px solid rgba(71, 167, 235, 1);
    border-radius: 4px;
    display: flex;
    justify-content: center;
    align-items: center;
    font-family: 'Noto Sans SC';
    font-style: normal;
    font-weight: 400;
    font-size: 14px;
    line-height: 22px;
    color: rgba(71, 167, 235, 1);

    &:hover {
      cursor: pointer;
    }
  }

  .active {
    border: 1px solid #00FFFF;
    background: linear-gradient(180deg, rgba(40, 241, 255, 0) 0%, rgba(40, 241, 255, 0.45) 100%), rgba(73, 231, 254, 0.15);
    color: #00FFFF
  }
}






</style>
