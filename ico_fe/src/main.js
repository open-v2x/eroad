import { createApp } from 'vue';
import App from './App.vue';
import router from './router';
import store from './store';
import { drag } from './derective/drag';
import Axios from 'axios';
import bus from '@/assets/js/bus';
import * as Cesium from "cesium";
import "cesium/Build/Cesium/Widgets/widgets.css";
import 'element-plus/dist/index.css'
import './assets/css/date.less'
import './assets/css/table.less'
window.Cesium = Cesium;

import ArcoVue from '@arco-design/web-vue';
import '@arco-design/web-vue/dist/arco.css';

import ElementPlus from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import JsonViewer from "vue3-json-viewer";
import "vue3-json-viewer/dist/index.css"; // 引入样式





import vue3SeamlessScroll from "vue3-seamless-scroll";
import '@/assets/font/myfont.css'

import AMapLoader from '@amap/amap-jsapi-loader';
window.AMapLoader = AMapLoader;

import { initGlobalState } from "qiankun";

const initialState = { msg: 0 }; // 全局状态池给了个默认值
const shareActions = initGlobalState(initialState);


shareActions.onGlobalStateChange((state, prevState) => {
  console.log("主应用观察到改变前的值为 ", prevState);
  console.log("主应用检测到变化", state);
});



shareActions.setGlobalState({ msg: null });

qiankunApps.forEach((item)=>{
  item.props.actions = shareActions
})


Axios.defaults.timeout = 45000;

const encrypt = false;

const notNeedEncodeArr = [];

const notNeedDecodeArr = [];
Axios.interceptors.request.use(
  function(config) {
    /* eslint-disable no-undef */
    if (encrypt) {
      config.requestData = config.data;
      if (
        !notNeedEncodeArr.some(x => config.url.indexOf(x) > -1) &&
        !config.url.endsWith('.json')
      ) {
        if (config.responseType !== 'blob') {
          config.responseType = 'arraybuffer';
        }
        if (window.location.host !== 'zctbrandhub.winzct.com') {
          console.warn(
            `=====接口路径：${config.url} =====,请求参数：`,
            config.data
          );
        }
        config.data = encryptData(config.data);
      }
    }
    if (window.location.host !== 'zctbrandhub.winzct.com') {
      console.warn(
        `=====接口路径：${config.url} =====,请求参数：`,
        config.data
      );
    }
    return config;
  },
  function(error) {
    return Promise.reject(error);
  }
);

Axios.interceptors.response.use(
  function(response) {
    if (
      encrypt &&
      !notNeedDecodeArr.some(x => response.config.url.indexOf(x) > -1) &&
      !notNeedEncodeArr.some(x => response.config.url.indexOf(x) > -1) &&
      !response.config.url.endsWith('.json')
    ) {
      response.data = decryptData(response.data) || {};
    }
    // 线上环境不打印
    if (window.location.host !== 'zctbrandhub.winzct.com') {
      console.warn(
        `=====接口路径：${response.config.url} =====,返回数据：`,
        response.data
      );
    }
    return response.data;
  },
  function(error) {
    return Promise.reject(error);
  }
);


const app = createApp(App)
drag(app)
app.config.globalProperties.$bus = bus
app.use(JsonViewer)
app.use(router).use(ElementPlus, {
  locale: zhCn,
}).use(ArcoVue).use(store).use(vue3SeamlessScroll).mount('#app');

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}
