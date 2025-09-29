<template>
  <div class="data-table-container">
    <!-- 头部搜索区域 -->
    <div class="header-section" v-if="showSearch">
      <div class="search-section">
        <el-input
          v-model="searchText"
          :placeholder="searchPlaceholder || '输入关键词搜索'"
          style="width: 250px; margin-right: 10px;"
          @keyup.enter="handleSearch"
          clearable
        >
          <el-button slot="append" icon="el-icon-search" @click="handleSearch"></el-button>
        </el-input>
        <el-button @click="clearSearch" type="primary" plain>清除搜索</el-button>
      </div>
    </div>

    <!-- 表格区域 -->
    <div class="table-container">
      <el-table
        :data="filteredData"
        v-loading="loading"
        style="width: 100%"
        :default-sort="defaultSort"
        @sort-change="handleSortChange"
        :row-class-name="rowClassName"
        @row-click="handleRowClick"
        :highlight-current-row="highlightCurrentRow"
      >
        <!-- 动态生成列 -->
        <el-table-column
          v-for="column in columns"
          :key="column.prop"
          :prop="column.prop"
          :label="column.label"
          :width="column.width"
          :sortable="column.sortable !== false ? 'custom' : false"
          :sort-orders="column.sortable !== false ? ['ascending', 'descending'] : null"
          :show-overflow-tooltip="column.showTooltip !== false"
        >
          <!-- 自定义列内容 -->
          <template slot-scope="scope">
            <!-- 时间格式化 -->
            <span v-if="column.type === 'datetime'">
              {{ formatDateTime(scope.row[column.prop]) }}
            </span>
            <!-- 状态标签 -->
            <el-tag
              v-else-if="column.type === 'status'"
              :type="getStatusType(scope.row[column.prop], column.statusMap)"
            >
              {{ getStatusText(scope.row[column.prop], column.statusMap) }}
            </el-tag>
            <!-- 数字格式化 -->
            <span v-else-if="column.type === 'number'">
              {{ scope.row[column.prop] || 0 }}
            </span>
            <!-- 自定义渲染函数 -->
            <span v-else-if="column.render">
              {{ column.render(scope.row[column.prop], scope.row) }}
            </span>
            <!-- 默认显示 -->
            <span v-else>
              {{ scope.row[column.prop] || '-' }}
            </span>
          </template>
        </el-table-column>

        <!-- 操作列 -->
        <el-table-column
          v-if="actions && actions.length > 0"
          label="操作"
          :width="actionsWidth || 150"
          :fixed="actionsFixed"
        >
          <template slot-scope="scope">
            <el-button
              v-for="action in actions"
              :key="action.key"
              :type="action.type || 'text'"
              :size="action.size || 'mini'"
              :icon="action.icon"
              :disabled="action.disabled && action.disabled(scope.row)"
              @click.stop="handleAction(action, scope.row, scope.$index)"
            >
              {{ action.label }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页区域 -->
    <div class="pagination-container" v-if="showPagination && total > 0">
      <el-pagination
        @current-change="handlePageChange"
        :current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        :page-sizes="pageSizes"
        @size-change="handleSizeChange"
        layout="total, sizes, prev, pager, next, jumper"
      >
      </el-pagination>
    </div>
  </div>
</template>

<script>
export default {
  name: 'DataTable',
  props: {
    // 数据源
    data: {
      type: Array,
      default: () => []
    },
    // 列配置
    columns: {
      type: Array,
      default: () => []
    },
    // 操作配置
    actions: {
      type: Array,
      default: () => []
    },
    // 加载状态
    loading: {
      type: Boolean,
      default: false
    },
    // 分页相关
    showPagination: {
      type: Boolean,
      default: true
    },
    currentPage: {
      type: Number,
      default: 1
    },
    pageSize: {
      type: Number,
      default: 10
    },
    total: {
      type: Number,
      default: 0
    },
    pageSizes: {
      type: Array,
      default: () => [10, 20, 50, 100]
    },
    // 搜索相关
    showSearch: {
      type: Boolean,
      default: true
    },
    searchPlaceholder: {
      type: String,
      default: ''
    },
    searchFields: {
      type: Array,
      default: () => []
    },
    // 排序相关
    defaultSort: {
      type: Object,
      default: () => ({ prop: '', order: '' })
    },
    // 表格相关
    highlightCurrentRow: {
      type: Boolean,
      default: false
    },
    rowClassName: {
      type: [String, Function],
      default: ''
    },
    // 操作列相关
    actionsWidth: {
      type: [String, Number],
      default: 150
    },
    actionsFixed: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      searchText: '',
      sortProp: this.defaultSort.prop || '',
      sortOrder: this.defaultSort.order || ''
    }
  },
  computed: {
    // 过滤后的数据
    filteredData() {
      let data = [...this.data]

      // 搜索过滤
      if (this.searchText && this.searchText.trim()) {
        const searchValue = this.searchText.trim().toLowerCase()
        const fields = this.searchFields.length > 0 ? this.searchFields : this.columns.map(col => col.prop)

        data = data.filter(item => {
          return fields.some(field => {
            const value = item[field]
            return value && String(value).toLowerCase().includes(searchValue)
          })
        })
      }

      // 排序
      if (this.sortProp && this.sortOrder) {
        data.sort((a, b) => {
          let aVal = a[this.sortProp]
          let bVal = b[this.sortProp]

          // 处理null值
          if (aVal == null && bVal == null) return 0
          if (aVal == null) return this.sortOrder === 'ascending' ? -1 : 1
          if (bVal == null) return this.sortOrder === 'ascending' ? 1 : -1

          // 字符串排序
          if (typeof aVal === 'string' && typeof bVal === 'string') {
            aVal = aVal.toLowerCase()
            bVal = bVal.toLowerCase()
          }

          // 时间排序
          if (aVal instanceof Date && bVal instanceof Date) {
            aVal = aVal.getTime()
            bVal = bVal.getTime()
          }

          if (aVal < bVal) {
            return this.sortOrder === 'ascending' ? -1 : 1
          }
          if (aVal > bVal) {
            return this.sortOrder === 'ascending' ? 1 : -1
          }
          return 0
        })
      }

      return data
    }
  },
  methods: {
    // 搜索处理
    handleSearch() {
      this.$emit('search', this.searchText)
    },

    // 清除搜索
    clearSearch() {
      this.searchText = ''
      this.handleSearch()
    },

    // 排序处理
    handleSortChange({ prop, order }) {
      this.sortProp = prop
      this.sortOrder = order
      this.$emit('sort-change', { prop, order })
    },

    // 分页处理
    handlePageChange(page) {
      this.$emit('page-change', page)
    },

    handleSizeChange(size) {
      this.$emit('size-change', size)
    },

    // 行点击处理
    handleRowClick(row, column, event) {
      this.$emit('row-click', row, column, event)
    },

    // 操作处理
    handleAction(action, row, index) {
      this.$emit('action', action.key, row, index)
    },

    // 时间格式化
    formatDateTime(value) {
      if (!value) return '-'
      const date = new Date(value)
      return date.getFullYear() + '-' +
             String(date.getMonth() + 1).padStart(2, '0') + '-' +
             String(date.getDate()).padStart(2, '0') + ' ' +
             String(date.getHours()).padStart(2, '0') + ':' +
             String(date.getMinutes()).padStart(2, '0') + ':' +
             String(date.getSeconds()).padStart(2, '0')
    },

    // 状态类型获取
    getStatusType(value, statusMap) {
      if (!statusMap || !statusMap[value]) return ''
      return statusMap[value].type || ''
    },

    // 状态文本获取
    getStatusText(value, statusMap) {
      if (!statusMap || !statusMap[value]) return value || '-'
      return statusMap[value].text || value
    }
  }
}
</script>

<style scoped>
.data-table-container {
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

/* 响应式设计 */
@media (max-width: 768px) {
  .data-table-container {
    padding: 10px;
  }

  .header-section {
    flex-direction: column;
    align-items: stretch;
    gap: 10px;
  }

  .search-section {
    justify-content: center;
  }

  .table-container,
  .pagination-container {
    padding: 10px;
  }
}
</style>
