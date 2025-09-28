import axios from 'axios'

let base = '';
export const postRequest = (url, params) => {
  return axios({
    method: 'post',
    url: `${base}${url}`,
    data: params,
    transformRequest: [function (data) {
      // Do whatever you want to transform the data
      let ret = ''
      for (let it in data) {
        ret += encodeURIComponent(it) + '=' + encodeURIComponent(data[it]) + '&'
      }
      return ret
    }],
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  });
}
export const uploadFileRequest = (url, params) => {
  return axios({
    method: 'post',
    url: `${base}${url}`,
    data: params,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
}
export const putRequest = (url, params) => {
  return axios({
    method: 'put',
    url: `${base}${url}`,
    data: params,
    transformRequest: [function (data) {
      let ret = ''
      for (let it in data) {
        ret += encodeURIComponent(it) + '=' + encodeURIComponent(data[it]) + '&'
      }
      return ret
    }],
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  });
}
export const deleteRequest = (url) => {
  return axios({
    method: 'delete',
    url: `${base}${url}`
  });
}
export const getRequest = (url,params) => {
  return axios({
    method: 'get',
    data:params,
    transformRequest: [function (data) {
      let ret = ''
      for (let it in data) {
        ret += encodeURIComponent(it) + '=' + encodeURIComponent(data[it]) + '&'
      }
      return ret
    }],
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    },
    url: `${base}${url}`
  });
}

export const postJsonRequest = (url, params) => {
  return axios({
    method: 'post',
    url: `${base}${url}`,
    data: params,
    headers: {
      'Content-Type': 'application/json'
    }
  });
}

export const putJsonRequest = (url, params) => {
  return axios({
    method: 'put',
    url: `${base}${url}`,
    data: params,
    headers: {
      'Content-Type': 'application/json'
    }
  });
}

// 表元管理相关API
export const addSqlTable = (projectId, sql, entityPath) => {
  return postJsonRequest(`/project/${projectId}/sql-tables`, { sql, entityPath })
}

export const getProjectSqlTables = (projectId) => {
  return getRequest(`/project/${projectId}/sql-tables`)
}

export const updateSqlTable = (projectId, tableName, sql, entityPath) => {
  return putJsonRequest(`/project/${projectId}/sql-tables/${tableName}`, { sql, entityPath })
}

export const updateTableMeta = (projectId, tableName, tableMeta) => {
  return putJsonRequest(`/project/${projectId}/table-meta/${tableName}`, tableMeta)
}

export const deleteSqlTable = (projectId, tableName) => {
  return deleteRequest(`/project/${projectId}/sql-tables/${tableName}`)
}

// 通知相关API
export const getUnreadNotificationCount = () => {
  return getRequest('/notification/unreadCount')
}

export const getUserNotifications = (limit = 20) => {
  return getRequest(`/notification/list?limit=${limit}`)
}

export const markNotificationAsRead = (id) => {
  return postRequest(`/notification/markRead/${id}`)
}

export const markAllNotificationsAsRead = () => {
  return postRequest('/notification/markAllRead')
}

export const deleteNotification = (id) => {
  return deleteRequest(`/notification/${id}`)
}

// Prompt生成器相关API
export const generatePrompt = (data) => {
  return postJsonRequest('/prompt/generate', data)
}

export const getUserPromptLogs = () => {
  return getRequest('/prompt/logs/my')
}

export const getProjectPromptLogs = (projectId) => {
  return getRequest(`/prompt/logs/project/${projectId}`)
}

export const getPromptLogDetail = (id) => {
  return getRequest(`/prompt/logs/${id}`)
}

export const deletePromptLog = (id) => {
  return deleteRequest(`/prompt/logs/${id}`)
}
// ========== 使用统计相关API ==========

// 获取使用记录列表
export const getUsageLogs = (page, count) => {
  return getRequest('/article/usageLogs', {
    params: { page, count }
  })
}

// 按用户统计使用次数
export const getUsageStatsByUser = () => {
  return getRequest('/article/usageStatsByUser')
}

// 根据用户名搜索使用记录
export const searchUsageLogs = (username, page, count) => {
  return getRequest('/article/searchUsageLogs', {
    params: { username, page, count }
  })
}

// 获取用户使用统计（个人使用情况）
export const getUserUsageStats = () => {
  return getRequest('/article/userUsageStats')
}
