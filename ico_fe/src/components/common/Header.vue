<template>
    <div class="header">
        <!-- 折叠按钮 -->
        <div class="collapse-btn">
            <!-- <img class="logo" src="@/assets/img/logo.png" alt=""> -->
            <!-- <i class="el-icon-menu"></i> -->
        </div>
        <div class="carousel-wrapper" v-if="role!='ROLE_USER_MANAGER_LOOK_OVER'">
            <el-button size="mini" class="carousel-label" type="warning" icon="el-icon-chat-line-round" round>公告</el-button>
            <el-carousel trigger="click" height="40px" indicator-position="none" arrow="never" direction="vertical">
                <el-carousel-item v-for="item in noticeList" :key="item.id">
                    <h3 @click="showDialog(item)" class="small">{{ item.title }}</h3>
                </el-carousel-item>
            </el-carousel>
        </div>
        <div class="logo-msg">品牌站管理系统</div>
        <div class="header-right">
            <div class="header-user-con">
                <!-- 全屏显示 -->
                <div class="btn-fullscreen" @click="handleFullScreen">
                    <el-tooltip effect="dark" :content="fullscreen?`取消全屏`:`全屏`" placement="bottom">
                        <i class="el-icon-rank"></i>
                    </el-tooltip>
                </div>
                <!-- 消息中心 -->
                <!-- <div class="btn-bell">
                    <el-tooltip effect="dark" :content="message?`有${message}条未读消息`:`消息中心`" placement="bottom">
                        <router-link to="/tabs">
                            <i class="el-icon-bell"></i>
                        </router-link>
                    </el-tooltip>
                    <span class="btn-bell-badge" v-if="message"></span>
                </div> -->
                <!-- 用户头像 -->
                <!-- <div class="user-avator"><img src="../../assets/img/img.jpg"></div> -->
                <!-- 用户名下拉菜单 -->
                <el-dropdown class="user-name" trigger="click" @command="handleCommand">
                    <span class="el-dropdown-link">
                        {{username}} <i class="el-icon-caret-bottom"></i>
                    </span>
                    <el-dropdown-menu slot="dropdown">
                        <!-- <a href="http://blog.gdfengshuo.com/about/" target="_blank">
                            <el-dropdown-item>关于作者</el-dropdown-item>
                        </a>
                        <a href="https://github.com/lin-xin/vue-manage-system" target="_blank">
                            <el-dropdown-item>项目仓库</el-dropdown-item>
                        </a> -->
                        <el-dropdown-item v-if="role!='ROLE_USER_MANAGER_LOOK_OVER'" divided  command="updatePassword">修改密码</el-dropdown-item>
                        <el-dropdown-item divided  command="loginout">退出登录</el-dropdown-item>
                    </el-dropdown-menu>
                </el-dropdown>
            </div>
            <!-- 修改密码弹出框 -->
            <el-dialog title="修改密码" :visible.sync="editVisible" width="42%">
                <el-form :rules="rules" ref="form" :model="form" label-width="100px">
                    <el-form-item label="用户名">
                        <el-input v-model="form.username" readonly></el-input>
                    </el-form-item>
                    <el-form-item label="原密码" prop="oldPwd">
                        <el-input type="password" v-model="form.oldPwd" placeholder="请输入原密码" @blur="checkPwd(form.oldPwd)"></el-input>
                    </el-form-item>
                    <el-form-item label="新密码" prop="newPwd">
                        <el-input type="password" v-model="form.newPwd" placeholder="请输入新密码"></el-input>
                    </el-form-item>
                    <el-form-item label="确认新密码" prop="confirmNewPwd">
                        <el-input type="password" v-model="form.confirmNewPwd" placeholder="请再次输入新密码"></el-input>
                    </el-form-item>
                </el-form>
                <span slot="footer" class="dialog-footer">
                    <el-button @click="editVisible = false">取 消</el-button>
                    <el-button type="primary" @click="saveEdit">确 定</el-button>
                </span>
            </el-dialog>
            <!-- 公告详情提示框 -->
            <el-dialog class="notice-wrapper" :visible.sync="showVisible" width="600px" center>
                <!-- <div class="del-dialog-cnt" v-html="contentDetail">

                </div>
                <span slot="footer" class="dialog-footer">
                    <el-button @click="showVisible = false">关 闭</el-button>
                </span> -->
                <!-- <slot name="title">
                    <p class="dialog-title">告 示</p>
                </slot> -->
                <el-form :model="noticeForm" ref="ruleForm" label-width="120px">
                     <div class="notice-title">{{noticeForm.title}}</div>
                     <div class="notice-date">
                         发布日期：{{noticeForm.publishDate}}
                     </div>
                     <div class="notice-attach" v-if="noticeForm.attachmentAddress" @click="downloadAttachment">
                        {{ noticeAttachment }}
                    </div>
                     <div class="notice-content" v-html="noticeForm.contentDetail">
                     </div>
                     <div class="notice-url" v-if="noticeForm.goToUrl">
                        <a target="blank" :href="noticeForm.goToUrl">
                            {{noticeForm.goToUrl}}
                        </a> 
                    </div>
                    <!-- <el-form-item label="公告名称 :">
                        <div>{{noticeForm.title}}</div>
                    </el-form-item>
                    <el-form-item label="公告内容 :">
                        <div v-html="noticeForm.contentDetail"></div>
                    </el-form-item>
                    <el-form-item label="相关链接 :">
                        <div v-if="noticeForm.goToUrl">
                            <a target="blank" :href="noticeForm.goToUrl">
                                {{noticeForm.goToUrl}}
                            </a> 
                        </div>
                    </el-form-item>
                    <el-form-item label="可下载附件 :">
                        <div class="attach" v-if="noticeForm.attachmentAddress" @click="downloadAttachment">
                            {{noticeForm.attachmentAddress}}
                        </div>
                    </el-form-item> -->
                    <!-- <el-form-item>
                        <el-button @click="showVisible = false">关 闭</el-button>
                    </el-form-item> -->
                </el-form>
            </el-dialog>
        </div>
    </div>
