let objectUrl = {
  car: "static/model/car-baise.gltf",
  SUV: "static/model/SUV.glb",
  bus: "static/model/bus.glb",
  ambulance: "static/model/ambulance.glb",
  point: "static/model/point.gltf",
  black: "static/model/car/black_car.glb",
  blue: "static/model/car/blue_car.glb",
  brown: "static/model/car/brown_car.glb",
  grey: "static/model/car/grey_car.glb",
  red: "static/model/car/red_car.gltf",
  white: "static/model/car/white_car.glb",
  yellow: "static/model/car/yellow_car.glb"
}
export default class object {
  constructor(config) {
    this.timer = null
    this.x = config.x
    this.y = config.y
    this.position = config.position;
    this.headingRoll = config.headingRoll
    this.fixedFrameTransforms = Cesium.Transforms.localFrameToFixedFrameGenerator('north', 'west');
    this.object = Cesium.Model.fromGltf({
      url: objectUrl[config.modelUrl],
      scale: config.scale,
      distanceDisplayCondition: new Cesium.DistanceDisplayCondition(0.0, 6000.0),
      // 矩阵
      modelMatrix: Cesium.Transforms.headingPitchRollToFixedFrame(config.position, config.headingRoll, Cesium.Ellipsoid.WGS84, this.fixedFrameTransforms),
    })
  }
  setPosition(position, headingRoll = this.headingRoll) {
    cancelAnimationFrame(this.timer)
    this.headingRoll = headingRoll
    let positions = []
    let steps = 7; // 太小会顺移，太大也会瞬移,太小会马上到达目的地停下，显示为突然运动突然停下，太大则每次还没到达目的地，下一次调用函数直接闪现至当前目标地点的下一帧
    for (let index = 1; index <= steps; index++) {
      let splinePos = {
        x:this.position.x + index/steps*(position.x - this.position.x),
        y:this.position.y + index/steps*(position.y - this.position.y),
        z:this.position.z + index/steps*(position.z - this.position.z),
      }
      positions.push(splinePos)
    }
    this.position = position;
    Cesium.Transforms.headingPitchRollToFixedFrame(positions.shift(), headingRoll, Cesium.Ellipsoid.WGS84, this.fixedFrameTransforms, this.object.modelMatrix);
    this.splinePos(positions,headingRoll)
  }
  splinePos(array,headingRoll) {
      this.timer = requestAnimationFrame(()=>{
        // console.log(array.length)
        Cesium.Transforms.headingPitchRollToFixedFrame(array.shift(), headingRoll, Cesium.Ellipsoid.WGS84, this.fixedFrameTransforms, this.object.modelMatrix);

        if(array.length > 0) {
          this.splinePos(array,headingRoll)
        }
      })
  }

}
