<template>
  <div class="prompt-generator">
    <!-- 项目选择阶段 -->
    <div v-if="!selectedProject" class="project-selection">
      <div class="selection-header">
        <h2>选择项目</h2>
        <p>请选择一个项目来使用其配置的AI模型生成Prompt</p>
      </div>
      
      <el-row :gutter="20">
        <el-col :span="24">
          <el-input
            placeholder="搜索项目名称..."
            v-model="searchKeyword"
            @input="filterProjects"
            prefix-icon="el-icon-search"
            style="margin-bottom: 20px;">
          </el-input>
        </el-col>
      </el-row>
      
      <el-row :gutter="20" v-loading="loading">
        <el-col :span="8" v-for="project in filteredProjects" :key="project.id">
          <el-card 
            class="project-card" 
            :body-style="{ padding: '20px' }"
            @click.native="selectProject(project)"
            shadow="hover">
            <div class="project-info">
              <h3>{{ project.projectName }}</h3>
              <p class="project-code">编码: {{ project.projectCode }}</p>
              <p class="project-owner">拥有者: {{ project.ownerNickname }}</p>
              <div class="project-config" v-if="project.apiKey">
                <el-tag type="success" size="mini">已配置AI</el-tag>
                <span class="model-name">{{ project.modelName }}</span>
              </div>
              <div v-else>
                <el-tag type="warning" size="mini">未配置AI</el-tag>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      
      <div v-if="filteredProjects.length === 0 && !loading" class="no-projects">
        <el-empty description="没有找到项目">
          <el-button type="primary" @click="$router.push('/createProject')">创建项目</el-button>
        </el-empty>
      </div>
    </div>
    
    <!-- Prompt生成阶段 -->
    <div v-if="selectedProject" class="prompt-generation">
      <div class="generation-header">
        <el-button @click="backToSelection" icon="el-icon-arrow-left" type="text">返回项目选择</el-button>
        <h2>{{ selectedProject.projectName }} - Prompt生成</h2>
        <div class="project-config-info">
          <el-tag type="primary" size="small">{{ selectedProject.modelName }}</el-tag>
          <span class="api-url">{{ getDisplayUrl(selectedProject.apiUrl) }}</span>
        </div>
      </div>
      
      <el-row :gutter="20">
        <el-col :span="16">
          <el-card class="chat-card">
            <div slot="header" class="card-header">
              <span>对话区域</span>
              <el-button @click="clearChat" type="text" icon="el-icon-delete">清空对话</el-button>
            </div>
            
            <!-- 消息列表 -->
            <div class="message-list" ref="messageList">
              <div v-for="(message, index) in messages" :key="index" class="message-item">
                <div :class="['message', message.role]">
                  <div class="message-header">
                    <span class="role-label">{{ message.role === 'user' ? '用户' : 'AI助手' }}</span>
                    <span class="timestamp">{{ formatTime(message.timestamp) }}</span>
                  </div>
                  <div class="message-content" v-html="formatMessage(message.content)"></div>
                  <div v-if="message.role === 'assistant'" class="message-actions">
                    <el-button @click="copyMessage(message.content)" type="text" size="mini" icon="el-icon-copy-document">复制</el-button>
                  </div>
                </div>
              </div>
              
              <!-- 加载中提示 -->
              <div v-if="isGenerating" class="message-item">
                <div class="message assistant loading">
                  <div class="message-header">
                    <span class="role-label">AI助手</span>
                  </div>
                  <div class="message-content">
                    <i class="el-icon-loading"></i> 正在生成回复...
                  </div>
                </div>
              </div>
            </div>
            
            <!-- 输入区域 -->
            <div class="input-area">
              <el-input
                type="textarea"
                :rows="4"
                placeholder="请输入您的问题或需求..."
                v-model="currentMessage"
                @keydown.ctrl.enter.native="sendMessage"
                :disabled="isGenerating">
              </el-input>
              <div class="input-actions">
                <span class="shortcut-tip">Ctrl + Enter 发送</span>
                <el-button 
                  type="primary" 
                  @click="sendMessage" 
                  :loading="isGenerating"
                  :disabled="!currentMessage.trim()">
                  {{ isGenerating ? '生成中...' : '发送' }}
                </el-button>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="8">
          <el-card class="sidebar-card">
            <div slot="header">
              <span>快速模板</span>
            </div>
            <div class="template-list">
              <el-button 
                v-for="template in promptTemplates" 
                :key="template.id"
                @click="useTemplate(template.content)"
                type="text"
                class="template-button">
                {{ template.name }}
              </el-button>
            </div>
          </el-card>
          
          <el-card class="sidebar-card" style="margin-top: 20px;">
            <div slot="header">
              <span>项目信息</span>
            </div>
            <div class="project-details">
              <p><strong>项目名称:</strong> {{ selectedProject.projectName }}</p>
              <p><strong>项目编码:</strong> {{ selectedProject.projectCode }}</p>
              <p><strong>AI模型:</strong> {{ selectedProject.modelName }}</p>
              <p><strong>拥有者:</strong> {{ selectedProject.ownerNickname }}</p>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script>
