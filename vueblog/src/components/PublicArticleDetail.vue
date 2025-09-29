<template>
  <div class="public-article-detail">
    <div class="container" v-loading="loading">
      <!-- 返回按钮 -->
      <div class="back-button">
        <el-button type="text" icon="el-icon-back" @click="goBack" style="padding: 0;">
          返回Prompt列表
        </el-button>
      </div>

      <!-- 文章不存在提示 -->
      <div v-if="!article.id && !loading" class="not-found">
        <h2>Prompt不存在</h2>
        <p>抱歉，您访问的Prompt可能已被删除或不存在。</p>
        <el-button type="primary" @click="goBack">返回列表</el-button>
      </div>

      <!-- 文章内容 -->
      <div v-if="article.id" class="article-content">
        <!-- 文章标题 -->
        <div class="article-header">
          <h1 class="article-title">{{ article.title }}</h1>

          <!-- 文章元信息 -->
          <div class="article-meta">
            <div class="meta-item">
              <i class="el-icon-user"></i>
              <span>{{ article.nickname }}</span>
            </div>
            <div class="meta-item">
              <i class="el-icon-folder-opened"></i>
              <span>{{ article.cateName }}</span>
            </div>
            <div class="meta-item">
              <i class="el-icon-time"></i>
              <span>{{ article.editTime | formatDateTime }}</span>
            </div>
            <div class="meta-item">
              <i class="el-icon-view"></i>
              <span>浏览 {{ article.pageView || 0 }}</span>
            </div>
          </div>

          <!-- 文章标签 -->
          <div class="article-tags" v-if="article.tags && article.tags.length > 0">
            <el-tag v-for="tag in article.tags" :key="tag.id" size="medium" type="info">
              {{ tag.tagName }}
            </el-tag>
          </div>
        </div>

        <!-- 文章正文 -->
        <div class="article-body">
          <div class="content" v-html="article.htmlContent"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { getRequest } from '../utils/api'

export default {
  name: 'PublicArticleDetail',
  data() {
    return {
      article: {},
      loading: false
    }
  },
  mounted() {
    const aid = this.$route.query.aid
    if (aid) {
      this.loadArticle(aid)
    } else {
      this.$message.error('PromptID参数缺失')
      this.goBack()
    }
  },
  methods: {
    loadArticle(aid) {
      // 参数验证
      if (!aid) {
        this.$message.error('PromptID参数缺失')
        this.goBack()
        return
      }

      this.loading = true
      const url = `/article/public/${aid}`

      getRequest(url).then(resp => {
        this.loading = false
        if (resp.status === 200 && resp.data) {
          this.article = resp.data
        } else {
          this.$message.error('Prompt加载失败')
        }
      }).catch(() => {
        this.loading = false
        this.$message.error('Prompt加载失败')
      })
    },
    goBack() {
      this.$router.push('/public')
    }
  }
}
</script>

<style scoped>
.public-article-detail {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

.back-button {
  margin-bottom: 20px;
}

.not-found {
  text-align: center;
  padding: 80px 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.not-found h2 {
  color: #909399;
  margin-bottom: 10px;
}

.not-found p {
  color: #c0c4cc;
  margin-bottom: 30px;
}

.article-content {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.article-header {
  padding: 30px;
  border-bottom: 1px solid #e4e7ed;
}

.article-title {
  margin: 0 0 20px 0;
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  line-height: 1.4;
}

.article-meta {
  display: flex;
  align-items: center;
  gap: 25px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #909399;
}

.meta-item i {
  font-size: 16px;
}

.article-tags {
  margin-top: 15px;
}

.article-tags .el-tag {
  margin-right: 10px;
  margin-bottom: 5px;
}

.article-body {
  padding: 30px;
}

.content {
  font-size: 16px;
  line-height: 1.8;
  color: #606266;
}

.content >>> h1,
.content >>> h2,
.content >>> h3,
.content >>> h4,
.content >>> h5,
.content >>> h6 {
  margin-top: 24px;
  margin-bottom: 16px;
  font-weight: 600;
  color: #303133;
}

.content >>> h1 { font-size: 28px; }
.content >>> h2 { font-size: 24px; }
.content >>> h3 { font-size: 20px; }
.content >>> h4 { font-size: 18px; }
.content >>> h5 { font-size: 16px; }
.content >>> h6 { font-size: 14px; }

.content >>> p {
  margin: 16px 0;
}

.content >>> blockquote {
  border-left: 4px solid #409eff;
  padding-left: 16px;
  margin: 20px 0;
  color: #606266;
  background-color: #f8f9fa;
  padding: 16px;
  border-radius: 4px;
}

.content >>> pre {
  background-color: #f6f8fa;
  border-radius: 6px;
  padding: 16px;
  overflow-x: auto;
  margin: 20px 0;
}

.content >>> code {
  background-color: #f6f8fa;
  padding: 2px 6px;
  border-radius: 3px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 14px;
}

.content >>> pre code {
  background-color: transparent;
  padding: 0;
}

.content >>> ul,
.content >>> ol {
  padding-left: 24px;
  margin: 16px 0;
}

.content >>> li {
  margin: 8px 0;
}

.content >>> table {
  border-collapse: collapse;
  margin: 20px 0;
  width: 100%;
}

.content >>> th,
.content >>> td {
  border: 1px solid #e4e7ed;
  padding: 8px 12px;
  text-align: left;
}

.content >>> th {
  background-color: #f5f7fa;
  font-weight: 600;
}

.content >>> img {
  max-width: 100%;
  height: auto;
  border-radius: 6px;
  margin: 20px 0;
}

.content >>> a {
  color: #409eff;
  text-decoration: none;
}

.content >>> a:hover {
  text-decoration: underline;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .container {
    padding: 15px;
  }

  .article-header {
    padding: 20px;
  }

  .article-title {
    font-size: 24px;
  }

  .article-meta {
    gap: 15px;
  }

  .article-body {
    padding: 20px;
  }

  .content {
    font-size: 15px;
  }
}
</style>
