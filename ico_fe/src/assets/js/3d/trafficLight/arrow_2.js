

import arrow from '../../../img/arrow.png'


export default class Arrow{
    polylinePrimitive = null;
    time = 30000;
    //RED = Cesium.Color.RED;
    //GREEN = Cesium.Color.GREEN;
    count = 0;
    // RED = Cesium.Color.fromCssColorString('#FF6568');
    // GREEN = Cesium.Color.fromCssColorString('#65FF81');
    constructor(options = {}){
        this.color = Cesium.Color.WHITE;
        this.time = this.time;
        var instance = new Cesium.GeometryInstance({
            geometry : new Cesium.GroundPolylineGeometry({
               positions : Cesium.Cartesian3.fromDegreesArray(options.positions),
               width : 30,
            }),

            id:options.id
        });
        this.polylinePrimitive = new Cesium.GroundPolylinePrimitive({
            //classificationType:
            geometryInstances : instance,
            appearance : this.set(this.color)
        });
        window.viewer.scene.groundPrimitives.add(this.polylinePrimitive);

        //this.setTime();
    }

    turn(){
        if(this.color == this.RED)
            this.polylinePrimitive.appearance = this.set(this.GREEN);
        else
            this.polylinePrimitive.appearance = this.set(this.RED);
    }

    set(color){
        this.color = color;
        return new Cesium.PolylineMaterialAppearance({
            material : new Cesium.Material({
                fabric: {
                    type: Cesium.Material.PolylineImageTrailType,
                    uniforms: {
                      color: this.color,
                      image: arrow,
                      speed: 100,
                      repeat: new Cesium.Cartesian2(80, 1)
                    },
                    source:  `czm_material czm_getMaterial(czm_materialInput materialInput){
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
                    // source:  `czm_material czm_getMaterial(czm_materialInput materialInput){
                    //     czm_material material = czm_getDefaultMaterial(materialInput);
                    //     vec2 st = repeat * materialInput.st;
                    //     float time = fract(czm_frameNumber * speed / 1000.0);
                    //     vec4 colorImage = texture2D(image, vec2(fract(st.s - time), st.t));
                    //     if(color.a == 0.0){
                    //       if(colorImage.rgb == vec3(1.0) || colorImage.rgb == vec3(0.0)){
                    //         discard;
                    //       }
                    //     material.alpha = colorImage.a;
                    //     material.diffuse = colorImage.rgb;
                    //     }else{
                    //     material.alpha = colorImage.a * color.a;
                    //     material.diffuse = max(color.rgb * material.alpha * 3.0, color.rgb);
                    //     }
                    //     return material;
                    // }`
                },
                translucent: function(material) {
                    return true
                }
            })
        })
    }
    setTime(){
        setInterval(()=>{
            if(this.count == 2){
                return;
            }
            this.turn();
            this.count++;
        },this.time)
    }
    
}