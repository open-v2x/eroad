


var Cesium = window.Cesium;
export class PolylineTrailLinkMaterialProperty {
  constructor (image) {
    this._definitionChanged = new Cesium.Event()
    this._color = undefined
    this._colorSubscription = undefined
    this.color = new Cesium.Color(0, 1, 0, 1)
    this.duration = 3000
    this.image=image
    this._time = (new Date()).getTime()
    this.init()
  }

  init () {
    Cesium.Material.PolylineTrailLinkType = 'PolylineTrailLink'
    Cesium.Material.PolylineTrailLinkImage = this.image
    Cesium.Material.PolylineTrailLinkSource = `czm_material czm_getMaterial(czm_materialInput materialInput)\n\
                                                 {\n\
                                                      czm_material material = czm_getDefaultMaterial(materialInput);\n\
                                                      vec2 st = materialInput.st;\n\
                                                      vec4 colorImage = texture2D(image, vec2(fract(st.s - time), st.t));\n\
                                                      material.alpha = colorImage.a;\n\
                                                      material.diffuse = (colorImage.rgb);\n\
                                                      return material;\n\
                                                  }`
    Cesium.Material._materialCache.addMaterial(Cesium.Material.PolylineTrailLinkType, {
      fabric: {
        type: Cesium.Material.PolylineTrailLinkType,
        uniforms: {
          color: new Cesium.Color(1.0, 0.0, 0.0, 0.5),
          image: Cesium.Material.PolylineTrailLinkImage,
          time: 0
        },
        source: Cesium.Material.PolylineTrailLinkSource
      },
      translucent () {
        return true
      }
    })
  }

  getType () {
    return 'PolylineTrailLink'
  }

  getValue (time, result) {
    if (!Cesium.defined(result)) {
      result = {}
    }
    result.color = Cesium.Property.getValueOrClonedDefault(this._color, time, Cesium.Color.WHITE, result.color)
    result.image = Cesium.Material.PolylineTrailLinkImage
    result.time = (((new Date()).getTime() - this._time) % this.duration) / this.duration
    return result
  }

  equals (other) {
    return this === other || (other instanceof PolylineTrailLinkMaterialProperty && Cesium.Property.equals(this._color, other._color))
  }

  get isConstant () {
    return false
  }

  get definitionChanged () {
    return this._definitionChanged
  }
}

Object.defineProperties(PolylineTrailLinkMaterialProperty.prototype, {
  color: Cesium.createPropertyDescriptor('color')
})
