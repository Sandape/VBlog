<template>
  <div class="login-records-container">
    <div class="title-section">
      <h2>登录记录</h2>
    </div>

    <DataTable
      :data="records"
      :columns="columns"
      :loading="loading"
      :show-pagination="true"
      :current-page="currentPage"
      :page-size="pageSize"
      :total="totalCount"
      :search-placeholder="'输入用户名搜索'"
      :search-fields="['username', 'nickname']"
      :default-sort="{ prop: 'loginTime', order: 'descending' }"
      @page-change="handlePageChange"
      @size-change="handleSizeChange"
      @search="handleSearch"
    />
  </div>
</template>

<script>
import axios from 'axios'
import DataTable from './DataTable.vue'

export default {
  name: 'LoginRecords',
  components: {
    DataTable
  },
  data() {
    return {
      records: [],
      loading: false,
      currentPage: 1,
      pageSize: 10,
      totalCount: 0,
      searchUsername: '',
      columns: [
        {
          prop: 'username',
          label: '用户名',
          width: 120,
          sortable: true
        },
        {
          prop: 'nickname',
          label: '昵称',
          width: 120,
          sortable: true
        },
        {
          prop: 'ipAddress',
          label: 'IP地址',
          width: 130,
          sortable: true
        },
        {
          prop: 'loginStatus',
          label: '状态',
          width: 80,
          sortable: true,
          type: 'status',
          statusMap: {
            'SUCCESS': { type: 'success', text: '成功' },
            'FAILED': { type: 'danger', text: '失败' }
          }
        },
        {
          prop: 'loginTime',
          label: '登录时间',
          width: 180,
          sortable: true,
          type: 'datetime'
        }
      ]
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

    handlePageChange(page) {
      this.currentPage = page
      this.loadRecords()
    },

    handleSizeChange(size) {
      this.pageSize = size
      this.currentPage = 1
      this.loadRecords()
    },

    handleSearch(searchText) {
      this.searchUsername = searchText || ''
      this.currentPage = 1
      this.loadRecords()
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

.title-section {
  margin-bottom: 20px;
}

.title-section h2 {
  margin: 0;
  color: #303133;
  font-size: 20px;
  font-weight: 600;
  text-align: center;
}
</style>


