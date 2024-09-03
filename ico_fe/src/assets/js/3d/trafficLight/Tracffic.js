import TracfficLight from "./tracfficLight";

export default class Tracffic{
    constructor(viewer){
        this.viewer = viewer;
        this.ws = new Socket({
            'url':'ws://127.0.0.1:5678/'
        })

        this.ws.onmessage(msg => {
            let data = JSON.parse(msg);
            let lights = data.lights;
            lights.creations.forEach(item => {
                
            });
        })
    }


}