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
            :class="{ 'no-permission': !canAccessProject(project) }"
            :body-style="{ padding: '20px' }"
            @click.native="selectProject(project)"
            shadow="hover">
            <div class="project-info">
              <h3>{{ project.projectName }}</h3>
              <p class="project-code">编码: {{ project.projectCode }}</p>
              <p class="project-owner">拥有者: {{ project.ownerNickname }}</p>

              <!-- 权限状态 -->
              <div class="project-permission" v-if="!canAccessProject(project)">
                <el-tag type="info" size="mini">需要权限</el-tag>
              </div>
              <div v-else-if="project.userRole === 1">
                <el-tag type="primary" size="mini">项目拥有者</el-tag>
              </div>
              <div v-else>
                <el-tag type="success" size="mini">项目成员</el-tag>
              </div>

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
    
    <!-- Prompt生成表单阶段 -->
    <div v-if="selectedProject" class="prompt-generation">
      <div class="generation-header">
        <el-button @click="backToSelection" icon="el-icon-arrow-left" type="text">返回项目选择</el-button>
        <h2>{{ selectedProject.projectName }} - 接口Prompt生成</h2>
        <div class="project-config-info">
          <el-tag type="primary" size="small">{{ selectedProject.modelName }}</el-tag>
          <span class="api-url">{{ getDisplayUrl(selectedProject.apiUrl) }}</span>
        </div>
      </div>
      
      <el-row :gutter="20">
        <!-- 表单填写区域 -->
        <el-col :span="16">
          <el-card class="form-card">
            <div slot="header" class="card-header">
              <span>接口信息填写</span>
              <el-button @click="resetForm" type="text" icon="el-icon-refresh">重置表单</el-button>
            </div>
            
            <el-form :model="apiForm" :rules="formRules" ref="apiForm" label-width="120px">
              <!-- 基础信息 -->
              <el-form-item label="接口名称" prop="apiName">
                <el-input v-model="apiForm.apiName" placeholder="请输入接口名称，如：用户登录接口"></el-input>
              </el-form-item>
              
              <el-form-item label="接口路径" prop="apiPath">
                <el-input v-model="apiForm.apiPath" placeholder="请输入接口路径，如：POST /api/user/login"></el-input>
              </el-form-item>
              
              <el-form-item label="接口描述">
                <el-input 
                  type="textarea" 
                  :rows="3" 
                  v-model="apiForm.apiDesc" 
                  placeholder="请输入接口描述（可选）">
                </el-input>
              </el-form-item>
              
              <!-- 请求体 -->
              <el-form-item label="接口请求体">
                <el-input 
                  type="textarea" 
                  :rows="8" 
                  v-model="apiForm.apiRequest" 
                  placeholder="请输入接口请求体JSON格式（可选）&#10;示例：&#10;{&#10;  &quot;username&quot;: &quot;string&quot;,&#10;  &quot;password&quot;: &quot;string&quot;&#10;}">
                </el-input>
              </el-form-item>
              
              <!-- 响应体 -->
              <el-form-item label="接口响应体">
                <el-input 
                  type="textarea" 
                  :rows="8" 
                  v-model="apiForm.apiResponse" 
                  placeholder="请输入接口响应体JSON格式（可选）&#10;示例：&#10;{&#10;  &quot;code&quot;: 200,&#10;  &quot;message&quot;: &quot;成功&quot;,&#10;  &quot;data&quot;: {&#10;    &quot;userId&quot;: 1,&#10;    &quot;token&quot;: &quot;...&quot;&#10;  }&#10;}">
                </el-input>
              </el-form-item>
              
              <!-- SQL列表 -->
              <el-form-item label="数据库表SQL" prop="apiSqlList">
                <div class="sql-list-container">
                  <div v-for="(sql, index) in apiForm.apiSqlList" :key="index" class="sql-item">
                    <div class="sql-item-header">
                      <span>SQL {{ index + 1 }}</span>
                      <div class="sql-item-actions">
                        <el-button
                          @click="openTableMetaDialog(index)"
                          type="text"
                          icon="el-icon-search"
                          size="mini"
                          title="从表元列表选择">
                          选择表元
                        </el-button>
                        <el-button
                          @click="removeSql(index)"
                          type="text"
                          icon="el-icon-delete"
                          size="mini"
                          :disabled="apiForm.apiSqlList.length === 1">
                          删除
                        </el-button>
                      </div>
                    </div>
                    <el-input
                      type="textarea"
                      :rows="6"
                      v-model="apiForm.apiSqlList[index].sql"
                      :placeholder="`请输入第${index + 1}个SQL语句（CREATE TABLE或INSERT语句）或从表元列表中选择`">
                    </el-input>

                    <!-- Entity路径输入框 -->
                    <div class="entity-input-container">
                      <label class="entity-label">Entity路径:</label>
                      <el-input
                        v-model="apiForm.apiSqlList[index].entityPath"
                        placeholder="请输入对应的Entity类路径（可选）"
                        size="small"
                        class="entity-input">
                      </el-input>
                    </div>
                  </div>
                  <div class="sql-actions">
                    <el-button @click="addSql" type="dashed" icon="el-icon-plus" class="add-sql-btn">
                      添加SQL语句
                    </el-button>
                    <el-button @click="openNewTableMetaDialog" type="primary" icon="el-icon-plus" class="add-table-btn">
                      新增表元
                    </el-button>
                  </div>
                </div>
              </el-form-item>
              
              <!-- 操作按钮 -->
              <el-form-item>
                <el-button 
                  type="primary" 
                  @click="generatePrompt" 
                  :loading="isGenerating"
                  :disabled="!canGenerate">
                  {{ isGenerating ? 'AI解读生成中...' : '生成Prompt' }}
                </el-button>
                <el-button @click="resetForm">重置</el-button>
              </el-form-item>
            </el-form>
          </el-card>
          

          <!-- 发表Prompt面板 -->
          <el-container v-if="generatedPrompt" class="publish-container" style="margin-top: 20px;">
            <el-header class="publish-header">
              <el-select v-model="publishForm.cid" placeholder="请选择Prompt专题" style="width: 150px;">
                <el-option
                  v-for="item in categories"
                  :key="item.id"
                  :label="item.cateName"
                  :value="item.id">
                </el-option>
              </el-select>
              <el-input
                v-model="publishForm.title"
                :placeholder="autoTitle || '请输入标题...'"
                style="width: 400px;margin-left: 10px"
                @focus="autoFillTitle">
              </el-input>
              <el-tag
                v-for="tag in publishForm.dynamicTags"
                :key="tag"
                closable
                :disable-transitions="false"
                @close="handleCloseTag(tag)"
                style="margin-left: 10px">
                {{tag}}
              </el-tag>
              <el-input
                class="input-new-tag"
                v-if="tagInputVisible"
                v-model="tagValue"
                ref="saveTagInput"
                size="small"
                @keyup.enter.native="handleInputConfirm"
                @blur="handleInputConfirm">
              </el-input>
              <el-button v-else class="button-new-tag" size="small" @click="showTagInput">+ 标签</el-button>
            </el-header>
            <el-main class="publish-main">
              <div id="publish-editor">
                <mavon-editor
                  style="height: 100%;width: 100%;"
                  ref="publishMd"
                  @imgAdd="imgAdd"
                  @imgDel="imgDel"
                  v-model="markdownContent">
                </mavon-editor>
              </div>
              <div style="display: flex;align-items: center;margin-top: 15px;justify-content: flex-end">
                <el-button type="primary" @click="publishPromptDirectly">发表Prompt</el-button>
                <el-button @click="saveAsDraft" style="margin-left: 10px;">保存到草稿箱</el-button>
              </div>
            </el-main>
          </el-container>
        </el-col>

        <!-- 右侧边栏 -->
        <el-col :span="8">
          <!-- 项目信息 -->
          <el-card class="sidebar-card">
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
          
          <!-- 历史记录 -->
          <el-card class="sidebar-card" style="margin-top: 20px;">
            <div slot="header">
              <span>历史记录</span>
              <el-button @click="loadPromptLogs" type="text" icon="el-icon-refresh" size="mini">刷新</el-button>
            </div>
            <div class="history-list" v-loading="historyLoading">
              <div 
                v-for="log in promptLogs" 
                :key="log.id" 
                class="history-item"
                @click="viewHistoryDetail(log)">
                <div class="history-title">{{ log.apiName }}</div>
                <div class="history-path">{{ log.apiPath }}</div>
                <div class="history-time">{{ formatTime(log.createTime) }}</div>
              </div>
              <div v-if="promptLogs.length === 0" class="no-history">
                暂无历史记录
              </div>
            </div>
          </el-card>
          
          <!-- 快速模板 -->
          <el-card class="sidebar-card" style="margin-top: 20px;">
            <div slot="header">
              <span>快速模板</span>
            </div>
            <div class="template-list">
              <el-button 
                v-for="template in apiTemplates" 
                :key="template.id"
                @click="useTemplate(template)"
                type="text"
                class="template-button">
                {{ template.name }}
              </el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 表元选择对话框 -->
      <el-dialog title="选择表元" :visible.sync="tableMetaDialogVisible" width="800px" class="table-meta-dialog">
        <div class="table-meta-search">
          <el-input
            placeholder="搜索表元名称..."
            v-model="tableMetaSearchKeyword"
            @input="filterTableMeta"
            prefix-icon="el-icon-search"
            style="margin-bottom: 20px;">
          </el-input>
        </div>

        <div class="table-meta-list" v-loading="tableMetaLoading">
          <div v-if="filteredTableMetaList.length === 0" class="no-table-meta">
            <el-empty description="没有找到表元">
              <el-button type="primary" @click="openNewTableMetaDialog">新增表元</el-button>
            </el-empty>
          </div>

          <div v-for="tableMeta in filteredTableMetaList" :key="tableMeta.name" class="table-meta-item">
            <div class="table-meta-header">
              <h4>{{ tableMeta.name }}</h4>
              <div class="table-meta-actions">
                <el-tag v-if="tableMeta.parseStatus === 'COMPLETED'" type="success" size="mini">已解析</el-tag>
                <el-tag v-else-if="tableMeta.parseStatus === 'PARSING'" type="warning" size="mini">解析中</el-tag>
                <el-tag v-else-if="tableMeta.parseStatus === 'FAILED'" type="danger" size="mini">解析失败</el-tag>
                <el-tag v-else type="info" size="mini">待解析</el-tag>

                <el-button
                  v-if="tableMeta.entityPath"
                  type="text"
                  size="mini"
                  class="entity-path-btn">
                  Entity: {{ tableMeta.entityPath }}
                </el-button>
              </div>
            </div>

            <div class="table-meta-content">
              <div class="table-sql-preview">
                <pre>{{ tableMeta.originalSql }}</pre>
              </div>

              <div v-if="tableMeta.colum && tableMeta.colum.length > 0" class="table-columns">
                <h5>字段信息：</h5>
                <el-table :data="tableMeta.colum" size="mini" stripe>
                  <el-table-column prop="columName" label="字段名" width="120"></el-table-column>
                  <el-table-column prop="columType" label="类型" width="100"></el-table-column>
                  <el-table-column prop="columdesc" label="描述"></el-table-column>
                </el-table>
              </div>
            </div>

            <div class="table-meta-footer">
              <el-button @click="selectTableMeta(tableMeta)" type="primary" size="small">
                选择此表元
              </el-button>
              <el-button @click="editTableMeta(tableMeta)" size="small">
                编辑表元
              </el-button>
            </div>
          </div>
        </div>

        <div slot="footer" class="dialog-footer">
          <el-button @click="tableMetaDialogVisible = false">关闭</el-button>
          <el-button type="primary" @click="openNewTableMetaDialog">新增表元</el-button>
        </div>
      </el-dialog>

      <!-- 新增/编辑表元对话框 -->
      <el-dialog
        :title="isEditTableMeta ? '编辑表元' : '新增表元'"
        :visible.sync="newTableMetaDialogVisible"
        width="700px"
        class="new-table-meta-dialog">

        <el-form :model="tableMetaForm" :rules="tableMetaFormRules" ref="tableMetaForm" label-width="100px">
          <el-form-item label="表名" prop="name">
            <el-input v-model="tableMetaForm.name" placeholder="请输入表名"></el-input>
          </el-form-item>

          <el-form-item label="原始SQL" prop="originalSql">
            <el-input
              type="textarea"
              :rows="8"
              v-model="tableMetaForm.originalSql"
              placeholder="请输入CREATE TABLE或INSERT SQL语句">
            </el-input>
          </el-form-item>

          <el-form-item label="Entity路径" prop="entityPath">
            <el-input v-model="tableMetaForm.entityPath" placeholder="请输入Entity类路径（可选）"></el-input>
            <div class="form-tip">如果有对应的Entity类，请填写完整路径，如：com.example.entity.User</div>
          </el-form-item>

          <div v-if="tableMetaForm.colum && tableMetaForm.colum.length > 0" class="columns-section">
            <h4>字段信息</h4>
            <div v-for="(column, index) in tableMetaForm.colum" :key="index" class="column-item">
              <el-row :gutter="10">
                <el-col :span="6">
                  <el-input v-model="column.columName" placeholder="字段名" size="small"></el-input>
                </el-col>
                <el-col :span="5">
                  <el-input v-model="column.columType" placeholder="类型" size="small"></el-input>
                </el-col>
                <el-col :span="10">
                  <el-input v-model="column.columdesc" placeholder="描述" size="small"></el-input>
                </el-col>
                <el-col :span="3">
                  <el-button @click="removeColumn(index)" type="text" icon="el-icon-delete" size="mini"></el-button>
                </el-col>
              </el-row>
            </div>
            <el-button @click="addColumn" type="text" icon="el-icon-plus" size="small">添加字段</el-button>
          </div>
        </el-form>

        <div slot="footer" class="dialog-footer">
          <el-button @click="newTableMetaDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveTableMeta" :loading="savingTableMeta">
            {{ isEditTableMeta ? '更新' : '保存' }}
          </el-button>
        </div>
      </el-dialog>
    </div>
  </div>
