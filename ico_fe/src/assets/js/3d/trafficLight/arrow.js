
import PolylineImageTrailMaterialProperty from "./PolylineImageTrailMaterialProperty"
import { PolylineTrailLinkMaterialProperty } from '../polylineTrailLinkMaterial'
import arrow_1 from '../../../img/car.png'

export default class Arrow{
    polylinePrimitive = null;
    time = 30000;
    RED = Cesium.Color.fromCssColorString('#FF6568');
    GREEN = Cesium.Color.fromCssColorString('#65FF81');
    constructor(options = {}){
        if(options.sign)
            this.color = this.GREEN;
        else
            this.color = this.RED;
        this.time = this.time;
        var instance = new Cesium.GeometryInstance({
            geometry : new Cesium.GroundPolylineGeometry({
               positions : Cesium.Cartesian3.fromDegreesArray(options.positions),
               width : options.width,
            }),

            id:options.id
        });

        this.polylinePrimitive = new Cesium.GroundPolylinePrimitive({
            //classificationType:
            geometryInstances : instance,
            appearance : this.setColor(this.color)
        });
        window.viewer.scene.groundPrimitives.add(this.polylinePrimitive);

        this.setTime();
    }

    turn(){
        if(this.color == this.RED)
            this.polylinePrimitive.appearance = this.setColor(this.GREEN);
        else
            this.polylinePrimitive.appearance = this.setColor(this.RED);
    }

    setColor(color){
        this.color = color;
        return new Cesium.PolylineMaterialAppearance({
            material:Cesium.Material.fromType(Cesium.Material.PolylineArrowType, {
                color : color,
            })
        })
    }
    setTime(){
        setInterval(()=>{
            this.turn();
        },this.time)
    }
    
}