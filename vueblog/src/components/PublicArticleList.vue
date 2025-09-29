<template>
  <div class="public-article-list">
    <div class="header">
      <div class="search-bar">
        <el-input
          placeholder="搜索Prompt标题..."
          prefix-icon="el-icon-search"
          v-model="keywords"
          style="width: 300px"
          size="small"
          @keyup.enter.native="searchClick">
        </el-input>
        <el-button type="primary" icon="el-icon-search" size="small" style="margin-left: 10px" @click="searchClick">搜索</el-button>
      </div>
    </div>

    <div class="article-table-container" v-loading="loading">
      <el-table
        :data="articles"
        style="width: 100%"
        :row-class-name="tableRowClassName"
        @row-click="viewArticle"
        stripe
        border>
        <el-table-column
          prop="title"
          label="名称"
          min-width="300"
          show-overflow-tooltip>
          <template slot-scope="scope">
            <div class="article-title-cell">
              <i class="el-icon-document article-icon"></i>
              <span class="article-title-text">{{ scope.row.title }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column
          prop="nickname"
          label="作者"
          width="120"
          align="center">
        </el-table-column>

        <el-table-column
          prop="cateName"
          label="分类"
          width="100"
          align="center">
        </el-table-column>

        <el-table-column
          label="修改时间"
          width="150"
          align="center">
          <template slot-scope="scope">
            {{ scope.row.editTime | formatDateTime }}
          </template>
        </el-table-column>

        <el-table-column
          label="浏览量"
          width="100"
          align="center">
          <template slot-scope="scope">
            {{ scope.row.pageView || 0 }}
          </template>
        </el-table-column>

        <el-table-column
          label="标签"
          min-width="200">
          <template slot-scope="scope">
            <div class="article-tags-cell">
              <el-tag
                v-for="tag in scope.row.tags"
                :key="tag.id"
                size="mini"
                type="info"
                class="article-tag">
                {{ tag.tagName }}
              </el-tag>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="articles.length === 0 && !loading" class="no-articles">
        <el-empty description="暂无Prompt" :image-size="80"></el-empty>
      </div>
    </div>

    <div class="pagination" v-if="articles.length > 0">
      <el-pagination
        background
        :page-size="pageSize"
        layout="prev, pager, next"
        :total="totalCount"
        @current-change="currentChange">
      </el-pagination>
    </div>
  </div>
</template>

<script>
import { getRequest } from '../utils/api'

export default {
  name: 'PublicArticleList',
  data() {
    return {
      articles: [],
      loading: false,
      currentPage: 1,
      totalCount: 0,
      pageSize: 10,
      keywords: ''
    }
  },
  mounted() {
    this.loadArticles(1, this.pageSize)
  },
  methods: {
    tableRowClassName({row, rowIndex}) {
      return 'article-table-row'
    },
    loadArticles(page, count) {
      this.loading = true
      const url = `/article/public/all?page=${page}&count=${count}&keywords=${encodeURIComponent(this.keywords || '')}`

      getRequest(url).then(resp => {
        this.loading = false
        if (resp.status === 200) {
          const data = resp.data
          // 适配新的返回格式：PageResultDTO
          if (data.records && data.totalCount !== undefined) {
            this.articles = data.records || []
            this.totalCount = data.totalCount || 0
          } else {
            // 兼容旧的返回格式
            this.articles = data.articles || []
            this.totalCount = data.totalCount || 0
          }
        } else {
          this.$message.error('加载Prompt失败')
        }
      }).catch(() => {
        this.loading = false
        this.$message.error('加载Prompt失败')
      })
    },
    searchClick() {
      this.loadArticles(1, this.pageSize)
    },
    currentChange(currentPage) {
      this.currentPage = currentPage
      this.loadArticles(currentPage, this.pageSize)
    },
    viewArticle(article) {
      if (!article || !article.id) {
        this.$message.error('PromptID不存在')
        return
      }
      this.$router.push({ path: '/blogDetail', query: { aid: article.id } })
    }
  }
}
</script>

<style scoped>
.public-article-list {
  padding: 20px 0;
}

.header {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-bottom: 20px;
}

.search-bar {
  display: flex;
  align-items: center;
}

.article-table-container {
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

/* 自定义表格样式 */
:deep(.el-table) {
  border-radius: 4px;
}

:deep(.el-table th) {
  background-color: #f5f7fa;
  color: #606266;
  font-weight: 500;
  border-bottom: 1px solid #e4e7ed;
  padding: 12px 0;
}

:deep(.el-table td) {
  border-bottom: 1px solid #f0f0f0;
  padding: 10px 0;
  color: #606266;
}

:deep(.el-table--border) {
  border: 1px solid #e4e7ed;
  border-radius: 4px;
}

:deep(.el-table--border th),
:deep(.el-table--border td) {
  border-right: 1px solid #e4e7ed;
}

:deep(.el-table--border th:last-child),
:deep(.el-table--border td:last-child) {
  border-right: none;
}

/* 行悬停效果 */
.article-table-row:hover {
  background-color: #f5f7fa;
  cursor: pointer;
}

/* 标题列样式 */
.article-title-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.article-icon {
  color: #20a0ff;
  font-size: 16px;
}

.article-title-text {
  color: #303133;
  font-weight: 500;
  transition: color 0.2s ease;
}

.article-title-text:hover {
  color: #20a0ff;
}

/* 标签列样式 */
.article-tags-cell {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.article-tag {
  margin: 0;
  border-radius: 12px;
  font-size: 11px;
  padding: 2px 8px;
}

/* 空状态样式 */
.no-articles {
  text-align: center;
  padding: 60px 20px;
}

:deep(.el-empty__description) {
  color: #909399;
  font-size: 14px;
}

/* 分页样式 */
.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  padding-bottom: 20px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .public-article-list {
    padding: 15px 0;
  }

  .header {
    justify-content: flex-start;
  }

  .search-bar {
    width: 100%;
  }

  .search-bar .el-input {
    flex: 1;
  }

  .article-table-container {
    margin: 0 -5px;
  }

  :deep(.el-table th),
  :deep(.el-table td) {
    padding: 8px 0;
  }

  .article-title-cell {
    gap: 6px;
  }

  .article-icon {
    font-size: 14px;
  }

  .article-title-text {
    font-size: 14px;
  }
}

/* 表格斑马纹样式增强 */
:deep(.el-table--striped .el-table__body tr.el-table__row--striped td) {
  background-color: #fafafa;
}

:deep(.el-table--striped .el-table__body tr.el-table__row--striped:hover td) {
  background-color: #f0f8ff;
}
</style>
