import object from './sumoObject'
import proj4 from 'proj4';
import { lookAtTransform, lookAt } from '../common/cameraLookAt';
export default function sumoWebsocket() {
  let cars = ["blue", "red", "red", "blue", "red", "yellow", "yellow", "ambulance", "bus", "SUV", "red"]
  let lvboNames = [] // 数组存放lvbo车辆名
  let lvboName // 存放当前跟随视角的车辆名
  let location = {
    // 地图位置信息
    // convBoundary:"0.00,0.00,4508.13,2337.67",
    // origBoundary:"0.000000,0.000000,0.000000,0.000000",
    //projParameter:"+proj=tmerc +lat_0=-0.01567 +zone=50 +lon_0=117.000425 +k=0.9996 +x_0=91260 +y_0=-4326476 +ellps=WGS84 +units=m +no_defs",
    //projParameter:"+proj=utm +zone=50  +k=0.9996  +ellps=WGS84 +units=m +no_defs",
    //netOffset:"-403291.06,-4321624.04",
    //netOffset:"-403291.06,-4321624.04"
    netOffset: "3680.76,1188.04",
    projParameter: "+proj=tmerc +lat_0=0.00000 +zone=50 +lon_0=117.000010 +k=1 +x_0=91260 +y_0=-4326476 +ellps=WGS84 +units=m +no_defs"
  }
  const [dx, dy] = location.netOffset.split(',').map(Number);
  const { projParameter } = location;
  function toLatLng(xz) { // xy坐标转经纬度
    if (projParameter === '!') return null;
    const projX = xz[0] - dx;
    const projY = xz[1] - dy;
    const [lng, lat] = proj4(projParameter).inverse([projX, projY]);
    return [lng, lat];
  }

  // 创建车辆实例并添加入primitives的函数,属性信息和名字
  function carToPrimitives(primitive, i) {
    let position = toLatLng([primitive.x, primitive.y])
    let modelUrl = random() // 随机模型
    let scale = 1 // 默认缩放1
    if (modelUrl == "SUV" || modelUrl == "ambulance") { // SUV模型太小，放大
      scale = 1.6
    }
    let newCar = new object({
      position: Cesium.Cartesian3.fromDegrees(position[0], position[1], 5),
      headingRoll: new Cesium.HeadingPitchRoll(Cesium.Math.toRadians(primitive.angle), 0, 0),
      scale: scale,
      modelUrl: modelUrl,
      x: primitive.x,
      y: primitive.y
    })
    primitives.set(i, newCar);
    primitiveCollection.add(newCar.object);
  }
  // 创建箭头添加入primitives的函数，属性信息和名字
  function pointToPrimitives(primitive, i) {
    let position = toLatLng([primitive.x, primitive.y])
    let newPoint = new object({
      position: Cesium.Cartesian3.fromDegrees(position[0], position[1], 15),
      headingRoll: new Cesium.HeadingPitchRoll(Cesium.Math.toRadians(primitive.angle), 0, 0),
      modelUrl: 'point',
      x: primitive.x,
      y: primitive.y
    })
    primitives.set(i, newPoint);
    primitiveCollection.add(newPoint.object);
  }

  function random() { // 随机各种颜色/材质的汽车并返回相应字符串
    let value = Math.random()
    if (value < 0.1) {
      return cars[10]
    } else if (value < 0.15) {
      return cars[1]
    } else if (value < 0.2) {
      return cars[2]
    } else if (value < 0.3) {
      return cars[3]
    } else if (value < 0.5) {
      return cars[4]
    } else if (value < 0.72) {
      return cars[5]
    } else if (value < 0.73) {
      return cars[6]
    } else if (value < 0.75) {
      return cars[7]
    } else if (value < 0.77) {
      return cars[8]
    } else if (value < 0.9) {
      return cars[9]
    } else {
      return cars[10]
    }
  }

  function updatePosition(primitive, headingRoll, x, y, height) { // 更新primitive位置
    let radians
    if (!x) {
      x = primitive.x // 没有更新的话，取上一次存在car里的
    } else {
      primitive.x = x // 更新了的话，拿x来给car赋值
    }
    if (!y) { // y同x操作
      y = primitive.y
    } else {
      primitive.y = y
    }
    if (!headingRoll) { // headingRoll同xy操作
      headingRoll = primitive.headingRoll
    } else {
      radians = Cesium.Math.toRadians(headingRoll)
      headingRoll = new Cesium.HeadingPitchRoll(radians, 0, 0)
      primitive.headingRoll = headingRoll
    }
    let position = toLatLng([x, y])
    primitive.setPosition(Cesium.Cartesian3.fromDegrees(position[0], position[1], height), headingRoll,position)
    return { position, headingRoll }
  }

  let primitiveCollection = new Cesium.PrimitiveCollection();
  let primitives = new Map();
  viewer.scene.primitives.add(primitiveCollection);
  let sumows = new WebSocket('wss://city189.cn:3106/');
  let num = 0

  function send() {
    sumows.send(JSON.stringify({
      "type": "action",
      "action": "start",
      "scenario": "le_an_bei_yi",//选方案
      "stepLength": 0.5
    }));
  }
  sumows.onmessage = (ms) => {
    let data = JSON.parse(ms.data);
    // 接受一帧sumo仿真信息，具有创建，更新，删除信息
    if (data.type == 'snapshot') {
      let creations = data.vehicles.creations;
      let updates = data.vehicles.updates;
      let removals = data.vehicles.removals;
      if (creations) {
        for (let i in creations) {
          let primitive = creations[i];
          carToPrimitives(primitive, i)
        }
      }
      if (updates) { // 处理更新信息
        for (let i in updates) {
          let greenWave = i.indexOf("lvbo");
          let x = updates[i].x
          let y = updates[i].y
          let headingRoll = updates[i].angle
          let car = primitives.get(i)
          updatePosition(car, headingRoll, x, y, 5)

          if (greenWave == 0) { // 如果是绿波车辆
            let point = primitives.get(i + "_") // 先查看是否存在
            if (point) { // 存在则更新信息，并查看是否跟随视角，跟随则设置摄像机，不跟随则实时更新最新绿波车辆，准备下次跟随
              let attr = updatePosition(point, headingRoll, x, y, 15)
              if (window.follow) {
                if (i + "_" == lvboName) {
                  // lookAtTransform(attr.position[0],attr.position[1],attr.headingRoll)
                  lookAt(attr.position[0], attr.position[1], attr.headingRoll.heading)
                }
              } else {
                lvboName = lvboNames[lvboNames.length - 1]
              }
            }
            else { // 不存在则添加primitive
              let pointName = i + "_"
              lvboNames.push(pointName)
              if (num == 0) {
                lvboName = pointName
              }
              num++
              let primitive = updates[i]
              if (!primitive.angle) {
                primitive.angle = 0
              }
              if (!primitive.x) {
                primitive.x = 0
              }
              if (!primitive.y) {
                primitive.y = 0
              }
              pointToPrimitives(primitive, pointName)
              // console.log('primitives',primitives)
            }
          }
        }
      }
      if (removals) { // 删除primitive
        removals.forEach(item => {
          primitiveCollection.remove(primitives.get(item).object)
          primitives.delete(item)
          // console.log('delete', item, primitives)
          if (item.indexOf("lvbo") == 0) { // 如果是绿波车，不仅要删除车，还要删除Point
            primitiveCollection.remove(primitives.get(item + '_').object)
            primitives.delete(item + '_')
            if (item + '_' == lvboName) { // 如果删除的是当前跟随视角的车，则改变window.follow，取消跟随，由数据劫持set函数进行相应动作。
              window.follow = false
              lvboName = lvboNames[lvboNames.length - 1] // 将下次的视角跟随车辆名更新一下，其实上面已经实时更新了，但是这里加一句更保险。
            }
          };
        })
      }
    }
  };
  sumows.addEventListener('open', send);
  return sumows
}


