import axios from 'axios';
import { ElLoading , ElMessageBox, ElMessage } from 'element-plus';
import bus from '@/assets/js/bus';

let loading = {
    loadingInstance1: '',
    loadingStart() {
        this.loadingInstance = ElLoading.service({
            fullscreen: true,
            lock: true,
            text: '数据加载中...',
            background: 'rgba(0, 0, 0, 0.6)'
        });
    },
    loadingEnd() {
        this.loadingInstance.close();
    }
}


export function fetch(url, params = {}, method = 'POST', formData = false, { withMask = true, ...config } = {}) {
    if (!url) {
        throw new Error('interface path not found');
    }
    withMask && loading.loadingStart();
    // if(!localStorage.getItem('tokenObj')){
    //     bus.$emit('goLogin');
    // }
    let _headers = {
        Authorization: config.isLogin ? 'Basic aW9jOmlvYw==' : `Bearer ${JSON.parse(localStorage.getItem('tokenObj')).access_token}`,
        'Cache-Control':'no-cache',
        // 'Token': localStorage.getItem('b_token'),
        // 'Token': 'cc9a8b5feb3a492c8b755d12c5b8aea0',
        // 'Content-Type': formData ? 'multipart/form-data' : 'application/x-www-form-=urlencoded'
        'Content-Type': 'application/x-www-form-=urlencoded'
    }
    // 开发或者测试环境用，方便查看未加密数据
    // if (window.location.hostname === 'localhost' || window.location.hostname === '10.0.0.10' ) {
    //    config._requestParams = params
    // }

    let isPOST = method === 'POST'
    return new Promise((resolve, reject) => {
        console.log(params)
        axios({
            url: url,
            method,
            params: isPOST ? '' : params,
            data: isPOST ? params : '',
            headers: _headers,
            ...config
        }).then(rep => {
            withMask && loading.loadingEnd();
            const token = localStorage.getItem('b_token');
            if (rep.code === 1002 && rep.message === '登录失效，请重新登录') {
                if (token) {
                    // 开发环境登录失效不删除token
                    // 主要为了开发时连不同后台主机，有些后台服务未启动导致失效提示，并且需要频繁登录的问题。
                    // 开发环境用的confirm 可以取消，uat和线上用的alert，失效后强制去重新登录。
                    if (process.env.NODE_ENV === 'development') {
                        ElMessageBox.confirm('登录失效，请点击【确定】按钮重新登录','登录失效',{
                            type: 'warning',
                            callback: function(action){
                                if(action === 'confirm') {
                                    bus.$emit('goLogin')
                                }
                            }
                        });
                    } else {
                        localStorage.removeItem('b_token');
                        ElMessageBox.alert('登录失效，请点击【确定】按钮重新登录','登录失效',{
                            type: 'warning',
                            callback: function(action){
                                if(action === 'confirm') {
                                    bus.$emit('goLogin')
                                }
                            }
                        });
                    }
                }
                reject({requestUrl: url, resErr: rep});
            } else if ((rep.code == 200 || rep.code == 0 || rep.subCode == 1) && (typeof (rep.code !== 'undefined')) || +rep.status === 1) {
                resolve(rep);
            } else {
                //Message.warning(rep.subMessage || rep.message || '内部错误，请稍候再试');
                resolve(rep);//TODO接口规范统一后再完善，返回状态码暂不统一
                //reject(rep);
            }

        }, err => {
            Message.warning('请求异常，请重新登录')
            withMask && loading.loadingEnd();
            reject({requestUrl: url, resErr: err});
        }).finally(()=>{
            withMask && loading.loadingEnd();
        });
    });
}
