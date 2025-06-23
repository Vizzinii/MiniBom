import request from '@/utils/request'

// 创建分类
export const classificationCreateService = (data) => {
    return request.post('/classifications', data)
}

// 获取分类列表（树状或搜索）
export const classificationListService = (params) => {
    return request.get('/classifications', { params })
}

// 更新分类
export const classificationUpdateService = (id, data) => {
    return request.put(`/classifications/${id}`, data)
}

// 删除分类
export const classificationDeleteService = (id) => {
    return request.delete(`/classifications/${id}`)
}

// 关联属性
export const classificationLinkAttrService = (data) => {
    return request.post('/classifications/link-attributes', data)
}

// 获取分类详情
export const classificationDetailService = (id) => {
    return request.get(`/classifications/${id}/details`)
} 