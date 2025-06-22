import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useTokenStore } from '@/stores/token'
import router from '@/router'
import JSONbig from 'json-bigint'

const baseURL = '/api'
const instance = axios.create({
  baseURL,
  transformResponse: [function (data) {
    try {
      // storeAsString: true 保证所有大整数都以字符串返回
      return JSONbig({ storeAsString: true }).parse(data)
    } catch (err) {
      return data
    }
  }]
})

// 请求拦截器: 附加Token
instance.interceptors.request.use(
  (config) => {
    const tokenStore = useTokenStore()
    if (tokenStore.token) {
      config.headers.token = tokenStore.token
    }
    return config
  },
  (err) => Promise.reject(err)
)

// 响应拦截器: 解析业务码
instance.interceptors.response.use(
  (res) => {
    // 根据后端的 ReturnCode.java 定义所有业务成功状态码
    const successCodes = [123, 116, 20011, 20021, 20031, 20041, 60003];
    if (successCodes.includes(res.data.code)) {
      // 业务成功, 根据后端不同接口返回格式，有的成功需要提示，有的不需要
      // 为保持一致，对于登录/注册等成功信息，在各自的vue文件中单独提示
      return res.data.data
    }
    
    // 业务失败, 手动制造一个错误, 进入下面的catch
    ElMessage.error(res.data.message || '服务异常')
    return Promise.reject(new Error(res.data.message))
  },
  (err) => {
    // 处理HTTP层面的错误
    if (err.response?.status === 401) {
      ElMessage.error('登录状态已过期，请重新登录')
      // 清理本地数据并跳转登录页
      const tokenStore = useTokenStore()
      tokenStore.removeToken()
      router.push('/login')
    } else {
      // 其他HTTP错误
      ElMessage.error(err.response?.data?.message || '服务器开小差了')
    }
    return Promise.reject(err)
  }
)

export default instance 