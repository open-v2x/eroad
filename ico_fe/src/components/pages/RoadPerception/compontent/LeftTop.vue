<template>
    <div>
        <div class="title">
            <span class="caption">当前通行量</span>
        </div>
        <div class="cell-2">
            <div class="cell-2-img1Outer">
                <div class="cell-2-img1">
                    <div class="pic">
                        <span class="valueSpan1">{{trafficVolume.now}}</span>
                        <div class="leftTop" :class="passImageLeft[0]"></div>
                        <div class="leftBottom" :class="passImageLeft[3]"></div>
                        <div class="rightTop" :class="passImageLeft[1]"></div>
                        <div class="rightBottom" :class="passImageLeft[2]"></div>
                    </div>
                </div>
            </div>
            <div class="cell-2-img2Outer">
                <div class="cell-2-img2">
                    <div class="pic">
                      <LeftTopPie/>
                    </div> 
                </div>
            </div>
            <div class="descriptions">
              <span class="span-1">实时通行车次</span>
              <span class="span-1">路口通过率</span>
            </div>
        </div>

    </div>
</template>

<script>
import Socket from '../../../../assets/js/socket'
import LeftTopPie from './LeftTopPie.vue'
export default{
    components:{LeftTopPie},
    data(){
        return{
            trafficVolume :{
                now:0,
                sum:0
            },
            passImageLeft:['small','medium','big','gaint'],
            passImageRight:['bigR','gaintR','smallR','mediumR',],
            passTimer:null
        }
    },
    methods:{
        changePassClass(){
            this.passImageLeft.unshift(this.passImageLeft.pop())
            this.passImageRight.unshift(this.passImageRight.pop())
        }
    },
    mounted(){
        let ws = new Socket({
            'url':window.ioc_index_realtime,
            'heartBeat':1000,
        })
        ws.onmessage((ms)=>{
            if(ms == '连接成功')
                return;
            let data = JSON.parse(ms);
            this.trafficVolume.now = data.car_instant_cnt;
        })
      // 组件销毁时调用，中断websocket链接
      this.over = () => {
        ws.close()
      };
        this.passTimer = setInterval(()=>{
            this.changePassClass()
        },300)

    },
    beforeUnmount(){
      this.over();
        clearInterval(this.passTimer)
    }
}
</script>

<style lang="less" scoped>
@height:10.8vh;
    .cell-2{
        flex: 1;
        margin-bottom: (15/@height);
        display: flex;
        align-items: center;
        .cell-2-img1Outer{
          margin-left: 36px;
        }
        .cell-2-img2Outer{
          flex: 1;
        }
        .descriptions{
          position: absolute;
          width: 364px;
          display: flex;
          flex-direction: row;
          margin-left: 36px;
          bottom: (25/@height);
          span:nth-child(1){
            width: 120px;
          }
          span:nth-child(2){
            flex: 1;
          }
        }
        >div{
            display:flex;
            flex-direction: column;
            text-align: center;
            .span-1{
                font-family: 'Noto Sans SC';
                font-style: normal;
                font-weight: 500;
                font-size: 14px;
                line-height: 22px;
                // align-items: center;
                text-align: center;
                color: #01FFFF;
            }
            .cell-2-img1{
                width: 100%;
                display: flex;
                justify-content: center;
                .pic{
                    position: relative;
                    width: 120px;
                    height: 120px;
                    background: url('../../../../assets/img/Group1000002316.png');
                    background-repeat: no-repeat;
                    background-size: cover;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    .valueSpan1{
                        font-size: 20px;
                        font-weight: normal;
                        color: #01FFFF;
                        line-height: 192px;
                    }
                }

            }
            .cell-2-img2{
                // margin-top: 32px;
                width: 100%;
                display: flex;
                justify-content: center;
                .pic{
                    position: relative;
                    display: flex;
                    align-items: center;
                    width: 100%;
                    height: 190px;
                    background: url('../../../../assets/img/Ellipse4.png');
                    background-repeat: no-repeat;
                    background-position: 50% 50%;
                    background-size: 120px 120px;
                    .valueSpan2{
                        font-size: 20px;
                        font-weight: normal;
                        color: #01FFFF;
                        line-height: 192px;
                    }
                }
            }

        }
    }

    .small{
    background-image: url("../../../../assets/img/SubtractL1.png");
    background-size: 100% 100%;
}
.medium{
    background-image: url("../../../../assets/img/SubtractL2.png");
    background-size: 100% 100%;
}
.big{
    background-image: url("../../../../assets/img/SubtractL3.png");
    background-size: 100% 100%;
}
.gaint{
    background-image: url("../../../../assets/img/SubtractL4.png");
    background-size: 100% 100%;
}
.smallR{
    background-image: url("../../../../assets/img/SubtractR1.png");
    background-size: 100% 100%;
}
.mediumR{
    background-image: url("../../../../assets/img/SubtractR2.png");
    background-size: 100% 100%;
}
.bigR{
    background-image: url("../../../../assets/img/SubtractR3.png");
    background-size: 100% 100%;
}
.gaintR{
    background-image: url("../../../../assets/img/SubtractR4.png");
    background-size: 100% 100%;
}

.leftTop,.leftBottom,.rightBottom,.rightTop{
    position: absolute;
    height: 48px;
    width: 46px;
}
.leftTop{
    top: 9px;
    left: 8px;

}
.leftBottom{
    bottom: 6px;
    left: 10px;
    transform: rotate(270deg);
}
.rightTop{
    top: 7px;
    right: 10px;
    transform: rotate(90deg);
}
.rightBottom{
    bottom: 8px;
    right: 8px;
    transform: rotate(180deg);

}


</style>
