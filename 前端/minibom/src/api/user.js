import request from '@/utils/request'

// 用户注册
export const userRegisterService = (registerData) => {
  return request.post('/user/register', registerData)
}

// 用户登录
export const userLoginService = (loginData) => {
  return request.post('/user/login', loginData)
} 