import { getRequest, postJsonRequest } from '../utils/api'

export default {
  name: 'PromptGenerator',
  data() {
    return {
      loading: false,
      isGenerating: false,
      searchKeyword: '',
      selectedProject: null,
      allProjects: [],
      filteredProjects: [],
      messages: [],
      currentMessage: '',
      promptTemplates: [
        {
          id: 1,
          name: '代码生成',
          content: '请帮我生成一个用于[具体功能]的代码，使用[编程语言]，要求[具体要求]。'
        },
        {
          id: 2,
          name: '文档编写',
          content: '请帮我编写关于[主题]的技术文档，包括[具体内容要求]。'
        },
        {
          id: 3,
          name: '问题解答',
          content: '我遇到了一个关于[技术领域]的问题：[具体问题描述]，请帮我分析并提供解决方案。'
        },
        {
          id: 4,
          name: '代码优化',
          content: '请帮我优化以下代码，提高其性能和可读性：\n[粘贴代码]'
        },
        {
          id: 5,
          name: '学习指导',
          content: '我想学习[技术/概念]，请为我制定一个学习计划和推荐相关资源。'
        }
      ]
    }
  },
  mounted() {
    this.loadProjects()
  },
  methods: {
    async loadProjects() {
      this.loading = true
      try {
        const response = await getRequest('/project/list')
        if (response.data.status === 'success') {
          this.allProjects = response.data.obj || []
          this.filterProjects()
        } else {
          this.$message.error('加载项目列表失败：' + response.data.msg)
        }
      } catch (error) {
        this.$message.error('加载项目列表失败：' + error.message)
      } finally {
        this.loading = false
      }
    },
    
    filterProjects() {
      if (!this.searchKeyword) {
        this.filteredProjects = this.allProjects
      } else {
        this.filteredProjects = this.allProjects.filter(project => 
          project.projectName.toLowerCase().includes(this.searchKeyword.toLowerCase()) ||
          project.projectCode.toLowerCase().includes(this.searchKeyword.toLowerCase())
        )
      }
    },
    
    selectProject(project) {
      if (!project.apiKey) {
        this.$message.warning('该项目未配置AI接口，无法使用Prompt生成功能')
        return
      }
      this.selectedProject = project
      this.messages = []
      this.currentMessage = ''
    },
    
    backToSelection() {
      this.selectedProject = null
      this.messages = []
      this.currentMessage = ''
    },
    
    useTemplate(template) {
      this.currentMessage = template
    },
    
    async sendMessage() {
      if (!this.currentMessage.trim() || this.isGenerating) {
        return
      }
      
      const userMessage = {
        role: 'user',
        content: this.currentMessage.trim(),
        timestamp: new Date()
      }
      
      this.messages.push(userMessage)
      const messageToSend = this.currentMessage.trim()
      this.currentMessage = ''
      this.isGenerating = true
      
      // 滚动到底部
      this.$nextTick(() => {
        this.scrollToBottom()
      })
      
      try {
        const response = await postJsonRequest('/llm/chat', {
          apiKey: this.selectedProject.apiKey,
          apiUrl: this.selectedProject.apiUrl,
          modelName: this.selectedProject.modelName,
          message: messageToSend
        })
        
        if (response.data.status === 'success') {
          const assistantMessage = {
            role: 'assistant',
            content: response.data.obj,
            timestamp: new Date()
          }
          this.messages.push(assistantMessage)
        } else {
          this.$message.error('AI调用失败：' + response.data.msg)
          // 添加错误消息
          const errorMessage = {
            role: 'assistant',
            content: '抱歉，AI调用失败：' + response.data.msg,
            timestamp: new Date(),
            isError: true
          }
          this.messages.push(errorMessage)
        }
      } catch (error) {
        this.$message.error('网络请求失败：' + error.message)
        const errorMessage = {
          role: 'assistant',
          content: '抱歉，网络请求失败：' + error.message,
          timestamp: new Date(),
          isError: true
        }
        this.messages.push(errorMessage)
      } finally {
        this.isGenerating = false
        this.$nextTick(() => {
          this.scrollToBottom()
        })
      }
    },
    
    clearChat() {
      this.$confirm('确定要清空所有对话记录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.messages = []
        this.$message.success('对话记录已清空')
      })
    },
    
    copyMessage(content) {
      const textarea = document.createElement('textarea')
      textarea.value = content
      document.body.appendChild(textarea)
      textarea.select()
      document.execCommand('copy')
      document.body.removeChild(textarea)
      this.$message.success('内容已复制到剪贴板')
    },
    
    formatMessage(content) {
      // 简单的格式化，将换行符转换为<br>
      return content.replace(/\n/g, '<br>')
    },
    
    formatTime(timestamp) {
      return new Date(timestamp).toLocaleTimeString()
    },
    
    getDisplayUrl(url) {
      try {
        const urlObj = new URL(url)
        return urlObj.hostname
      } catch (error) {
        return url
      }
    },
    
    scrollToBottom() {
      const messageList = this.$refs.messageList
      if (messageList) {
        messageList.scrollTop = messageList.scrollHeight
      }
    }
  }
}
</script>

