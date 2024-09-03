import axios from 'axios';
import router from '../../router';



export const queryAllChannels = () => fetch(`/api/device/query/allChannels`, {}, 'GET');
export const queryChannelsByDeviceId = params => fetch(`/api/device/query/allChannels?deviceId=${params.deviceId}`, {}, 'GET');
export const sendDevicePush = params => fetch(
  `/api/play/start/${params.deviceId}/${params.channelId}`, {}, 'GET'
);
export const queryByChannelName = params => fetch(`/api/device/query/getAllChannels?channelName=${params.channelName}`, {}, 'GET')
export const queryBydeviceName = params => fetch(`/api/device/query/devices?page=1&count=10&deviceName=${params.deviceName}`, {}, 'GET')

export const deviceProxyPlay = (params) => fetch('/api/proxy/deviceProxyPlay', params, 'POST');


export const vedioLogin = () => axios({//视频接口登录获取token
  url: '/api/user/newLogin',
  method: 'post',
  headers: {
    'Content-Type': 'application/json',
  },
  data : {
    password: "UtkddkQdBAbus2ODE351VApNsv89V1RvHoHcF7yrq2X7JSz8v7khEC2d6C/FDz140ufbIrxOvbHeQbwyGPBsgA==",
    username: "IOC"
  }
})










export const cloudRecord = (params) => fetch('/api/cloudRecord/startPlayBack', params, 'GET');




export const seekPlayBack = (params) => fetch('/api/cloudRecord/seekPlayBack', params, 'GET',
)


export const setPlayBackPaused = (params) => fetch('/api/cloudRecord/setPlayBackPaused', params, 'POST')




export const setPlayBackSpeed = (params) => fetch('/api/cloudRecord/setPlayBackSpeed', params, 'GET')



function fetch(url, params = {}, method = 'POST', formData = false, { withMask = true, ...config } = {}) {
  if (!url) {
    throw new Error('interface path not found');
  }
  let isPOST = method === 'POST'
  return new Promise((resolve, reject) => {
    console.log(params)
    request();
    function request() {
      let _headers = {
        Authorization: `Bearer ${JSON.parse(localStorage.getItem('vedioToken'))}`,
        'Cache-Control': 'no-cache',
        'Content-Type': 'application/json'
      }
      axios({
        url: url,
        method,
        params: isPOST ? '' : params,
        data: isPOST ? params : '',
        headers: _headers,
        ...config
      }).then(res => {
        if (res.code === 401) {
          vedioLogin().then((res) => {
            if(res.code==200){
              localStorage.setItem('vedioToken', JSON.stringify(res.data))
              request();
            }
          });
          // code401 token过期后重新请求视界token，获取到token后重新执行失败的axios，将成功结果resolve。
        } else if (res.code === -1) {
          reject({ requestUrl: url, resErr: res });
        } else if (res.code === 403) {
          reject({ requestUrl: url, resErr: res });
        } else if ((res.code == 200 || res.code == 0 || res.subCode == 1) && (typeof (res.code !== 'undefined')) || +res.status === 1) {
          resolve(res);
        } else {
          //Message.warning(res.subMessage || res.message || '内部错误，请稍候再试');
          resolve(res);//TODO接口规范统一后再完善，返回状态码暂不统一
          //reject(res);
        }

      }, err => {
        //Message.warning('请求异常，请重新登录')
        //withMask && loading.loadingEnd();
        reject({ requestUrl: url, resErr: err });
      }).finally(() => {
        //withMask && loading.loadingEnd();
      });
    }
  });
}
