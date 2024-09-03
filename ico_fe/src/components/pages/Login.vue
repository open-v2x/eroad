<template>
  <div id="container">
    <video autoplay muted  class="login-wrap" src="/video/content.mp4">    </video>
    <div id="main_content">
        <div id="head">
            <h1>数字道路智能运营中心</h1>
             <br/>
            <!-- <p class="sub_head">Rongdong Digital Road Intelligent Operation Center.</p>
            <p class="info">数字、感知、协同、联接</p> -->
        </div>
        <br/>
        <el-form :inline="true" :model="ruleForm" :rules="rules" ref="ruleForm" label-width="0" class="content">
            <!-- <el-form-item label="" prop="name"> -->
                <!-- <el-input class="username_input" v-model="ruleForm.name" placeholder="用户名"></el-input> -->
                <input class="username_input" v-model="ruleForm.name"/>
            <!-- </el-form-item> -->
            <br/>
            <!-- <el-form-item label="" prop="password"> -->
                <!-- <el-input class="password_input" placeholder="密码" v-model="ruleForm.password" show-password  @keyup.enter.native='login'></el-input> -->
                <input class="password_input" type="password"  v-model="ruleForm.password"/>
            <!-- </el-form-item> -->
            <br/>
            <!-- <el-form-item> -->
                <div id="wrapper">
                    <div id="drop_box">
                        <span class="iconfont icon-jiantouyou" id="spanBox"></span>
                    </div>
                    <p id="text">拖动滑块验证登录</p>
                    <div id="bg">

                    </div>
                </div>
            <!-- </el-form-item> -->
            <br/>
            <!-- <el-form-item>
                <el-button class="login_btn" round @click="login">登录</el-button>
            </el-form-item> -->
        </el-form>


    </div>


  </div>
</template>

<script>
import {vedioLogin} from '../../assets/js/api_video'
import { loginOs, getUserInfo } from '../../assets/js/api'
import identifyCode from '../common/IdentifyCode.vue'
import { Fc3DBtn } from 'fancy-components'
new Fc3DBtn()

var dropBox;
var wrapper;
var main;
var text;
var bg;
var spanBox;
var classVal;