<style scoped>
.prompt-generator {
  padding: 20px;
}

.selection-header {
  text-align: center;
  margin-bottom: 30px;
}

.selection-header h2 {
  color: #303133;
  margin-bottom: 10px;
}

.selection-header p {
  color: #606266;
  font-size: 14px;
}

.project-card {
  margin-bottom: 20px;
  cursor: pointer;
  transition: all 0.3s;
}

.project-card:hover {
  transform: translateY(-2px);
}

.project-info h3 {
  margin: 0 0 10px 0;
  color: #303133;
}

.project-code, .project-owner {
  margin: 5px 0;
  color: #606266;
  font-size: 13px;
}

.project-config {
  margin-top: 10px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.model-name {
  font-size: 12px;
  color: #909399;
}

.no-projects {
  text-align: center;
  margin-top: 50px;
}

.generation-header {
  margin-bottom: 20px;
}

.generation-header h2 {
  margin: 10px 0;
  color: #303133;
}

.project-config-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.api-url {
  font-size: 12px;
  color: #909399;
}

.chat-card {
  height: 600px;
  display: flex;
  flex-direction: column;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 10px 0;
  max-height: 400px;
}

.message-item {
  margin-bottom: 15px;
}

.message {
  padding: 12px 15px;
  border-radius: 8px;
  max-width: 80%;
}

.message.user {
  background-color: #409EFF;
  color: white;
  margin-left: auto;
}

.message.assistant {
  background-color: #F5F7FA;
  color: #303133;
  margin-right: auto;
}

.message.assistant.loading {
  background-color: #E6F7FF;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 5px;
  font-size: 12px;
}

.role-label {
  font-weight: bold;
}

.timestamp {
  opacity: 0.7;
}

.message-content {
  line-height: 1.5;
  word-wrap: break-word;
}

.message-actions {
  margin-top: 8px;
  text-align: right;
}

.input-area {
  border-top: 1px solid #EBEEF5;
  padding-top: 15px;
}

.input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
}

.shortcut-tip {
  font-size: 12px;
  color: #909399;
}

.sidebar-card {
  margin-bottom: 20px;
}

.template-list {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.template-button {
  text-align: left;
  padding: 8px 12px;
  border: 1px solid #EBEEF5;
  border-radius: 4px;
  background-color: #FAFAFA;
  transition: all 0.3s;
}

.template-button:hover {
  background-color: #F0F9FF;
  border-color: #409EFF;
}

.project-details p {
  margin: 8px 0;
  font-size: 13px;
  color: #606266;
}

.project-details strong {
  color: #303133;
}
</style>
