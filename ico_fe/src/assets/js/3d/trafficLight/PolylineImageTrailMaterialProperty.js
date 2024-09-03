/**
 * @Author: Caven
 * @Date: 2020-07-17 22:15:56
 */

import MaterialProperty from './MaterialProperty'

var Cesium = window.Cesium;
class PolylineImageTrailMaterialProperty extends MaterialProperty {
  constructor(options = {}) {
    super(options)
    this._image = undefined
    this._imageSubscription = undefined
    this._repeat = undefined
    this._repeatSubscription = undefined
    this.image = options.image
    this.repeat = new Cesium.Cartesian2(
      // options.repeat?.x || 1,
      // options.repeat?.y || 1
      options.repeat.x,
      options.repeat.y,
    )
    this.init();
  }

  init(){
    Cesium.Material.PolylineTrailLinkSource = `czm_material czm_getMaterial(czm_materialInput materialInput){
            czm_material material = czm_getDefaultMaterial(materialInput);
            vec2 st = repeat * materialInput.st;
            float time = fract(czm_frameNumber * speed / 1000.0);
            vec4 colorImage = texture2D(image, vec2(fract(st.s - time), st.t));
            if(color.a == 0.0){
              if(colorImage.rgb == vec3(1.0) || colorImage.rgb == vec3(0.0)){
                discard;
              }
            material.alpha = colorImage.a;
            material.diffuse = colorImage.rgb;
            }else{
            material.alpha = colorImage.a * color.a;
            material.diffuse = max(color.rgb * material.alpha * 3.0, color.rgb);
            }
            return material;
        }`
    Cesium.Material._materialCache.addMaterial(
      Cesium.Material.PolylineImageTrailType,
      {
        fabric: {
          type: Cesium.Material.PolylineImageTrailType,
          uniforms: {
            color: new Cesium.Color(1.0, 0.0, 0.0, 0.7),
            image: Cesium.Material.DefaultImageId,
            speed: 1,
            repeat: new Cesium.Cartesian2(1, 1)
          },
          source:  Cesium.Material.PolylineTrailLinkSource//LineImageTrailMaterial
          //shaderSource:Cesium.Material.PolylineTrailLinkSource
        },
        translucent: function(material) {
          return true
        }
      }
    )
  }


  getType(time) {
    return Cesium.Material.PolylineImageTrailType
  }

  getValue(time, result) {
    if (!result) {
      result = {}
    }
    result.color = Cesium.Property.getValueOrUndefined(this._color, time)
    result.image = Cesium.Property.getValueOrUndefined(this._image, time)
    result.repeat = Cesium.Property.getValueOrUndefined(this._repeat, time)
    result.speed = this._speed
    return result
  }

  equals(other) {
    return (
      this === other ||
      (other instanceof PolylineImageTrailMaterialProperty &&
        Cesium.Property.equals(this._color, other._color) &&
        Cesium.Property.equals(this._image, other._image) &&
        Cesium.Property.equals(this._repeat, other._repeat) &&
        Cesium.Property.equals(this._speed, other._speed))
    )
  }
}

Object.defineProperties(PolylineImageTrailMaterialProperty.prototype, {
  color: Cesium.createPropertyDescriptor('color'),
  speed: Cesium.createPropertyDescriptor('speed'),
  image: Cesium.createPropertyDescriptor('image'),
  repeat: Cesium.createPropertyDescriptor('repeat')
})

export default PolylineImageTrailMaterialProperty
