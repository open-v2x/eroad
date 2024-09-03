<template>
  <div class="topbar">
    <div class="mask" v-if="$route.path == '/roadPerception' && showMask"></div>

    <div class="videoWrap" v-if="showVideo">
      <!-- <div class="getBack" @click="close"></div> -->
      <el-icon class="getBack" @click="close">
        <Close />
      </el-icon>
      <!-- <video autoplay controls muted class="video" ref="videoPlayer" :src="path"/> -->
      <video ref="videoPlayer" autoplay controls class="video" :src="path" />
    </div>
    <div class="bg-wrap">
      <div class="title">
        <span>数字道路智能运营中心</span>
      </div>
      <div class="nav-wrap">
        <div class="nav-block marginRight">
          <router-link to='/roadPerception' class="item-nav" :class="[active === 1 ? 'active' : '']">
            综合态势
          </router-link>
          <router-link to='/tables' class="item-nav" :class="[active === 6 ? 'active' : '']">
            指标分析
          </router-link>
        </div>
        
        <div class="nav-block marginLeft"> </div>
      </div>
      <div class="weather-warp">
        <div class="date-wrap">
          <div class="date1">
            {{ date.time }}
          </div>
          <img class="weatherImg" :src="this.img" onerror="this.src='../../assets/img/weather/102.png'" alt=""
            v-if="weatherIcon" @click="weathertest">
          <div class="weatherSpan">
            <span>{{ temperature }}</span>
            <span>{{ weather }}</span>
          </div>
        </div>
      </div>
      <div class="right-warp">
        <el-button type="primary" text link :bg="false" @click="toUrl">进入后台</el-button>
      </div>
    </div>
  </div>
</template>

<script>
import { getWeather } from '../../assets/js/api'
import { addWeatherState, destoryEarth } from "../../assets/js/3d/initEarth"
export default {
  data() {
    return {
      date: {
        day: '',
        week: '',
        time: ''
      },
      showVideo: false,
      w: ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六'],
      timer: null,
      interval: null,
      weather: '',
      temperature: '',
      weatherIcon: '',
      i: 0,
      path: null,
      showMask: false,
      title: null,
      environment: api_environment
      // img:require('../../assets/img/weather/和风.png')
    }
  },
  props: {
    active: {
      type: Number,
      default: 1
    }
  },
  watch: {
    "$store.state.topBar": {
      immediate: true,
      handler(now) {
        this.title = now
      }
    }
  },
  computed: {
    img() {
      let weatherImg
      try {
        weatherImg = require('../../assets/img/weather/' + this.weatherIcon + '.png')
      } catch (err) {
        console.log(err)
        weatherImg = require('../../assets/img/weather/102.png')
      }
      return weatherImg
    }
  },
  // created() {
  //   this.path = vedio_path
  // },
  mounted() {
    this.path = this.$store.state.videoPath
    this.timer = setInterval(() => {
      var today = new Date();
      var month = today.getMonth() + 1;
      this.date.day = today.getFullYear() + '.' + month + '.' + today.getDate();
      this.date.week = this.w[today.getDay()];
      var minutes = today.getMinutes() < 10 ? '0' + today.getMinutes() : today.getMinutes();
      this.date.time = today.getHours() + ':' + minutes;
      //window.now = today.getFullYear()+'-'+ month + '-' +  today.getDate() + ' ' + today.getHours() + ':' + today.getMinutes() + ':' + today.getSeconds();
    }, 1000)
    getWeather({
      city: '容城',
      extensions: 'base',
      key: '63b2abf7010d6d97f40d16d91f96bb97'
    }).then(res => {
      let todayWeather = res.lives[0];
      this.weatherIcon = todayWeather.weather;
      this.weather = todayWeather.weather;
      this.temperature = `${todayWeather.temperature}°C`;
    })

  },
  methods: {
    toUrl() {
      this.$router.push('/admin');
    },
    close() {
      this.showVideo = false
      this.$store.commit('setVideoTime', { time: this.$refs.videoPlayer.currentTime, index: this.$store.state.videoIndex })
      if (this.$route.path !== '/roadPerception') {
        this.$router.push('/roadPerception')
      }
    },
    loginout() {
      this.$confirm('确定要退出该系统吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$router.push({
          path: '/'
        })
        localStorage.clear()
      }).catch(() => {
        this.$message({
          type: 'info',
          message: '已取消'
        });
      })
    },
    skip() {
      window.location.href = 'http://144.7.108.34:90/dashboard';
    },
    weathertest() {
      const weather = ['雪', '雨', '平常天'];
      addWeatherState(weather[this.i++]);
    },
    showTheMask(res) {
      this.showMask = res
    },
    getTitle(title) {
      this.title = title
    },
    ChangeVideo(path) {
      if (this.path !== path) {
        this.$store.commit('setvideoPath', path)
        this.path = path
      }
    },

    open() {
      this.$bus.emit('changeCameraListShow', false)
      clearInterval(this.interval)
      this.showVideo = true
      this.$nextTick(() => {
        this.$refs.videoPlayer.currentTime = this.$store.state.videoTime[this.$store.state.videoIndex]
        this.$refs.videoPlayer.addEventListener("ended", () => {
          console.log("end");
          this.$refs.videoPlayer.currentTime = 0
        })
        this.$refs.videoPlayer.play()
        this.$refs.videoPlayer.muted = true
      })
    }
  },
}
</script>

