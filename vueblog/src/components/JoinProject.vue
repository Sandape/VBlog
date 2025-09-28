<template>
  <div>
    <div class="join-header">
      <h2>加入项目</h2>
      <p style="color: #666;">请输入8位项目编码加入项目</p>
    </div>
    
    <el-card style="max-width: 600px; margin: 0 auto;">
      <div class="join-form">
        <el-form :model="joinForm" :rules="rules" ref="joinForm" label-width="100px">
          <el-form-item label="项目编码" prop="projectCode">
            <el-input 
              v-model="joinForm.projectCode" 
              placeholder="请输入8位项目编码"
              maxlength="8"
              @input="handleCodeInput"
              @blur="previewProject"
              style="width: 300px;">
              <template slot="append">
                <el-button @click="previewProject" :loading="previewing">预览</el-button>
              </template>
            </el-input>
          </el-form-item>
        </el-form>
        
        <!-- 项目预览 -->
        <div v-if="projectPreview" class="project-preview">
          <el-divider>项目预览</el-divider>
          <div class="preview-content">
            <el-row :gutter="20">
              <el-col :span="8">
                <div class="preview-item">
                  <label>项目名称：</label>
                  <span>{{ projectPreview.projectName }}</span>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="preview-item">
                  <label>项目拥有者：</label>
                  <span>{{ projectPreview.ownerNickname }}</span>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="preview-item">
                  <label>成员数量：</label>
                  <span>{{ projectPreview.memberCount }}</span>
                </div>
              </el-col>
            </el-row>
          </div>
        </div>
        
        <div class="join-actions">
          <el-button 
            type="primary" 
            @click="joinProject" 
            :disabled="!projectPreview || joining"
            :loading="joining">
            加入项目
          </el-button>
          <el-button @click="resetForm">重置</el-button>
        </div>
      </div>
    </el-card>

    <!-- 加入成功对话框 -->
    <el-dialog title="加入项目成功" :visible.sync="successDialogVisible" width="400px" :close-on-click-modal="false">
      <div style="text-align: center;">
        <i class="el-icon-success" style="font-size: 50px; color: #67C23A;"></i>
        <h3 style="margin: 20px 0;">恭喜您成功加入项目！</h3>
        <p v-if="joinedProjectName">项目名称：{{ joinedProjectName }}</p>
        <div style="margin-top: 30px;">
          <el-button type="primary" @click="goToProjectList">查看我的项目</el-button>
          <el-button @click="continueJoin">继续加入其他项目</el-button>
        </div>
      </div>
    </el-dialog>

    <!-- 使用说明 -->
    <el-card style="max-width: 600px; margin: 30px auto 0;">
      <div slot="header">
        <span>使用说明</span>
      </div>
      <div class="instructions">
        <h4>如何获取项目编码？</h4>
        <ol>
          <li>项目拥有者在创建项目后会获得8位数字编码</li>
          <li>项目拥有者可以在项目详情中查看和复制编码</li>
          <li>项目拥有者将编码分享给团队成员</li>
          <li>团队成员在此页面输入编码即可加入项目</li>
        </ol>
        
        <h4>加入项目后可以做什么？</h4>
        <ul>
          <li>查看项目的开发规范和示例代码路径</li>
          <li>参与项目协作和讨论</li>
          <li>查看项目成员列表</li>
          <li>随时退出项目（项目拥有者除外）</li>
        </ul>
        
        <el-alert
          title="注意"
          type="info"
          description="只有项目拥有者可以查看和编辑项目的API配置信息，普通成员只能查看开发规范和示例路径。"
          show-icon
          :closable="false">
        </el-alert>
      </div>
    </el-card>
  </div>
</template>

<script>
import { postRequest, getRequest, postJsonRequest } from '../utils/api'

export default {
  name: 'JoinProject',
  data() {
    return {
      joinForm: {
        projectCode: ''
      },
      rules: {
        projectCode: [
          { required: true, message: '请输入项目编码', trigger: 'blur' },
          { len: 8, message: '项目编码必须是8位数字', trigger: 'blur' },
          { pattern: /^\d{8}$/, message: '项目编码只能包含数字', trigger: 'blur' }
        ]
      },
      projectPreview: null,
      previewing: false,
      joining: false,
      successDialogVisible: false,
      joinedProjectName: ''
    }
  },
  methods: {
    handleCodeInput(value) {
      // 只允许输入数字
      this.joinForm.projectCode = value.replace(/\D/g, '')
      
      // 清除之前的预览
      if (this.projectPreview) {
        this.projectPreview = null
      }
    },
    
    previewProject() {
      if (!this.joinForm.projectCode || this.joinForm.projectCode.length !== 8) {
        return
      }
      
      this.previewing = true
      this.projectPreview = null
      
      getRequest(`/project/preview/${this.joinForm.projectCode}`).then(resp => {
        this.previewing = false
        if (resp.data.status === 'success') {
          this.projectPreview = resp.data.obj
        } else {
          this.$message.error(resp.data.msg)
        }
      }).catch(() => {
        this.previewing = false
        this.$message.error('获取项目信息失败')
      })
    },
    
    joinProject() {
      this.$refs.joinForm.validate((valid) => {
        if (valid && this.projectPreview) {
          this.joining = true
          
          postJsonRequest('/project/join', { projectCode: this.joinForm.projectCode }).then(resp => {
            this.joining = false
            if (resp.data.status === 'success') {
              this.joinedProjectName = this.projectPreview.projectName
              this.successDialogVisible = true
            } else {
              this.$message.error(resp.data.msg)
            }
          }).catch(() => {
            this.joining = false
            this.$message.error('加入项目失败')
          })
        }
      })
    },
    
    resetForm() {
      this.$refs.joinForm.resetFields()
      this.projectPreview = null
    },
    
    goToProjectList() {
      this.successDialogVisible = false
      this.$router.push('/projectList')
    },
    
    continueJoin() {
      this.successDialogVisible = false
      this.resetForm()
    }
  }
}
</script>

<style scoped>
.join-header {
  text-align: center;
  margin-bottom: 30px;
}

.join-form {
  padding: 20px;
}

.project-preview {
  margin: 20px 0;
}

.preview-content {
  background-color: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
}

.preview-item {
  margin-bottom: 10px;
}

.preview-item label {
  font-weight: bold;
  color: #333;
}

.join-actions {
  text-align: center;
  margin-top: 30px;
}

.instructions {
  line-height: 1.6;
}

.instructions h4 {
  color: #333;
  margin: 20px 0 10px 0;
}

.instructions ol, .instructions ul {
  padding-left: 20px;
}

.instructions li {
  margin-bottom: 5px;
}

.el-alert {
  margin-top: 20px;
}
</style>