</template>

<script>
import { getRequest, generatePrompt, getUserPromptLogs, getPromptLogDetail, uploadFileRequest, postRequest } from '../utils/api'
// Local Registration
import { mavonEditor } from 'mavon-editor'
// 可以通过 mavonEditor.markdownIt 获取解析器markdown-it对象
import 'mavon-editor/dist/css/index.css'

export default {
  name: 'PromptGenerator',
  components: {
    mavonEditor
  },
  data() {
    return {
      loading: false,
      isGenerating: false,
      historyLoading: false,
      searchKeyword: '',
      selectedProject: null,
      allProjects: [],
      filteredProjects: [],
      generatedPrompt: '',
      promptLogs: [],
      markdownContent: '',

      // 发表相关数据
      categories: [],
      publishForm: {
        id: '-1',
        title: '',
        cid: '',
        dynamicTags: []
      },
      tagInputVisible: false,
      tagValue: '',
      
      // 表单数据
      apiForm: {
        apiName: '',
        apiPath: '',
        apiDesc: '',
        apiRequest: '',
        apiResponse: '',
        apiSqlList: [{ sql: '', entityPath: '' }]
      },

      // 表元数据相关
      tableMetaList: [],
      tableMetaDialogVisible: false,
      selectedTableMeta: null,
      newTableMetaDialogVisible: false,
      isEditTableMeta: false,
      tableMetaLoading: false,
      savingTableMeta: false,
      tableMetaSearchKeyword: '',
      filteredTableMetaList: [],
      currentSqlIndex: undefined,

      // 表元表单
      tableMetaForm: {
        name: '',
        originalSql: '',
        entityPath: '',
        colum: []
      },
      tableMetaFormRules: {
        name: [
          { required: true, message: '请输入表名', trigger: 'blur' }
        ],
        originalSql: [
          { required: true, message: '请输入SQL语句', trigger: 'blur' }
        ]
      },
      
      // 表单验证规则
      formRules: {
        apiName: [
          { required: true, message: '请输入接口名称', trigger: 'blur' }
        ],
        apiPath: [
          { required: true, message: '请输入接口路径', trigger: 'blur' }
        ],
        apiSqlList: [
          {
            validator: this.validateSqlList,
            trigger: 'blur'
          }
        ]
      },
      
      // 快速模板
      apiTemplates: [
        {
          id: 1,
          name: '用户登录接口',
          data: {
            apiName: '用户登录接口',
            apiPath: 'POST /api/user/login',
            apiDesc: '用户通过用户名和密码进行登录验证',
            apiRequest: '{\n  "username": "string",\n  "password": "string"\n}',
            apiResponse: '{\n  "code": 200,\n  "message": "登录成功",\n  "data": {\n    "userId": 1,\n    "username": "admin",\n    "nickname": "管理员",\n    "token": "eyJhbGciOiJIUzI1NiJ9..."\n  }\n}',
            apiSqlList: [{ sql: 'CREATE TABLE `user` (\n  `id` bigint NOT NULL AUTO_INCREMENT,\n  `username` varchar(50) NOT NULL,\n  `password` varchar(255) NOT NULL,\n  `nickname` varchar(50) DEFAULT NULL,\n  `status` int DEFAULT \'1\',\n  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,\n  PRIMARY KEY (`id`)\n) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;', entityPath: 'com.example.entity.User' }]
          }
        },
        {
          id: 2,
          name: '用户注册接口',
          data: {
            apiName: '用户注册接口',
            apiPath: 'POST /api/user/register',
            apiDesc: '新用户注册账号',
            apiRequest: '{\n  "username": "string",\n  "password": "string",\n  "email": "string",\n  "nickname": "string"\n}',
            apiResponse: '{\n  "code": 200,\n  "message": "注册成功",\n  "data": {\n    "userId": 1\n  }\n}',
            apiSqlList: [{ sql: 'CREATE TABLE `user` (\n  `id` bigint NOT NULL AUTO_INCREMENT,\n  `username` varchar(50) NOT NULL UNIQUE,\n  `password` varchar(255) NOT NULL,\n  `email` varchar(100) DEFAULT NULL,\n  `nickname` varchar(50) DEFAULT NULL,\n  `status` int DEFAULT \'1\',\n  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,\n  PRIMARY KEY (`id`)\n) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;', entityPath: 'com.example.entity.User' }]
          }
        },
        {
          id: 3,
          name: '商品列表接口',
          data: {
            apiName: '商品列表接口',
            apiPath: 'GET /api/product/list',
            apiDesc: '获取商品列表，支持分页和筛选',
            apiRequest: '{\n  "page": 1,\n  "size": 10,\n  "category": "string",\n  "keyword": "string"\n}',
            apiResponse: '{\n  "code": 200,\n  "message": "成功",\n  "data": {\n    "list": [{\n      "id": 1,\n      "name": "商品名称",\n      "price": 99.99,\n      "categoryName": "分类名称"\n    }],\n    "total": 100\n  }\n}',
            apiSqlList: [
              { sql: 'CREATE TABLE `product` (\n  `id` bigint NOT NULL AUTO_INCREMENT,\n  `name` varchar(255) NOT NULL,\n  `price` decimal(10,2) NOT NULL,\n  `category_id` bigint NOT NULL,\n  `status` int DEFAULT \'1\',\n  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,\n  PRIMARY KEY (`id`)\n) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;', entityPath: 'com.example.entity.Product' },
              { sql: 'CREATE TABLE `category` (\n  `id` bigint NOT NULL AUTO_INCREMENT,\n  `name` varchar(100) NOT NULL,\n  `status` int DEFAULT \'1\',\n  PRIMARY KEY (`id`)\n) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;', entityPath: 'com.example.entity.Category' }
            ]
          }
        }
      ]
    }
  },
  computed: {
    canGenerate() {
      return this.apiForm.apiName && this.apiForm.apiPath &&
             this.apiForm.apiSqlList.length > 0 &&
             this.apiForm.apiSqlList.some(item => item.sql.trim() !== '')
    },

    renderedMarkdown() {
      if (this.$refs.publishMd && this.markdownContent) {
        return this.$refs.publishMd.d_render
      }
      return ''
    },

    autoTitle() {
      return this.apiForm.apiName ? `${this.apiForm.apiName} - Prompt` : ''
    }
  },
  watch: {
    'apiForm.apiName'(newVal) {
      // 当接口名称改变时，如果标题还没有被手动设置，自动更新标题
      if (newVal && !this.publishForm.title) {
        this.publishForm.title = `${newVal} - Prompt`
      }
    }
  },
  mounted() {
    this.loadProjects()
    this.getCategories()
  },
  methods: {
    canAccessProject(project) {
      // 用户是项目成员（userRole > 0）或项目拥有者都可以访问
      return project.userRole > 0 || project.ownerId === this.$store.state.user.id
    },

    async loadProjects() {
      this.loading = true
      try {
        const response = await getRequest('/project/my-projects')
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
      // 检查用户是否有权限操作该项目（必须是项目成员或项目拥有者）
      if (project.userRole === 0 && project.ownerId !== this.$store.state.user.id) {
        this.$message.warning('您没有权限操作此项目，请先加入该项目')
        return
      }

      if (!project.apiKey) {
        this.$message.warning('该项目未配置AI接口，无法使用Prompt生成功能')
        return
      }

      this.selectedProject = project
      this.resetForm()
      this.loadPromptLogs()
      this.loadTableMetaList()
    },
    
    backToSelection() {
      this.selectedProject = null
      this.resetForm()
      this.generatedPrompt = ''
      this.markdownContent = ''
      this.resetPublishForm()
      this.promptLogs = []
    },
    
    // 表单相关方法
    resetForm() {
      this.apiForm = {
        apiName: '',
        apiPath: '',
        apiDesc: '',
        apiRequest: '',
        apiResponse: '',
        apiSqlList: [{ sql: '', entityPath: '' }]
      }
      this.generatedPrompt = ''
      this.markdownContent = ''
      this.resetPublishForm()
      this.resetTableMetaForm()
      if (this.$refs.apiForm) {
        this.$refs.apiForm.clearValidate()
      }
    },

    // 表元相关方法
    async loadTableMetaList() {
      if (!this.selectedProject) return

      this.tableMetaLoading = true
      try {
        const response = await getRequest(`/project/${this.selectedProject.id}/table-meta-list`)
        if (response.data.status === 'success') {
          this.tableMetaList = response.data.obj || []
          this.filteredTableMetaList = this.tableMetaList
        } else {
          this.$message.error('加载表元列表失败：' + response.data.msg)
        }
      } catch (error) {
        this.$message.error('加载表元列表失败：' + error.message)
      } finally {
        this.tableMetaLoading = false
      }
    },

    filterTableMeta() {
      if (!this.tableMetaSearchKeyword) {
        this.filteredTableMetaList = this.tableMetaList
      } else {
        this.filteredTableMetaList = this.tableMetaList.filter(tableMeta =>
          tableMeta.name.toLowerCase().includes(this.tableMetaSearchKeyword.toLowerCase())
        )
      }
    },

    openTableMetaDialog(sqlIndex) {
      this.currentSqlIndex = sqlIndex
      this.tableMetaDialogVisible = true
    },

    selectTableMeta(tableMeta) {
      // 将表元信息填充到对应的SQL输入框
      if (this.currentSqlIndex !== undefined) {
        this.apiForm.apiSqlList[this.currentSqlIndex] = {
          sql: tableMeta.originalSql,
          entityPath: tableMeta.entityPath || ''
        }
      }
      this.tableMetaDialogVisible = false
      this.$message.success('表元已选择')
    },

    openNewTableMetaDialog() {
      this.isEditTableMeta = false
      this.resetTableMetaForm()
      this.newTableMetaDialogVisible = true
    },

    editTableMeta(tableMeta) {
      this.isEditTableMeta = true
      this.tableMetaForm = {
        name: tableMeta.name,
        originalSql: tableMeta.originalSql,
        entityPath: tableMeta.entityPath || '',
        colum: tableMeta.colum ? [...tableMeta.colum] : []
      }
      this.newTableMetaDialogVisible = true
    },

    resetTableMetaForm() {
      this.tableMetaForm = {
        name: '',
        originalSql: '',
        entityPath: '',
        colum: []
      }
      if (this.$refs.tableMetaForm) {
        this.$refs.tableMetaForm.clearValidate()
      }
    },

    addColumn() {
      this.tableMetaForm.colum.push({
        columName: '',
        columType: '',
        columdesc: ''
      })
    },

    removeColumn(index) {
      this.tableMetaForm.colum.splice(index, 1)
    },

    async saveTableMeta() {
      this.$refs.tableMetaForm.validate(async (valid) => {
        if (!valid) {
          this.$message.error('请完善表元信息')
          return
        }

        this.savingTableMeta = true
        try {
          const requestData = {
            ...this.tableMetaForm
          }

          const response = await postRequest(`/project/${this.selectedProject.id}/table-meta`, requestData)
          if (response.data.status === 'success') {
            this.$message.success(this.isEditTableMeta ? '表元更新成功' : '表元添加成功')
            this.newTableMetaDialogVisible = false
            this.loadTableMetaList() // 重新加载表元列表
          } else {
            this.$message.error('保存失败：' + response.data.msg)
          }
        } catch (error) {
          this.$message.error('保存失败：' + error.message)
        } finally {
          this.savingTableMeta = false
        }
      })
    },
    
    addSql() {
      this.apiForm.apiSqlList.push({ sql: '', entityPath: '' })
    },

    removeSql(index) {
      if (this.apiForm.apiSqlList.length > 1) {
        this.apiForm.apiSqlList.splice(index, 1)
      }
    },
    
    validateSqlList(rule, value, callback) {
      if (!value || value.length === 0) {
        callback(new Error('请至少添加一个SQL语句'))
        return
      }

      const hasValidSql = value.some(item => item.sql && item.sql.trim() !== '')
      if (!hasValidSql) {
        callback(new Error('请输入有效的SQL语句'))
        return
      }

      callback()
    },
    
    useTemplate(template) {
      this.apiForm = { ...template.data }
      this.$message.success('模板已应用')
    },
    
    async generatePrompt() {
      // 表单验证
      this.$refs.apiForm.validate(async (valid) => {
        if (!valid) {
          this.$message.error('请完善表单信息')
          return
        }
        
        this.isGenerating = true
        try {
          const requestData = {
            projectId: this.selectedProject.id,
            apiName: this.apiForm.apiName,
            apiPath: this.apiForm.apiPath,
            apiDesc: this.apiForm.apiDesc || null,
            apiRequest: this.apiForm.apiRequest || null,
            apiResponse: this.apiForm.apiResponse || null,
            apiSqlList: this.apiForm.apiSqlList
              .filter(item => item.sql.trim() !== '')
              .map(item => ({
                sql: item.sql,
                entityPath: item.entityPath || null
              }))
          }
          
          const response = await generatePrompt(requestData)
          
          if (response.data.status === 'success') {
            this.generatedPrompt = response.data.obj
            this.markdownContent = response.data.obj // 设置markdown内容
            console.log('Prompt生成成功，内容长度:', this.markdownContent.length)
            console.log('generatedPrompt已设置:', !!this.generatedPrompt)
            console.log('markdownContent已设置:', !!this.markdownContent)
            // 自动填充标题
            this.autoFillTitle()
            this.$message.success('Prompt生成成功！')
            this.loadPromptLogs() // 刷新历史记录

            // 确保编辑器正确更新内容
            this.$nextTick(() => {
              console.log('编辑器引用存在:', !!this.$refs.publishMd)
              // 等待编辑器组件完全初始化
              setTimeout(() => {
                if (this.$refs.publishMd && this.markdownContent) {
                  console.log('强制刷新编辑器')
                  // 强制触发编辑器重新渲染
                  this.$forceUpdate()
                }
              }, 100)
            })
          } else {
            this.$message.error('生成失败：' + response.data.msg)
          }
        } catch (error) {
          this.$message.error('生成失败：' + error.message)
        } finally {
          this.isGenerating = false
        }
      })
    },
    
    copyPrompt() {
      const textarea = document.createElement('textarea')
      textarea.value = this.generatedPrompt
      document.body.appendChild(textarea)
      textarea.select()
      document.execCommand('copy')
      document.body.removeChild(textarea)
      this.$message.success('Prompt已复制到剪贴板')
    },
    
    downloadPrompt() {
      const element = document.createElement('a')
      const file = new Blob([this.generatedPrompt], { type: 'text/plain' })
      element.href = URL.createObjectURL(file)
      element.download = `${this.apiForm.apiName}_prompt.md`
      document.body.appendChild(element)
      element.click()
      document.body.removeChild(element)
      this.$message.success('Prompt已下载')
    },
    
    // 历史记录相关方法
    async loadPromptLogs() {
      if (!this.selectedProject) return
      
      this.historyLoading = true
      try {
        const response = await getUserPromptLogs()
        if (response.data.status === 'success') {
          // 过滤当前项目的记录
          this.promptLogs = (response.data.obj || []).filter(log => 
            log.projectId === this.selectedProject.id
          ).slice(0, 10) // 只显示最近10条
        } else {
          console.error('加载历史记录失败：', response.data.msg)
        }
      } catch (error) {
        console.error('加载历史记录失败：', error.message)
      } finally {
        this.historyLoading = false
      }
    },
    
    async viewHistoryDetail(log) {
      try {
        const response = await getPromptLogDetail(log.id)
        if (response.data.status === 'success') {
          const detail = response.data.obj
          
          // 填充表单
          this.apiForm = {
            apiName: detail.apiName || '',
            apiPath: detail.apiPath || '',
            apiDesc: detail.apiDesc || '',
            apiRequest: detail.apiRequest || '',
            apiResponse: detail.apiResponse || '',
            apiSqlList: detail.apiSql ? JSON.parse(detail.apiSql).map(item =>
              typeof item === 'string' ? { sql: item, entityPath: '' } : item
            ) : [{ sql: '', entityPath: '' }]
          }
          
          // 显示生成的结果
          this.generatedPrompt = detail.finalPrompt || ''
          this.markdownContent = detail.finalPrompt || '' // 设置markdown内容
          console.log('历史记录加载成功，内容长度:', this.markdownContent.length)

          // 确保编辑器正确更新内容
          this.$nextTick(() => {
            setTimeout(() => {
              if (this.$refs.publishMd && this.markdownContent) {
                this.$forceUpdate()
              }
            }, 100)
          })

          this.$message.success('历史记录已加载')
        } else {
          this.$message.error('加载详情失败：' + response.data.msg)
        }
      } catch (error) {
        this.$message.error('加载详情失败：' + error.message)
      }
    },
    
    formatTime(timestamp) {
      if (!timestamp) return ''
      const date = new Date(timestamp)
      return date.toLocaleString('zh-CN', {
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      })
    },

    // 图片上传相关方法
    imgAdd(pos, $file) {
      var _this = this
      // 第一步.将图片上传到服务器.
      var formdata = new FormData()
      formdata.append('image', $file)
      uploadFileRequest("/article/uploadimg", formdata).then(resp => {
        var json = resp.data
        if (json.status == 'success') {
          _this.$refs.publishMd.$imglst2Url([[pos, json.msg]])
        } else {
          _this.$message({type: json.status, message: json.msg})
        }
      })
    },

    imgDel(pos) {
      // 处理图片删除逻辑
    },

    getCategories() {
      let _this = this
      getRequest("/admin/category/all").then(resp => {
        _this.categories = resp.data
      })
    },

    // 标签管理方法
    handleCloseTag(tag) {
      this.publishForm.dynamicTags.splice(this.publishForm.dynamicTags.indexOf(tag), 1)
    },

    showTagInput() {
      this.tagInputVisible = true
      this.$nextTick(_ => {
        this.$refs.saveTagInput.$refs.input.focus()
      })
    },

    handleInputConfirm() {
      let tagValue = this.tagValue
      if (tagValue) {
        this.publishForm.dynamicTags.push(tagValue)
      }
      this.tagInputVisible = false
      this.tagValue = ''
    },

    // 发表Prompt方法
    publishPromptDirectly() {
      if (!this.validatePublishForm()) {
        return
      }

      this.loading = true
      const htmlContent = this.$refs.publishMd.d_render

      postRequest("/article/", {
        id: this.publishForm.id,
        title: this.publishForm.title,
        mdContent: this.markdownContent,
        htmlContent: htmlContent,
        cid: this.publishForm.cid,
        state: 1, // 1表示发表
        dynamicTags: this.publishForm.dynamicTags
      }).then(resp => {
        this.loading = false
        if (resp.status == 200 && resp.data.status == 'success') {
          this.$message({
            type: 'success',
            message: 'Prompt发表成功!'
          })
          // 重置发表表单
          this.resetPublishForm()
          // 刷新文章列表（如果有的话）
          window.bus && window.bus.$emit('blogTableReload')
        } else {
          this.$message({
            type: 'error',
            message: '发表失败: ' + (resp.data.msg || '未知错误')
          })
        }
      }).catch(error => {
        this.loading = false
        this.$message({
          type: 'error',
          message: '发表失败: ' + error.message
        })
      })
    },

    saveAsDraft() {
      if (!this.validatePublishForm()) {
        return
      }

      this.loading = true
      const htmlContent = this.$refs.publishMd.d_render

      postRequest("/article/", {
        id: this.publishForm.id,
        title: this.publishForm.title,
        mdContent: this.markdownContent,
        htmlContent: htmlContent,
        cid: this.publishForm.cid,
        state: 0, // 0表示保存到草稿箱
        dynamicTags: this.publishForm.dynamicTags
      }).then(resp => {
        this.loading = false
        if (resp.status == 200 && resp.data.status == 'success') {
          this.$message({
            type: 'success',
            message: '保存到草稿箱成功!'
          })
          // 重置发表表单
          this.resetPublishForm()
        } else {
          this.$message({
            type: 'error',
            message: '保存失败: ' + (resp.data.msg || '未知错误')
          })
        }
      }).catch(error => {
        this.loading = false
        this.$message({
          type: 'error',
          message: '保存失败: ' + error.message
        })
      })
    },

    validatePublishForm() {
      if (!this.publishForm.title) {
        this.$message({type: 'error', message: '请输入标题!'})
        return false
      }
      if (!this.publishForm.cid) {
        this.$message({type: 'error', message: '请选择专题!'})
        return false
      }
      if (!this.markdownContent) {
        this.$message({type: 'error', message: '内容不能为空!'})
        return false
      }
      return true
    },

    resetPublishForm() {
      this.publishForm = {
        id: '-1',
        title: '',
        cid: '',
        dynamicTags: []
      }
      this.tagValue = ''
      this.tagInputVisible = false
    },

    autoFillTitle() {
      if (!this.publishForm.title && this.apiForm.apiName) {
        this.publishForm.title = this.apiForm.apiName + ' - Prompt'
      }
    },

    publishPrompt() {
      // 自动填充标题
      this.autoFillTitle()
      // 滚动到发表面板
      this.$nextTick(() => {
        const publishContainer = document.querySelector('.publish-container')
        if (publishContainer) {
          publishContainer.scrollIntoView({ behavior: 'smooth' })
        }
      })
    },
    
    getDisplayUrl(url) {
      try {
        const urlObj = new URL(url)
        return urlObj.hostname
      } catch (error) {
        return url
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

.project-card.no-permission {
  opacity: 0.6;
  cursor: not-allowed;
}

.project-card.no-permission:hover {
  transform: none;
  border-color: #DCDFE6;
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

/* 表单相关样式 */
.form-card {
  min-height: 600px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.sql-list-container {
  border: 1px solid #DCDFE6;
  border-radius: 4px;
  padding: 15px;
  background-color: #FAFAFA;
}

.sql-item {
  margin-bottom: 15px;
}

.sql-item:last-child {
  margin-bottom: 0;
}

.sql-item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #606266;
}

.add-sql-btn {
  width: 100%;
  margin-top: 10px;
  border-style: dashed;
  color: #409EFF;
  background-color: #FAFBFC;
}

.add-sql-btn:hover {
  background-color: #F0F9FF;
  border-color: #409EFF;
}

/* Entity路径输入框样式 */
.entity-input-container {
  margin-top: 10px;
}

.entity-label {
  display: block;
  font-size: 12px;
  color: #606266;
  margin-bottom: 5px;
  font-weight: 500;
}

.entity-input {
  width: 100%;
}

#prompt-editor {
  width: 100%;
  height: 400px;
}

/* 发表面板样式 */
.publish-container {
  border: 1px solid #E4E7ED;
  border-radius: 4px;
  background-color: #ececec;
  min-height: 500px;
}

.publish-header {
  background-color: #ececec;
  margin-top: 10px;
  padding-left: 5px;
  display: flex;
  justify-content: flex-start;
  align-items: center;
  border-bottom: 1px solid #E4E7ED;
}

.publish-main {
  display: flex;
  flex-direction: column;
  padding-left: 5px;
  background-color: #ececec;
  padding-top: 0px;
}

.publish-header > .el-tag + .el-tag {
  margin-left: 10px;
}

.publish-header > .button-new-tag {
  margin-left: 10px;
  height: 32px;
  line-height: 30px;
  padding-top: 0;
  padding-bottom: 0;
}

.publish-header > .input-new-tag {
  width: 90px;
  margin-left: 10px;
  vertical-align: bottom;
}

#publish-editor {
  width: 100%;
  height: 450px;
  text-align: left;
}


/* 侧边栏样式 */
.sidebar-card {
  margin-bottom: 20px;
}

.project-details p {
  margin: 8px 0;
  font-size: 13px;
  color: #606266;
}

.project-details strong {
  color: #303133;
}

/* 历史记录样式 */
.history-list {
  max-height: 300px;
  overflow-y: auto;
}

.history-item {
  padding: 12px;
  border: 1px solid #E4E7ED;
  border-radius: 4px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: all 0.3s;
  background-color: #FAFAFA;
}

.history-item:hover {
  background-color: #F0F9FF;
  border-color: #409EFF;
  transform: translateX(2px);
}

.history-item:last-child {
  margin-bottom: 0;
}

.history-title {
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.history-path {
  color: #909399;
  font-size: 12px;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.history-time {
  color: #C0C4CC;
  font-size: 11px;
}

.no-history {
  text-align: center;
  color: #909399;
  padding: 20px;
  font-size: 13px;
}

/* 模板列表样式 */
.template-list {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.template-button {
  text-align: left;
  padding: 10px 12px;
  border: 1px solid #EBEEF5;
  border-radius: 4px;
  background-color: #FAFAFA;
  transition: all 0.3s;
  font-size: 13px;
}

.template-button:hover {
  background-color: #F0F9FF;
  border-color: #409EFF;
  color: #409EFF;
}

/* 表元相关样式 */
.table-meta-dialog .table-meta-search {
  margin-bottom: 20px;
}

.table-meta-list {
  max-height: 500px;
  overflow-y: auto;
}

.table-meta-item {
  border: 1px solid #E4E7ED;
  border-radius: 4px;
  margin-bottom: 15px;
  padding: 15px;
  background-color: #FAFAFA;
}

.table-meta-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.table-meta-header h4 {
  margin: 0;
  color: #303133;
}

.table-meta-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.entity-path-btn {
  font-size: 11px;
  color: #606266;
}

.table-sql-preview {
  background-color: #F8F9FA;
  border: 1px solid #E9ECEF;
  border-radius: 4px;
  padding: 10px;
  margin-bottom: 15px;
}

.table-sql-preview pre {
  margin: 0;
  font-size: 12px;
  color: #495057;
  white-space: pre-wrap;
  word-break: break-all;
}

.table-columns {
  margin-top: 15px;
}

.table-columns h5 {
  margin: 0 0 10px 0;
  color: #303133;
  font-size: 14px;
}

.table-meta-footer {
  margin-top: 15px;
  text-align: right;
  border-top: 1px solid #E4E7ED;
  padding-top: 10px;
}

.columns-section {
  margin-top: 15px;
  padding: 15px;
  background-color: #F8F9FA;
  border-radius: 4px;
}

.column-item {
  margin-bottom: 10px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
  line-height: 1.4;
}

.sql-actions {
  display: flex;
  gap: 10px;
  margin-top: 10px;
}

.add-table-btn {
  background-color: #67C23A;
  border-color: #67C23A;
}

.add-table-btn:hover {
  background-color: #85CE61;
  border-color: #85CE61;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .prompt-generator {
    padding: 10px;
  }

  .form-card {
    min-height: auto;
  }

  .prompt-content {
    font-size: 12px;
    max-height: 300px;
  }

  .history-list {
    max-height: 200px;
  }

  .table-meta-dialog {
    width: 95% !important;
  }

  .table-meta-item {
    padding: 10px;
  }

  .table-meta-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}
</style>
