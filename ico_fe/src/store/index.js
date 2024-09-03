import { createStore } from "vuex";

export default createStore({
  state: {
    mapInstance: null,
    magnification: 1, // 倍数
    cars: 0, // 车数
    jamValue: 1, // 交通预测配时决定的乘数，越好越小
    fullScreen: false, // 全息路口是否全屏
    signal: 1,
    topBar: { name: "迁入分析", value: "/MigrationAnalysis" },
    videoTime: [0, 0],
    videoPath: vedio_path_one_minute,
    videoIndex: 0
  },
  getters: {
    mapInstance: state => state.mapInstance
  },
  mutations: {
    setvideoPath(state, path) {
      state.videoPath = path;
    },
    setVideoIndex(state, index) {
      state.videoIndex = index;
    },
    setMapInstance(state, mapInstance) {
      state.mapInstance = mapInstance;
    },
    setMagnification(state, magnification) {
      state.magnification = magnification;
    },
    setCars(state, cars) {
      state.cars = cars;
    },
    setJamValue(state, jamValue) {
      state.jamValue = jamValue;
    },
    setFullScreen(state, fullScreen) {
      state.fullScreen = fullScreen;
    },
    setSignal(state, signal) {
      state.signal = signal;
      // console.log(signal)
    },
    setTopbar(state, topBar) {
      state.topBar = topBar;
    },
    setVideoTime(state, { time, index }) {
      state.videoTime[index] = time;
    }
    // clearPop2D(state,_){
    //   state.clearPop2D = !state.clearPop2D
    // },
    //DoubleScreen已不再在综合态势页面触发，目前已无需解决视频覆盖问题。
  },
  actions: {},
  modules: {}
});