</template>
<script>
    import bus from '@/assets/js/bus';
    export default {
        data() {
            return {
                editVisible: false,
                fullscreen: false,
                form: {
                    username: '',
                    oldPwd: '',
                    newPwd: '',
                    confirmNewPwd: ''
                },
                noticeForm: {
                    id: '',
                    title: '',
                    attachmentAddress: '',
                    contentDetail: '',
                    goToUrl: ''
                    
                },
                rules: {
                    oldPwd: [
                        { required: true, message: '请输入原密码', trigger: 'blur' }
                    ],
                    newPwd: [
                        { required: true, message: '请输入新密码', trigger: 'blur' }
                    ],
                    confirmNewPwd: [
                        { required: true, message: '请再次输入新密码', trigger: 'blur' }
                    ],
                },
                message: 2,
                noticeList: [],
                role: '',
                showVisible: false,
                contentDetail: ''
            }
        },
        computed:{
            username(){
                let username = localStorage.getItem('hd_username');
                return username;
            },
            noticeAttachment() {
                let _attach = this.noticeForm.attachmentAddress
                if (_attach) {
                    let idx = _attach.indexOf('-')
                    return idx === -1 ? _attach : _attach.substring(idx + 1)
                }
                return ''
            }
        },
        created() {
            this.role = localStorage.getItem('hd_role');
            // this.getNotificationList()
        },
        methods:{
            downloadAttachment() {
                let appendix = this.noticeForm.attachmentAddress
                if (!appendix) {
                    this.$message.warning('暂无可下载的附件')
                    return
                }
                downloadNotificationAttachment({attachmentAddress: appendix}).then(res => {
                    let _name = res.headers["content-disposition"] || res.headers["Content-Disposition"]
                    if (_name) {
                        let idx = _name.indexOf('filename=')
                        _name = _name.substring(idx+9)
                    } 
                    // 如果拿不到响应头里面的filename就先用一个固定的名称
                    let fileName =  (_name && decodeURIComponent(_name)) || '下载文件'
                    let data = res.data
                    let blob = new Blob([data])
                    let a = document.createElement('a')
                    let url = window.URL.createObjectURL(blob)
                    a.href = url
                    a.download = fileName
                    a.style.display = 'none'
                    document.body.append(a)
                    a.click()
                    a.remove()
                    window.URL.revokeObjectURL(url)
                })
            },
            showDialog(item) {
                this.showVisible = true
                this.noticeForm = item
            },
            getNotificationList() {
                getNotificationListForUser().then(res => {
                    if (res.data.status === 20000) {
                        this.noticeList = res.data.data.objectElements;
                    }
                })
            },
            // 验证原密码
            checkPwd(value) {
                let obj = {
                    oldPwd : this.form.oldPwd
                }
                checkPassword(obj).then(res =>{
                    if(res.data.status !== 20000) {
                        this.$message.warning('原密码输入有误')
                        this.form.oldPwd = ''
                    }
                })
            },
            // 用户名下拉菜单选择事件
            handleCommand(command) {
                if(command == 'loginout'){
                    localStorage.removeItem('hd_username')
                    localStorage.removeItem('hd_token')
                    if (this.role === 'ROLE_USER_MANAGER_LOOK_OVER') {
                        this.$router.push('/user_login')
                    } else {
                        this.$router.push('/login')
                    }
                }else if(command == 'updatePassword'){
                    this.form.username = localStorage.getItem('hd_username');
                    this.editVisible = true;
                }
            },
            // 修改密码
            saveEdit() {
                if(this.form.newPwd === this.form.confirmNewPwd){
                    let obj ={
                        oldPwd : this.form.oldPwd,
                        newPwd: this.form.newPwd
                    }
                    updatePwd(obj).then(res => {
                        if(res.data.status === 20000) {
                            this.$message.success('修改密码成功')
                            this.editVisible = false;
                            this.form.oldPwd = ''
                            this.form.newPwd = ''
                            this.form.confirmNewPwd = ''
                        }
                    })
                }else{
                    this.$message.warning('新密码两次输入不一致')
                    this.form.confirmNewPwd = '';
                }
            },
            // 全屏事件
            handleFullScreen(){
                let element = document.documentElement;
                if (this.fullscreen) {
                    if (document.exitFullscreen) {
                        document.exitFullscreen();
                    } else if (document.webkitCancelFullScreen) {
                        document.webkitCancelFullScreen();
                    } else if (document.mozCancelFullScreen) {
                        document.mozCancelFullScreen();
                    } else if (document.msExitFullscreen) {
                        document.msExitFullscreen();
                    }
                } else {
                    if (element.requestFullscreen) {
                        element.requestFullscreen();
                    } else if (element.webkitRequestFullScreen) {
                        element.webkitRequestFullScreen();
                    } else if (element.mozRequestFullScreen) {
                        element.mozRequestFullScreen();
                    } else if (element.msRequestFullscreen) {
                        // IE11
                        element.msRequestFullscreen();
                    }
                }
                this.fullscreen = !this.fullscreen;
            }
        }
    }
