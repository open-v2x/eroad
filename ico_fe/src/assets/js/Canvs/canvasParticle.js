
export default class CanvasParticle {
    constructor(opts={}) {
      this.canvas = opts.el;//document.querySelector('canvas'); // 获取canvas DOM对象
      this.canvasWidth = document.body.offsetWidth; // 获取显示区域的宽度
      this.canvasHeight = document.body.offsetHeight; // 获取显示区域的高度
      this.canvas.width = opts.el.offsetWidth;//this.canvasWidth; // 设置canvas宽度
      this.canvas.height = opts.el.offsetHeight;//this.canvasHeight; // 设置canvas高度
      this.ctx = this.canvas.getContext('2d'); // 获取在画布上绘图的环境

      // setInterval(()=>{
      //   new Particle();
      // },1000/60)

      // 生成粒子数组
      this.particles = new Array(200).fill(
        new Particle(
          Math.floor(Math.random() * this.canvasWidth),
          Math.floor(this.canvasHeight)
        )
      );
      this.animateFrame = null; // requestAnimationFrame事件句柄，用来清除操作
      let self = this;
      (function frame() {
        self.animateFrame = requestAnimationFrame(frame);
        self.animate();
      })();
    }

    // 绘制方法
    draw() {
      this.ctx.lineWidth = 3; // 设置线宽
      this.ctx.globalCompositeOperation = 'destination-in'; // 设置模式为：目标图形和源图形重叠的部分会被保留（源图形），其余显示为透明
      this.ctx.fillRect(0, 0, this.canvasWidth, this.canvasHeight); // 绘制矩形
      this.ctx.globalCompositeOperation = 'lighter'; // 设置模式为: 源图像 + 目标图像。重叠部分的颜色会重新计算
      this.ctx.globalAlpha = 0.8; // 设置画布上绘制图形的不透明度

      this.ctx.beginPath(); // 开始一条新路径
      this.ctx.strokeStyle = '#00ffff'; // 设置画笔路径的颜色
      // 绘制每个粒子产生的新路径
      this.particles.forEach((particle) => {
        this.ctx.moveTo(particle.x, particle.y); // 从粒子的旧坐标
        this.ctx.lineTo(particle.nextX, particle.nextY); // 到粒子的新坐标
      });
      this.ctx.stroke(); // 沿着路径绘制一条线
      this.ctx.closePath(); // 关闭路径
    }

    animate() {
      // 更新粒子数组
      this.particles = this.particles.map((particle) => {
        particle.update(); // 调用粒子实例的更新方法
        if (particle.lifetime < 0) {
          // 如果粒子的生命周期小于0，则生成一个新的粒子
          let x = Math.floor(Math.random() * this.canvasWidth); // 生成随机坐标x
          let y = Math.floor(this.canvasHeight); // 生成随机坐标y
          return new Particle(x, y);
        } else {
          return particle;
        }
      });
      // 调用绘制方法
      this.draw();
    }
  }

  // 粒子类
  class Particle {
    constructor(x, y) {
      this.x = x; // 坐标x值
      this.y = y; // 坐标y值
      this.speedRate = 0.4; // 速率，用来控制粒子流动的快慢
      this.speedX = 0; // 速度在x方向上的增量
      this.speedY = 0; // 速度在y方向上的增量
      this.lifetime = y;//1 + Math.random() * 800; // 粒子生命周期，每次更新都会减小
      this.nextX = x + this.speedX; // 接下来粒子的x坐标
      this.nextY = y + this.speedY; // 接下来粒子的y坐标
    }

    // 更新粒子的方法
    update() {
      this.x = this.nextX; // 更新x坐标
      this.y = this.nextY; // 更新y坐标
      this.speedX += 0;//(Math.random() * 2 - 1) * this.speedRate; // x方向增量
      this.speedY += (Math.random() * 2 - 1) * this.speedRate; // y方向增量
      this.nextX = this.x + this.speedX; // 计算接下来粒子的x坐标
      this.nextY = this.y - this.speedY; // 计算接下来粒子的y坐标
      this.lifetime--; // 生命周期减1
    }
  }
