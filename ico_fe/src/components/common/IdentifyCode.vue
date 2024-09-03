<template>
    <div class="s-canvas" @click="refreshCode">
        <canvas id="s-canvas" :width="contentWidth" :height="contentHeight"></canvas>
        <div class="tips">看不清？点我换一张</div>
    </div>
</template>
<script>
export default {
    name: 'SIdentify',
    props: {
        identifyCode: {
            type: String,
            default: '6798'
        },
        fontSizeMin: {
            type: Number,
            default: document.body.clientWidth / 1920 * 40
        },
        fontSizeMax: {
            type: Number,
            default: document.body.clientWidth / 1920 * 45
        },
        backgroundColorMin: {
            type: Number,
            default: 255
        },
        backgroundColorMax: {
            type: Number,
            default: 255
        },
        colorMin: {
            type: Number,
            default: 0
        },
        colorMax: {
            type: Number,
            default: 160
        },
        lineColorMin: {
            type: Number,
            default: 100
        },
        lineColorMax: { // 干扰线色值
            type: Number,
            default: 255
        },
        dotColorMin: { // 干扰点色值
            type: Number,
            default: 0
        },
        dotColorMax: {
            type: Number,
            default: 255
        },
        contentWidth: { // 画布宽度
            type: Number,
            default: document.body.clientWidth / 1920 * 160
        },
        contentHeight: {
            type: Number,
            default: document.body.clientWidth / 1920 * 60
        }
    },
    data() {
        return {
            
        }
    },
    methods: {
        refreshCode() {
           let code = ''
           for(let i = 0; i < 4; i++) {
               code += this.randomNum(0, 10)
           }
           this.$emit('exportcode', code)
        },
        // 生成一个随机数
        randomNum(min, max) {
            return Math.floor(Math.random() * (max - min) + min)
        },
        // 生成一个随机的颜色
        randomColor(min, max) {
            let r = this.randomNum(min, max)
            let g = this.randomNum(min, max)
            let b = this.randomNum(min, max)
            return 'rgb(' + r + ',' + g + ',' + b + ')'
        },
        drawPic() {
            let canvas = document.getElementById('s-canvas')
            let ctx = canvas.getContext('2d')
            ctx.textBaseline = 'bottom'
            // 绘制背景
            ctx.fillStyle = this.randomColor(this.backgroundColorMin, this.backgroundColorMax)
            ctx.fillRect(0, 0, this.contentWidth, this.contentHeight)
            // 绘制文字
            for (let i = 0; i < this.identifyCode.length; i++) {
                this.drawText(ctx, this.identifyCode[i], i)
            }
            this.drawLine(ctx)
            this.drawDot(ctx)
        },
        drawText(ctx, txt, i) {
            ctx.fillStyle = this.randomColor(this.colorMin, this.colorMax)
            ctx.font = this.randomNum(this.fontSizeMin, this.fontSizeMax) + 'px SimHei'
            let x = (i + 1) * (this.contentWidth / (this.identifyCode.length + 1))
            let y = this.randomNum(this.fontSizeMax, this.contentHeight - 5)
            var deg = this.randomNum(-45, 45)
            // 修改坐标原点和旋转角度
            ctx.translate(x, y)
            ctx.rotate(deg * Math.PI / 180)
            ctx.fillText(txt, 0, 0)
            // 恢复坐标原点和旋转角度
            ctx.rotate(-deg * Math.PI / 180)
            ctx.translate(-x, -y)
        },
        drawLine(ctx) {
            // 绘制干扰线
            for (let i = 0; i < 5; i++) {
                ctx.strokeStyle = this.randomColor(this.lineColorMin, this.lineColorMax)
                ctx.beginPath()
                ctx.moveTo(this.randomNum(0, this.contentWidth), this.randomNum(0, this.contentHeight))
                ctx.lineTo(this.randomNum(0, this.contentWidth), this.randomNum(0, this.contentHeight))
                ctx.stroke()
            }
        },
        drawDot(ctx) {
            // 绘制干扰点
            for (let i = 0; i < 80; i++) {
                ctx.fillStyle = this.randomColor(0, 255)
                ctx.beginPath()
                ctx.arc(this.randomNum(0, this.contentWidth), this.randomNum(0, this.contentHeight), 1, 0, 2 * Math.PI)
                ctx.fill()
            }
        }
    },
    watch: {
        identifyCode() {
            this.drawPic()
        }
    },
    mounted() {
        this.refreshCode()
        this.drawPic()
    }
}
</script>
<style lang='less' coped>
.s-canvas {
    cursor: pointer;
    width: 100%;
    .tips {
        color: #16fffc;
        font-size: 16px;
        margin: 5px 0 0 5px;
    }
}
.s-canvas canvas{
    margin-top: 1px;
    margin-left: 8px;
    border-radius: 5px;
    cursor: pointer;
}
</style>