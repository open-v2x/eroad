<template>
    <div class="selectionOuter">
      <div class="topbar2">
          <input type="text" v-model="inputValue">
          <span class="select" @click="sendAxios">确定</span>
          <span class="select" @click="delAllChecked">取消全部</span>
          <div v-show="roadData[0]" class="tip" ref="tip" @click="changeList"></div>
      </div>
      <ul v-show="roadData[0]||hasOptions" class="list" ref="list">
          <li v-if="hasOptions" class="listItem">暂无数据</li>
          <li v-for="item in roadData" :key="item+index">
              <label :for="item+index" class="labelOuter">
                  <input type="checkbox" :id="item+index" class="check" :value="item" v-model="selectList" @click="doCheck($event)">
                  <span class="item">{{item}}</span>
              </label>
          </li>
      </ul>
    </div>
  </template>
  
  <script>
  let timer
  let cssTimer
  let lock = false
  import {getCrossName} from '../../assets/js/api'
  import { ElMessage } from 'element-plus'
  import axios from 'axios'
  export default {
  props:['index','getAxios','getSelectList','initCrossRoad'],
  data(){
    return{
      selectList: [],
      roadData: [],
      inputValue: '',
      flag:false,
      checkedNum: 0,
      hasOptions: false,
      source: null,
    }
  },
  watch:{
      inputValue(now,old){
          clearTimeout(timer)
          timer = setTimeout(()=>{
              this.hasOptions = false// 新请求可能比较慢，先把暂无数据框去掉，防止误解
              console.log(this.selectList)
              // this.selectList = []之后暂存，不关闭selectList
              // this.checkedNum = 0之后暂存，不取消选中限制
              // if(now!=''){
                  if(typeof this.source === "function"){
                    this.source("终止axios请求,crossRoad")
                  }
                  getCrossName({cross_name: '%'+this.inputValue+'%'},new axios.CancelToken((c)=>{this.source = c})).then(res=>{
                      this.roadData = []
                      if(!res.data.length){
                          this.hasOptions = true
                      }else{
                          this.hasOptions = false
                      }
                      res.data.forEach(item=>{
                          this.roadData.push(item.cross_name)
                      })
                      this.$nextTick(()=>{
                          if(!this.flag){
                              this.changeList()
                          }
                          this.$refs.list.style.display = 'flex'
                      })
                  }).catch(err=>{
                    console.log(err)
                  })
              // }
          },500)
      },
      selectList(now,old){
          this.getSelectList(this.index,now)
      },
      initCrossRoad(now,old){
          this.selectList = now
          this.checkedNum = this.selectList.length
      }
  },
  created(){
      getCrossName({cross_name: '%%'}).then(res=>{
          res.data.forEach(item=>{
              this.roadData.push(item.cross_name)
          })
          
      })
  },
  mounted(){
      setTimeout(()=>{
          
      })
  },
  methods:{
  delAllChecked(){
      if(!lock){
          ElMessage({
              message: '取消成功',
              type: 'success',
              offset: 75,
              icon: '',
              duration: 3000,
              customClass: 'message-override'
          })
          lock = true
          setTimeout(()=>{
              lock = false
          },3000)
      }
      this.selectList = []
      this.checkedNum = 0
  },
  sendAxios(){
      this.getAxios(this.index,this.selectList)
      this.flag = true
      this.changeList()
  },
  doCheck(e){
      // console.log(e,,e.currentTarget)
      // this.$nextTick(()=>{
      e.target.checked ? this.checkedNum++ : this.checkedNum--
      
      if(this.checkedNum>5){
          e.target.checked = false
          this.checkedNum--
      }
      
      // })
  },
  changeList(){
      if(!this.flag){
          clearTimeout(cssTimer)
          cssTimer = null
          this.$refs.list.style.display = 'flex'
          this.$refs.tip.style.transform = "rotate(0deg)"
          // this.$refs.tip.style.transition = "transform 1s,opacity 0.2s;"
          this.$refs.list.style.opacity = '0'
          this.$nextTick(()=>{
              setTimeout(()=>{
                  this.$refs.list.style.opacity = '1'
              })
              
          })
          
      }else{
          this.$refs.tip.style.transform = "rotate(-90deg)"
          this.$refs.list.style.opacity = '0'
          cssTimer = setTimeout(()=>{
              this.$refs.list.style.display = 'none'
          },500)
      }
      this.flag = !this.flag
  }
  }
  }
  </script>
  
  <style lang="less" scoped>
  @height: 10.8vh;
  .selectionOuter{
      position: absolute;
      left: 90px;
      top: 70px;
      height: 20px;
      width: 100%;
      z-index: 10002;
      .topbar2{
          position: relative;
          height: (24/@height);
          display: flex;
          justify-content: flex-start;
          align-items: center;
          .tip{
              position: absolute;
              left: 176px;
              height: (24/@height);
              width: (24/@height);
              background: url(../../assets/img/Overview/tip.png) no-repeat;
              background-size: 100% 100%;
              transform: rotate(-90deg);
              transition: all 1s;
              z-index: 10001;
          }
      }
      .select{
          box-sizing: border-box;
          display: inline-block;
          text-align: center;
          padding: 0 10px;
          margin-left: 10px;
          border: 1px solid #3399FF;
          border-radius: 2px;
          line-height: (24/@height);
          font-family: 'Noto Sans SC';
          font-weight: 400;
          font-size: 12px;
          color: #fff;
          background: rgba(51, 153, 255, 1);
          &:hover{
              cursor: pointer;
              background: rgba(51, 153, 255, .8);
          }
      }
      .list{
      box-sizing: border-box;
      padding: 10px;
      position: absolute;
      display: none;
      flex-direction: column;
      align-items: flex-start;
      backdrop-filter: blur(.5rem);
      min-width: 200px;
      max-height: 250px;
      overflow: auto;
      border: 2px solid rgba(51, 153, 255, 0.3);
      border-radius: 2px;
      background-color: rgba(51, 153, 255, 0.15);
      top: 28px;
      transition: transform 1s,opacity .5s;
      overflow-x:hidden;
      
          .item{
              line-height: 24px;
              font-size: 12px;
              color: rgba(255, 255, 255, 0.6);  
              margin-left: 10px;
              user-select: none;
  
          }
      }
  }
  ul{
      list-style: none;
  }
  .labelOuter{
      display: flex;
      flex-wrap: nowrap;
      align-items: center;
  }
  input[type=text]{
      box-sizing: border-box;
      display: inline-block;
      width: 200px;
      outline: none;
      padding-left: 5px;
      background-color: transparent;
      border: 1px solid rgba(51, 153, 255, 1);
      border-radius: 2px;
      font-size: 12px;
      line-height: (24/@height);
      color: #fff;
  }
  input[type=checkbox]{
      -webkit-appearance: none;
      &:hover{
          cursor: pointer;
      }
  }
  input[type=checkbox]::after{
      content: '';
      display:inline-block;  
      line-height: 16px;  
      width: 16px;  
      height: 16px; 
      font-size: 12px;  
      border: 1px solid rgba(71, 235, 235, 1);  
      border-radius: 8px;
      box-sizing: border-box;
  }
  
  input[type=checkbox]:checked::after{
      content: "√";  
      display: inline-block;
      text-align: center;
      line-height: 16px;  
      width: 16px;  
      height: 16px; 
      font-size: 12px;  
      color: #fff;
      background-color: rgba(71, 235, 235, 1);
      border: 1px solid rgba(71, 235, 235, 1);  
      border-radius: 8px;
      box-sizing: border-box;
  }  
      ::-webkit-scrollbar {
      width: 4px; 
      }
      ::-webkit-scrollbar-track{
      border-radius: 4px;
      background-color:rgba(1, 255, 255, 0.3);
      }
      ::-webkit-scrollbar-thumb{
          border-radius: 4px;
          background-color: #01FFFF;
      }
      ::-webkit-scrollbar-thumb:hover{
          background-color: rgba(1, 255, 255, 0.6);
      }
      .listItem{
      width: 100%;
      margin: 0;
      list-style: none;
      height: 60px;
      line-height: 55px;
      text-align: center;
      user-select: none;
      font-size: 14;
      color: rgba(255, 255, 255, 0.6);
  
      &:hover{
        background-color: transparent;
        cursor: auto;
      }
    }
  </style>