<template>
  <div class="usage-records-container">
    <div class="header-section">
      <h2>使用记录</h2>
      <div class="search-section">
        <el-input
          v-model="searchUsername"
          placeholder="输入用户名搜索"
          style="width: 200px; margin-right: 10px;"
          @keyup.enter="searchRecords"
        >
          <el-button slot="append" icon="el-icon-search" @click="searchRecords"></el-button>
        </el-input>
        <el-button @click="loadAllRecords" type="primary" plain>显示全部</el-button>
      </div>
    </div>

    <div class="table-container">
      <el-table
        :data="records"
        v-loading="loading"
        style="width: 100%"
        @row-click="showDetail"
      >
        <el-table-column prop="username" label="用户名" width="120"></el-table-column>
        <el-table-column prop="nickname" label="昵称" width="120"></el-table-column>
        <el-table-column prop="projectName" label="项目名称" width="150"></el-table-column>
        <el-table-column prop="apiName" label="接口名称" width="200"></el-table-column>
        <el-table-column prop="apiPath" label="接口路径" width="200"></el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template slot-scope="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template slot-scope="scope">
            <el-button type="text" @click.stop="showDetail(scope.row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="pagination-container">
      <el-pagination
        @current-change="handlePageChange"
        :current-page="currentPage"
        :page-size="pageSize"
        :total="totalCount"
        layout="total, prev, pager, next"
      >
      </el-pagination>
    </div>

    <!-- 详情对话框 -->
    <el-dialog
      title="使用记录详情"
      :visible.sync="detailVisible"
      width="80%"
      :before-close="closeDetail"
    >
      <div v-if="currentRecord" class="detail-content">
        <el-row :gutter="20">
          <el-col :span="12">
            <div class="detail-item">
              <label>用户信息：</label>
              <span>{{ currentRecord.userNickname }} ({{ currentRecord.username }})</span>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="detail-item">
              <label>项目名称：</label>
              <span>{{ currentRecord.projectName }}</span>
            </div>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <div class="detail-item">
              <label>接口名称：</label>
              <span>{{ currentRecord.apiName }}</span>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="detail-item">
              <label>接口路径：</label>
              <span>{{ currentRecord.apiPath }}</span>
            </div>
          </el-col>
        </el-row>

        <div class="detail-item">
          <label>接口描述：</label>
          <p>{{ currentRecord.apiDesc || '无' }}</p>
        </div>

        <div class="detail-item">
          <label>请求体：</label>
          <pre class="code-block">{{ currentRecord.apiRequest || '无' }}</pre>
        </div>

        <div class="detail-item">
          <label>响应体：</label>
          <pre class="code-block">{{ currentRecord.apiResponse || '无' }}</pre>
        </div>

        <div class="detail-item">
          <label>生成的Prompt：</label>
          <pre class="code-block">{{ currentRecord.finalPrompt || '无' }}</pre>
        </div>

        <div class="detail-item">
          <label>创建时间：</label>
          <span>{{ formatDate(currentRecord.createTime) }}</span>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'UsageRecords',
  data() {
    return {
      records: [],
      loading: false,
      currentPage: 1,
      pageSize: 10,
      totalCount: 0,
      searchUsername: '',
      detailVisible: false,
      currentRecord: null
    }
  },
  mounted() {
    this.loadRecords()
  },
  methods: {
    loadRecords() {
      this.loading = true
      const params = {
        page: this.currentPage,
        pageSize: this.pageSize
      }
      if (this.searchUsername.trim()) {
        params.username = this.searchUsername
      }

      axios.get('/statistics/usage/records', { params }).then(resp => {
        this.loading = false
        if (resp && resp.data && resp.data.status === 'success') {
          const data = resp.data.obj
          this.records = data.records || []
          this.totalCount = data.totalCount || 0
        }
      }).catch(() => {
        this.loading = false
        this.$message.error('获取使用记录失败')
      })
    },

    searchRecords() {
      this.currentPage = 1
      this.loadRecords()
    },

    loadAllRecords() {
      this.searchUsername = ''
      this.currentPage = 1
      this.loadRecords()
    },

    handlePageChange(page) {
      this.currentPage = page
      this.loadRecords()
    },

    showDetail(row) {
      // 获取详细信息
      axios.get(`/statistics/usage/records/${row.id}`).then(resp => {
        if (resp && resp.data && resp.data.status === 'success') {
          this.currentRecord = resp.data.obj
          this.detailVisible = true
        }
      }).catch(() => {
        this.$message.error('获取详情失败')
      })
    },

    closeDetail() {
      this.detailVisible = false
      this.currentRecord = null
    },

    formatDate(date) {
      if (!date) return '-'
      const d = new Date(date)
      return d.getFullYear() + '-' + 
             String(d.getMonth() + 1).padStart(2, '0') + '-' + 
             String(d.getDate()).padStart(2, '0') + ' ' +
             String(d.getHours()).padStart(2, '0') + ':' + 
             String(d.getMinutes()).padStart(2, '0') + ':' + 
             String(d.getSeconds()).padStart(2, '0')
    }
  }
}
</script>

<style scoped>
.usage-records-container {
  padding: 20px;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.header-section h2 {
  margin: 0;
  color: #303133;
  font-size: 20px;
  font-weight: 600;
}

.search-section {
  display: flex;
  align-items: center;
}

.table-container {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  margin-bottom: 20px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.detail-content {
  padding: 20px;
}

.detail-item {
  margin-bottom: 20px;
}

.detail-item label {
  font-weight: bold;
  color: #303133;
  display: inline-block;
  width: 100px;
}

.code-block {
  background-color: #f5f5f5;
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 10px;
  margin-top: 5px;
  white-space: pre-wrap;
  word-wrap: break-word;
  max-height: 200px;
  overflow-y: auto;
}

.el-table tbody tr {
  cursor: pointer;
}

.el-table tbody tr:hover {
  background-color: #f5f7fa;
}
</style>


