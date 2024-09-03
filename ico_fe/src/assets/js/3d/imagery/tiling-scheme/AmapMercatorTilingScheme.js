/**
 * @Author: Caven
 * @Date: 2021-01-31 22:07:05
 */



var CoordTransform=require('coordtransform');
var Cesium = window.Cesium;

class AmapMercatorTilingScheme extends Cesium.WebMercatorTilingScheme {
  constructor(options) {
    super(options)
    let projection = new Cesium.WebMercatorProjection()
    this._projection.project = function(cartographic, result) {
      result = CoordTransform.wgs84togcj02(
        Cesium.Math.toDegrees(cartographic.longitude),
        Cesium.Math.toDegrees(cartographic.latitude)
      )
      result = projection.project(
        new Cesium.Cartographic(
          Cesium.Math.toRadians(result[0]),
          Cesium.Math.toRadians(result[1])
        )
      )
      return new Cesium.Cartesian2(result.x, result.y)
    }
    this._projection.unproject = function(cartesian, result) {
      let cartographic = projection.unproject(cartesian)
      result = CoordTransform.gcj02towgs84(
        Cesium.Math.toDegrees(cartographic.longitude),
        Cesium.Math.toDegrees(cartographic.latitude)
      )
      return new Cesium.Cartographic(
        Cesium.Math.toRadians(result[0]),
        Cesium.Math.toRadians(result[1])
      )
    }
  }
}

export default AmapMercatorTilingScheme
