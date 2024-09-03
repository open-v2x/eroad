<template>
    <div class="num-cell">
        <div class="num" v-for="(num,i) in numArr" :key="i">
          <div class="num-item" :style="{transform: `translateY(-${num*3.385}vw)`}">
            <div>0</div>
            <div>1</div>
            <div>2</div>
            <div>3</div>
            <div>4</div>
            <div>5</div>
            <div>6</div>
            <div>7</div>
            <div>8</div>
            <div>9</div>
          </div>
        </div>
    </div>
</template>

<script>
export default {
  props: {
    carTotal: Number,
  },
  data() {
    return {
      numArr:[],
    };
  },
  watch: {
    carTotal:{
      handler() {
        this.change()
      },
      immediate: true

    }
  },
  mounted() {
    this.change()
  },
  methods: {
    // 数值转换
    change() {
        this.carTotalArray = (Array(5).join(0)+ this.carTotal.toString()).slice(-6)
        this.numArr = this.carTotalArray.split('')
    },
  },
}
</script>

<style lang="less" scoped>
.num-cell{
  display:flex;
  flex-direction: row;
  .num {
    display: inline-block;
    flex: 1;
    margin-left: 7px;
    box-sizing: border-box;
    width: 40px;
    height: 65px;
    background: linear-gradient(180deg, rgba(1, 255, 255, 0) 0%, rgba(1, 255, 255, 0.6) 48.23%, rgba(1, 255, 255, 0) 100%), #032838;
    border: 0.597774px solid #47EBEB;
    border-radius: 4.78219px;
    font-family: 'Acens';
    font-style: normal;
    font-weight: 400;
    font-size: 40px;
    line-height: 65px;
    text-align: center;
    color: #47EBEB;
    overflow: hidden;
    .num-item {
      transition: all 1s ease-out;
    }
  }
  .num>div{
    width: 40px;
    height: 65px;
  }
}
</style>
