<template>
  <div class="mask">
    <div class="circle-3"></div>
    <div class="centence">
      <span class="letter" v-for="item in content">{{item}}</span>
    </div>
  </div>
</template>

<script setup>
  import { ref } from 'vue';
  let centence = '地图加载中...'
  let content = ref()
  content.value = centence.split('')
</script>

<style lang="less">
.mask {
  width: 100%;
  height: 100vh;
  position: absolute;
  top: 0;
  left: 0;
  z-index: 2;
  background-color: rgba(2, 1, 68, 1);
  display: flex;
  justify-content: center;
  align-items: center;
  flex-direction: column;
}
.centence {
  margin-top: 20px;
  font-size: 20px;
  color: #fff;
  display: flex;
}
.letter {
  animation: letter-animate 1s linear infinite;
  &:nth-child(2) {
    animation-delay: 100ms;
  }
  &:nth-child(3) {
    animation-delay: 200ms;
  }
  &:nth-child(4) {
    animation-delay: 300ms;
  }
  &:nth-child(5) {
    animation-delay: 400ms;
  }
  &:nth-child(6) {
    animation-delay: 500ms;
  }
  &:nth-child(7) {
    animation-delay: 600ms;
  }
  &:nth-child(8) {
    animation-delay: 700ms;
  }
}
.circle-3 {
  width: 90px;
  height: 90px;
  border-radius: 50%;
  display: inline-block;
  position: relative;
  border: 3px solid;
  border-color: #fff #fff transparent transparent;
  animation: rotation 1s linear infinite;
}

.circle-3::after,
.circle-3::before {
  content: "";
  position: absolute;
  left: 0;
  right: 0;
  top: 0;
  bottom: 0;
  margin: auto;
  border-radius: 50%;
  border: 3px solid;
  animation: rotation-back 0.5s linear infinite;
}

.circle-3::after {
  border-color: transparent #01FFFF #01FFFF transparent;
  width: 72px;
  height: 72px;
}

.circle-3::before {
  border-color: transparent transparent #fff #fff;
  width: 54px;
  height: 54px;
}

@keyframes rotation {
  0% {
    transform: rotate(0deg);
  }

  100% {
    transform: rotate(360deg);
  }
}

@keyframes rotation-back {
  0% {
    transform: rotate(0deg);
  }

  100% {
    transform: rotate(-360deg);
  }
}

@keyframes letter-animate {
  0%,100%{
    transform: scale(0.9);
    opacity: 1;
  }
  50% {
    transform: scale(0.7);
    opacity: 0.5;
  }
}
</style>
