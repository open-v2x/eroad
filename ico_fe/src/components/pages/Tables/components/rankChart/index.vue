<template>
  <div class="selection">
    <selectButtons  :mode="flags" @changeTimeQuantumActive="changeTimeQuantumActive">
      <template #left> 倒序排名 </template>
      <template #right> 正序排名 </template>
    </selectButtons>
    <span class="left">指标类型</span>
    <el-select v-model="categoryValue" class="input1" placeholder="请选择指标" @change="fetchDataCross">
      <el-option
        v-for="item in categoryOptions" 
        :key="item.value"
        :label="item.label"
        :value="item.value"
      />
    </el-select>
    <span>特殊时段</span>
    <el-select v-model="timeValue" class="input2" placeholder="请选择时段" @change="fetchDataCross">
      <el-option
        v-for="item in timeOptions" 
        :key="item.value"
        :label="item.label"
        :value="item.value"
      />
    </el-select>
  </div>
  <div style="width: 100%; height: 100%">
    <echarts-chart :options="options"/>
  </div>
</template>

<script>
import echartsChart from '../echartsChart.vue'
import selectButtons from './selectButtons.vue'
import { cross_prop, cross_prop_m_n } from '../../api'
import { setSize } from '@/utils/setSize'
import * as echarts from 'echarts'
export default {
  name: 'rankChart',
  components: {
    echartsChart,
    selectButtons,
  },
  data() {
    return {
      timeQuantumActive: false,
      categoryValue: 1,
      categoryOptions: [{value:1,label:'通行效率'},{value:2,label:'饱和度'},{value:3,label:'行程时间比'},{value:4,label:'延误时间比'}],
      timeValue: '',
      timeOptions: [{value:'',label:'无'},{value:'早高峰',label:'早高峰'},{value:'晚高峰',label:'晚高峰'}],
      options: {
        grid: {
          top: setSize(20),
          left: setSize(24),
          bottom: setSize(10),
          right: setSize(24),
        },
        tooltip: {
          trigger: "item",
          backgroundColor: "rgba(6, 27, 71, 0.7)",
          textStyle: {
            color: "#fff",
          },
          borderWidth: "2",
          borderColor: "rgba(1, 255, 255, 1)",
          axisPointer: {
            type: "shadow",
          },
        },
        yAxis: [
          {
            type: "category",
            inverse: true,
            data: [],
            axisTick: {
              show: false,
            },
            axisLine: {
              show: false,
            },
            axisLabel: {
              show:false,
              textStyle: {
                fontSize: setSize(14),
                color: "#ddd",
                padding: [100, 0, 1000, setSize(-110)],
                align: "center",
              },
              margin: 30,
            },
            interval: 1,
          },
        ],
        xAxis: [
          {
            show: false,
            type: "value",
          },
        ],
        series: [
          {
            type: "bar",
            showBackground: true,
            barWidth:setSize(6),
            label:{
              show:true,
              position:'left',
              color:'rgba(230, 247, 255, 1)',
              align:'left',
              offset:[setSize(6),setSize(-14)],
              fontSize: setSize(14),
              formatter:(param)=>{
                return `{a|No.${param.dataIndex+1}}   ${param.name}`
              },
              rich:{
                a: {
                    color: 'rgba(1, 255, 255, 1)',
                    fontSize: setSize(14)
                },
              }
            },
            barGap: "1%", // Make series be overlap
            itemStyle: {
                borderRadius: setSize(4), 
                color:new echarts.graphic.LinearGradient(1, 0, 0, 0, [
                  {
                    offset: 0,
                    color: "rgba(1, 255, 255, 1)",
                  },
                  {
                    offset: 1,
                    color: "rgba(25, 159, 255, 1)",
                  }
                ]),
                opacity: 0.8,
            },
            data: []
          },
          {
            // 背景
            type: "pictorialBar",
            symbolRepeat: "fixed",
            label: {
              show: true,
              position: 'right',
              align:'right',
              offset:[setSize(-6),setSize(-12)],
              color: "rgba(230, 247, 255, 1)",
              fontSize: setSize(14),
              formatter: function (val) {
                return `${val.value}`;
              },
            },
            itemStyle: {
              color: "transparent",
            },
            data: []
          },
        ],
      }
    }
  },

  methods: {
    changeTimeQuantumActive(bool) {
      this.timeQuantumActive = bool;
      this.fetchDataCross()
    },

    fetchDataCross() {
      let apiUrl = null
      if (!this.timeValue) {
        apiUrl = cross_prop
      } else {
        apiUrl = cross_prop_m_n
      }
      apiUrl({
        "start_date": "2024-06-10",
        "end_date": "2024-06-16",
        "special_time": this.timeValue || null
      }).then(res=>{
        if(res.code === 200){
          const mapping = {
            1: 'tra_efficiency', // '通行效率'
            2: 'saturation', // '饱和度'
            3: 'tra_time_index', // '行程时间比'
            4: 'delay_time_prop', // '延误时间比'
          };

          let data = res.data.splice(0, 15);

          // 排序的比较函数
          const getCompareFunction = (isAscending) => {
            return (a, b) => {
              const key = mapping[this.categoryValue];
              const valueA = parseFloat(a[key]);
              const valueB = parseFloat(b[key]);

              if (isAscending) {
                return valueA - valueB; // 升序
              } else {
                return valueB - valueA; // 降序
              }
            };
          };

          // 用于判断排序方式：timeQuantumActive 为 true 使用升序，为 false 使用降序
          const compare = getCompareFunction(this.timeQuantumActive);

          // 对数据进行排序
          data.sort(compare);

          this.options.yAxis[0].data = data.map(item => item.cross_name);
          this.options.series[0].data = data.map(item => item[mapping[this.categoryValue]].toFixed(2));
        }
      })
    }
  },

  mounted() {
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
  width: 120px;
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
  display: flex;
  justify-content: space-between;
}
</style>