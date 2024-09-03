export default class Particle{
	num = 500;
	stars = [];
	rnd;
    constructor(opts={}){
        this.canvas = opts.el;
		this.canvas.width = opts.el.offsetWidth;
		this.canvas.height = opts.el.offsetHeight;

		this.context = this.canvas.getContext('2d');
        this.mouseX = this.canvas.width/2;
	    this.mouseY = this.canvas.height/2;
		this.addStar();
		setInterval(()=>{
            //this.context.fillStyle = 'rgba(0,0,0,0.1)';
            this.context.globalCompositeOperation = 'destination-in'; // 设置模式为：目标图形和源图形重叠的部分会被保留（源图形），其余显示为透明
            this.context.fillRect(0,0,this.canvas.width,this.canvas.height);
            this.context.globalCompositeOperation = 'lighter'; // 设置模式为: 源图像 + 目标图像。重叠部分的颜色会重新计算
            // context.clearRect(0,0,WINDOW_WIDTH,WINDOW_HEIGHT)
            for(var i =0; i<this.num ; i++){
                var star = this.stars[i];
                if(i == this.rnd){
                    star.vx = -5;
                    star.vy = 20;
                    this.context.beginPath();
                    this.context.strokeStyle = '#00ffff';
                    this.context.lineWidth = star.r;
                    this.context.moveTo(star.x,star.y);
                    this.context.lineTo(star.x+star.vx,star.y+star.vy);
                    this.context.stroke();
                    this.context.closePath();
                }
                star.alpha += star.ra;
                if(star.alpha<=0){
                    star.alpha = 0;
                    star.ra = -star.ra;
                    star.vx = Math.random()*0.2-0.1;
                    star.vy = Math.random()*0.2-0.1;
                }else if(star.alpha>1){
                    star.alpha = 1;
                    star.ra = -star.ra
                }
                star.x += star.vx;
                if(star.x>=this.canvas.width){
                    star.x = 0;
                }else if(star.x<0){
                    star.x = this.canvas.width;
                    star.vx = Math.random()*0.2-0.1;
                    star.vy = Math.random()*0.2-0.1;
                }
                star.y += star.vy;
                if(star.y>=this.canvas.height){
                    star.y = 0;
                    star.vy = Math.random()*0.2-0.1;
                    star.vx = Math.random()*0.2-0.1;
                }else if(star.y<0){
                    star.y = this.canvas.height;
                }
                this.context.beginPath();
                var bg = this.context.createRadialGradient(star.x, star.y, 0, star.x, star.y, star.r);
                bg.addColorStop(0,'rgba(255,255,255,'+star.alpha+')')
                bg.addColorStop(1,'rgba(255,255,255,0)')
                this.context.fillStyle  = bg;
                this.context.arc(star.x,star.y, star.r, 0, Math.PI*2, true);
                this.context.fill();
                this.context.closePath();
            }
        },33);
		this.liuxing();

		// render();
		//document.body.addEventListener('mousemove',this.mouseMove);

        
    
    }

    liuxing(){
		var time = Math.round(Math.random()*3000+33);
		setTimeout(()=>{
			this.rnd = Math.ceil(Math.random()*this.stars.length)
			this.liuxing();
		},time)
	}

	mouseMove(e){
		//因为是整屏背景，这里不做坐标转换
		this.mouseX = e.clientX;
		this.mouseY = e.clientY;
	}


	addStar(){
		for(var i = 0; i<this.num ; i++){
			var aStar = {
				x:Math.round(Math.random()*this.canvas.width),
				y:Math.round(Math.random()*this.canvas.height),
				r:Math.random()*3,
				ra:Math.random()*0.05,
				alpha:Math.random(),
				vx:Math.random()*0.2-0.1,
				vy:Math.random()*0.2-0.1
			}
			this.stars.push(aStar);
		}
	}
}