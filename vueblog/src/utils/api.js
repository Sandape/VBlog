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