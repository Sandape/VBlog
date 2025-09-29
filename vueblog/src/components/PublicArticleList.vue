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

    <div class="article-list" v-loading="loading">
      <div v-for="article in articles" :key="article.id" class="article-item" @click="viewArticle(article)">
        <div class="article-header">
          <h3 class="article-title">{{ article.title }}</h3>
          <div class="article-meta">
            <span class="author">{{ article.nickname }}</span>
            <span class="category">{{ article.cateName }}</span>
            <span class="time">{{ article.editTime | formatDateTime }}</span>
            <span class="views">浏览 {{ article.pageView || 0 }}</span>
          </div>
        </div>
        <div class="article-summary" v-html="article.summary"></div>
        <div class="article-tags" v-if="article.tags && article.tags.length > 0">
          <el-tag v-for="tag in article.tags" :key="tag.id" size="mini" type="info">{{ tag.tagName }}</el-tag>
        </div>
      </div>

      <div v-if="articles.length === 0 && !loading" class="no-articles">
        <p>暂无Prompt</p>
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

.article-list {
  min-height: 300px;
}

.article-item {
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 15px;
  margin-bottom: 15px;
  cursor: pointer;
  transition: all 0.2s ease;
  background: #fff;
}

.article-item:hover {
  border-color: #20a0ff;
  box-shadow: 0 2px 8px 0 rgba(32, 160, 255, 0.1);
}

.article-header {
  margin-bottom: 12px;
}

.article-title {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 16px;
  font-weight: 500;
  line-height: 1.4;
}

.article-title:hover {
  color: #20a0ff;
}

.article-meta {
  display: flex;
  align-items: center;
  font-size: 12px;
  color: #909399;
  gap: 15px;
  flex-wrap: wrap;
}

.article-summary {
  color: #606266;
  line-height: 1.5;
  margin-bottom: 10px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.article-summary >>> * {
  margin: 0;
  padding: 0;
}

.article-tags {
  margin-top: 8px;
}

.article-tags .el-tag {
  margin-right: 6px;
  margin-bottom: 4px;
}

.no-articles {
  text-align: center;
  padding: 40px 0;
  color: #909399;
}

.no-articles p {
  font-size: 14px;
  margin: 0;
}

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

  .article-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 5px;
  }

  .article-item {
    padding: 12px;
  }

  .article-title {
    font-size: 15px;
  }
}
</style>