</script>
<style lang="less" scoped>
    .header {
        position: relative;
        box-sizing: border-box;
        width: 100%;
        height: 80px;
        font-size: 22px;
        color: #fff;
    }
    .carousel-wrapper {
        position: absolute;
        line-height: 40px;
        width: 600px;
        margin-left: 500px;
        top: 15px;
        padding: 5px 5px;
        font-size: 14px;
        border-radius: 2px;
    }
    .carousel-label {
        position: absolute;
        left: -80px;
        top: 10px;
        cursor:text;
    }
    .dialog-title {
        font-size: 20px;
        text-align: center;
        padding-bottom: 10px;
    }
    .notice-title {
        text-align: center;
        font-size: 20px;
        // font-weight: 800;
        padding-bottom: 10px;
    }
    .notice-date {
        text-align: center;
        font-size: 12px;
        padding-bottom: 10px;
    }
    .notice-content {
        text-indent: 20px;
        padding: 10px 0 15px;
    }
    .notice-attach {
        text-align: center;
        color:#409EFF;
        text-decoration:underline;
        cursor: pointer;
        padding: 10px 0;
        border-top: 1px solid #e3e3e3;
    }
    .collapse-btn{
        float: left;
        padding: 2px 21px;
        // padding-left: 10px;
        line-height: 80px;

        &>.logo {
            width: 150px;
            height: 80px;
        }
    }
    .header .logo-msg{
        float: left;
        width:280px;
        line-height: 80px;
    }
    .header-right{
        float: right;
        padding-right: 50px;
    }
    .header-user-con{
        display: flex;
        height: 80px;
        align-items: center;
    }
    .btn-fullscreen{
        transform: rotate(45deg);
        margin-right: 5px;
        font-size: 24px;
    }
    .btn-bell, .btn-fullscreen{
        position: relative;
        width: 30px;
        height: 30px;
        text-align: center;
        border-radius: 15px;
        cursor: pointer;
    }
    .btn-bell-badge{
        position: absolute;
        right: 0;
        top: -2px;
        width: 8px;
        height: 8px;
        border-radius: 4px;
        background: #f56c6c;
        color: #fff;
    }
    .btn-bell .el-icon-bell{
        color: #fff;
    }
    .user-name{
        margin-left: 10px;
    }
    .user-avator{
        margin-left: 20px;
    }
    .user-avator img{
        display: block;
        width:40px;
        height:40px;
        border-radius: 50%;
    }
    .el-dropdown-link{
        color: #fff;
        cursor: pointer;
    }
    .el-dropdown-menu__item{
        text-align: center;
    }
</style>
