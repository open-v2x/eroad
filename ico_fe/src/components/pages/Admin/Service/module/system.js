import { request } from '../request'

export const systemDictAdd = (data) => {
  return request('/system/dict/add', {
    method: 'POST',
    data
  })
}

export const systemDictDelete = (data) => {
  return request('/system/dict/delete', {
    method: 'POST',
    data
  })
}

export const systemDictFind = (params) => {
  return request('/system/dict/find', {
    method: 'GET',
    params
  })
}

export const systemDictFindAll = (data) => {
  return request('/system/dict/findAll', {
    method: 'POST',
    data
  })
}

export const systemDictList = (data) => {
  return request('/system/dict/list', {
    method: 'POST',
    data
  })
}

export const systemDictRpcFind = (data) => {
  return request('/system/dict/rpc/find', {
    method: 'POST',
    data
  })
}

export const systemDictUpdate = (data) => {
  return request('/system/dict/update', {
    method: 'POST',
    data
  })
}

export const systemrpcFind = (params) => {
  return request('/system/dict/rpc/find', {
    method: 'GET',
    params
  })
}

export const systemLogList = (data) => {
  return request('/system/log/list', {
    method: 'POST',
    data
  })
}

export const systemLogDel = (data) => {
  return request('/system/log/del', {
    method: 'POST',
    data
  })
}
