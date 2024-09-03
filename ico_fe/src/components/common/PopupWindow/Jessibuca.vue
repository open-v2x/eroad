<template>
  <div id="container" ref="container"></div>
</template>
  <script>
  import moment from 'moment'
  let _ts
  export default {
    name: "DemoPlayer",
    props: {
      isPlayFetch:{
        type:Boolean,
        default:false
      },
      decorderPath:{
        type: String,
        default: 'decoder-pro.js'
      },
      isSmid:{
        type:Boolean,
        default:false
      },
      playUrl: {
        type: String,
        default: ''
      }
    },
    watch: {
      playUrl: {
        immediate: true,
        handler(val) {
          if(val) {
            this.destroy();
            this.videoUrl = val;
            this.$nextTick(()=>{
              this.play(val)
            })
          }
        }
      }
    },
    data() {
      return {
        jessibuca: null,
        timer:null, // 定时任务
        version: '',
        wasm: false,
        vc: "ff",
        isfull: false,
        titleName:'截屏',
        showOperateBtns: true, // 是否显示功能按钮
        showBandwidth: true, // 是否显示网速
        err: "",
        useWCS: false,
        useMSE: false,
        useSIMD:true,
        speed: 0,
        ptz:false,
        performance: "",
        volume: 1,
        rotate: 0,
        useOffscreen: false,
        recording: false,
        recordType: 'webm',
        scale: 0,
        logoText:'',
        pts:0
      };
    },
    mounted() {
      // this.getUserInfo()
      window.onerror = (msg) => (this.err = msg);
    },
    unmounted() {
      if(this.jessibuca)this.jessibuca.destroy();
    },
    beforeDestroy() {
      if (this.timer) {
        clearInterval(this.timer);
        this.timer = null;
      }
    },
    methods: {
      getUserInfo() {
          this.$axios({
              url:`/api/user/getUserInfo`,
              method: "get"
          }).then(res=>{
              if (res.data.code === 0) {
                this.logoText = `${res.data.data.user.username}-${res.data.data.user.loginIp}` || '集成影像系统'
              }
          })
      },
      desTroyPlay() {
        if (this.timer) {
          clearInterval(this.timer);
          this.timer = null;
        }
        if (this.jessibuca) {
          this.jessibuca.destroy()
          this.jessibuca = null 
        }
      },
      // 手动清除延迟
      handleClearCache() {
        // 暂停状态不处理
        if (this.jessibuca.isPlaying()) {
          this.jessibuca.pause().then(()=>{
            this.jessibuca.play().then(()=>{
              console.log('自动重新播放，消除延迟')
            }).catch(e=>{
              console.log('自动播放失败',e)
            })
          }).catch(e=>{
            console.log('pause error', e)
          })
        }
      },
      create(options) {
        let that = this
        if (this.jessibuca) {
          this.jessibuca.destroy()
          this.jessibuca = null
        }
        options = options || {};
        this.jessibuca = new JessibucaPro(
            Object.assign(
                {
                  container: this.$refs.container,
                  decoder: `/Jessibuca/${this.decorderPath}`,
                  videoBuffer: 1, // 缓存时长
                  isResize: false,
                  useWCS: this.useWCS,
                  useMSE: this.useMSE,
                  useSIMD:this.useSIMD,
                  controlAutoHide: true,
                  heartTimeout: 5,
                  heartTimeoutReplay: true,
                  heartTimeoutReplayTimes: -1,
                  loadingTimeout: 30,
                  loadingTimeoutReplay: true,
                  loadingTimeoutReplayTimes: -1,
                  text: "",
                  // background: "bg.jpg",
                  loadingText: "加载中...",
                  hasAudio:false,
                  debug: false, // 是否打开debug
                  supportDblclickFullscreen: false,
                  showBandwidth: this.showBandwidth, // 显示网速
                  fullscreenWatermarkConfig:{
                        text:this.logoText
                  },
                  // watermarkConfig:{
                  //     text:{
                  //         content:'集成影响系统',
                  //         fontSize:'14',
                  //         color:'#000'
                  //     },
                  //     right: 10,
                  //     top:10,
                  //     left:10,
                  //     bottom:10
                  // },
                  showPerformance: false,
                  operateBtns: {
                    fullscreen: this.showOperateBtns,
                    screenshot: this.showOperateBtns,
                    zoom:true,
                    screenshotFn:()=>{
                      _this.jessibuca.screenshotWatermark({
                            filename:`${"监控截图"}-${moment().format('YYYY-MM-DD HH:mm:ss')}`,
                            text:{
                                content:_this.logoText,
                                fontSize: '40',
                                color: 'white'
                            },
                            right: 20,
                            top: 40
                        })
                    },
                    play: this.showOperateBtns,
                    audio: this.showOperateBtns,
                    ptz: this.ptz,//是否开启操控功能
                    record: this.showOperateBtns, // 录像功能
                    performance :false,
                  },
                  vod: this.vod,
                  forceNoOffscreen: !this.useOffscreen,
                  isNotMute: true,
                  timeout: 1
                },
                options
            )
        );
        var _this = this;
        this.jessibuca.on("load", function () {
          console.log("on load");
        });
        this.jessibuca.on("log", function (msg) {
          console.log("on log", msg);
        });
        this.jessibuca.on("record", function (msg) {
          console.log("on record:", msg);
        });
        this.jessibuca.on("pause", function () {
          console.log("on pause");
          if (_this.isPlayFetch) {
            _this.$emit('pauseClick')
          }
        });
        this.jessibuca.on("play", function () {
          console.log("on play");
          if (_this.isPlayFetch) {
            _this.$emit('playClick')
          }
        });
        this.jessibuca.on("fullscreen", function (msg) {
          console.log("on fullscreen", msg);
          _this.isfull = msg
        });
        this.jessibuca.on("mute", function (msg) {
          console.log("on mute", msg);
        });
        this.jessibuca.on("mute", function (msg) {
          console.log("on mute2", msg);
        });
        this.jessibuca.on("audioInfo", function (msg) {
          console.log("audioInfo", msg);
        });
        // this.jessibuca.on("bps", function (bps) {
        //   // console.log('bps', bps);
        // });
        // let _ts = 0;
        // this.jessibuca.on("timeUpdate", function (ts) {
        //     console.log('timeUpdate,old,new,timestamp', _ts, ts, ts - _ts);
        //     _ts = ts;
        // });
        this.jessibuca.on("videoInfo", function (info) {
          console.log("videoInfo", info);
        });
        this.jessibuca.on("error", function (error) {
          console.log("error", error);
          // 播放出错就算结束，因为第二次推流地址已变
          _this.$emit('errorBack',error)
        });
        this.jessibuca.on("recordStart", function (error) {
          console.log("record start")
        });
        this.jessibuca.on("recordEnd", function (error) {
          console.log("record end")
        });
        this.jessibuca.on("timeout", function () {
          _this.$emit('onVideoEnd')
          // _this.close()
          console.log("timeout");
        });
        this.jessibuca.on('start', function () {
          console.log('frame start');
        })
        this.jessibuca.on("performance", function (performance) {
          var show = "卡顿";
          if (performance === 2) {
            show = "非常流畅";
          } else if (performance === 1) {
            show = "流畅";
          }
          _this.performance = show;
        });
        this.jessibuca.on('buffer', function (buffer) {
          console.log('buffer', buffer);
        })
        this.jessibuca.on('stats', function (stats) {
          // console.log(stats.pTs)
          _this.pts = stats.pTs
        })
        this.jessibuca.on('kBps', function (kBps) {
          // console.log('kBps', kBps);
        });
        this.jessibuca.on('recordingTimestamp', (ts) => {
          console.log('recordingTimestamp', ts);
        })
        // console.log(this.jessibuca);
      },
      play(url,title,byText) {
        console.log(url)
        this.titleName = title
        if (byText) {
          this.logoText = byText
        }
        this.create()
        this.jessibuca.play(this.playUrl)
        setTimeout(()=>{
          if(this.$route.path=='/roadPerception')
            {
              document.querySelector('.jessibuca-controls').style.opacity = '0'
              // document.querySelector('.jessibuca-controls').style.display = 'none'
            }
        })
      },
      pause() {
        this.jessibuca.pause();
        this.err = "";
        this.performance = "";
      },
      mute() {
        this.jessibuca.mute();
      },
      cancelMute() {
        this.jessibuca.cancelMute();
      },
      volumeChange() {
        this.jessibuca.setVolume(this.volume);
      },
      rotateChange() {
        this.jessibuca.setRotate(this.rotate);
      },
      destroy() {
        if (this.jessibuca) {
          this.jessibuca.destroy();
        }
        // this.create();
        this.performance = "";
      },
      setFullscreen() {
        this.jessibuca.setFullscreen(true);
      },
      exitFullscreen() {
        this.jessibuca.setFullscreen(false);
      },
      clearView() {
        this.jessibuca.clearView();
      },
      startRecord() {
        const time = new Date().getTime();
        this.jessibuca.startRecord(time, this.recordType);
      },
      stopAndSaveRecord() {
        this.jessibuca.stopRecordAndSave();
      },
      changeBuffer() {
        this.jessibuca.setBufferTime(Number(this.$refs.buffer.value));
      },
      scaleChange() {
        this.jessibuca.setScaleMode(this.scale);
      },
    },
  };
  </script>
  <style scoped lang="less">
  .root {
    display: flex;
    place-content: center;
    margin-top: 3rem;
  }
  .container-shell {
    position: relative;
    backdrop-filter: blur(5px);
    background: hsla(0, 0%, 50%, 0.5);
    padding: 30px 4px 10px 4px;
    /* border: 2px solid black; */
    width: auto;
    position: relative;
    border-radius: 5px;
    box-shadow: 0 10px 20px;
  }
  .container-shell-title {
    position: absolute;
    color: darkgray;
    top: 4px;
    left: 10px;
    text-shadow: 1px 1px black;
  }
  #container {
    background: rgba(13, 14, 27, 0.7);
    width: 100%;
    height: 100%; 
    position: relative;
  }
  .input {
    display: flex;
    align-items: center;
    margin-top: 10px;
    color: white;
    place-content: stretch;
  }
  .input2 {
    bottom: 0px;
  }
  .input input[type='input'] {
    flex: auto;
  }
  .err {
    position: absolute;
    top: 40px;
    left: 10px;
    color: red;
  }
  .option {
    position: absolute;
    top: 4px;
    right: 10px;
    display: flex;
    place-content: center;
    font-size: 12px;
  }
  .option span {
    color: white;
  }
  .page {
    background: url(/bg.jpg);
    background-repeat: no-repeat;
    background-position: top;
  }

  </style>