import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useTokenStore } from '@/stores/token'
import router from '@/router'
import JSONbig from 'json-bigint'

const baseURL = '/api'
const instance = axios.create({
  baseURL,
  timeout: 30000, // 增加超时时间到30秒
  transformResponse: [function (data) {
    try {
      // storeAsString: true 保证所有大整数都以字符串返回
      return JSONbig({ storeAsString: true }).parse(data)
    } catch (err) {
      return data
    }
  }]
})

// 重试配置
const retryConfig = {
  retries: 3,
  retryDelay: 1000,
  retryCondition: (error) => {
    // 对网络错误、超时、连接重置等进行重试
    return (
      error.code === 'ECONNRESET' ||
      error.code === 'ECONNABORTED' ||
      error.code === 'ETIMEDOUT' ||
      error.code === 'ENOTFOUND' ||
      error.message.includes('Connection reset') ||
      error.message.includes('timeout') ||
      (error.response?.status >= 500)
    )
  }
}

// 请求拦截器: 附加Token
instance.interceptors.request.use(
  (config) => {
    const tokenStore = useTokenStore()
    if (tokenStore.token) {
      config.headers.token = tokenStore.token
    }
    
    // 添加重试配置
    config.retryCount = config.retryCount || 0
    
    console.log(`发送请求: ${config.method?.toUpperCase()} ${config.url}`, config.params || config.data)
    
    return config
  },
  (err) => Promise.reject(err)
)

// 响应拦截器: 解析业务码和重试逻辑
instance.interceptors.response.use(
  (res) => {
    console.log(`响应成功: ${res.config.method?.toUpperCase()} ${res.config.url}`, res.data)
    
    // 根据后端的 ReturnCode.java 定义所有业务成功状态码
    const successCodes = [200, 123, 116, 20011, 20021, 20031, 20041, 60003];
    if (successCodes.includes(res.data.code)) {
      // 业务成功, 根据后端不同接口返回格式，有的成功需要提示，有的不需要
      // 为保持一致，对于登录/注册等成功信息，在各自的vue文件中单独提示
      return res.data.data
    }
    
    // 业务失败, 手动制造一个错误, 进入下面的catch
    ElMessage.error(res.data.message || '服务异常')
    return Promise.reject(new Error(res.data.message))
  },
  async (err) => {
    const config = err.config
    
    console.error(`请求失败: ${config?.method?.toUpperCase()} ${config?.url}`)
    console.error('错误状态码:', err.response?.status)
    console.error('错误响应数据:', err.response?.data)
    console.error('错误信息:', err.message)
    
    // 特别针对BOM相关接口的详细调试
    if (config?.url?.includes('bom-links')) {
      console.error('BOM接口错误详情:')
      console.error('- 请求URL:', config.url)
      console.error('- 请求方法:', config.method)
      console.error('- 请求数据:', config.data)
      console.error('- 请求头:', config.headers)
      console.error('- 响应状态:', err.response?.status)
      console.error('- 响应数据:', JSON.stringify(err.response?.data, null, 2))
    }
    
    // 重试逻辑
    if (config && retryConfig.retryCondition(err) && config.retryCount < retryConfig.retries) {
      config.retryCount += 1
      
      console.log(`正在进行第 ${config.retryCount} 次重试...`)
      ElMessage.warning(`网络不稳定，正在重试... (${config.retryCount}/${retryConfig.retries})`)
      
      // 等待后重试
      await new Promise(resolve => setTimeout(resolve, retryConfig.retryDelay * config.retryCount))
      
      return instance(config)
    }
    
    // 处理HTTP层面的错误
    if (err.response?.status === 401) {
      ElMessage.error('登录状态已过期，请重新登录')
      // 清理本地数据并跳转登录页
      const tokenStore = useTokenStore()
      tokenStore.removeToken()
      router.push('/login')
    } else if (err.response?.status === 500) {
      console.error('服务器内部错误详情:', err.response.data)
      // 对于500错误，不显示默认错误提示，让调用方处理
    } else if (err.code === 'ECONNRESET' || err.message.includes('Connection reset')) {
      ElMessage.error('网络连接被重置，请检查网络连接')
    } else if (err.code === 'ETIMEDOUT' || err.message.includes('timeout')) {
      ElMessage.error('请求超时，请检查网络连接')
    } else if (err.code === 'ECONNABORTED') {
      ElMessage.error('请求被中断，请重试')
    } else if (err.response?.status !== 500) {
      // 其他HTTP错误(除了500，因为500由调用方处理)
      ElMessage.error(err.response?.data?.message || err.message || '服务器开小差了')
    }
    return Promise.reject(err)
  }
)

export default instance 