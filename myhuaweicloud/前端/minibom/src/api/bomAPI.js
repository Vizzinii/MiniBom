import request from '@/utils/request'

// 创建BOM关系
export function bomLinkCreateService(data) {
  return request({
    url: '/bom-links',
    method: 'post',
    data
  })
}

// 查询BOM结构树
export function bomTreeQueryService(versionId, params = {}) {
  return request({
    url: `/parts/${versionId}/bom-tree`,
    method: 'get',
    params
  })
}

// 查询BOM清单
export function bomListQueryService(versionId, params = {}) {
  return request({
    url: `/parts/${versionId}/bom-list`,
    method: 'get',
    params
  })
}

// 更新BOM关系
export function bomLinkUpdateService(bomLinkId, data) {
  return request({
    url: `/bom-links/${bomLinkId}`,
    method: 'put',
    data
  })
}

// 删除BOM关系
export function bomLinkDeleteService(bomLinkId) {
  return request({
    url: `/bom-links/${bomLinkId}`,
    method: 'delete'
  })
}

// 批量创建BOM关系
export function bomLinkBatchCreateService(data) {
  return request({
    url: '/bom-links/batch',
    method: 'post',
    data
  })
}

// 查询Part的父级使用情况
export function partWhereUsedService(masterId, params = {}) {
  return request({
    url: `/parts/${masterId}/where-used`,
    method: 'get',
    params
  })
}
