<template>
  <el-form class="login-container" label-position="left"
           label-width="0px" v-loading="loading">
    <h3 class="login_title">用户注册</h3>
    <el-form-item>
      <el-input type="text" v-model="registerForm.username" auto-complete="off" placeholder="用户名"></el-input>
    </el-form-item>
    <el-form-item>
      <el-input type="password" v-model="registerForm.password" auto-complete="off" placeholder="密码"></el-input>
    </el-form-item>
    <el-form-item>
      <el-input type="email" v-model="registerForm.email" auto-complete="off" placeholder="邮箱"></el-input>
    </el-form-item>
    <el-form-item style="width: 100%">
      <el-button type="primary" @click="handleRegister" style="width: 100%">注册</el-button>
    </el-form-item>
    <el-form-item style="width: 100%">
      <el-button type="success" @click="testAPI" style="width: 100%">测试API连接</el-button>
    </el-form-item>
    <div class="register-link">
      <span>已有账号？</span>
      <el-link type="primary" @click="goToLogin">立即登录</el-link>
    </div>
  </el-form>
</template>
<script>
  import {postRequest} from '../utils/api'
  export default{
    data(){
      return {
        registerForm: {
          username: '',
          password: '',
          email: ''
        },
        loading: false
      }
    },
    methods: {
      handleRegister: function () {
        console.log('注册按钮被点击');
        console.log('表单数据:', this.registerForm);

        var _this = this;
        this.loading = true;

        postRequest('/reg', {
          username: this.registerForm.username,
          password: this.registerForm.password,
          email: this.registerForm.email
        }).then(resp=> {
          console.log('注册响应:', resp);
          _this.loading = false;
          if (resp.status == 200) {
            var json = resp.data;
            if (json.status == 'success') {
              _this.$alert('注册成功！请登录', '成功!', {
                confirmButtonText: '确定',
                callback: action => {
                  _this.$router.push('/');
                }
              });
            } else {
              _this.$alert(json.message || '注册失败！', '失败!');
            }
          } else {
            _this.$alert('注册失败！', '失败!');
          }
        }, error=> {
          console.log('注册请求失败:', error);
          _this.loading = false;
          _this.$alert('找不到服务器，请稍后重试！', '失败!');
        });
      },
      goToLogin: function () {
        this.$router.push('/');
      },
      testAPI: function () {
        console.log('测试API连接...');
        postRequest('/reg', {
          username: 'testuser',
          password: 'testpass',
          email: 'test@example.com'
        }).then(resp=> {
          console.log('API测试成功:', resp);
          this.$alert('API连接正常！响应: ' + JSON.stringify(resp.data), '测试成功');
        }, error=> {
          console.log('API测试失败:', error);
          this.$alert('API连接失败！错误: ' + error.message, '测试失败');
        });
      }
    }
  }
</script>
<style>
  .login-container {
    border-radius: 15px;
    background-clip: padding-box;
    margin: 180px auto;
    width: 350px;
    padding: 35px 35px 15px 35px;
    background: #fff;
    border: 1px solid #eaeaea;
    box-shadow: 0 0 25px #cac6c6;
  }

  .login_title {
    margin: 0px auto 40px auto;
    text-align: center;
    color: #505458;
  }

  .register-link {
    text-align: center;
    margin-top: 20px;
    color: #606266;
  }
</style>
