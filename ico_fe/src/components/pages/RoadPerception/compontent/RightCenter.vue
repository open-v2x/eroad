<template>
    <div>
        <div class="title">
            <span class="caption">路口拥堵排行</span>
        </div>
        <div class="trafficJam-chart" ref="RightCenter"></div>
        <button class="see" :class="see ? 'seeReport' : ''" @click="window">查看报告</button>
    </div>
</template>

<script>
import * as echarts from 'echarts'
import {setSize} from '../../../../assets/js/echartsSetSize'
import { queryByChannelName } from '../../../../assets/js/api_video'
let myChart
export default{
    data(){
        return{
            chartList:['双文街与文营路路口','甘棠街与崇文北路路口','乐民街与佳泰路路口','悦容街与善容路路口','乐安街与富家路路口'],
            trafficJamList:[9.0,8.5,4.5,3.2,1.2],
            attribute:{},
        }
    },
    mounted(){
        myChart = echarts.init(this.$refs.RightCenter);
        this.query((data)=>{
            if(data.length > 0){
                myChart.clear();
                this.chartList = [];
                this.trafficJamList = [];
                data = data.sort((a,b) => {
                    return b.tci-a.tci
                })
                data.forEach(element => {
                    this.chartList.push(element.crossroad_name);
                    this.trafficJamList.push(element.tci);
                    this.attribute[element.crossroad_name] = element;
                })
                let lineY = [];
                for (var i = 0; i < this.chartList.length; i++) {
                    var color = this.getColor(this.trafficJamList[i]);
                    var data = {
                        name: this.chartList[i],
                        color: '#01FFFF',
                        value: this.trafficJamList[i],
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
                                            color: 'rgba(' + color + ',0.2)'
                                        },
                                        {
                                            offset: 1,
                                            color: 'rgba(' + color + ',1)',
                                        },
                                    ],
                                    false
                                ),
                                barBorderRadius: 10,
                            },
                            emphasis: {
                                shadowBlur: setSize(15),
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
                        left: "5%",
                        right: "20%",
                        bottom: "0%",
                    },
                    color: '#01FFFF',
                    yAxis: [
                        {
                            inverse: true,
                            type: "category",
                            position:'right',
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
                                    color: (val)=>{
                                        return 'rgba(' + this.getColor(val) + ',1)';
                                    },
                                    fontSize: setSize(22),
                                    fontWeight:'bold'
                                },
                                formatter: function (val) {
                                    return `${val}`;
                                },
                            },
                            splitArea: {
                                show: false,
                            },
                            splitLine: {
                                show: false,
                            },
                            data: this.trafficJamList,
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
                            name: "路口拥堵指数",
                            type: "bar",
                            barWidth: setSize(10),
                            //xAxisIndex: 0,
                            //yAxisIndex: 1,

                            data: [10, 10, 10, 10, 10],
                            itemStyle: {
                                normal: {
                                    color:'transparent',
                                },
                            },
                            zlevel: 1,
                            showBackground: true,
                            backgroundStyle: {
                                borderWidth:1,
                                borderRadius: 10,
                                borderColor:new echarts.graphic.LinearGradient(
                                    0,
                                    0,
                                    1,
                                    0,
                                    [
                                        {
                                            offset: 0,
                                            color: 'rgba(0,255,0,0.3)',
                                        },
                                        {
                                            offset: 1,
                                            color: 'rgba(0,255,0,1)',
                                        },
                                    ],
                                    false
                                ),
                                color: 'transparent'
                            },
                        },
                        {
                            name: "外圆",
                            type: "scatter",
                            hoverAnimation: false,
                            symbolOffset: [0, setSize(1.5)],
                            data: this.trafficJamList,
                            symbolSize: setSize(6),
                            //position:[0,12],
                            itemStyle: {
                                normal: {
                                    borderColor: "#fff",
                                    borderWidth: setSize(1),
                                    color:(params) =>{

                                        return new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                                            {
                                            offset: 0,
                                            color: "rgba(" + this.getColor(params.data)+ ",1)",
                                            },
                                            {
                                            offset: 1,
                                            color: "rgba(" + this.getColor(params.data)+ ",1)",
                                            },
                                        ])
                                    },
                                    opacity: 1,
                                },
                            },
                            zlevel: 2,
                            z: 3,
                        },
                        {
                            realtimeSort: true,
                            name: "",
                            type: "bar",
                            zlevel: 2,
                            barWidth: setSize(4),
                            data: lineY,
                            barGap: "-70%",
                            animationDuration: 1500,
                            showAllSymbol: true,
                            z: 2,
                            label: {
                                normal: {
                                    color: "#fff",
                                    show: true,
                                    position: [0, -setSize(20)],
                                    textStyle: {
                                        fontSize: setSize(14),
                                        fontWeight:'normal',
                                    },
                                    rich:{
                                        a:{
                                            fontSize:setSize(14),
                                            fontWeight:'bold',
                                            color:'rgba(0,255,0)'
                                        },
                                        b:{
                                            fontSize:setSize(14),
                                            fontWeight:'bold',
                                            color:'rgba(255,255,0)'
                                        },
                                        c:{
                                            fontSize:setSize(14),
                                            fontWeight:'bold',
                                            color:'rgba(255,153,0)'
                                        },
                                        d:{
                                            fontSize:setSize(14),
                                            fontWeight:'bold',
                                            color:'rgba(255,0,0)'
                                        }
                                    },
                                    formatter: function (a, b) {
                                        let indexValue = Number(a.value);
                                        if(indexValue<2.75){
                                            return `{a|TOP.${j++}  }`  + a.name;
                                        }else if(indexValue<5){
                                            return `{b|TOP.${j++}  }`  + a.name;
                                        }else if(indexValue<7.25){
                                            return `{c|TOP.${j++}  }`  + a.name;
                                        }else{
                                            return `{d|TOP.${j++}  }`  + a.name;
                                        }
                                    },
                                },
                            },
                        },
                    ],
                    animationEasing: "cubicOut",
                };


                myChart.setOption(option);
                //myChart.resize();
            }
        })
        myChart.on('dblclick', (params) => {
            //this.inToIntersection(params.name);
        });
    },
    unmounted(){
        if(!myChart)return
        this.$bus.off('ads_sc_rad_road_tci_10min')
        myChart.dispose()
        myChart = null
    },
    methods:{
        inToIntersection(name){
            let n = name.split('-');
            let channelName = n[0] + '与' + n[1];
            queryByChannelName({'channelName':channelName}).then(res => {
                console.log(channelName,res)
                var list = res.list;
                //this.$bus.emit('isShow', {'videoList':list,'attribute':{'lng':this.attribute[name].longitude,'lat':this.attribute[name].latitude,'crossName':channelName + '交叉口'}});
                localStorage.setItem('intersection_data', JSON.stringify({'videoList':list,'attribute':{'lng':this.attribute[name].crossroad_lon,'lat':this.attribute[name].crossroad_lat,'crossName':channelName + '交叉口'}}))
                this.$router.push('/holographicIntersection');
            })
        },
        query(callback){
            this.$bus.on('ads_sc_rad_road_tci_10min',res => {
                callback(res.data);
            })
        },
        getTime(){
            var today = new Date();
            var m = Math.floor(today.getMinutes()/10)*10-10;
            var h = today.getHours();
            if(m<0){
                h -= 1;
                m = 50;
            }//todo h<0
            m = m<10?'0'+m:m;
            h = h<10?'0'+h:h;
            return today.toJSON().split('T').join(' ').split(' ')[0] + ' ' + h +':'+m + ':' + '00'
        },
        getColor(indexValue){
            if(indexValue<2.75){
                return '0,255,0'
            }else if(indexValue<5){
                return '255,255,0'
            }else if(indexValue<7.25){
                return '255,153,0'
            }else{
                return '255,0,0'
            }
        },
        getRich(indexValue){
            if(indexValue<2.75){
                return '79,210,125'
            }else if(indexValue<5){
                return '255,208,69'
            }else if(indexValue<7.25){
                return '232,14,14'
            }else{
                return '180,0,0'
            }
        },
        window(){
            this.$parent.$refs.pop1.see = false;
            this.$parent.$refs.pop2.see = !this.$parent.$refs.pop2.see;
            //this.$bus.emit('windowShow',this.see)
        }
    }
}
</script>

<style lang="less" scoped>
    .trafficJam-chart{
        height: 100%;
        width: 100%;
    }
    .see {
      position: absolute;
      top: 40px;
      right: 20px;
      padding:0 5px;
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
    .see:hover{
        cursor: pointer;
    }
    .seeReport{
      background-color: #3399FF;
      color: #fff;
    }
</style>