export default {
    components: {
        identifyCode
    },
    data() {
        return {
            flag:false,
            ruleForm: {
                name: '',
                password: '',
                code: ''
            },
            identifyCodeVal: '',
            rules: {
                name: [
                     { required: true, message: '请输入用户名', trigger: 'blur' },
                ],
                password: [
                    { required: true, message: '请输入密码', trigger: 'blur' },
                ],
                code: [
                    { required: true, message: '请输入验证码', trigger: 'blur' },
                    {
                        validator: (rule, value, cb) => {
                            if (+value === +this.identifyCodeVal) {
                                cb()
                            } else {
                                 cb(new Error('验证码错误'))
                            }
                        },
                        trigger: 'blur'
                    }
                ]
            },
            beginClientX:0,               // 距离屏幕左端距离
            mouseMoveStata:false,         // 触发拖动状态  判断
            maxwidth:'',                  // 拖动最大宽度，依据滑块宽度算出来的
            confirmWords:'拖动滑块登录',   // 滑块文字
            confirmSuccess:false          // 验证成功判断
        }
    },
    created() {
      // this.vedioNewLogin()
    },
    mounted() {
        console.log(this.$route.params)
        if(this.$route.params.expire){
          this.$message.warning('身份验证已过期,请重新登录！');
        }
        localStorage.setItem('vedioToken', null)
        var that = this;
        dropBox = document.querySelector('#drop_box');
        wrapper = document.querySelector('#wrapper');
        main = document.querySelector('#main_content')
        text = document.querySelector('#text');
        bg = document.querySelector('#bg');
        spanBox = document.querySelector('#spanBox');
        classVal = document.getElementById("spanBox").getAttribute("class");
        classVal = classVal.replace("icon-jiantouyou","icon-zhengque");
        dropBox.onmousedown = function(event){
            var downX = event.clientX;//鼠标按下去的clientX
            main.onmousemove = function(event){
                var moveX = event.clientX - downX;//鼠标移动的clientX
                if(moveX > 0){
                    dropBox.style.left = moveX + 'px';
                    bg.style.width = moveX + 'px';
                    if(moveX >= wrapper.offsetWidth - dropBox.offsetWidth ){
                        dropBox.style.left = wrapper.offsetWidth - dropBox.offsetWidth + 'px';
                        bg.style.width = wrapper.offsetWidth - dropBox.offsetWidth + 'px';
                        that.login();
                        this.onmousemove = null;
                    }
                }
            }
        };
        //松开
        dropBox.onmouseup = function(event){
            this.onmousemove = null;
            if(that.flag) return;
            this.style.left = 0;
            bg.style.width = 0;
        }

    },
    methods: {
        vedioNewLogin(){
            vedioLogin().then(res=>{
                console.log(res)
                localStorage.setItem('vedioToken', JSON.stringify(res.data))
            })
        },

        login() {
            this.$refs['ruleForm'].validate((valid) => {
                if (valid) {
                    let pas = new Buffer(this.ruleForm.password).toString('base64')
                    let row = {
                        grant_type: 'password',
                        username: this.ruleForm.name,
                        password: pas,
                        scope: 'server'
                    }
                    // const params = new URLSearchParams();
                    // params.append('grant_type', row.grant_type)
                    // params.append('username', row.username)
                    // params.append('password', pas)
                    // params.append('grant_type', row.grant_type)
                    // params.append('scope', row.scope)
                    loginOs(row).then(res => {
                        if(res.code == 1){
                            this.$message.warning(res.msg);
                            dropBox.style.left = 0 + 'px';
                            bg.style.width = 0 + 'px';
                        }else{
                            localStorage.setItem('tokenObj', JSON.stringify(res))
                            this.vedioNewLogin()
                            getUserInfo().then(res => {
                                this.flag = true;
                                text.innerText = '验证登录成功';
                                text.style.color = '#fff';
                                spanBox.setAttribute("class",classVal );//添加正确通过的icon类
                                main.onmousemove = null;//移除移动事件
                                dropBox.onmousedown = null;//移除鼠标按下事件
                                console.log(res)
                                localStorage.setItem('userInfo', JSON.stringify(res.data))
                                this.$router.push({
                                    path: '/roadPerception'
                                })
                            })
                        }
                    }).catch(e => {
                        this.$message.warning('网络不可用！');
                        dropBox.style.left = 0 + 'px';
                        bg.style.width = 0 + 'px';
                    })
                } else {
                    return false
                }
            })
        },

    }
}
</script>
<style lang="less"  scoped>
#container {
    overflow: hidden;
    position: absolute;
    top: 0; left: 0; right: 0; bottom: 0;
    height: 100%;
    text-align: center;
}
.login-wrap {
    left: 0;
    width: 100%;
    height: 100vh;
    position:fixed;
    object-fit:fill;
    font-size: 22px;
    overflow: hidden;
    z-index: -99;
}
#main_content {
    z-index: 2;
    position: relative;
    display: inline-block;

    /* Vertical center */
    top: 50%;
    transform: translateY(-50%);
    margin:0 auto !important;

}

#main_content h1 {
    text-transform: uppercase;
    font-weight: 600;
    font-family: 'proxima-nova-condensed', Helvetica;
    color: #fff;
    font-size: 35px;
}

#main_content .sub_head {
    color: rgba(255,255,255,0.5);
    font-size: 18px;
}

#main_content .info {
    color: rgba(255,255,255,0.5);
    font-size: 12px;
    margin-top: 10px;
}
/deep/el-form{

}

.content{
    overflow-y:hidden;
    .username_input{
        width:230px ;
        background: rgba(255,255,255,0.20) !important;
        height: 30px;
        border: 2px solid rgba(255,255,255,0.20);
        border-radius: 5px;
        font-size: 16px;
        color: #FFFFFF;
        margin-bottom: 20px;
    }
    .password_input{
        width:230px ;
        height: 30px;
        background: rgba(255,255,255,0.20)!important;
        border: 2px solid rgba(255,255,255,0.20);
        border-radius: 5px;
        font-size: 16px;
        color: #FFFFFF;
    }
    .login_btn{
        width: 230px;
        height: 35px;
        font-size: 16px;
        color: #FFFFFF;
        background: #1F76FF;
        border: 3px solid rgba(255,255,255,0.20);
        border-radius: 5px;
    }
}
#div::-webkit-scrollbar{
    display: none;
}

#wrapper{
				width: 230px;
				height: 30px;
				background: #9F9F9F;
				text-align: center;
				line-height: 30px;
				margin: 20px auto;
				position: relative;
			}
			#drop_box{
				width: 40px;
				height: 30px;
				background-color: #ddd;
				position: absolute;
				z-index:1000
			}
			#bg{
				height: 30px;
				background: #0BB20C;
				position: absolute;
				left: 0px;
				top: 0px;
			}
			#text{
				position: relative;
				z-index: 999;
			}

</style>
