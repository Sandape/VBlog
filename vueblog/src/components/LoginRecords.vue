<template>
  <div class="login-records-container">
    <div class="header-section">
      <h2>登录记录</h2>
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
      >
        <el-table-column prop="username" label="用户名" width="120"></el-table-column>
        <el-table-column prop="nickname" label="昵称" width="120"></el-table-column>
        <el-table-column prop="ipAddress" label="IP地址" width="130"></el-table-column>
        <el-table-column prop="loginStatus" label="状态" width="80">
          <template slot-scope="scope">
            <el-tag :type="scope.row.loginStatus === 'SUCCESS' ? 'success' : 'danger'">
              {{ scope.row.loginStatus === 'SUCCESS' ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="loginTime" label="登录时间" width="180">
          <template slot-scope="scope">
            {{ formatDate(scope.row.loginTime) }}
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
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'LoginRecords',
  data() {
    return {
      records: [],
      loading: false,
      currentPage: 1,
      pageSize: 10,
      totalCount: 0,
      searchUsername: ''
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

      axios.get('/statistics/login/records', { params }).then(resp => {
        this.loading = false
        if (resp && resp.data && resp.data.status === 'success') {
          const data = resp.data.obj
          this.records = data.records || []
          this.totalCount = data.totalCount || 0
        }
      }).catch(() => {
        this.loading = false
        this.$message.error('获取登录记录失败')
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
.login-records-container {
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
</style>


