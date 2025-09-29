<template>
  <div class="login-stats-container">
    <div class="title-section">
      <h2>登录统计</h2>
    </div>

    <div class="stats-content">
      <div class="table-section">
        <h3>详细统计数据</h3>
        <DataTable
          :data="statsData"
          :columns="columns"
          :loading="loading"
          :show-pagination="false"
          :show-search="false"
          :default-sort="{ prop: 'loginCount', order: 'descending' }"
        />
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
import DataTable from './DataTable.vue'

export default {
  name: 'LoginStats',
  components: {
    DataTable
  },
  data() {
    return {
      statsData: [],
      loading: false,
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
          prop: 'loginCount',
          label: '登录次数',
          width: 100,
          sortable: true,
          type: 'number'
        },
        {
          prop: 'lastLoginTime',
          label: '最后登录时间',
          width: 180,
          sortable: true
        },
        {
          prop: 'lastLoginIp',
          label: '最后登录IP',
          width: 130,
          sortable: true
        }
      ]
    }
  },
  mounted() {
    this.loadStats()
  },
  methods: {
    loadStats() {
      this.loading = true
      axios.get('/statistics/login/stats').then(resp => {
        this.loading = false
        if (resp && resp.data && resp.data.status === 'success') {
          this.statsData = resp.data.obj || []
        }
      }).catch(() => {
        this.loading = false
        this.$message.error('获取登录统计失败')
      })
    },

  }
}
</script>

<style scoped>
.login-stats-container {
  padding: 20px;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.header-section {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  margin-bottom: 20px;
}

.header-section h2 {
  margin: 0;
  color: #303133;
  font-size: 20px;
  font-weight: 600;
}

.stats-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.table-section {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.table-section h3 {
  margin: 0 0 20px 0;
  color: #303133;
  font-size: 16px;
  font-weight: 600;
  border-bottom: 2px solid #409EFF;
  padding-bottom: 10px;
}
</style>


