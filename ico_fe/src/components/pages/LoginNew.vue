<template>
  <div id="container">
    <video autoplay muted class="login-wrap" src="/video/content.mp4"> </video>
    <div id="main_content">
      <div id="head">
        <h1>数字道路智能运营中心</h1>
        <br />
      </div>
      <br />
      <el-form :inline="true" :model="ruleForm" :rules="rules" ref="ruleForm" label-width="0">
        <el-form-item label="" prop="name">
          <el-input v-model="ruleForm.name" placeholder="用户名" style="width: 318px;height: 42px;"></el-input>
        </el-form-item>
        <br />
        <el-form-item label="" prop="password">
          <el-input placeholder="密码" v-model="ruleForm.password" style="width: 318px;height: 42px;"
            show-password></el-input>
        </el-form-item>
        <br />
        <el-form-item>
          <el-button size="medium" type="primary" style="width: 318px;height: 42px;" @click="login">登录</el-button>
        </el-form-item>
      </el-form>


    </div>
  </div>
</template>
<script>
import { ElMessage } from 'element-plus'
import { vedioLogin } from '../../assets/js/api_video'
import { loginOs, getUserInfo } from '../../assets/js/api'
export default {
  data() {
    return {
      ruleForm: {
        name: '',
        password: '',
        code: ''
      },
      rules: {
        name: [
          { required: true, message: '请输入用户名', trigger: 'blur' },
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
        ],
        // code: [
        //   { required: true, message: '请输入验证码', trigger: 'blur' },
        //   {
        //     validator: (rule, value, cb) => {
        //       if (+value === +this.identifyCodeVal) {
        //         cb()
        //       } else {
        //         cb(new Error('验证码错误'))
        //       }
        //     },
        //     trigger: 'blur'
        //   }
        // ]
      },
    }
  },
  mounted() {
    console.log(this.$route.params)
    if (this.$route.params.expire) {
      this.$message.warning('身份验证已过期,请重新登录！');
    }
    localStorage.setItem('vedioToken', null)
  },
  methods: {
    vedioNewLogin() {
      vedioLogin().then(res => {
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

          loginOs(row).then(res => {
            if (res.code == 1) {
              this.$message.warning(res.msg);
            } else {
              localStorage.setItem('tokenObj', JSON.stringify(res))
              this.vedioNewLogin()
              getUserInfo().then(res => {
                // ElMessage({
                //   showClose: true,
                //   message: '登录成功',
                //   type: 'success',
                //   duration: 500
                // });
                console.log(res)
                localStorage.setItem('userInfo', JSON.stringify(res.data))
                this.$router.push({
                  path: '/roadPerception'
                })
              })
            }
          }).catch(e => {
            this.$message.warning('网络不可用！');
          })
        } else {
          return false
        }
      })
    },

  }
}
</script>
<style>
input:-internal-autofill-previewed,
input:-internal-autofill-selected {
  font-size: 16px;
  -webkit-text-fill-color: #fff;
  transition: background-color 5000s ease-out 0.5s;
}
</style>
<style lang='less' scoped>
input {
  font-size: 16px;
  // 自动填充文本样式
  &:-webkit-autofill::first-line {
    font-size: 16px;
  }
}

#container {
  overflow: hidden;
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  height: 100%;
  text-align: center;
}

/deep/ .el-input__inner {
  color: #fff;
  font-size: 16px;
  font-family: Helvetica Neue, Helvetica, PingFang SC, Hiragino Sans GB, Microsoft YaHei, SimSun, sans-serif !important;
}
/deep/ .el-input__wrapper {
  background-color: hsla(0,0%,100%,.2) !important;
  color: #fff;
}
.login-wrap {
  left: 0;
  width: 100%;
  height: 100vh;
  position: fixed;
  object-fit: fill;
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
  margin: 0 auto !important;
}

#main_content h1 {
  text-transform: uppercase;
  font-weight: 600;
  color: #fff;
  font-size: 35px;
}
</style>
