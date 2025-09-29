<template>
  <div>
    <div class="create-header">
      <h2>{{ isEdit ? '编辑项目' : '创建项目' }}</h2>
      <p style="color: #666;">{{ isEdit ? '修改项目信息和配置' : '创建新项目并配置AI服务参数' }}</p>
    </div>

    <el-card style="max-width: 650px; margin: 0 auto;">
      <div class="create-form">
        <el-form :model="projectForm" :rules="rules" ref="projectForm" label-width="130px">
      <el-form-item label="项目名称" prop="projectName">
        <el-input v-model="projectForm.projectName" placeholder="请输入项目名称"></el-input>
      </el-form-item>
      
      <el-form-item label="项目开发规范" prop="developmentSpec">
        <el-input 
          type="textarea" 
          v-model="projectForm.developmentSpec" 
          :rows="6"
          placeholder="请输入项目开发规范，包括代码规范、命名规范、目录结构等"></el-input>
      </el-form-item>
      
      <el-form-item label="API Key" prop="apiKey">
        <el-input v-model="projectForm.apiKey" placeholder="请输入API Key" show-password></el-input>
      </el-form-item>

      <el-form-item label="API URL" prop="apiUrl">
        <el-input v-model="projectForm.apiUrl" placeholder="请输入API URL"></el-input>
      </el-form-item>

      <el-form-item label="Model Name" prop="modelName">
        <el-input v-model="projectForm.modelName" placeholder="请输入模型名称"></el-input>
      </el-form-item>

      <el-form-item label="示例Mapper路径" prop="exampleMapperPath">
        <el-input v-model="projectForm.exampleMapperPath" placeholder="请输入示例Mapper类路径"></el-input>
      </el-form-item>

      <el-form-item label="示例实体类路径" prop="exampleEntityPath">
        <el-input v-model="projectForm.exampleEntityPath" placeholder="请输入示例实体类路径"></el-input>
      </el-form-item>

      <el-form-item label="示例接口路径" prop="exampleInterfacePath">
        <el-input v-model="projectForm.exampleInterfacePath" placeholder="请输入示例接口路径"></el-input>
      </el-form-item>
    </el-form>

    <!-- 项目预览 -->
    <div v-if="projectPreview" class="project-preview">
      <el-divider>项目预览</el-divider>
      <div class="preview-content">
        <el-row :gutter="20">
          <el-col :span="12">
            <div class="preview-item">
              <label>项目名称：</label>
              <span>{{ projectPreview.projectName }}</span>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="preview-item">
              <label>AI模型：</label>
              <span>{{ projectPreview.modelName || '未配置' }}</span>
            </div>
          </el-col>
        </el-row>
        <div class="preview-item" style="margin-top: 15px;">
          <label>开发规范预览：</label>
          <span style="display: block; margin-top: 5px; color: #666;">
            {{ projectPreview.developmentSpec ? (projectPreview.developmentSpec.length > 50 ? projectPreview.developmentSpec.substring(0, 50) + '...' : projectPreview.developmentSpec) : '暂无开发规范' }}
          </span>
        </div>
      </div>
    </div>

    <div class="create-actions">
      <el-button type="primary" @click="submitForm" :loading="submitting" style="margin-right: 10px;">
        {{ isEdit ? '更新项目' : '创建项目' }}
      </el-button>
      <el-button @click="resetForm" style="margin-right: 10px;">重置</el-button>
      <el-button @click="goBack">返回</el-button>
    </div>
      </div>
    </el-card>

    <!-- 创建成功对话框 -->
    <el-dialog title="项目创建成功" :visible.sync="successDialogVisible" width="500px" :close-on-click-modal="false">
      <div style="text-align: center;">
        <i class="el-icon-success" style="font-size: 50px; color: #67C23A;"></i>
        <h3 style="margin: 20px 0;">项目创建成功！</h3>
        <p>您的项目编码是：</p>
        <div style="margin: 20px 0;">
          <el-tag type="success" size="large" style="font-size: 18px; padding: 10px 20px;">
            {{ createdProjectCode }}
          </el-tag>
        </div>
        <p style="color: #999; font-size: 14px;">
          请将此编码分享给团队成员，他们可以通过此编码加入项目
        </p>
        <div style="margin-top: 30px;">
          <el-button type="primary" @click="copyProjectCode">复制编码</el-button>
          <el-button @click="goToProjectList">查看项目列表</el-button>
        </div>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import { postRequest, putRequest, getRequest, postJsonRequest, putJsonRequest } from '../utils/api'

