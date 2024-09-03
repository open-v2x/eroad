import { request } from '../request'

export const getDeviceList = (data) => {
  return request('/device/api/list', {
    method: 'POST',
    data
  })
}

export const deviceAddList = (data) => {
  return request('/device/add', {
    method: 'POST',
    data
  })
}

export const deviceEditList = (data) => {
  return request('/device/edit', {
    method: 'POST',
    data
  })
}

export const deviceDeleteList = (data) => {
  return request('/device/del', {
    method: 'POST',
    data
  })
}

export const deviceDetailList = (data) => {
  return request('/device/selectById', {
    method: 'POST',
    data
  })
}

export const deviceExcelImport = (data) => {
  return request('/device/excelImport', {
    method: 'POST',
    data
  })
}

export const deviceExcelExport = (data) => {
  return request('/device/excelExport', {
    method: 'POST',
    data
  })
}

export const deviceAlarmPage = (data) => {
  return request('/device/alarm/page', {
    method: 'POST',
    data
  })
}

export const deviceAlarmExpire = (data) => {
  return request('/device/alarm/expire', {
    method: 'POST',
    data
  })
}
