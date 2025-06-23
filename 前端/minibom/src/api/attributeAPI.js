import request from '@/utils/request';

/**
 * 查询属性列表
 * @param {string} name - 查询关键字 (可选)
 */
export const attributeQueryService = (name) => {
    return request.get('/attribute/query', { params: { name } });
}

/**
 * 创建属性
 * @param {object} attributeData - 包含属性信息的对象
 */
export const attributeCreateService = (attributeData) => {
    return request.post('/attribute/create', attributeData);
}

/**
 * 更新属性
 * @param {object} attributeData - 包含属性ID和要更新信息的对象
 */
export const attributeUpdateService = (attributeData) => {
    return request.put('/attribute/update', attributeData);
}

/**
 * 删除属性
 * @param {Array<string>} ids - 要删除的属性ID数组
 */
export const attributeDeleteService = (ids) => {
    return request.delete('/attribute/delete', { data: ids });
} 