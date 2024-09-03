<template>
    <div class="scatter-wrap">
        <div ref='chart' style="width:100%; height:100%"></div>
    </div>
</template>

<script>
import * as echarts from 'echarts'
import {setSize} from '../../assets/js/echartsSetSize'
export default {
    data() {
        return {
            chart: null
        }
    },
    mounted() {
        this.__initChart()
    },
    methods: {
        __initChart() {
            this.chart = echarts.init(this.$refs.chart)
            var reslult = [{
                    name: "汽油车",
                    value: 0,
                    color: "#20F397",
                    position: [30, 100],
                    fontSize: "12",
                    number: "2",
                    symbol: ''
                },
                {
                    name: "自行车",
                    value: 40,
                    color: "#FEF009",
                    position: [50, 80],
                    fontSize: "12",
                    number: "4",
                    symbol: `image://${require('../../assets/img/green-circle.png')}`
                },
                {
                    name: "电动汽车",
                    value: 80,
                    color: "#02DDDF",
                    position: [28, 42],
                    number: "2",
                    fontSize: 12,
                    symbol: `image://${require('../../assets/img/blue-circle.png')}`
                },
                {
                    name: "无人巴士",
                    value: 70,
                    color: "#FF0000",
                    position: [75, 58],
                    number: "4",
                    fontSize: "12",
                    symbol: `image://${require('../../assets/img/red-circle.png')}`
                },
                {
                    name: "电动自行车",
                    value: 60,
                    color: "#00FFFF",
                    position: [60, 15],
                    fontSize: "12",
                    number: "3",
                    symbol: `image://${require('../../assets/img/blue-icon-1.png')}`
                }
            ];
            let option = {
                    grid: {
                        show: false,
                        top: setSize(10),
                        bottom: setSize(10),
                    },
                    tooltip: {
                        show: true
                    },
                    xAxis: [{
                        gridIndex: 0,
                        type: "value",
                        show: false,
                        min: 0,
                        max: 100,
                        nameLocation: "middle",
                        nameGap: 5,
                    }, ],
                    yAxis: [{
                        gridIndex: 0,
                        min: 0,
                        show: false,
                        max: 100,
                        nameLocation: "middle",
                        nameGap: 30,
                    }, ],

                    series: [{
                        type: "scatter",
                        symbol: "circle",
                        label: {
                            normal: {
                                show: true,
                                formatter: "{b}",
                                color: "#fff",
                                textStyle: {
                                    fontSize: setSize(12),
                                },
                            },
                        },
                        animationDurationUpdate: 500,
                        animationEasingUpdate: 500,
                        animationDelay: function(idx) {
                            // 越往后的数据延迟越大
                            return idx * 100;
                        },
                        data: reslult.map(x => ({
                            value: x.position, // 圆的位置[x, y]
                            symbolSize: setSize(x.value), // 元的大小
                            symbol: x.symbol, // 自定义图片
                            name: x.name, // 名称
                            fontSize: setSize(10)
                        })),
                    }, 
                ]
            }
            this.chart.setOption(option)
        }
    }
}
</script>

<style lang="less" scoped>
.scatter-wrap {
    height: 100%;
}
</style>