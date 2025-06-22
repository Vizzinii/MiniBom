import request from '@/utils/request'

// 查询Part列表
export function partQueryService(params) {
  return request({
    url: '/parts',
    method: 'get',
    params
  })
}

// 创建Part
export function partCreateService(data) {
  return request({
    url: '/parts',
    method: 'post',
    data
  })
}

// 获取Part详情
export function partDetailService(masterId) {
  return request({
    url: `/parts/${masterId}`,
    method: 'get'
  })
}

// 更新Part
export function partUpdateService(masterId, data) {
  return request({
    url: `/parts/${masterId}`,
    method: 'put',
    data
  })
}

// 删除Part
export function partDeleteService(masterId) {
  return request({
    url: `/parts/${masterId}`,
    method: 'delete'
  })
}

// 检出Part
export function partCheckoutService(masterId) {
  return request({
    url: `/parts/${masterId}/checkout`,
    method: 'post'
  })
}

// 撤销检出Part
export function partUndoCheckoutService(masterId) {
  return request({
    url: `/parts/${masterId}/undo-checkout`,
    method: 'post'
  })
}

// 检入Part
export function partCheckinService(masterId, iterationNote) {
  return request({
    url: `/parts/${masterId}/checkin`,
    method: 'post',
    data: iterationNote
  })
}

// 获取Part版本历史
export function partVersionsService(masterId) {
  return request({
    url: `/parts/${masterId}/versions`,
    method: 'get'
  })
}

// 获取指定版本详情
export function partVersionDetailService(versionId) {
  return request({
    url: `/parts/versions/${versionId}`,
    method: 'get'
  })
}

// 获取枚举值
export function partEnumService(enumType) {
  return request({
    url: `/parts/enums/${enumType}`,
    method: 'get'
  })
} 