<style lang="less" scoped>
.date-wrap {
  display: flex;
  flex-direction: row;

  .date1 {
    width: 86px;
    height: 44px;
    font-size: 36px;
    font-family: 'DINPro';
    font-weight: 400;
    color: #FFFFFF;
    line-height: 44px;
    margin-right: 20px;
  }

  .date2 {
    font-size: 10px;
    font-family: 'DINPro';
    font-weight: 400;
    color: #FFFFFF;
    line-height: 16px;
    display: flex;
    /*弹性容器*/
    flex-direction: column;
    flex-wrap: wrap;
    /*自动换行*/
    align-content: center;
    /*纵向对齐方式居中*/
    margin: auto;
    // &::after {
    //     content: '';
    //     width: 1px;
    //     height: 28px;
    //     // background: #f00;
    //     background: white;
    //     position:absolute;
    //     top: 8px;
    //     left: 180px;
    // }
  }

  margin-right: 5px;
}

.weatherImg {
  margin-right: 10px;
  width: 34px;
  height: 34px;
  cursor: pointer;
  background-repeat: no-repeat;
  background-size: cover;
}

.weatherSpan {
  //margin-left: 10px;
  font-size: 10px;
  font-family: Noto Sans SC-Regular, Noto Sans SC;
  font-weight: 400;
  color: #FFFFFF;
  line-height: 16px;
  display: flex;
  /*弹性容器*/
  flex-wrap: wrap;
  /*自动换行*/
  align-content: center;
  /*纵向对齐方式居中*/
  flex-direction: column;
  margin: auto;
  ;
}

.videoIcon {
  user-select: none;
  position: absolute;
  z-index: 9999;
  // top: 30%;
  top: 10px;
  line-height: 30px;
  right: 100px;
  margin-right: 10px;
  width: 90px;
  text-align: center;
  height: 30px;
  // background: url(../../assets/img/button-cell-select.png) no-repeat;
  // background-size: 100% 100%;

  &:hover {
    cursor: pointer;
    color: #01FFFF;
  }
}

.videoWrap {
  position: absolute;
  width: 100%;
  height: 100vh;
  z-index: 9999;
  background-color: #fff;

  .getBack {

    position: absolute;
    right: 0px;
    top: 0px;
    width: 100px;
    height: 100px;
    z-index: 2;
    color: #fff;
    font-size: 35px;

    &:hover {
      cursor: pointer;
    }
  }

  .video {
    width: 100%;
    height: 100%;
    object-fit: fill;
  }
}

.mask {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 100vw;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: -1;
}