export default {
  name: 'CreateProject',
  data() {
    return {
      isEdit: false,
      projectId: null,
      projectForm: {
        projectName: '',
        developmentSpec: '',
        apiKey: '',
        apiUrl: '',
        modelName: '',
        exampleMapperPath: '',
        exampleEntityPath: '',
        exampleInterfacePath: ''
      },
      rules: {
        projectName: [
          { required: true, message: '请输入项目名称', trigger: 'blur' },
          { min: 2, max: 50, message: '项目名称长度在 2 到 50 个字符', trigger: 'blur' }
        ]
      },
      submitting: false,
      successDialogVisible: false,
      createdProjectCode: '',
      projectPreview: null
    }
  },
  watch: {
    projectForm: {
      handler() {
        this.updateProjectPreview()
      },
      deep: true
    }
  },
  mounted() {
    // 检查是否是编辑模式
    if (this.$route.query.id) {
      this.isEdit = true
      this.projectId = this.$route.query.id
      this.loadProjectData()
    } else {
      this.updateProjectPreview()
    }
  },
  methods: {
    loadProjectData() {
      getRequest(`/project/detail/${this.projectId}`).then(resp => {
        if (resp.data.status === 'success') {
          const project = resp.data.obj
          this.projectForm = {
            projectName: project.projectName || '',
            developmentSpec: project.developmentSpec || '',
            apiKey: project.apiKey || '',
            apiUrl: project.apiUrl || '',
            modelName: project.modelName || '',
            exampleMapperPath: project.exampleMapperPath || '',
            exampleEntityPath: project.exampleEntityPath || '',
            exampleInterfacePath: project.exampleInterfacePath || ''
          }
        } else {
          this.$message.error(resp.data.msg)
          this.goBack()
        }
      }).catch(() => {
        this.$message.error('加载项目数据失败')
        this.goBack()
      })
    },
    
    submitForm() {
      this.$refs.projectForm.validate((valid) => {
        if (valid) {
          this.submitting = true
          
          if (this.isEdit) {
            this.updateProject()
          } else {
            this.createProject()
          }
        }
      })
    },
    
    createProject() {
      postJsonRequest('/project/create', this.projectForm).then(resp => {
        this.submitting = false
        if (resp.data.status === 'success') {
          this.createdProjectCode = resp.data.obj.projectCode
          this.successDialogVisible = true
          this.resetForm()
        } else {
          this.$message.error(resp.data.msg)
        }
      }).catch(() => {
        this.submitting = false
        this.$message.error('创建项目失败')
      })
    },
    
    updateProject() {
      const updateData = {
        id: this.projectId,
        ...this.projectForm
      }
      
      putJsonRequest('/project/update', updateData).then(resp => {
        this.submitting = false
        if (resp.data.status === 'success') {
          this.$message.success('项目更新成功')
          this.goBack()
        } else {
          this.$message.error(resp.data.msg)
        }
      }).catch(() => {
        this.submitting = false
        this.$message.error('更新项目失败')
      })
    },
    
    updateProjectPreview() {
      // 当项目名称不为空时显示预览
      if (this.projectForm.projectName.trim()) {
        this.projectPreview = {
          projectName: this.projectForm.projectName,
          modelName: this.projectForm.modelName,
          developmentSpec: this.projectForm.developmentSpec
        }
      } else {
        this.projectPreview = null
      }
    },

    resetForm() {
      this.$refs.projectForm.resetFields()
      this.projectPreview = null
    },
    
    goBack() {
      this.$router.go(-1)
    },
    
    copyProjectCode() {
      const code = this.createdProjectCode
      if (navigator.clipboard) {
        navigator.clipboard.writeText(code).then(() => {
          this.$message.success('项目编码已复制到剪贴板')
        })
      } else {
        // 兼容旧浏览器
        const textArea = document.createElement('textarea')
        textArea.value = code
        document.body.appendChild(textArea)
        textArea.select()
        document.execCommand('copy')
        document.body.removeChild(textArea)
        this.$message.success('项目编码已复制到剪贴板')
      }
    },
    
    goToProjectList() {
      this.successDialogVisible = false
      this.$router.push('/projectList')
    }
  }
}
</script>

<style scoped>
.create-header {
  text-align: center;
  margin-bottom: 30px;
}

.create-form {
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

.create-actions {
  text-align: center;
  margin-top: 30px;
}

</style>
