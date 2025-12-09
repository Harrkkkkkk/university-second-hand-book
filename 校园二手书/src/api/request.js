import axios from 'axios'

const service = axios.create({
    baseURL: '/book-api',
    timeout: 5000
})

service.interceptors.request.use(
    (config) => {
        return config
    },
    (error) => {
        return Promise.reject(error)
    }
)

service.interceptors.response.use(
    (response) => {
        return response.data
    },
    (error) => {
        console.error('请求失败：', error.message)
        return Promise.reject(error)
    }
)

export default service