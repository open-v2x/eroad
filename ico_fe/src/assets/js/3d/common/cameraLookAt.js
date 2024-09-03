export function lookAtTransform(lng, lat, headingPitchRolls) {
  let center = Cesium.Cartesian3.fromDegrees(lng, lat, 50);
  let transform = Cesium.Transforms.eastNorthUpToFixedFrame(center);
  let orientations = Cesium.Transforms.headingPitchRollQuaternion(center, headingPitchRolls)
  transform = Cesium.Matrix4.fromRotationTranslation(Cesium.Matrix3.fromQuaternion(orientations), center); // Cesium.Matrix3.fromQuaternion 会根据提供的四元数提供一个三维矩阵
  let heading = Cesium.Math.toRadians(0);
  let pitch = Cesium.Math.toRadians(-30);
  let range = 0.1;
  viewer.camera.lookAtTransform(transform, new Cesium.HeadingPitchRange(heading, pitch, range))
}

export function lookAt(lng, lat, heading) {
  let center = Cesium.Cartesian3.fromDegrees(lng,lat,10);
  let pitch = Cesium.Math.toRadians(-5);
  let range = 20.0;
  viewer.camera.lookAt(center, new Cesium.HeadingPitchRange(heading, pitch, range));
}