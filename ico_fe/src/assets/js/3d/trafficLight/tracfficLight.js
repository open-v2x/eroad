export default class TracfficLight{
    
    fixedFrameTransforms =  Cesium.Transforms.localFrameToFixedFrameGenerator('north', 'west');
    step = 0.8
    constructor(viewer,opts = {scale:2}){
        this.viewer = viewer;

        this.primitiveCollection = new Cesium.PrimitiveCollection();   
        this.viewer.scene.primitives.add(this.primitiveCollection);

        //let position = Cesium.Cartesian3.fromDegrees(opts.lng,opts.lat,opts.h);//(lng+0.00006,lat+0.000015,0.4);
        let hpRoll =  new Cesium.HeadingPitchRoll(opts.headingAngle,0,0);// - 1.5707964

        //this.position = position;
        this.hpRoll = hpRoll;
        this.scale = 2;//opts.scale;
        this.color = opts.color;
        this.step *=this.scale;
        this.LeftArrow = Cesium.Model.fromGltf({
            //id:opts.id,
            url:'static/model/arrows/LeftArrow.glb',
            scale:this.scale,
            color:this.color,
            distanceDisplayCondition:new Cesium.DistanceDisplayCondition(0.0, 1500.0),
            modelMatrix: Cesium.Transforms.headingPitchRollToFixedFrame(
                Cesium.Cartesian3.fromDegrees(opts.lng,opts.lat,opts.h+this.step*2), 
                this.hpRoll,
                Cesium.Ellipsoid.WGS84,this.fixedFrameTransforms
            ),
        })
        this.primitiveCollection.add(this.LeftArrow);

        this.RightArrow = Cesium.Model.fromGltf({
            //id:opts.id,
            url:'static/model/arrows/RightArrow.glb',
            scale:this.scale,
            color:this.color,
            distanceDisplayCondition:new Cesium.DistanceDisplayCondition(0.0, 1500.0),
            modelMatrix: Cesium.Transforms.headingPitchRollToFixedFrame(
                Cesium.Cartesian3.fromDegrees(opts.lng,opts.lat,opts.h),
                this.hpRoll, 
                Cesium.Ellipsoid.WGS84,this.fixedFrameTransforms
            ),
        })
        this.primitiveCollection.add(this.RightArrow);

        this.StraightArrow = Cesium.Model.fromGltf({
            //id:opts.id,
            url:'static/model/arrows/StraightArrow.glb',
            scale:this.scale,
            color:this.color,
            distanceDisplayCondition:new Cesium.DistanceDisplayCondition(0.0, 1500.0),
            modelMatrix: Cesium.Transforms.headingPitchRollToFixedFrame(
                Cesium.Cartesian3.fromDegrees(opts.lng,opts.lat,opts.h+this.step),
                this.hpRoll, 
                Cesium.Ellipsoid.WGS84,this.fixedFrameTransforms
            ),
        })
        this.primitiveCollection.add(this.StraightArrow);

        this.UTurnArrow = Cesium.Model.fromGltf({
            //id:opts.id,
            url:'static/model/arrows/UTurnArrow.glb',
            scale:this.scale,
            color:this.color,
            distanceDisplayCondition:new Cesium.DistanceDisplayCondition(0.0, 1500.0),
            modelMatrix: Cesium.Transforms.headingPitchRollToFixedFrame(
                Cesium.Cartesian3.fromDegrees(opts.lng,opts.lat,opts.h+this.step*3),
                this.hpRoll,
                Cesium.Ellipsoid.WGS84,this.fixedFrameTransforms
            ),
        })
        this.primitiveCollection.add(this.UTurnArrow);

        
    }

    


}