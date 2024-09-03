let CoordTransform = require('coordtransform');
let lastPolyline
let lastMarker

export const polyLine = ({ lnglats, names }) => {
  if(lastPolyline) window.map.remove(lastPolyline)
  let converted = lnglats.map(item => {
    return CoordTransform.wgs84togcj02(item[0], item[1])
  })
  let polyline = new AMap.Polyline({
    path: converted,
    isOutline: false,
    strokeColor: "#32CF4C",
    strokeOpacity: 1,
    strokeWeight: 8,
    // 折线样式还支持 'dashed'
    strokeStyle: "solid",
    lineJoin: 'round',
    lineCap: 'round',
    zIndex: 50,
    showDir: true
  })
  lastPolyline = polyLine
  window.map.add([polyline]);
  window.map.setFitView([polyLine])
}
export const marker = (lnglat, name) => {

}
