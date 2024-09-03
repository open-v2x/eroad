import axios from 'axios'
export default {
    websocket: (url, callback, error, this_) => {
        // url = protocol + '//' + 'www.lampmind.com:8888'

        let websocket = null
        if ("WebSocket" in window) {
            // websocket = new WebSocket("ws://"+url);
            websocket = new WebSocket(url);
        } else if ("MozWebSocket" in window) {
            // websocket = new WebSocket("ws://"+url);
            websocket = new WebSocket(url);
        } else {
            console.error("'不支持 WebSocKet");
        }
        //连接发生错误的回调方法
        websocket.onerror = event => {
            console.log(event)
            websocket = null
            if ("WebSocket" in window) {
                websocket = new WebSocket(url);
            } else if ("MozWebSocket" in window) {
                websocket = new WebSocket(url);
            }
            if(websocket){
                websocketOnopen(websocket)
            }
        }
        websocketOnopen(websocket)
        //接收到消息的回调方法
        function websocketOnopen(websocket) {
            websocket.onopen = event => {
                //接收到消息的回调方法
                websocket.onmessage = event => {
                    if (callback) {
                        if (event.data) {
                            try {
                                let data = JSON.parse(event.data)
                                if (data.code == '0000') {
                                    callback(data);
                                } else {
                                    if (error) {
                                        error(data)
                                    }
                                    this_.$alert(data.msg, this_.$t('App.message.text26'), {
                                        confirmButtonText: this_.$t('App.message.text27'),
                                    });
                                }
                                if (data.data.status == 2) {
                                    websocket.close();
                                    // console.log('websocket链接关闭')
                                }
                            } catch (e) {}
                        } else {
                            callback()
                        }
                    }
                };
                websocket.onerror = event => {
                    if(isTrue){
                        websocket = null
                        if ("WebSocket" in window) {
                            websocket = new WebSocket(url);
                        } else if ("MozWebSocket" in window) {
                            websocket = new WebSocket(url);
                        }
                    }
                }
                //连接关闭的回调方法
                // websocket.onclose = () => console.log("close");
                //监听窗口关闭事件，当窗口关闭时，主动去关闭websocket连接，防止连接还没断开就关闭窗口，server端会抛异常。
                window.onbeforeunload = () => websocket.close();
                //return websocket;
            };
        }

        return websocket;
    },
    websocket_radio: (url, callback, error, this_) => {
        let websocket = null
        if ("WebSocket" in window) {
            // websocket = new WebSocket("ws://"+url);
            websocket = new WebSocket(url);
        } else if ("MozWebSocket" in window) {
            // websocket = new WebSocket("ws://"+url);
            websocket = new WebSocket(url);
        } else {
            // console.error("'不支持 WebSocKet");
        }
        //连接发生错误的回调方法
        websocket.onerror = event => {
            console.log(event)
            websocket = null
            if ("WebSocket" in window) {
                websocket = new WebSocket(url);
            } else if ("MozWebSocket" in window) {
                websocket = new WebSocket(url);
            }
            if(websocket){
                websocketOnopen(websocket)
            }
        }
        websocketOnopen(websocket)
        //接收到消息的回调方法
        function websocketOnopen(websocket) {
            websocket.onopen = event => {
                //接收到消息的回调方法
                callback(websocket)
                // websocket.onmessage = event => {
                //     //接收到消息的回调方法
                //     callback(websocket)
                // };
                websocket.onerror = event => {
                    if(isTrue){
                        websocket = null
                        if ("WebSocket" in window) {
                            websocket = new WebSocket(url);
                        } else if ("MozWebSocket" in window) {
                            websocket = new WebSocket(url);
                        }
                    }
                }
                //连接关闭的回调方法
                // websocket.onclose = () => console.log("close");
                //监听窗口关闭事件，当窗口关闭时，主动去关闭websocket连接，防止连接还没断开就关闭窗口，server端会抛异常。
                window.onbeforeunload = () => websocket.close();
                //return websocket;
            };
        }
        
        // let isTrue = true;
        // //连接发生错误的回调方法
        // websocket.onerror = event => {
        //     console.log(event)
        //     // if(isTrue){
        //         websocket = null
        //         if ("WebSocket" in window) {
        //             websocket = new WebSocket(url);
        //         } else if ("MozWebSocket" in window) {
        //             websocket = new WebSocket(url);
        //         } else {
        //         }
        //     // }
        //     // isTrue = false;
        //     // console.log("error");
        // }
        // //接收到消息的回调方法
        // websocket.onopen = event => {
        //     //接收到消息的回调方法
        //     callback(websocket)
        // };

        return websocket;
    },


    ajax_get: (url, param, callback, this_) => {
        axios.get(url, { params: param }).then(res => {
            let data = res.data
            if (data) {
                if (callback) {
                    // if (data.model && JSON.stringify(data.model).length > 2) {
                        try {
                            // console.log(data.model)
                            callback(data)
                        } catch (e) {
                            // console.log('异常了')
                            // console.log(e)
                        }
                    // } else {
                    //     callback(data.model)
                    // }
                }

            } 
            // else {
            //     this_.$alert(data.message, this_.$t('App.message.text26'));
            // }
        })
    },
    ajax_post: (url, data, param, callback, this_, error, error_t) => {
        let qs = require('qs')
            // console.log(url,data,param)
            // console.log(qs.stringify(data))
        // data.version = this_.version;
        axios.post(url, qs.stringify(data), { params: param }).then(res => {
            let data = res
                // console.log(data)
            if (data.code == '0000') {
                if (callback) {
                    callback(data, this_)
                }
            } else if (data.code == '0001' || data.code == '0002' || data.code == '0003' || data.code == '0004' || data.code == '0102') {
                if (error) {
                    error(data, this_)
                }
                // this_.$alert(data.msg, this_.$t('App.message.text26'), {
                //     confirmButtonText: this_.$t('App.message.text27'),
                // });
                // this.a.setCookie('userData', '');
                // this_.$router.push({ path: '/' })
                // this_.$store.commit('linkToTypeFun', {
                //     linkToType: '/',
                // })
                // this_.$store.commit('pageTypeFun', {
                //     pageType: 'login',
                // })

                if(this_.websocket){
                  this_.websocket.close();
                }
            } else {
                if (error) {
                    error(data, this_)
                }
                if (!error_t) {
                    this_.$alert(data.msg, '系统提示', {
                        confirmButtonText: '确认',
                    });
                }
            }
        })
    },

    setCookie: (key, value, iDay) => {
        var oDate = new Date();
        oDate.setTime(oDate.getTime() + (iDay * 24 * 60 * 60 * 1000));
        oDate = oDate.toUTCString();
        // document.cookie = key + '=' + value;
        console.log(key + '=' + value + ';expires=' + oDate)
        document.cookie = key + '=' + value + ';expires=' + oDate;
    },
    removeCookie: (key) => {
        setCookie(key, '', -1); //这里只需要把Cookie保质期退回一天便可以删除
    },
    getCookie: (key) => {
        var cookieArr = document.cookie.split('; ');
        for (var i = 0; i < cookieArr.length; i++) {
            var arr = cookieArr[i].split('=');
            if (arr[0] === key) {
                return arr[1];
            }
        }
        return false;
    },
    download(url){
        try {
            var elemIF = document.createElement("iframe");
            elemIF.src = url;
            elemIF.style.display = "none";
            document.body.appendChild(elemIF);
        } catch (e) {
            alert("下载异常！");
        }
    },
    downloadIamge(imgsrc, name) {//下载图片地址和图片名
        let image = new Image();
        // 解决跨域 Canvas 污染问题
        image.setAttribute("crossOrigin", "anonymous");
        image.onload = function() {
            let canvas = document.createElement("canvas");
            canvas.width = image.width;
            canvas.height = image.height;
            let context = canvas.getContext("2d");
            context.drawImage(image, 0, 0, image.width, image.height);
            let url = canvas.toDataURL("image/png"); //得到图片的base64编码数据
            let a = document.createElement("a"); // 生成一个a元素
            let event = new MouseEvent("click"); // 创建一个单击事件
            a.download = name || "photo"; // 设置图片名称
            a.href = url; // 将生成的URL设置为a.href属性
            a.dispatchEvent(event); // 触发a的单击事件
        };
        image.src = imgsrc;
    }
}