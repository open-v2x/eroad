import cameraData1 from './电警卡口IPv6规划.json'
import lampData from './lamp.json'
import * as Player from "xgplayer";
import {queryAllChannels,queryChannelsByDeviceId,sendDevicePush} from '../../api_video.js'
import cameraIcon from '../../../../assets/img/Frame 1000002226.png'
export const getUrlByLamp = (lampID,callback) =>{
  var data = cameraData1.filter((m)=>{
    return m['灯杆编号'] == lampID;
  });
  var deviceId;
  if(data.length == 0){
    alert(lampID + ':该灯杆未查询到摄像头信息');
    return;
  }else{
      deviceId = data[0]['SIP用户名/设备编号'];
      if(!deviceId){
        deviceId = data[0]['国标编码'];
      }
  }
  queryChannelsByDeviceId({
    deviceId:deviceId
  }).then((res)=>{
    if(res.length == 0){
        alert('已查询到摄像头信息,但该设备未建立通道');
        return;
    }
    var channelId = res[0]['channelId'];
    if(!channelId){
        channelId = res['channelId'];
    }
    sendDevicePush({
      deviceId: deviceId,
      channelId: channelId
    }).then((re)=>{
      if(re['data']){
        let url = re['data']['flv'];
        callback(url);
      }else{
        alert(re['msg']);
      }
    })
  })
}

export const cameraOnLine = () => {

  queryAllChannels({}).then((response)=>{
    var cameraOnLine = [];
    var conditions = [];
    console.log('response',response);
    response.forEach((item,i)=>{
      let deviceId = item['deviceId'];
      var data = cameraData1.filter((m)=>{
        return m['SIP用户名/设备编号'] == deviceId;
      });
      if(data.length > 0)
        cameraOnLine.push(data[0]);
    })
    console.log('cameraOnLine',cameraOnLine);
    cameraOnLine.forEach((data , i)=>{
        if(data){
            var queryData = lampData.filter(_data => {
                return _data['灯杆编号'] == data['灯杆编号'];
            })
            if(queryData.length>0){
              if(!viewer.entities.getById(queryData[0]['灯杆编号']+'_icon')){
                //addFlyintLine([queryData[0].X,queryData[0].Y]);
                var i = 8;
                var t = 0;
                var s = 0;
                viewer.entities.add({
                  id:queryData[0]['灯杆编号']+'_icon',
                  position: new Cesium.CallbackProperty(() => {
                    s += 0.005;
                    t += s;
                    if (t > 1) {
                      t = 0;
                      s *= -1;
                      s *= 0.85;
                    }
                    return Cesium.Cartesian3.fromDegrees(queryData[0].X,queryData[0].Y, i - t);
                  }, false),
                  billboard: {
                    image: cameraIcon,
                    scale:0.6
                  },
                });
              }
            }


        }
    })

    // cameraOnLine.forEach((data , i)=>{
    //   if(data){
    //     var str = '${name} ===' + "'" + data['灯杆编号'] + "'";
    //     conditions.push([str,"color('red',1.0)"]);
    //   }
    // });
    // conditions.push(['true', "color('white',1.0)"]);
    
    // tileset.style = new Cesium.Cesium3DTileStyle({
    //   defines: {
    //     name:"${feature['name']}",
    //   },
    //   color: {
    //     conditions: conditions    
    //   }
    // });
  })
}

export const existAllCamera = () => {
  //var existLamp = cameraData.filter(m =>m['灯杆编号'])
  var conditions = [];
  cameraData1.forEach((data , i)=>{
    //console.log(data['灯杆编号']);
    var str = '${name} ===' + "'" + data['灯杆编号'] + "'";
    conditions.push([str,"color('red',1.0)"]);
  });
  conditions.push(['true', "color('white',1.0)"]);
  console.log(conditions);
  tileset.style = new Cesium.Cesium3DTileStyle({
    defines: {
      name:"${feature['name']}",
    },
    color: {
      conditions: conditions    
    }
  });

}
export const play = (url) => {
  let player = new Player({
    id: "mse", 
    url: url, 
    cors: true,
    controls: false,
    ignores: ["time"],
  });
}