.topbar {
  width: 100%;
  height: 90px;
  position: absolute;
  z-index: 1000;
  top: 0;
  background: linear-gradient(rgba(20, 34, 58, 0.8)70%, transparent 100%);

  .bg-wrap {
    width: 100%;
    height: 77px;
    display: inline-block;
    background-image: url('../../assets/img/topbarBg.png');
    background-repeat: no-repeat;
    background-size: 100% 100%;
    backdrop-filter: blur(3px);
    color: #fff;
    font-size: 16px;

    .title {
      // display: flex;
      // flex-direction: column;
      // align-items: center;
      position: absolute;
      left: 50%;
      top: 17px;
      transform: translateX(-50%);

      span {
        font-family: 'MStiffHei PRC';
        font-style: normal;
        font-weight: 400;
        font-size: 44px;
        line-height: 48px;
        height: 56px;
        color: #FFFFFF;
        text-shadow: 0px 2px 2px rgba(0, 0, 0, 0.25);
      }
    }

    .right-warp {
      right: 20px;
      top: 22px;
      height: 44px;
      position: absolute;
      display: flex;
      flex-direction: row;
      justify-content: flex-end;
      align-items: center;

      .set {
        width: 40px;
        height: 40px;
        background: rgba(0, 149, 255, 0.3);
        box-shadow: inset 0px 0px 4px 1px #0095FF;
        border-radius: 4px 4px 4px 4px;
        opacity: 1;
        border: 1px solid #0095FF;
        display: flex;
        justify-content: center;
        align-items: center;
        margin-left: 15px;
        position: relative;
        z-index: 0;

        .img {
          //padding:5px;
          height: 35px;
          width: 35px;
          background: url('../../assets/img/Slice88.png');
          background-repeat: no-repeat;
          background-size: 100% 100%;
          cursor: pointer;
        }
      }

      .s {
        content: '';
        width: 1px;
        height: 28px;
        background: white;
        margin: auto;
      }
    }

    .weather-warp {
      margin-left: 24px;
      margin-top: 24px;
      width: 230px;
      display: flex;

    }

    .account-wrap {
      margin-right: 15px;
      margin-top: 10px;
      display: flex;

      .bb {
        width: 24px;
        height: 24px;
        display: block;
        background-image: url('../../assets/img/close-icon.png');
        background-repeat: no-repeat;
        background-size: 100% 100%;
        margin-left: 10px;
        cursor: pointer;
        // margin-right: 15px;
      }

      .aa {
        width: 24px;
        height: 24px;
        display: block;
        background-image: url('../../assets/img/equipment-icon.png');
        background-repeat: no-repeat;
        background-size: 100% 100%;
        margin-left: 10px;
        cursor: pointer;
        // margin-right: 15px;
      }
    }

    .nav-wrap {
      width: 1370px;
      // height: 48px;
      top: 27px;
      left: 50%;
      transform: translateX(-50%);
      position: absolute;
      display: flex;
      flex-direction: row;
      align-items: flex-end;
      //background: transparent;//linear-gradient(rgba(0,0,0,1)0%,rgba(0,0,0,0.5)66%, rgba(0,0,0,0)99%);

      .nav-block {
        display: flex;

        .item-nav {
          display: block;
          height: 43px;
          margin: 0 16.5px;
          line-height: 28px;
          font-size: 24px;
          text-align: center;
          font-family: Noto Sans SC-Regular, Noto Sans SC;
          font-weight: 400;
          color: rgba(255, 255, 255, 0.8);
          cursor: pointer;

          &:hover {
            color: #01FFFF;
          }
        }

        .active {
          background-image: url('../../assets/img/Group 1000002320.png');
          background-repeat: no-repeat;
          background-size: 100% 100%;
          color: #01FFFF;
        }
      }
    }

    // &::before{
    //   content: '';
    //   display: block;
    //   height: 100%;
    //   width: 100%;
    //   backdrop-filter: blur(3px);
    //   z-index: -1000000;
    // }
  }

  .decoration {
    position: absolute;
    top: 0;
    left: 50%;
    transform: translateX(-49.80%);
    width: 47.2%;
    height: 94px;
    z-index: -99999;
  }

}
</style>
