import request from './request'

// 登录接口
export const login = (data) => {
    return request({
        url: '/user/login',
        method: 'post',
        data
    })
}

// 退出登录
export const logout = () => {
    return request({
        url: '/user/logout',
        method: 'post',
        headers: {
            token: localStorage.getItem('token') // 携带token
        }
    })
}

// 获取当前用户信息
export const getUserInfo = () => {
    return request({
        url: '/user/info',
        method: 'get',
        headers: {
            token: localStorage.getItem('token')
        }
    })
}

export const register = (data) => {
    return request({
        url: '/user/register',
        method: 'post',
        data
    })
}

// 申请成为卖家
export const applySeller = () => {
    return request({
        url: '/user/apply-seller',
        method: 'post',
        headers: {
            token: localStorage.getItem('token')
        }
    })
}
