<template>
    <div>
        <div class="title">
            <span class="caption">分时段流量统计</span>
        </div>
        <div class="cell-2">
            <!-- <select class="road-select">
                <option value ="乐民街">乐民街</option>
                <option value ="乐安北一街">乐安北一街</option>
                <option value="金湖街">金湖街</option>
                <option value="崇文北路">崇文北路</option>
            </select> -->
        </div>
        <div  ref="LeftBottom" style="width:100%;height:100%"/>
    </div> 
</template>

<script>
    import * as echarts from 'echarts'
    //import {rad_tra_flow_10min} from '../../../../assets/js/api'
    import {setSize} from '../../../../assets/js/echartsSetSize'
    let myChart;
    const areaData = [
        [
            {
                name: '早高峰',
                xAxis: '07:00:00'
            },
            {
                xAxis: '09:00:00'
            }
        ],
        [
            {
                name: '晚高峰',
                xAxis: '17:00:00'
            },
            {
                xAxis: '20:00:00'
            }
        ]
    ];
    export default{
        data(){
            return{
                topOption : {                   
                    grid:{
                        top:setSize(40),
                        left:setSize(15),
                        right:setSize(25),
                        bottom:setSize(15),
                        containLabel:true
                    },
                      tooltip: {
                        trigger: 'axis',
                        axisPointer: {
                        type: 'cross'
                        }
                    },
                    legend: {
                        selectedMode:false,
                        icon:'none',
                        left: setSize(-16),
                        data:['车流量'],
                        textStyle:{
                            fontSize:setSize(14),
                            color:'#fff'
                        },
                        formatter:'单位：车次'
                    },
                    toolbox: {
                        show: true,
                        feature: {
                        }
                    },
                    dataZoom: [
                        {
                            type: 'inside',
                            start: 0,
                            end: 100
                        },
                        {
                            type: 'slider',
                            start: 0,
                            end: 100
                        }
                    ],
                    xAxis: {
                        type: 'category',
                        axisLabel: {
                            inside: false,
                            textStyle: {
                                color: '#fff',
                                fontSize: setSize(12),
                            }
                        },
                        data: ['0:00', '2:00','4:00', '6:00','8:00', '10:00','12:00', '14:00','16:00', '18:00','20:00','22:00']
                    },
                    yAxis: {
                        type: 'value',
                        axisLabel: {
                            inside: false,
                            textStyle: {
                                color: '#fff',
                                fontSize: setSize(12),
                            }
                        },
                    },
                    series: [
                    {
                        data: [203, 250, 132, 523, 1290, 1330,958,758,856,1353,1420,932,856,520],
                        markArea: {
                            itemStyle: {
                                //color: 'rgba(255, 173, 177, 0.4)'
                                color: new echarts.graphic.LinearGradient(
                                    0, 0, 0, 1,
                                    [    
                                        { offset: 0, color: "rgba(129, 241, 147,0.6)" },
                                        { offset: 0.5, color: "rgba(129, 241, 147,0.3)" },
                                        { offset: 1, color: "rgba(129, 241, 147,0.1)" },
                                    ]
                                )
                            },
                            data: [
                                [
                                    {
                                        name: '早高峰',
                                        xAxis: '07:00:00'
                                    },
                                    {
                                        xAxis: '09:00:00'
                                    }
                                ],
                                [
                                    {
                                        name: '晚高峰',
                                        xAxis: '17:00:00'
                                    },
                                    {
                                        xAxis: '20:00:00'
                                    }
                                ]
                            ]
                        },
                        type: 'line',
                        name: '车流量',
                        symbol:'none',
                        smooth: true,
                        lineStyle:{
                            color:"rgba(1,255,255,0.8)" //改变折线颜色
                        },
                        areaStyle: {
                            normal: {
                                color: new echarts.graphic.LinearGradient(
                                    0, 0, 0, 1,
                                    [    
                                        { offset: 0, color: "rgba(1,255,255,0.8)" },
                                        { offset: 0.5, color: "rgba(1,255,255,0.4)" },
                                        { offset: 1, color: "rgba(1,255,255,0.1)" },
                                    ]
                                )
                            }
                        }
                    }
                ],
                
            }

            }
        },
        mounted(){
            myChart = echarts.init(this.$refs.LeftBottom);         

            //setInterval(()=>{
                this.$bus.on('rad_tra_flow_10min',res => {
                    myChart.clear();
                    this.topOption.xAxis.data = [];
                    this.topOption.series[0].data = [];
                    var data = res.data;
                    this.checkTime(data);
                    data.forEach((item)=>{
                        this.topOption.xAxis.data.push(item.data_time.substr(item.data_time.length-8));
                        this.topOption.series[0].data.push(item.tra_flow);
                    })
                    myChart.setOption(this.topOption);
                })
            //},600000)

            
        },
        unmounted(){
            if(!myChart)return
            this.$bus.off('rad_tra_flow_10min')
            myChart.dispose()
            myChart = null
        },
        methods:{
            checkTime(data){
                //var today = new Date();
                //var now = today.toJSON().split('T').join(' ').substr(0,19);
                let now = data[data.length -1].data_time;
                let date = now.split(' ')[0];
                let hours = now.split(' ')[1].split(':')[0];//today.getHours();
                let m = now.split(' ')[1].split(':')[1];//Math.floor(today.getMinutes()/10)*10-10;
                if(m<0){
                    hours -= 1;
                    m = 50;
                }//todo h<0
                //m = m<10?'0'+m:m;
                this.topOption.series[0].markArea.data = areaData;
                const s = Number(hours+''+m);
                if(s < 700){
                    this.topOption.series[0].markArea.data[0][0].name = '';
                    this.topOption.series[0].markArea.data[1][0].name = '';
                }else if(s < 900 && s>699){
                    this.topOption.series[0].markArea.data[1][0].name = '';
                    this.topOption.series[0].markArea.data[0][1] = {x:'94%'};
                }else if(s < 1700 && s>899){
                    this.topOption.series[0].markArea.data[1][0].name = '';
                }else if(s < 2000 && s>1699){
                    this.topOption.series[0].markArea.data[1][1] = {x:'94%'};
                }
            }
        }

    }
</script>

<style lang="less" scoped>

    .cell-2{

        margin-top: 5px;
        margin-bottom: 5px;
        margin-left: 15px;
        margin-right: 15px;
        .road-select{
            background: transparent;
            border: 1px solid #01FFFF;
            color: #01FFFF;
            border-radius:2px;
            margin-right: 15px;
            float: right;
        }
        .btn{
            width: 52px;
            height: 24px;
            background: rgba(25, 159, 255, 0.3);                   
            border-radius: 4px 4px 4px 4px;
            opacity: 1;
            border: 1px solid #199FFF;

            font-size: 12px;
            font-family: Noto Sans SC-Regular, Noto Sans SC;
            font-weight: 400;
            color: #FFFFFF;
            line-height: 22px;
            &:first-child{
                box-shadow: inset 0px 0px 4px 1px #199FFF;
            }
            &:last-child {
                margin-left: 15px
            }
        }
    }
</style>