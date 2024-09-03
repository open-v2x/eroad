import axios from 'axios'
import codeMessage from './codeMessage'

axios.defaults.headers.post['Content-Type'] = 'application/x-www-form-urlencoded;charset=UTF-8'

const axiosInstance = axios.create({
  timeout: 10000
})

axiosInstance.interceptors.request.use(
  (config) => {
    const accessToken = sessionStorage.getItem('access_token')
    if (accessToken) {
      return {
        ...config,
        headers: {
          ...config.headers,
          Authorization: accessToken ? `Bearer ${accessToken}` : ''
        }
      }
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

axiosInstance.interceptors.response.use(
  (response) => {
    if (response.status === 200) {
      return Promise.resolve(response.data)
    } else {
      return Promise.reject(response)
    }
  },
  (error) => {
    if (error.message.includes('timeout')) {
      console.error('timeout')
    } else {
      console.error(
        codeMessage[error.response.status]
      )
      if (error.response.status === 403) console.error('403')
    }
    Promise.reject(error)
  }
)

const requestObj = {
  get: (url, options) => {
    return axiosInstance.get(url, options)
  },
  delete: (url, options) => {
    return axiosInstance.delete(url, options)
  },
  post: (url, data, options) => {
    return axiosInstance.post(url, data, options)
  },
  put: (url, data, options) => {
    return axiosInstance.put(url, data, options)
  },
  patch: (url, data,  options) => {
    return axiosInstance.patch(url, data, options)
  }
}

const request = (url, options) => {
  const method = options.method !== null ? options.method : 'GET';

  if (method === 'GET') {
    return requestObj.get(url, options)
  }
  if (method === 'DELETE') {
    return requestObj.delete(url, options)
  }
  if (method === 'POST') {
    return requestObj.post(url, options.data, options)
  }
  if (method === 'PUT') {
    return requestObj.post(url, options.data, options)
  }
  if (method === 'PATCH') {
    return requestObj.patch(url, options.data, options)
  }

  return requestObj.get(url, options)
}

export { axiosInstance, request }
